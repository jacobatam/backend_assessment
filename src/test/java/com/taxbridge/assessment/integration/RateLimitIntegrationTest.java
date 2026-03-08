package com.taxbridge.assessment.integration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import io.restassured.http.ContentType;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class RateLimitIntegrationTest extends BaseIntegrationTest {

    private static final UUID FREE_TENANT =
            UUID.fromString("a1000000-0000-0000-0000-000000000001");

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Generate JWT dynamically using application secret
     */
    private String generateToken(UUID tenantId) {

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        String token = Jwts.builder()
                .subject("user1")
                .claim("tenantId", tenantId.toString())
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        return "Bearer " + token;
    }

    /**
     * Test 1 — Rate limit enforced
     */
    @Test
    void rateLimit_enforced() {

        String token = generateToken(FREE_TENANT);

        for (int i = 0; i < 10; i++) {

            given()
                    .header("Authorization", token)
            .when()
                    .get("/api/v1/demo/ping")
            .then()
                    .statusCode(200);
        }

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(429)
                .body("errorCode", equalTo("RATE_LIMIT_EXCEEDED"))
                .header("Retry-After", notNullValue());
    }

    /**
     * Test 2 — Window expiry resets limit
     */
    @Test
    void windowExpiry_resetsLimit() throws InterruptedException {

        String token = generateToken(FREE_TENANT);

        jdbcTemplate.update(
                "UPDATE tenant_rate_limit_config SET window_seconds = ? WHERE tenant_id = ?",
                2,
                FREE_TENANT
        );

        for (int i = 0; i < 10; i++) {

            given()
                    .header("Authorization", token)
            .when()
                    .get("/api/v1/demo/ping")
            .then()
                    .statusCode(200);
        }

        Thread.sleep(3000);

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(200);
    }

    /**
     * Test 3 — Config update takes effect
     */
    @Test
    void configUpdate_takesEffect() {

        String token = generateToken(FREE_TENANT);

        Map<String, Object> request = Map.of(
                "maxRequests", 2,
                "windowSeconds", 60,
                "planName", "FREE"
        );

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", token)
                .body(request)
        .when()
                .put("/api/v1/admin/rate-limits/" + FREE_TENANT)
        .then()
                .statusCode(200);

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(200);

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(429);
    }

    /**
     * Test 4 — Unknown tenant
     */
    @Test
    void unknownTenant_returns404() {

        String token = generateToken(UUID.randomUUID());

        given()
                .header("Authorization", token)
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(404)
                .body("errorCode", equalTo("TENANT_NOT_FOUND"));
    }

    /**
     * Test 5 — Missing JWT
     */
    @Test
    void missingJwt_returns401() {

        given()
        .when()
                .get("/api/v1/demo/ping")
        .then()
                .statusCode(401);
    }
}