package com.juniverse.quarkus.crud.entity;

import com.juniverse.quarkus.crud.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testDefaultConstructor() {
        // Given & When
        User newUser = new User();

        // Then
        assertNull(newUser.name);
        assertNull(newUser.email);
        assertNull(newUser.phone);
        assertNull(newUser.city);
        assertNull(newUser.createdAt);
        assertNull(newUser.updatedAt);
    }

    @Test
    void testParameterizedConstructor() {
        // Given & When
        User newUser = new User("Test User", "test@email.com", "123-456-7890", "Test City");

        // Then
        assertEquals("Test User", newUser.name);
        assertEquals("test@email.com", newUser.email);
        assertEquals("123-456-7890", newUser.phone);
        assertEquals("Test City", newUser.city);
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        // Given & When
        User newUser = new User("Test User", "test@email.com", null, null);

        // Then
        assertEquals("Test User", newUser.name);
        assertEquals("test@email.com", newUser.email);
        assertNull(newUser.phone);
        assertNull(newUser.city);
    }

    @Test
    void testSetterAndGetterMethods() {
        // Given & When
        user.name = "Updated User";
        user.email = "updated@email.com";
        user.phone = "987-654-3210";
        user.city = "Updated City";
        
        LocalDateTime now = LocalDateTime.now();
        user.createdAt = now;
        user.updatedAt = now;

        // Then
        assertEquals("Updated User", user.name);
        assertEquals("updated@email.com", user.email);
        assertEquals("987-654-3210", user.phone);
        assertEquals("Updated City", user.city);
        assertEquals(now, user.createdAt);
        assertEquals(now, user.updatedAt);
    }

    @Test
    void testTimestampFields() {
        // Given
        LocalDateTime createdTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
        LocalDateTime updatedTime = LocalDateTime.of(2024, 1, 2, 10, 0, 0);

        // When
        user.createdAt = createdTime;
        user.updatedAt = updatedTime;

        // Then
        assertEquals(createdTime, user.createdAt);
        assertEquals(updatedTime, user.updatedAt);
        assertTrue(user.updatedAt.isAfter(user.createdAt));
    }

    @Test
    void testMaxLengthFields() {
        // Given
        String maxName = "a".repeat(50);
        String maxEmail = "a".repeat(90) + "@test.com"; // Close to 100 char limit
        String maxPhone = "1".repeat(20);
        String maxCity = "C".repeat(100);

        // When
        user.name = maxName;
        user.email = maxEmail;
        user.phone = maxPhone;
        user.city = maxCity;

        // Then
        assertEquals(50, user.name.length());
        assertTrue(user.email.length() <= 100);
        assertEquals(20, user.phone.length());
        assertEquals(100, user.city.length());
    }

    @Test
    void testEmptyStringFields() {
        // Given & When
        user.name = "";
        user.email = "";
        user.phone = "";
        user.city = "";

        // Then
        assertEquals("", user.name);
        assertEquals("", user.email);
        assertEquals("", user.phone);
        assertEquals("", user.city);
    }
}