package com.juniverse.quarkus.crud.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Quarkus CRUD API",
                version = "1.0.0",
                description = "API REST para operaciones CRUD con usuarios utilizando Quarkus, MySQL y Swagger",
                contact = @Contact(
                        name = "Java Universe",
                        url = "https://github.com/programming0026"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Servidor de desarrollo")
        }
)
/** Información general de la documentación OpenAPI / Swagger UI. */
public class OpenApiConfig extends Application {
}