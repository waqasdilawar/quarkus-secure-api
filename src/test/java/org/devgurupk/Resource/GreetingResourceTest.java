package org.devgurupk.Resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.anyOf;

@QuarkusTest
public class GreetingResourceTest {

  @Test
  @TestSecurity(user = "testUser", permissions = {"hello:READ"})
  public void testHelloEndpointWithPermission() {
    RestAssured.given()
      .when().get("/hello")
      .then()
      .statusCode(200)
      .body("message", is("Hello from secured endpoint"))
      .body("user", is("testUser"))
      .body("permissions", notNullValue());
  }

  @Test
  @TestSecurity(user = "testUser", roles = {"some_other_permission"})
  public void testHelloEndpointWithoutPermission() {
    RestAssured.given()
      .when().get("/hello")
      .then()
      .statusCode(403);
  }

  @Test
  public void testHelloEndpointUnauthorized() {
    RestAssured.given()
      .when().get("/hello")
      .then()
      .statusCode(401);
  }

  @Test
  @TestSecurity(user = "adminUser", permissions = {"admin:ACCESS"})
  public void testAdminEndpointWithPermission() {
    RestAssured.given()
      .when().get("/admin")
      .then()
      .statusCode(200)
      .body("message", is("Admin area - restricted access"))
      .body("user", is("adminUser"));
  }

  @Test
  @TestSecurity(user = "testUser", roles = {"hello:READ"})
  public void testAdminEndpointWithoutPermission() {
    RestAssured.given()
      .when().get("/admin")
      .then()
      .statusCode(403);
  }

  @Test
  public void testAdminEndpointUnauthorized() {
    RestAssured.given()
      .when().get("/admin")
      .then()
      .statusCode(401);
  }

  @Test
  public void testPublicEndpointNoAuth() {
    RestAssured.given()
      .when().get("/public")
      .then()
      .statusCode(200)
      .body("message", is("This is a public endpoint"))
      .body("authenticated", is(false))
      .body("timestamp", notNullValue());
  }

  @Test
  @TestSecurity(user = "testUser", roles = {"anyRole"})
  public void testPublicEndpointWithAuth() {
    RestAssured.given()
      .when().get("/public")
      .then()
      .statusCode(200)
      .body("message", is("This is a public endpoint"))
      .body("authenticated", is(true))
      .body("timestamp", notNullValue());
  }

  @Test
  @TestSecurity(user = "oidcUser", roles = {"user_role", "offline_access"})
  public void testTokenInfoEndpointWithOidcUser() {
    RestAssured.given()
      .when().get("/token-info")
      .then()
      .statusCode(anyOf(is(200), is(401)));
  }

  @Test
  public void testTokenInfoEndpointUnauthorized() {
    RestAssured.given()
      .when().get("/token-info")
      .then()
      .statusCode(401);
  }
}