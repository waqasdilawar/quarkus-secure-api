package org.devgurupk.filter;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    Log.debugf(
      ">> Request: %s %s",
      requestContext.getMethod(),
      requestContext.getUriInfo().getPath()
    );

    if (requestContext.getHeaderString("Authorization") != null) {
      Log.debugf(
        "Auth header present: %s",
        requestContext.getHeaderString("Authorization").substring(0, 15) + "..."
      );
    }
    else {
      Log.debugf("No Authorization header");
    }
  }

  @Override
  public void filter(
    ContainerRequestContext requestContext,
    ContainerResponseContext responseContext
  ) {
    Log.debugf(
      "<< Response: %s %s -> %d",
      requestContext.getMethod(),
      requestContext.getUriInfo().getPath(),
      responseContext.getStatus()
    );
  }
}
