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
        // Limpia los datos existentes
        userRepository.deleteAll();
        userRepository.flush();
        
        // Crea usuario de prueba.
        testUser = new User("Test User", "test@repository.com", "123-456-7890", "Test City");
        userRepository.persist(testUser);
        userRepository.flush();
    }

    @Test
    @Transactional
    void testFindByEmail_ExistingEmail_ShouldReturnUser() {
        // Cuando
        Optional<User> result = userRepository.findByEmail("test@repository.com");

        // Entonces
        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().name);
        assertEquals("test@repository.com", result.get().email);
    }

    @Test
    @Transactional
    void testFindByEmail_NonExistingEmail_ShouldReturnEmpty() {
        // Cuando
        Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

        // Entonces
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testExistsByEmail_ExistingEmail_ShouldReturnTrue() {
        // Cuando
        boolean exists = userRepository.existsByEmail("test@repository.com");

        // Entonces
        assertTrue(exists);
    }

    @Test
    @Transactional
    void testExistsByEmail_NonExistingEmail_ShouldReturnFalse() {
        // Cuando
        boolean exists = userRepository.existsByEmail("nonexistent@email.com");

        // Entonces
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_SameEmailDifferentId_ShouldReturnTrue() {
        // Dado: se crea otro usuario con el mismo correo (esta prueba necesita evitar la restricción de unicidad)
        // Se usa otro correo en la preparación del usuario de prueba y se revisan los duplicados
        User anotherUser = new User("Another User", "different@email.com", "098-765-4321", "Another City");
        userRepository.persist(anotherUser);
        userRepository.flush();

        // Cuando: se comprueba si hay otro usuario con el correo de testUser (pero distinto identificador)
        boolean exists = userRepository.existsByEmailAndIdNot("test@repository.com", anotherUser.id);

        // Entonces: debe devolver verdadero porque testUser existe con ese correo y otro identificador
        assertTrue(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_SameEmailSameId_ShouldReturnFalse() {
        // Cuando
        boolean exists = userRepository.existsByEmailAndIdNot("test@repository.com", testUser.id);

        // Entonces
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testExistsByEmailAndIdNot_DifferentEmail_ShouldReturnFalse() {
        // Cuando
        boolean exists = userRepository.existsByEmailAndIdNot("different@email.com", testUser.id);

        // Entonces
        assertFalse(exists);
    }

    @Test
    @Transactional
    void testPersistAndFind_ShouldWorkCorrectly() {
        // Dado
        User newUser = new User("New User", "new@email.com", "555-0000", "New City");

        // Cuando
        userRepository.persist(newUser);

        // Entonces
        assertNotNull(newUser.id);
        Optional<User> found = userRepository.findByIdOptional(newUser.id);
        assertTrue(found.isPresent());
        assertEquals("New User", found.get().name);
    }

    @Test
    @Transactional
    void testDeleteById_ExistingUser_ShouldReturnTrue() {
        // Cuando
        boolean deleted = userRepository.deleteById(testUser.id);

        // Entonces
        assertTrue(deleted);
        Optional<User> found = userRepository.findByIdOptional(testUser.id);
        assertFalse(found.isPresent());
    }

    @Test
    @Transactional
    void testDeleteById_NonExistingUser_ShouldReturnFalse() {
        // Cuando
        boolean deleted = userRepository.deleteById(999L);

        // Entonces
        assertFalse(deleted);
    }

    @Test
    void testListAll_ShouldReturnAllUsers() {
        // Cuando
        var users = userRepository.listAll();

        // Entonces
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        assertEquals("Test User", users.get(0).name);
    }
}