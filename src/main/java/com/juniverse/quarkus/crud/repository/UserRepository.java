package com.juniverse.quarkus.crud.repository;

import com.juniverse.quarkus.crud.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

/** Repositorio Panache de usuarios. */
@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    /**
     * Busca por correo.
     * @param email correo
     */
    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return count("email = ?1 and id != ?2", email, id) > 0;
    }
}