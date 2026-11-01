package com.juniverse.quarkus.crud.dto;

import com.juniverse.quarkus.crud.dto.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
    }

    @Test
    void testDefaultConstructor() {
        // Dado y cuando
        UserDTO newUserDTO = new UserDTO();

        // Entonces
        assertNull(newUserDTO.id);
        assertNull(newUserDTO.name);
        assertNull(newUserDTO.email);
        assertNull(newUserDTO.phone);
        assertNull(newUserDTO.city);
        assertNull(newUserDTO.createdAt);
        assertNull(newUserDTO.updatedAt);
    }

    @Test
    void testParameterizedConstructor() {
        // Dado
        Long id = 1L;
        String name = "Test User";
        String email = "test@email.com";
        String phone = "123-456-7890";
        String city = "Test City";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // Cuando
        UserDTO newUserDTO = new UserDTO(id, name, email, phone, city, createdAt, updatedAt);

        // Entonces
        assertEquals(id, newUserDTO.id);
        assertEquals(name, newUserDTO.name);
        assertEquals(email, newUserDTO.email);
        assertEquals(phone, newUserDTO.phone);
        assertEquals(city, newUserDTO.city);
        assertEquals(createdAt, newUserDTO.createdAt);
        assertEquals(updatedAt, newUserDTO.updatedAt);
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        // Dado y cuando
        UserDTO newUserDTO = new UserDTO(1L, "Test User", "test@email.com", null, null, null, null);

        // Entonces
        assertEquals(1L, newUserDTO.id);
        assertEquals("Test User", newUserDTO.name);
        assertEquals("test@email.com", newUserDTO.email);
        assertNull(newUserDTO.phone);
        assertNull(newUserDTO.city);
        assertNull(newUserDTO.createdAt);
        assertNull(newUserDTO.updatedAt);
    }

    @Test
    void testFieldAssignment() {
        // Dado
        LocalDateTime now = LocalDateTime.now();

        // Cuando
        userDTO.id = 123L;
        userDTO.name = "Assigned User";
        userDTO.email = "assigned@email.com";
        userDTO.phone = "555-0000";
        userDTO.city = "Assigned City";
        userDTO.createdAt = now;
        userDTO.updatedAt = now;

        // Entonces
        assertEquals(123L, userDTO.id);
        assertEquals("Assigned User", userDTO.name);
        assertEquals("assigned@email.com", userDTO.email);
        assertEquals("555-0000", userDTO.phone);
        assertEquals("Assigned City", userDTO.city);
        assertEquals(now, userDTO.createdAt);
        assertEquals(now, userDTO.updatedAt);
    }

    @Test
    void testTimestampComparison() {
        // Dado
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = createdAt.plusHours(1);

        // Cuando
        userDTO.createdAt = createdAt;
        userDTO.updatedAt = updatedAt;

        // Entonces
        assertTrue(userDTO.updatedAt.isAfter(userDTO.createdAt));
        assertFalse(userDTO.createdAt.isAfter(userDTO.updatedAt));
    }
}