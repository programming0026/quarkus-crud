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
        // Limpia los datos existentes
        userRepository.deleteAll();
        
        // Crea usuario de prueba.
        testUser = new User("Test User", "test@service.com", "123-456-7890", "Test City");
        testUser.createdAt = LocalDateTime.now();
        testUser.updatedAt = LocalDateTime.now();
        userRepository.persist(testUser);
    }

    @Test
    void testGetAllUsers_ShouldReturnAllUsers() {
        // Cuando
        List<UserDTO> users = userService.getAllUsers();

        // Entonces
        assertNotNull(users);
        assertEquals(1, users.size());
        UserDTO user = users.get(0);
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
    }

    @Test
    void testGetUserById_ExistingId_ShouldReturnUser() {
        // Cuando
        Optional<UserDTO> result = userService.getUserById(testUser.id);

        // Entonces
        assertTrue(result.isPresent());
        UserDTO user = result.get();
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
        assertEquals(testUser.id, user.id);
    }

    @Test
    void testGetUserById_NonExistingId_ShouldReturnEmpty() {
        // Cuando
        Optional<UserDTO> result = userService.getUserById(999L);

        // Entonces
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testCreateUser_ValidData_ShouldCreateUser() {
        // Dado
        CreateUserDTO createUserDTO = new CreateUserDTO("New User", "new@email.com", "555-0000", "New City");

        // Cuando
        UserDTO result = userService.createUser(createUserDTO);

        // Entonces
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
        // Dado
        CreateUserDTO createUserDTO = new CreateUserDTO("Duplicate User", "test@service.com", "555-0000", "New City");

        // Cuando y entonces
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(createUserDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe un usuario con el email"));
    }

    @Test
    @Transactional
    void testUpdateUser_ExistingUser_ShouldUpdateUser() {
        // Dado
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        // Cuando
        Optional<UserDTO> result = userService.updateUser(testUser.id, updateUserDTO);

        // Entonces
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
        // Dado
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "updated@email.com", "999-8888", "Updated City");

        // Cuando
        Optional<UserDTO> result = userService.updateUser(999L, updateUserDTO);

        // Entonces
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testUpdateUser_DuplicateEmail_ShouldThrowBusinessException() {
        // Dado: se crea primero otro usuario
        User anotherUser = new User("Another User", "another@email.com", "111-2222", "Another City");
        userRepository.persist(anotherUser);

        // Intenta actualizar testUser con el correo de anotherUser
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "another@email.com", "999-8888", "Updated City");

        // Cuando y entonces
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateUser(testUser.id, updateUserDTO);
        });

        assertTrue(exception.getMessage().contains("Ya existe otro usuario con el email"));
    }

    @Test
    @Transactional
    void testUpdateUser_SameEmailSameUser_ShouldWork() {
        // Dado: se actualiza con el mismo correo (debe permitirse)
        CreateUserDTO updateUserDTO = new CreateUserDTO("Updated User", "test@service.com", "999-8888", "Updated City");

        // Cuando
        Optional<UserDTO> result = userService.updateUser(testUser.id, updateUserDTO);

        // Entonces
        assertTrue(result.isPresent());
        UserDTO updatedUser = result.get();
        assertEquals("Updated User", updatedUser.name);
        assertEquals("test@service.com", updatedUser.email); // Same email should work
    }

    @Test
    @Transactional
    void testDeleteUser_ExistingUser_ShouldReturnTrue() {
        // Cuando
        boolean result = userService.deleteUser(testUser.id);

        // Entonces
        assertTrue(result);
        Optional<UserDTO> deletedUser = userService.getUserById(testUser.id);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @Transactional
    void testDeleteUser_NonExistingUser_ShouldReturnFalse() {
        // Cuando
        boolean result = userService.deleteUser(999L);

        // Entonces
        assertFalse(result);
    }

    @Test
    void testGetUserByEmail_ExistingEmail_ShouldReturnUser() {
        // Cuando
        Optional<UserDTO> result = userService.getUserByEmail("test@service.com");

        // Entonces
        assertTrue(result.isPresent());
        UserDTO user = result.get();
        assertEquals("Test User", user.name);
        assertEquals("test@service.com", user.email);
    }

    @Test
    void testGetUserByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // Cuando
        Optional<UserDTO> result = userService.getUserByEmail("nonexistent@email.com");

        // Entonces
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testCreateUser_WithNullValues_ShouldCreateUser() {
        // Dado
        CreateUserDTO createUserDTO = new CreateUserDTO("Minimal User", "minimal@email.com", null, null);

        // Cuando
        UserDTO result = userService.createUser(createUserDTO);

        // Entonces
        assertNotNull(result);
        assertEquals("Minimal User", result.name);
        assertEquals("minimal@email.com", result.email);
        assertNull(result.phone);
        assertNull(result.city);
    }

    @Test
    @Transactional
    void testGetAllUsers_EmptyDatabase_ShouldReturnEmptyList() {
        // Dado: se limpian todos los usuarios
        userRepository.deleteAll();

        // Cuando
        List<UserDTO> users = userService.getAllUsers();

        // Entonces
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}