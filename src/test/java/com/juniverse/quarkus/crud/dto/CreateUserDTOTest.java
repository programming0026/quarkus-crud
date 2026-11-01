package com.juniverse.quarkus.crud.dto;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserDTOTest {

    private CreateUserDTO createUserDTO;

    @BeforeEach
    void setUp() {
        createUserDTO = new CreateUserDTO();
    }

    @Test
    void testDefaultConstructor() {
        // Dado y cuando
        CreateUserDTO newCreateUserDTO = new CreateUserDTO();

        // Entonces
        assertNull(newCreateUserDTO.name);
        assertNull(newCreateUserDTO.email);
        assertNull(newCreateUserDTO.phone);
        assertNull(newCreateUserDTO.city);
    }

    @Test
    void testParameterizedConstructor() {
        // Dado
        String name = "Test User";
        String email = "test@email.com";
        String phone = "123-456-7890";
        String city = "Test City";

        // Cuando
        CreateUserDTO newCreateUserDTO = new CreateUserDTO(name, email, phone, city);

        // Entonces
        assertEquals(name, newCreateUserDTO.name);
        assertEquals(email, newCreateUserDTO.email);
        assertEquals(phone, newCreateUserDTO.phone);
        assertEquals(city, newCreateUserDTO.city);
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        // Dado y cuando
        CreateUserDTO newCreateUserDTO = new CreateUserDTO("Test User", "test@email.com", null, null);

        // Entonces
        assertEquals("Test User", newCreateUserDTO.name);
        assertEquals("test@email.com", newCreateUserDTO.email);
        assertNull(newCreateUserDTO.phone);
        assertNull(newCreateUserDTO.city);
    }

    @Test
    void testFieldAssignment() {
        // Dado y cuando
        createUserDTO.name = "Assigned User";
        createUserDTO.email = "assigned@email.com";
        createUserDTO.phone = "555-0000";
        createUserDTO.city = "Assigned City";

        // Entonces
        assertEquals("Assigned User", createUserDTO.name);
        assertEquals("assigned@email.com", createUserDTO.email);
        assertEquals("555-0000", createUserDTO.phone);
        assertEquals("Assigned City", createUserDTO.city);
    }

    @Test
    void testEmptyStringValues() {
        // Dado y cuando
        createUserDTO.name = "";
        createUserDTO.email = "";
        createUserDTO.phone = "";
        createUserDTO.city = "";

        // Entonces
        assertEquals("", createUserDTO.name);
        assertEquals("", createUserDTO.email);
        assertEquals("", createUserDTO.phone);
        assertEquals("", createUserDTO.city);
    }

    @Test
    void testWithRequiredFieldsOnly() {
        // Dado y cuando
        CreateUserDTO minimalDTO = new CreateUserDTO("Required User", "required@email.com", null, null);

        // Entonces
        assertEquals("Required User", minimalDTO.name);
        assertEquals("required@email.com", minimalDTO.email);
        assertNull(minimalDTO.phone);
        assertNull(minimalDTO.city);
    }

    @Test
    void testWithMaxLengthValues() {
        // Dado
        String maxName = "a".repeat(50);
        String longEmail = "a".repeat(90) + "@test.com"; // Close to limit
        String maxPhone = "1".repeat(20);
        String maxCity = "C".repeat(100);

        // Cuando
        createUserDTO.name = maxName;
        createUserDTO.email = longEmail;
        createUserDTO.phone = maxPhone;
        createUserDTO.city = maxCity;

        // Entonces
        assertEquals(50, createUserDTO.name.length());
        assertTrue(createUserDTO.email.length() <= 100);
        assertEquals(20, createUserDTO.phone.length());
        assertEquals(100, createUserDTO.city.length());
    }
}