package com.juniverse.quarkus.crud.controller;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import com.juniverse.quarkus.crud.entity.User;
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
class UserControllerTest {

    @Inject
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean existing data
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User("Test User", "test@controller.com", "123-456-7890", "Test City");
        userRepository.persist(testUser);
    }

    @Test
    void testGetAllUsers_ShouldReturnOkWithUserList() {
        given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("[0].name", equalTo("Test User"))
                .body("[0].email", equalTo("test@controller.com"));
    }

    @Test
    void testGetUserById_ExistingUser_ShouldReturnOk() {
        given()
            .pathParam("id", testUser.id)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test User"))
                .body("email", equalTo("test@controller.com"))
                .body("id", equalTo(testUser.id.intValue()));
    }

    @Test
    void testGetUserById_NonExistingUser_ShouldReturn404() {
        given()
            .pathParam("id", 999)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(404)
                .body(containsString("Usuario no encontrado con ID: 999"));
    }

    @Test
    void testCreateUser_ValidData_ShouldReturnCreated() {
        CreateUserDTO newUser = new CreateUserDTO("New User", "new@email.com", "555-0000", "New City");

        given()
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", equalTo("New User"))
                .body("email", equalTo("new@email.com"))
                .body("phone", equalTo("555-0000"))
                .body("city", equalTo("New City"))
                .body("id", notNullValue())
                .body("createdAt", notNullValue())
                .body("updatedAt", notNullValue());
    }

    @Test
    void testCreateUser_DuplicateEmail_ShouldReturn409() {
        CreateUserDTO duplicateUser = new CreateUserDTO("Duplicate User", "test@controller.com", "555-0000", "New City");

        given()
            .contentType(ContentType.JSON)
            .body(duplicateUser)
            .when()
                .post("/users")
            .then()
                .statusCode(409)
                .contentType(ContentType.JSON)
                .body("message", containsString("Ya existe un usuario con el email"))
                .body("status", equalTo(409));
    }

    @Test
    void testCreateUser_InvalidData_ShouldReturn400() {
        CreateUserDTO invalidUser = new CreateUserDTO("", "", "123", "City");

        given()
            .contentType(ContentType.JSON)
            .body(invalidUser)
            .when()
                .post("/users")
            .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("title", equalTo("Constraint Violation"))
                .body("status", equalTo(400))
                .body("violations", hasSize(greaterThan(0)));
    }

    @Test
    void testCreateUser_MissingRequiredFields_ShouldReturn400() {
        // Send empty object
        given()
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
                .post("/users")
            .then()
                .statusCode(400);
    }

    @Test
    void testUpdateUser_ExistingUser_ShouldReturnOk() {
        CreateUserDTO updateUser = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        given()
            .pathParam("id", testUser.id)
            .contentType(ContentType.JSON)
            .body(updateUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Updated User"))
                .body("email", equalTo("updated@email.com"))
                .body("phone", equalTo("999-8888"))
                .body("city", equalTo("Updated City"));
    }

    @Test
    void testUpdateUser_NonExistingUser_ShouldReturn404() {
        CreateUserDTO updateUser = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        given()
            .pathParam("id", 999)
            .contentType(ContentType.JSON)
            .body(updateUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(404)
                .body(containsString("Usuario no encontrado con ID: 999"));
    }

    @Test
    void testUpdateUser_DuplicateEmail_ShouldReturn409() {
        // First create another user via API to avoid transaction conflicts
        CreateUserDTO newUser = new CreateUserDTO("Another User", "another@email.com", "111-2222", "Another City");
        
        Integer anotherUserId = given()
            .contentType(ContentType.JSON)
            .body(newUser)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Now try to update testUser with the same email
        CreateUserDTO updateUser = new CreateUserDTO("Updated User", "another@email.com", "999-8888", "Updated City");

        given()
            .pathParam("id", testUser.id)
            .contentType(ContentType.JSON)
            .body(updateUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(409)
                .body("message", containsString("Ya existe otro usuario con el email"));
    }

    @Test
    void testUpdateUser_InvalidData_ShouldReturn400() {
        CreateUserDTO invalidUser = new CreateUserDTO("", "invalid-email", "123", "City");

        given()
            .pathParam("id", testUser.id)
            .contentType(ContentType.JSON)
            .body(invalidUser)
            .when()
                .put("/users/{id}")
            .then()
                .statusCode(400)
                .body("title", equalTo("Constraint Violation"))
                .body("violations", hasSize(greaterThan(0)));
    }

    @Test
    void testDeleteUser_ExistingUser_ShouldReturn204() {
        given()
            .pathParam("id", testUser.id)
            .when()
                .delete("/users/{id}")
            .then()
                .statusCode(204);

        // Verify user was deleted
        given()
            .pathParam("id", testUser.id)
            .when()
                .get("/users/{id}")
            .then()
                .statusCode(404);
    }

    @Test
    void testDeleteUser_NonExistingUser_ShouldReturn404() {
        given()
            .pathParam("id", 999)
            .when()
                .delete("/users/{id}")
            .then()
                .statusCode(404)
                .body(containsString("Usuario no encontrado con ID: 999"));
    }

    @Test
    void testGetUserByEmail_ExistingEmail_ShouldReturnOk() {
        given()
            .pathParam("email", "test@controller.com")
            .when()
                .get("/users/email/{email}")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", equalTo("Test User"))
                .body("email", equalTo("test@controller.com"));
    }

    @Test
    void testGetUserByEmail_NonExistingEmail_ShouldReturn404() {
        given()
            .pathParam("email", "nonexistent@email.com")
            .when()
                .get("/users/email/{email}")
            .then()
                .statusCode(404)
                .body(containsString("Usuario no encontrado con email: nonexistent@email.com"));
    }

    @Test
    void testCreateUser_WithOptionalFields_ShouldReturnCreated() {
        CreateUserDTO userWithOptionals = new CreateUserDTO("Minimal User", "minimal@email.com", null, null);

        given()
            .contentType(ContentType.JSON)
            .body(userWithOptionals)
            .when()
                .post("/users")
            .then()
                .statusCode(201)
                .body("name", equalTo("Minimal User"))
                .body("email", equalTo("minimal@email.com"))
                .body("phone", nullValue())
                .body("city", nullValue());
    }

    @Test
    void testCreateUser_LongNames_ShouldReturn400() {
        String longName = "a".repeat(60); // Exceeds 50 character limit
        CreateUserDTO userWithLongName = new CreateUserDTO(longName, "long@email.com", "123", "City");

        given()
            .contentType(ContentType.JSON)
            .body(userWithLongName)
            .when()
                .post("/users")
            .then()
                .statusCode(400)
                .body("violations.message", hasItem(containsString("entre 2 y 50 caracteres")));
    }

    @Test
    void testGetAllUsers_WithExistingData_ShouldReturnUsers() {
        // This test verifies that getAllUsers returns the test user created in setUp
        given()
            .when()
                .get("/users")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1))
                .body("name", hasItem("Test User"))
                .body("email", hasItem("test@controller.com"));
    }
}