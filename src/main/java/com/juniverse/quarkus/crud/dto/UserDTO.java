package com.juniverse.quarkus.crud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Usuario tal como se devuelve al cliente. */
public class UserDTO {

    public Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    public String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    public String email;

    @Size(max = 20, message = "El teléfono no puede tener más de 20 caracteres")
    public String phone;

    @Size(max = 100, message = "La ciudad no puede tener más de 100 caracteres")
    public String city;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    /** Crea un DTO vacío (necesario para la deserialización JSON). */
    public UserDTO() {}

    /**
     * Crea el DTO con todos sus datos.
     * @param id identificador
     * @param name nombre
     * @param email correo
     * @param phone teléfono
     * @param city ciudad
     * @param createdAt fecha de creación
     * @param updatedAt fecha de última modificación
     */
    public UserDTO(Long id, String name, String email, String phone, String city, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}