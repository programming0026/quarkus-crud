package com.juniverse.quarkus.crud.repository;

import com.juniverse.quarkus.crud.repository.UserRepository;
import com.juniverse.quarkus.crud.entity.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    private User testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean existing data
        userRepository.deleteAll();
        userRepository.flush();
        
        // Create test user
        testUser = new User("Test User", "test@repository.com", "123-456-7890", "Test City");
        userRepository.persist(testUser);
        userRepository.flush();
    }

    @Test
    @Transactional
    void testFindByEmail_ExistingEmail_ShouldReturnUser() {
        // When
        Optional<User> result = userRepository.findByEmail("test@repository.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().name);
        assertEquals("test@repository.com", result.get().email);
    }

    @Test
    @Transactional
    void testFindByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testExistsByEmail_ExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("test@repository.com");

        // Then
        assertTrue(exists);
    }

    @Test
    @Transactional
    void testExistsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@email.com");

        // Then
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_SameEmailDifferentId_ShouldReturnTrue() {
        // Given - Create another user with same email (this test specifically needs to bypass unique constraint)
        // We'll use a different email for the test user setup and check for duplicates
        User anotherUser = new User("Another User", "different@email.com", "098-765-4321", "Another City");
        userRepository.persist(anotherUser);
        userRepository.flush();

        // When - Check if there's another user with testUser's email (but different ID)
        boolean exists = userRepository.existsByEmailAndIdNot("test@repository.com", anotherUser.id);

        // Then - Should return true because testUser exists with that email and different ID
        assertTrue(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_SameEmailSameId_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmailAndIdNot("test@repository.com", testUser.id);

        // Then
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_DifferentEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmailAndIdNot("different@email.com", testUser.id);

        // Then
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testPersistAndFind_ShouldWorkCorrectly() {
        // Given
        User newUser = new User("New User", "new@email.com", "555-0000", "New City");

        // When
        userRepository.persist(newUser);

        // Then
        assertNotNull(newUser.id);
        Optional<User> found = userRepository.findByIdOptional(newUser.id);
        assertTrue(found.isPresent());
        assertEquals("New User", found.get().name);
    }

    @Test
    @Transactional
    void testDeleteById_ExistingUser_ShouldReturnTrue() {
        // When
        boolean deleted = userRepository.deleteById(testUser.id);

        // Then
        assertTrue(deleted);
        Optional<User> found = userRepository.findByIdOptional(testUser.id);
        assertFalse(found.isPresent());
    }

    @Test
    @Transactional
    void testDeleteById_NonExistingUser_ShouldReturnFalse() {
        // When
        boolean deleted = userRepository.deleteById(999L);

        // Then
        assertFalse(deleted);
    }

    @Test
    void testListAll_ShouldReturnAllUsers() {
        // When
        var users = userRepository.listAll();

        // Then
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("Test User", users.get(0).name);
    }
}