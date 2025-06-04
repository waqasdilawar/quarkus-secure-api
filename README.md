# Secure API with Keycloak UMA Authorization

This project demonstrates a Jakarta EE application using Quarkus and Keycloak with User-Managed Access (UMA) for fine-grained authorization. It showcases how to secure REST endpoints with permissions-based access control using UMA.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
  - [1. Start Keycloak](#1-start-keycloak)
  - [2. Start the Quarkus Application](#2-start-the-quarkus-application)
  - [3. Test the API](#3-test-the-api)
- [Understanding UMA Authorization](#understanding-uma-authorization)
- [Security Components](#security-components)
- [Troubleshooting](#troubleshooting)

## Architecture Overview

This application demonstrates a modern security architecture using:

- **Quarkus**: Java framework with native Jakarta EE support
- **Keycloak**: Identity and Access Management with OAuth 2.0/OIDC
- **User-Managed Access (UMA)**: Extension of OAuth 2.0 for fine-grained authorization
- **Requesting Party Token (RPT)**: Special access token containing permissions

## Prerequisites

- Java 24 or higher
- Maven 3.8.6 or higher
- Docker and Docker Compose
- curl and jq (for testing)

## Quick Start

### 1. Start Keycloak

```bash
  # Start Keycloak with automatic realm import
  docker compose up -d

  # Wait for Keycloak to start (check with)
  docker compose logs -f
```

This will:
- Start Keycloak on port 8180
- Create an admin user (admin/admin)
- Import the preconfigured realm with the secured-api client

### 2. Start the Quarkus Application

```bash
  mvn quarkus:dev
```

### 3. Test the API

#### Get an Access Token

```bash
  export access_token=$(curl --silent --location 'http://localhost:8180/realms/devgurupk/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'client_id=secured-api' \
  --data-urlencode 'client_secret=TvzMGHjySFk4Nd1jZV0uh1Z8NP8DPiIq' \
  --data-urlencode 'grant_type=client_credentials' | jq --raw-output '.access_token')

  echo "Access Token: $access_token"
```

#### Get a Requesting Party Token (RPT)

```bash
  export rpt_token=$(curl --silent --location 'http://localhost:8180/realms/devgurupk/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --header "Authorization: Bearer $access_token" \
  --data-urlencode 'grant_type=urn:ietf:params:oauth:grant-type:uma-ticket' \
  --data-urlencode 'audience=secured-api' | jq --raw-output '.access_token')
  
  echo "RPT Token: $rpt_token"
```

#### Call the Protected API

```bash
  curl --location 'http://localhost:8080/hello' --header "Authorization: Bearer $rpt_token"
```

#### Call the Admin API (should return 403)
```bash
  curl --location 'http://localhost:8080/admin' --header "Authorization: Bearer $rpt_token"
```

#### Call the Public API (no authentication required)
```bash
  curl --location 'http://localhost:8080/public'
```

#### Get Token Information
```bash
  curl --location 'http://localhost:8080/token-info' --header "Authorization: Bearer $rpt_token"
```

## Calling this API should return token info
```bash
  curl --location 'http://localhost:8080/token-info' --header "Authorization: Bearer $rpt_token"
```

## Understanding UMA Authorization

User-Managed Access (UMA) is an OAuth 2.0 extension that enables fine-grained authorization:

1. **Authentication**: User or client authenticates with Keycloak
2. **Access Token**: Standard OAuth 2.0 token is issued
3. **Permission Request**: Client requests specific permissions
4. **RPT Token**: Keycloak issues a Requesting Party Token with embedded permissions
5. **Authorization**: The application verifies permissions in the RPT

This flow separates authentication from authorization, enabling fine-grained, dynamic permission control.

## Security Components

### 1. CustomSecurityIdentityAugmentor

The `CustomSecurityIdentityAugmentor` class extracts UMA permissions from the JWT token and adds them to the security identity:

```java
@ApplicationScoped
public class CustomSecurityIdentityAugmentor implements SecurityIdentityAugmentor {
    // Extracts permissions from the 'authorization' claim
    // Maps them to Quarkus permissions as 'resourceName:scope'
}
```

### 2. GreetingResource

API endpoints use `@PermissionsAllowed` annotation to protect resources:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
@PermissionsAllowed("hello:READ")
public String hello() {
    // Only accessible with hello:READ permission
}
```

### 3. Keycloak UMA Configuration

The Keycloak realm is configured with:

- Resource Server (secured-api client)
- Protected Resources (hello)
- Permission Policies

## Troubleshooting

### Keycloak Issues

- **Realm Import Failed**: Log into admin console (http://localhost:8180/admin) and import manually
- **Connection Refused**: Ensure Keycloak is running (`docker-compose ps`)
- **Invalid Client**: Verify client_id and client_secret in the curl commands

### Token Issues

- **Invalid Token**: Check token expiration and scopes
- **Missing Permissions**: Verify UMA permissions in Keycloak

### Application Issues

- **Authentication Failed**: Check OIDC configuration in application.properties
- **Authorization Failed**: Verify the permission mapping in CustomSecurityIdentityAugmentor
- **Public Endpoints Require Authentication**: Ensure you've properly configured path-specific permissions in application.properties:
  ```properties
  # Secure specific paths
  quarkus.http.auth.permission.authenticated.paths=/hello,/admin,/token-info
  quarkus.http.auth.permission.authenticated.policy=authenticated

  # Public paths
  quarkus.http.auth.permission.public.paths=/public
  quarkus.http.auth.permission.public.policy=permit
  ```

---

## Keycloak Admin Access

- **URL**: http://localhost:8180/admin
- **Username**: admin
- **Password**: admin