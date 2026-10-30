package com.juniverse.quarkus.crud.integration;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import com.juniverse.quarkus.crud.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserIntegrationTest {

    @Inject
    UserRepository userRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean database before each test
        userRepository.deleteAll();
    }

    @Test
    void testCompleteUserLifecycle() {
        CreateUserDTO newUser = new CreateUserDTO("Integration User", "integration@test.com", "555-1234", "Integration City");

        // 1. Create user
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .body("name", equalTo("Integration User"))
                .body("email", equalTo("integration@test.com"))
                .extract().path("id");

        // 2. Get user by ID
        given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Integration User"))
                .body("email", equalTo("integration@test.com"));

        // 3. Get user by email
        given()
            .pathParam("email", "integration@test.com")
            .when()
                .get("/users/email/{email}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Integration User"));

        // 4. Update user
        CreateUserDTO updateUser = new CreateUserDTO("Updated Integration User", "updated-integration@test.com", "555-9876", "Updated City");
        
        given()
            .pathParam("id", userId)
            .contentType(ContentType.JSON)
            .body(updateUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Updated Integration User"))
                .body("email", equalTo("updated-integration@test.com"));

        // 5. Verify update
        given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Updated Integration User"))
                .body("email", equalTo("updated-integration@test.com"));

        // 6. Delete user
        given()
            .pathParam("id", userId)
            .when()
                .delete("/users/{id}")
            .then()
                .statusCode(204);

        // 7. Verify deletion
        given()
            .pathParam("id", userId)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(404);
    }

    @Test
    void testMultipleUsersCreationAndRetrieval() {
        // Create multiple users
        CreateUserDTO user1 = new CreateUserDTO("User One", "user1@test.com", "111-1111", "City One");
        CreateUserDTO user2 = new CreateUserDTO("User Two", "user2@test.com", "222-2222", "City Two");
        CreateUserDTO user3 = new CreateUserDTO("User Three", "user3@test.com", "333-3333", "City Three");

        // Create users
        given()
            .contentType(ContentType.JSON)
            .body(user1)
            .when()
                .post("/users")
            .then()
                .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(user2)
            .when()
                .post("/users")
            .then()
                .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(user3)
            .when()
                .post("/users")
            .then()
                .statusCode(201);

        // Get all users and verify count
        given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .body("size()", is(3))
                .body("name", hasItems("User One", "User Two", "User Three"))
                .body("email", hasItems("user1@test.com", "user2@test.com", "user3@test.com"));
    }

    @Test
    void testEmailUniquenessConstraint() {
        CreateUserDTO user1 = new CreateUserDTO("User One", "same@email.com", "111-1111", "City One");
        CreateUserDTO user2 = new CreateUserDTO("User Two", "same@email.com", "222-2222", "City Two");

        // Create first user
        given()
            .contentType(ContentType.JSON)
            .body(user1)
            .when()
                .post("/users")
            .then()
                .statusCode(201);

        // Try to create second user with same email
        given()
            .contentType(ContentType.JSON)
            .body(user2)
            .when()
                .post("/users")
            .then()
                .statusCode(409)
                .body("message", containsString("Ya existe un usuario con el email"));
    }

    @Test
    void testValidationErrors() {
        // Test empty name
        CreateUserDTO invalidUser1 = new CreateUserDTO("", "valid@email.com", "123-4567", "City");
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidUser1)
            .when()
                .post("/users")
            .then()
                .statusCode(400)
                .body("title", equalTo("Constraint Violation"))
                .body("violations.message", hasItem(containsString("nombre es obligatorio")));

        // Test invalid email
        CreateUserDTO invalidUser2 = new CreateUserDTO("Valid Name", "invalid-email", "123-4567", "City");
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidUser2)
            .when()
                .post("/users")
            .then()
                .statusCode(400)
                .body("violations.message", hasItem(containsString("email inválido")));

        // Test name too long
        String longName = "a".repeat(60); // Exceeds 50 character limit
        CreateUserDTO invalidUser3 = new CreateUserDTO(longName, "valid@email.com", "123-4567", "City");
        
        given()
            .contentType(ContentType.JSON)
            .body(invalidUser3)
            .when()
                .post("/users")
            .then()
                .statusCode(400)
                .body("violations.message", hasItem(containsString("entre 2 y 50 caracteres")));
    }

    @Test
    void testUpdateWithSameEmail() {
        // Create a user
        CreateUserDTO newUser = new CreateUserDTO("Test User", "test@email.com", "123-4567", "Test City");
        
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Update with same email (should be allowed)
        CreateUserDTO updateUser = new CreateUserDTO("Updated Test User", "test@email.com", "987-6543", "Updated City");
        
        given()
            .pathParam("id", userId)
            .contentType(ContentType.JSON)
            .body(updateUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(200)
                .body("name", equalTo("Updated Test User"))
                .body("email", equalTo("test@email.com"));
    }

    @Test
    void testConcurrentOperations() {
        // Create base user
        CreateUserDTO user = new CreateUserDTO("Concurrent User", "concurrent@test.com", "555-0000", "Concurrent City");
        
        Integer userId = given()
            .contentType(ContentType.JSON)
            .body(user)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Simulate concurrent reads (should all succeed)
        for (int i = 0; i < 5; i++) {
            given()
                .pathParam("id", userId)
                .when()
                    .get("/users/{id}")
                .then()
                    .statusCode(200)
                    .body("name", equalTo("Concurrent User"));
        }

        // Test multiple gets by email
        for (int i = 0; i < 5; i++) {
            given()
                .pathParam("email", "concurrent@test.com")
                .when()
                    .get("/users/email/{email}")
                .then()
                    .statusCode(200)
                    .body("name", equalTo("Concurrent User"));
        }
    }
}