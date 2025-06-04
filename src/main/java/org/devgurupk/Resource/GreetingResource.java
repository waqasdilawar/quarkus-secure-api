package org.devgurupk.Resource;

import io.quarkus.logging.Log;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/")
public class GreetingResource {
  @Inject
  SecurityIdentity securityIdentity;

  @GET
  @Path("/hello")
  @Produces(MediaType.APPLICATION_JSON)
  @PermissionsAllowed("hello:READ")
  public Response hello() {
    Log.infof("Security Identity: %s", securityIdentity.getPrincipal().getName());

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Hello from secured endpoint");
    response.put("user", securityIdentity.getPrincipal().getName());
    response.put("permissions", securityIdentity.getRoles());

    return Response.ok(response).build();
  }

  @GET
  @Path("/admin")
  @Produces(MediaType.APPLICATION_JSON)
  @PermissionsAllowed("admin:ACCESS")
  public Response admin() {
    return Response.ok(Map.of(
        "message", "Admin area - restricted access",
        "user", securityIdentity.getPrincipal().getName()
    )).build();
  }

  @GET
  @Path("/public")
  @Produces(MediaType.APPLICATION_JSON)
  @PermitAll
  public Response publicEndpoint() {
    return Response.ok(Map.of("message", "This is a public endpoint")).build();
  }

  @GET
  @Path("/token-info")
  @Produces(MediaType.APPLICATION_JSON)
  public Response tokenInfo() {
    if (securityIdentity.getPrincipal() instanceof OidcJwtCallerPrincipal principal) {
      JsonObject authorizationClaim = principal.getClaim("authorization");

      Map<String, Object> tokenInfo = new HashMap<>();
      tokenInfo.put("principal", principal.getName());
      tokenInfo.put("roles", securityIdentity.getRoles());
      tokenInfo.put("permissions", securityIdentity.getRoles());
      tokenInfo.put("authorization", authorizationClaim);

      return Response.ok(tokenInfo).build();
    }

    return Response.status(Response.Status.UNAUTHORIZED)
        .entity(Map.of("error", "Not authenticated with a valid token"))
        .build();
  }
}
