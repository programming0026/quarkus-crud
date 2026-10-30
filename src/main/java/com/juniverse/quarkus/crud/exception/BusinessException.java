package com.juniverse.quarkus.crud.exception;

/** Se incumple una regla de negocio (por ejemplo, un correo repetido); se responde 409. */
public class BusinessException extends RuntimeException {

    /**
     * Crea la excepción.
     * @param message descripción del problema
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con su causa.
     * @param message descripción del problema
     * @param cause causa original
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}