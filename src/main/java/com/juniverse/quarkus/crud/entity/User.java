package com.juniverse.quarkus.crud.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Entidad Panache de la tabla {@code users}. */
@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "name", nullable = false, length = 50)
    public String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    public String email;

    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    @Column(name = "phone", length = 20)
    public String phone;

    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    @Column(name = "city", length = 100)
    public String city;

    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public User() {}

    public User(String name, String email, String phone, String city) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
    }
}