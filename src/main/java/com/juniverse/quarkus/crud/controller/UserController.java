package com.juniverse.quarkus.crud.controller;

import com.juniverse.quarkus.crud.dto.CreateUserDTO;
import com.juniverse.quarkus.crud.dto.UserDTO;
import com.juniverse.quarkus.crud.service.UserService;
import com.juniverse.quarkus.crud.exception.ErrorResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

/** API REST de usuarios ({@code /users}): consulta, alta, modificación y baja. */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "Operaciones CRUD para usuarios")
public class UserController {

    @Inject
    UserService userService;

    @Context
    UriInfo uriInfo;

    /**
     * Lista todos los usuarios.
     * @return 200 con la lista
     */
    @GET
    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios")
    @APIResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")
    public Response getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return Response.ok(users).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario específico por su ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Usuario encontrado"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    /**
     * Busca un usuario por su id.
     * @param id identificador
     * @return 200 con el usuario o 404
     */
    public Response getUserById(@Parameter(description = "ID del usuario") @PathParam("id") Long id) {
        Optional<UserDTO> user = userService.getUserById(id);
        
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return notFound("Usuario no encontrado con ID: " + id);
        }
    }

    @POST
    @Operation(summary = "Crear nuevo usuario", description = "Crea un nuevo usuario en el sistema")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Usuario creado exitosamente"),
            @APIResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @APIResponse(responseCode = "409", description = "El email ya existe")
    })
    /**
     * Crea un usuario.
     * @param createUserDTO datos validados
     * @return 201 con el usuario creado; 400 si los datos son inválidos; 409 si el correo ya existe
     */
    public Response createUser(@Valid CreateUserDTO createUserDTO) {
        UserDTO createdUser = userService.createUser(createUserDTO);
        return Response.status(Response.Status.CREATED).entity(createdUser).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
            @APIResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado"),
            @APIResponse(responseCode = "409", description = "El email ya existe")
    })
    /**
     * Actualiza un usuario.
     * @param id identificador
     * @param updateUserDTO nuevos datos validados
     * @return 200 con el usuario actualizado, 404 si no existe o 409 si el correo es de otro usuario
     */
    public Response updateUser(
            @Parameter(description = "ID del usuario") @PathParam("id") Long id,
            @Valid CreateUserDTO updateUserDTO) {
        
        Optional<UserDTO> updatedUser = userService.updateUser(id, updateUserDTO);
        
        if (updatedUser.isPresent()) {
            return Response.ok(updatedUser.get()).build();
        } else {
            return notFound("Usuario no encontrado con ID: " + id);
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    /**
     * Elimina un usuario.
     * @param id identificador
     * @return 204, o 404 si no existe
     */
    public Response deleteUser(@Parameter(description = "ID del usuario") @PathParam("id") Long id) {
        boolean deleted = userService.deleteUser(id);
        
        if (deleted) {
            return Response.noContent().build();
        } else {
            return notFound("Usuario no encontrado con ID: " + id);
        }
    }

    @GET
    @Path("/email/{email}")
    @Operation(summary = "Buscar usuario por email", description = "Busca un usuario por su dirección de email")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Usuario encontrado"),
            @APIResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    /**
     * Busca un usuario por su correo.
     * @param email correo electrónico
     * @return 200 con el usuario o 404
     */
    public Response getUserByEmail(@Parameter(description = "Email del usuario") @PathParam("email") String email) {
        Optional<UserDTO> user = userService.getUserByEmail(email);
        
        if (user.isPresent()) {
            return Response.ok(user.get()).build();
        } else {
            return notFound("Usuario no encontrado con email: " + email);
        }
    }

    /**
     * Respuesta 404 con el cuerpo JSON estándar de errores (antes era texto plano dentro de application/json).
     *
     * @param message mensaje para el cliente
     * @return respuesta 404
     */
    private Response notFound(String message) {
        ErrorResponse error = new ErrorResponse(message, Response.Status.NOT_FOUND.getStatusCode(),
                uriInfo != null ? uriInfo.getPath() : "unknown");
        return Response.status(Response.Status.NOT_FOUND).entity(error).build();
    }
}
