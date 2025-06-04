package org.devgurupk;


import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
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
      return Uni.createFrom().item(build(identity, principal));
    }
    return Uni.createFrom().item(identity);
  }

  private SecurityIdentity build(SecurityIdentity identity, OidcJwtCallerPrincipal principal) {
    Set<String> permissions = new HashSet<>(identity.getRoles());

    JsonObject authorizationClaim = principal.getClaim("authorization");
    if (authorizationClaim != null && authorizationClaim.containsKey("permissions")) {
      JsonArray permissionsArray = authorizationClaim.getJsonArray("permissions");
      for (int i = 0; i < permissionsArray.size(); i++) {
        JsonObject permission = permissionsArray.getJsonObject(i);
        String resourceName = permission.getString("rsname");
        JsonArray scopes = permission.getJsonArray("scopes");

        for (int j = 0; j < scopes.size(); j++) {
          String scope = scopes.getString(j);
          permissions.add(resourceName + ":" + scope);
        }
      }
    }

    return QuarkusSecurityIdentity.builder(identity)
      .addPermissionsAsString(permissions)
      .build();
  }
}
