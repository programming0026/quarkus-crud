package com.juniverse.quarkus.crud.service;

import com.juniverse.quarkus.crud.service.UserService;
import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import com.juniverse.quarkus.crud.dto.UserDTO;
import com.juniverse.quarkus.crud.entity.User;
import com.juniverse.quarkus.crud.exception.BusinessException;
import com.juniverse.quarkus.crud.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Inject
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean existing data
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User("Test User", "test@service.com", "123-456-7890", "Test City");
        testUser.createdAt = LocalDateTime.now();
        testUser.updatedAt = LocalDateTime.now();
        userRepository.persist(testUser);
    }

    @Test
    void testGetAllUsers_ShouldReturnAllUsers() {
        // When
        List<UserDTO> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertEquals(1, users.size());
        UserDTO user = users.get(0);
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
    }

    @Test
    void testGetUserById_ExistingId_ShouldReturnUser() {
        // When
        Optional<UserDTO> result = userService.getUserById(testUser.id);

        // Then
        assertTrue(result.isPresent());
        UserDTO user = result.get();
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
        assertEquals(testUser.id, user.id);
    }

    @Test
    void testGetUserById_NonExistingId_ShouldReturnEmpty() {
        // When
        Optional<UserDTO> result = userService.getUserById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testCreateUser_ValidData_ShouldCreateUser() {
        // Given
        CreateUserDTO createUserDTO = new CreateUserDTO("New User", "new@email.com", "555-0000", "New City");

        // When
        UserDTO result = userService.createUser(createUserDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.id);
        assertEquals("New User", result.name);
        assertEquals("new@email.com", result.email);
        assertEquals("555-0000", result.phone);
        assertEquals("New City", result.city);
        assertNotNull(result.createdAt);
        assertNotNull(result.updatedAt);
    }

    @Test
    @Transactional
    void testCreateUser_DuplicateEmail_ShouldThrowBusinessException() {
        // Given
        CreateUserDTO createUserDTO = new CreateUserDTO("Duplicate User", "test@service.com", "555-0000", "New City");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(createUserDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe un usuario con el email"));
    }

    @Test
    @Transactional
    void testUpdateUser_ExistingUser_ShouldUpdateUser() {
        // Given
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        // When
        Optional<UserDTO> result = userService.updateUser(testUser.id, updateUserDTO);

        // Then
        assertTrue(result.isPresent());
        UserDTO updatedUser = result.get();
        assertEquals("Updated User", updatedUser.name);
        assertEquals("updated@email.com", updatedUser.email);
        assertEquals("999-8888", updatedUser.phone);
        assertEquals("Updated City", updatedUser.city);
    }

    @Test
    @Transactional
    void testUpdateUser_NonExistingUser_ShouldReturnEmpty() {
        // Given
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        // When
        Optional<UserDTO> result = userService.updateUser(999L, updateUserDTO);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testUpdateUser_DuplicateEmail_ShouldThrowBusinessException() {
        // Given - Create another user first
        User anotherUser = new User("Another User", "another@email.com", "111-2222", "Another City");
        userRepository.persist(anotherUser);

        // Try to update testUser with anotherUser's email
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "another@email.com", "999-8888", "Updated City");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUser(testUser.id, updateUserDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe otro usuario con el email"));
    }

    @Test
    @Transactional
    void testUpdateUser_SameEmailSameUser_ShouldWork() {
        // Given - Update with same email (should be allowed)
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "test@service.com", "999-8888", "Updated City");

        // When
        Optional<UserDTO> result = userService.updateUser(testUser.id, updateUserDTO);

        // Then
        assertTrue(result.isPresent());
        UserDTO updatedUser = result.get();
        assertEquals("Updated User", updatedUser.name);
        assertEquals("test@service.com", updatedUser.email); // Same email should work
    }

    @Test
    @Transactional
    void testDeleteUser_ExistingUser_ShouldReturnTrue() {
        // When
        boolean result = userService.deleteUser(testUser.id);

        // Then
        assertTrue(result);
        Optional<UserDTO> deletedUser = userService.getUserById(testUser.id);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @Transactional
    void testDeleteUser_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean result = userService.deleteUser(999L);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetUserByEmail_ExistingEmail_ShouldReturnUser() {
        // When
        Optional<UserDTO> result = userService.getUserByEmail("test@service.com");

        // Then
        assertTrue(result.isPresent());
        UserDTO user = result.get();
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
    }

    @Test
    void testGetUserByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<UserDTO> result = userService.getUserByEmail("nonexistent@email.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testCreateUser_WithNullValues_ShouldCreateUser() {
        // Given
        CreateUserDTO createUserDTO = new CreateUserDTO("Minimal User", "minimal@email.com", null, null);

        // When
        UserDTO result = userService.createUser(createUserDTO);

        // Then
        assertNotNull(result);
        assertEquals("Minimal User", result.name);
        assertEquals("minimal@email.com", result.email);
        assertNull(result.phone);
        assertNull(result.city);
    }

    @Test
    @Transactional
    void testGetAllUsers_EmptyDatabase_ShouldReturnEmptyList() {
        // Given - Clean all users
        userRepository.deleteAll();

        // When
        List<UserDTO> users = userService.getAllUsers();

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}