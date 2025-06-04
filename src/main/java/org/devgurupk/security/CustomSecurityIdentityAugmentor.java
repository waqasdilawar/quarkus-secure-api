package org.devgurupk.security;

import io.quarkus.logging.Log;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@ApplicationScoped
public class CustomSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

  @Override
  public Uni<SecurityIdentity> augment(
    SecurityIdentity securityIdentity,
    AuthenticationRequestContext authenticationRequestContext
  ) {
    return null;
  }

  @Override
  public Uni<SecurityIdentity> augment(
    SecurityIdentity identity,
    AuthenticationRequestContext context,
    Map<String, Object> attributes
  ) {
    if (identity.getPrincipal() instanceof OidcJwtCallerPrincipal principal) {
      Log.debugf("Augmenting security identity for principal: %s", principal.getName());
      return Uni.createFrom().item(build(identity, principal));
    }
    return Uni.createFrom().item(identity);
  }

  private SecurityIdentity build(SecurityIdentity identity, OidcJwtCallerPrincipal principal) {
    Set<String> permissions = new HashSet<>(identity.getRoles());
    Log.debugf("Initial roles/permissions: %s", permissions);

    // Extract UMA permissions from the authorization claim
    JsonObject authorizationClaim = principal.getClaim("authorization");
    if (authorizationClaim != null) {
      Log.debugf("Authorization claim found: %s", authorizationClaim);

      if (authorizationClaim.containsKey("permissions")) {
        JsonArray permissionsArray = authorizationClaim.getJsonArray("permissions");
        Log.debugf("Processing %d permissions", permissionsArray.size());

        for (JsonValue permissionValue : permissionsArray) {
          if (permissionValue instanceof JsonObject) {
            JsonObject permission = (JsonObject) permissionValue;
            String resourceName = permission.getString("rsname", "unknown");

            if (permission.containsKey("scopes")) {
              JsonArray scopes = permission.getJsonArray("scopes");
              for (JsonValue scopeValue : scopes) {
                if (scopeValue instanceof JsonString) {
                  String scope = ((JsonString) scopeValue).getString();
                  String permissionString = resourceName + ":" + scope;
                  permissions.add(permissionString);
                  Log.debugf("Added permission: %s", permissionString);
                }
              }
            }
          }
        }
      } else {
        Log.warn("No permissions array found in authorization claim");
      }
    } else {
      Log.warn("No authorization claim found in token");
    }

    Log.debugf("Final permissions set: %s", permissions);
    return QuarkusSecurityIdentity.builder(identity)
      .addPermissionsAsString(permissions)
      .build();
  }
}
