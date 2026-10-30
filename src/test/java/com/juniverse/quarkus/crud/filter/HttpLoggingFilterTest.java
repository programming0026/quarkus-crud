package com.juniverse.quarkus.crud.filter;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
class HttpLoggingFilterTest {

    @Test
    void testLoggingFilterWithGetRequest() {
        given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testLoggingFilterWithPostRequest() {
        String uniqueEmail = "filter" + System.currentTimeMillis() + "@test.com";
        CreateUserDTO newUser = new CreateUserDTO("Test Filter User", uniqueEmail, "555-0123", "Filter City");

        given()
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON);
    }

    @Test
    void testLoggingFilterWithInvalidRequest() {
        CreateUserDTO invalidUser = new CreateUserDTO("", "invalid-email", "", "");

        given()
            .contentType(ContentType.JSON)
            .body(invalidUser)
            .when()
                .post("/users")
            .then()
                .statusCode(400);
    }

    @Test
    void testLoggingFilterWithNotFoundRequest() {
        given()
            .pathParam("id", 99999)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(404);
    }
}