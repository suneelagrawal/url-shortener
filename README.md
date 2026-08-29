# URL Shortener

## Overview

Spring Boot based URL shortener service.

## Tech Stack

* Java 25
* Spring Boot 4.1.1
* Maven
* Spring Web
* Spring Data JPA
* H2 in-memory database
* Spring Boot Actuator
* Docker
* Docker Compose

## Architecture

```text
https://github.com/spring-projects/spring-boot/blob/main/README.adoc
                              |
                              v
                  +-------------------------+
                  | URL Shortening Service  |
                  +-------------------------+
                              |
                              v
                   http://localhost:8080/aB12Cd


                   http://localhost:8080/aB12Cd
                              |
                              v
                  +-------------------------+
                  | URL Shortening Service  |
                  +-------------------------+
                              |
                              v
                       HTTP 302 Redirect
                              |
                              v
https://github.com/spring-projects/spring-boot/blob/main/README.adoc
```

## Package Structure

```text
com.platform.urlshortener
├── controller
├── dto
├── entity
├── exception
├── repository
├── service
└── util
```

## AI Review and Engineering Decisions

* **PostgreSQL → H2**

  * **Why:** Simpler setup with no external database dependency.
  * **Trade-off:** Data is lost on restart. PostgreSQL would be preferred for production.

* **Used `@Column` unique constraint**

  * Removed duplicate table-level uniqueness configuration since `shortCode` requires only a single-column unique constraint.

* **Used the same 6-character alphanumeric rule for custom aliases**

  * AI suggested a variable length of 3–30 characters.
  * Changed to 6 characters to keep generated and custom short codes consistent.

* **Custom aliases are case-sensitive**

  * Keeps generated and custom short-code behavior consistent.

## Implementation Scenarios

### Greenfield Development

Built the initial URL shortener from scratch.

1. Implemented short URL generation
2. Implemented short URL redirect using HTTP `302`
3. Added validation and centralized error handling
4. Added unit and integration tests
5. Added basic click analytics

### Brownfield Development

Enhanced the existing application with optional URL expiration.

1. Added `expiresAt` to the existing URL entity
2. Updated DTO, service, redirect behavior, exception handling, and tests
3. Preserved backward compatibility for URLs without expiration
4. Added validation for:

   * Active URL → `302 Found`
   * Expired URL → `410 Gone`
   * Unknown URL → `404 Not Found`

### Ambiguous Requirement

Requirement:

> Users should be able to customize their shortened URL.

Assumptions:

* Custom alias is optional
* Alias must be exactly 6 characters
* Allowed characters: letters and numbers only
* Aliases are case-sensitive
* Duplicate alias returns `409 Conflict`
* Authentication and ownership are out of scope
* Custom aliases follow the same expiration behavior as generated URLs

Implemented custom aliases based on these assumptions and added validation for duplicate and invalid aliases.

## Testing Approach

Testing includes:

* Unit tests for short-code generation
* Unit tests for service logic
* Integration tests for REST APIs
* Validation and exception scenarios
* URL expiration tests
* Custom alias tests
* Manual end-to-end validation using `curl`

Run all tests:

```bash
mvn clean test
```

## Limitations and Trade-offs

* H2 is in-memory, so data is lost when the application restarts.
* The service currently runs as a single application instance.
* Analytics updates are synchronous during redirect.
* Authentication and ownership of custom aliases are out of scope.
* PostgreSQL and distributed caching would be considered for production scale.

## Run Locally

### Maven

Build and test:

```bash
mvn clean test
```

Start:

```bash
mvn spring-boot:run
```

Verify:

```bash
curl http://localhost:8080/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

### Docker Compose

From the directory containing `docker-compose.yml`:

```bash
docker compose up --build
```

Verify:

```bash
curl http://localhost:8080/actuator/health
```

Stop:

```bash
docker compose down
```

## API Examples

### Create Short URL

```bash
curl -X POST http://localhost:8080/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"url\":\"https://github.com/spring-projects/spring-boot/blob/main/README.adoc\"}"
```

Example response:

```json
{
  "shortCode": "aB12Cd",
  "shortUrl": "http://localhost:8080/aB12Cd",
  "originalUrl": "https://github.com/spring-projects/spring-boot/blob/main/README.adoc"
}
```

The generated short code will vary at runtime.

### Redirect Using Short URL

```bash
curl -i http://localhost:8080/aB12Cd
```

Example response:

```text
HTTP/1.1 302 Found
Location: https://github.com/spring-projects/spring-boot/blob/main/README.adoc
```

### Get Analytics

```bash
curl http://localhost:8080/api/v1/urls/aB12Cd/analytics
```

Example response:

```json
{
  "shortCode": "aB12Cd",
  "clickCount": 1,
  "createdAt": "2026-08-29T20:00:00Z",
  "lastAccessedAt": "2026-08-29T20:05:00Z"
}
```

### Create URL with Expiration

```bash
curl -X POST http://localhost:8080/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"url\":\"https://github.com\",\"expiresAt\":\"2026-08-30T18:00:00Z\"}"
```

If the short URL is accessed after expiration:

```text
HTTP/1.1 410 Gone
```

### Create URL with Custom Alias

```bash
curl -X POST http://localhost:8080/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"url\":\"https://github.com\",\"customAlias\":\"Spring\"}"
```

Example response:

```json
{
  "shortCode": "Spring",
  "shortUrl": "http://localhost:8080/Spring",
  "originalUrl": "https://github.com"
}
```

### Duplicate Custom Alias

```bash
curl -i -X POST http://localhost:8080/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"url\":\"https://google.com\",\"customAlias\":\"Spring\"}"
```

Expected:

```text
HTTP/1.1 409 Conflict
```

### Invalid URL

```bash
curl -i -X POST http://localhost:8080/api/v1/urls ^
  -H "Content-Type: application/json" ^
  -d "{\"url\":\"ftp://test\"}"
```

Expected:

```text
HTTP/1.1 400 Bad Request
```

Example response:

```json
{
  "error": "BAD_REQUEST",
  "message": "URL must start with http:// or https://"
}
```

### Unknown Short Code

```bash
curl -i http://localhost:8080/ABC999
```

Expected:

```text
HTTP/1.1 404 Not Found
```
