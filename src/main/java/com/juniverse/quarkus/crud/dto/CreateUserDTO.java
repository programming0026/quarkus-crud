package com.juniverse.quarkus.crud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o actualizar un usuario (con validaciones). */
public class CreateUserDTO {

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

    /** Crea un DTO vacío (necesario para la deserialización JSON). */
    public CreateUserDTO() {}

    /**
     * Crea el DTO con todos sus datos.
     * @param name nombre (2-50 caracteres)
     * @param email correo válido
     * @param phone teléfono (opcional, hasta 20 caracteres)
     * @param city ciudad (opcional, hasta 100 caracteres)
     */
    public CreateUserDTO(String name, String email, String phone, String city) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.city = city;
    }
}