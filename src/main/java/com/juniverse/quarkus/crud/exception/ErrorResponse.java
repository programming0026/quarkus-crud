package com.juniverse.quarkus.crud.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Cuerpo JSON de las respuestas de error. */
public class ErrorResponse {

    public String message;
    public int status;
    public LocalDateTime timestamp;
    public String path;
    public List<String> details;

    /** Crea la respuesta con la fecha actual. */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Crea la respuesta.
     * @param message mensaje para el cliente
     * @param status código HTTP
     * @param path ruta de la petición
     */
    public ErrorResponse(String message, int status, String path) {
        this();
        this.message = message;
        this.status = status;
        this.path = path;
    }

    /**
     * Crea la respuesta con detalles.
     * @param message mensaje para el cliente
     * @param status código HTTP
     * @param path ruta de la petición
     * @param details detalle de cada error (por ejemplo, de validación)
     */
    public ErrorResponse(String message, int status, String path, List<String> details) {
        this(message, status, path);
        this.details = details;
    }
}