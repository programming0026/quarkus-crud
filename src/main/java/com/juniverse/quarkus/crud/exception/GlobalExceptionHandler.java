package com.juniverse.quarkus.crud.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

/** Convierte las excepciones en respuestas JSON uniformes ({@link ErrorResponse}). */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Context
    UriInfo uriInfo;

    /**
     * Elige la respuesta según el tipo de excepción: JAX-RS conserva su código; negocio 409; validación 400; el resto 500.
     * @param exception excepción lanzada
     * @return respuesta de error
     */
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof NotFoundException) {
            LOG.warn("Resource not found: " + exception.getMessage());
            return handleNotFoundException((NotFoundException) exception);
        } else {
            LOG.error("Exception occurred: " + exception.getMessage(), exception);
        }

        if (exception instanceof WebApplicationException webException) {
            // Se conserva el código que decidió JAX-RS (400, 405, 415...) en lugar de convertirlo en 500
            int status = webException.getResponse().getStatus();
            ErrorResponse error = new ErrorResponse(
                    status < 500 ? "Solicitud incorrecta" : "Error interno del servidor", status, getPath());
            return Response.status(status).entity(error).build();
        }
        if (exception instanceof BusinessException) {
            return handleBusinessException((BusinessException) exception);
        } else if (exception instanceof ConstraintViolationException) {
            return handleValidationException((ConstraintViolationException) exception);
        } else {
            return handleGenericException(exception);
        }
    }

    /** 409: regla de negocio incumplida. */
    private Response handleBusinessException(BusinessException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                Response.Status.CONFLICT.getStatusCode(),
                getPath()
        );
        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }

    /** 400: fallos de validación, con el detalle de cada uno. */
    private Response handleValidationException(ConstraintViolationException ex) {
        List<String> violations = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorResponse error = new ErrorResponse(
                "Errores de validación",
                Response.Status.BAD_REQUEST.getStatusCode(),
                getPath(),
                violations
        );
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    /** 404: la ruta no existe. */
    private Response handleNotFoundException(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "Recurso no encontrado: " + getPath(),
                Response.Status.NOT_FOUND.getStatusCode(),
                getPath()
        );
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }

    /** 500: error inesperado (sin exponer el detalle). */
    private Response handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "Error interno del servidor",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                getPath()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    /** Ruta de la petición actual. */
    private String getPath() {
        return uriInfo != null ? uriInfo.getPath() : "unknown";
    }
}