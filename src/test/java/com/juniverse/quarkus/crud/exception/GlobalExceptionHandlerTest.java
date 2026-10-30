package com.juniverse.quarkus.crud.exception;

import com.juniverse.quarkus.crud.exception.ErrorResponse;
import com.juniverse.quarkus.crud.exception.BusinessException;
import com.juniverse.quarkus.crud.exception.GlobalExceptionHandler;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        // Inject the mock UriInfo using reflection or setter if available
        // For now, we'll test without UriInfo injection
    }

    @Test
    void testHandleBusinessException() {
        // Given
        BusinessException businessException = new BusinessException("Test business error");

        // When
        Response response = exceptionHandler.toResponse(businessException);

        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Test business error", errorResponse.message);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), errorResponse.status);
        assertNotNull(errorResponse.timestamp);
    }

    @Test
    void testHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        when(constraintViolation.getMessage()).thenReturn("Validation error message");
        violations.add(constraintViolation);
        
        ConstraintViolationException validationException = new ConstraintViolationException("Validation failed", violations);

        // When
        Response response = exceptionHandler.toResponse(validationException);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Errores de validación", errorResponse.message);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), errorResponse.status);
        assertNotNull(errorResponse.details);
        assertEquals(1, errorResponse.details.size());
        assertEquals("Validation error message", errorResponse.details.get(0));
    }

    @Test
    void testHandleGenericException() {
        // Given
        RuntimeException genericException = new RuntimeException("Generic error");

        // When
        Response response = exceptionHandler.toResponse(genericException);

        // Then
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Error interno del servidor", errorResponse.message);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), errorResponse.status);
        assertNotNull(errorResponse.timestamp);
    }

    @Test
    void testBusinessExceptionWithCause() {
        // Given
        Exception cause = new Exception("Root cause");
        BusinessException businessException = new BusinessException("Business error with cause", cause);

        // When
        Response response = exceptionHandler.toResponse(businessException);

        // Then
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Business error with cause", errorResponse.message);
        assertNotNull(errorResponse.timestamp);
    }

    @Test
    void testMultipleValidationErrors() {
        // Given
        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        
        ConstraintViolation<Object> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("First validation error");
        violations.add(violation1);
        
        ConstraintViolation<Object> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("Second validation error");
        violations.add(violation2);
        
        ConstraintViolationException validationException = new ConstraintViolationException("Multiple validation errors", violations);

        // When
        Response response = exceptionHandler.toResponse(validationException);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ErrorResponse errorResponse = (ErrorResponse) response.getEntity();
        assertEquals("Errores de validación", errorResponse.message);
        assertNotNull(errorResponse.details);
        assertEquals(2, errorResponse.details.size());
        assertTrue(errorResponse.details.contains("First validation error"));
        assertTrue(errorResponse.details.contains("Second validation error"));
    }
}