package org.devgurupk;

import io.quarkus.logging.Log;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {
  @Inject
  SecurityIdentity securityIdentity;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @PermissionsAllowed("hello:READ")
  public String hello() {
    Log.infof("Security Identity: %s", securityIdentity.getPrincipal().getName());
    return "Hello from Quarkus REST";
  }
}
