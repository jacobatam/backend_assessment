# Per‑Tenant Fixed Window Rate Limiter

Backend Engineering Assessment -- Task 1\
Java 17 · Spring Boot 3.4.x · PostgreSQL · Redis

This project implements a per‑tenant API rate limiter for a multi‑tenant
SaaS platform. Each tenant subscribes to a plan (FREE, STARTER, PRO)
that defines how many API requests they can make within a fixed time
window.

The rate limiter is implemented using a **Redis‑backed fixed window
counter** with Spring Boot and enforced via a **Spring AOP annotation**.

------------------------------------------------------------------------

# Technology Stack

• Java 17\
• Spring Boot 3.4.x\
• PostgreSQL 15\
• Redis 7\
• Liquibase\
• Spring Security + JWT\
• Spring AOP\
• Testcontainers\
• RestAssured\
• Maven

------------------------------------------------------------------------

# Running the Project Locally

Start PostgreSQL and Redis

docker compose up -d

Run the application

./mvnw spring-boot:run

Application runs on

http://localhost:8080

------------------------------------------------------------------------

# Running Tests

Run all unit and integration tests

./mvnw test

Integration tests require Docker because they use **Testcontainers**.

------------------------------------------------------------------------

# Seeded Tenants

Liquibase seeds three tenants with predefined rate limits.

  --------------------------------------------------------------------------------------
  Plan                    Tenant ID                              Limit
  ----------------------- -------------------------------------- -----------------------
  FREE                    a1000000-0000-0000-0000-000000000001   10 requests / 60
                                                                 seconds

  STARTER                 a2000000-0000-0000-0000-000000000002   50 requests / 60
                                                                 seconds

  PRO                     a3000000-0000-0000-0000-000000000003   200 requests / 60
                                                                 seconds
  --------------------------------------------------------------------------------------

------------------------------------------------------------------------

# Sample JWT Tokens

Tokens were generated using the configured secret in application.yml.\
They can be verified at https://jwt.io

------------------------------------------------------------------------

## FREE Tenant

Tenant ID

a1000000-0000-0000-0000-000000000001

Token

Bearer
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsInRlbmFudElkIjoiYTEwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMDAxIiwicm9sZSI6IlVTRVIifQ.g3biGbM1MZcgtYzFL3AQhKY0Uf5sAc_Y85ZjI164NaI

Payload

{ "sub": "user1", "tenantId": "a1000000-0000-0000-0000-000000000001",
"role": "USER" }

------------------------------------------------------------------------

## STARTER Tenant

Tenant ID

a2000000-0000-0000-0000-000000000002

Token

Bearer
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsInRlbmFudElkIjoiYTIwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMDAyIiwicm9sZSI6IlVTRVIifQ.Vvswc7xTSpDWFy27xnlngKd3GeRh79vHXtsJvBQ2D_Q

Payload

{ "sub": "user1", "tenantId": "a2000000-0000-0000-0000-000000000002",
"role": "USER" }

------------------------------------------------------------------------

## PRO Tenant

Tenant ID

a3000000-0000-0000-0000-000000000003

Token

Bearer
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsInRlbmFudElkIjoiYTMwMDAwMDAtMDAwMC0wMDAwLTAwMDAtMDAwMDAwMDAwMDAzIiwicm9sZSI6IlVTRVIifQ.CBEEJJN2XdqCz241OecFHn8xmFoYfEh8zMDFEeVyjVc

Payload

{ "sub": "user1", "tenantId": "a3000000-0000-0000-0000-000000000003",
"role": "USER" }

------------------------------------------------------------------------

# Example API Request

curl http://localhost:8080/api/v1/demo/ping -H "Authorization: Bearer
`<TOKEN>`{=html}"

Example response

{ "success": true, "message": "pong", "data": { "tenantId":
"a1000000-0000-0000-0000-000000000001" }, "timestamp":
"2026-01-01T10:00:00Z" }

------------------------------------------------------------------------

# Why Fixed Window Counter

The fixed window algorithm was chosen because it is simple and works
efficiently with Redis atomic operations. Redis guarantees atomic
increments using the INCR command, allowing reliable request counting
even under high concurrency.

------------------------------------------------------------------------

# Why Window Start Timestamp is Included in the Key

The Redis key includes the window start timestamp so that each time
window has its own counter. When the window changes, a new key is
generated automatically and the previous key expires naturally.

------------------------------------------------------------------------

# Why Manual Redis Caching

Manual Redis caching was implemented instead of using Spring's
@Cacheable to demonstrate full understanding and control over Redis
operations, serialization, and TTL behavior.

------------------------------------------------------------------------

# Trade‑offs

Fixed window algorithms may allow bursts of requests at window
boundaries. More advanced algorithms like sliding window or token bucket
could mitigate this but would increase complexity.

------------------------------------------------------------------------

# Test Coverage

Unit Tests

• FixedWindowRateLimiterTest\
• TenantRateLimitConfigServiceTest\
• RateLimitingAspectTest

Integration Tests

• Rate limit enforcement\
• Window expiry reset\
• Config update behavior\
• Unknown tenant handling\
• Missing JWT handling

Integration tests run using **Testcontainers with real PostgreSQL and
Redis**.

------------------------------------------------------------------------

# Expected Commit Structure

feat: initial project setup, maven wrapper, docker-compose, liquibase
config\
feat: add tenant_rate_limit_config schema and seed data\
feat: implement FixedWindowRateLimiter\
feat: implement TenantRateLimitConfigService with Redis caching\
feat: implement JWT utilities and security filter\
feat: implement RateLimited annotation and RateLimitingAspect\
feat: add controllers and API endpoints\
feat: implement GlobalExceptionHandler\
test: add unit tests for rate limiter, config service, and aspect\
test: add integration tests using Testcontainers\
docs: update README with architecture and usage
