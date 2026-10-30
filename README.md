# Quarkus CRUD API

Esta es una aplicación CRUD completa desarrollada con Quarkus, MySQL 8, Swagger UI y manejo global de excepciones.

## Características

- ✅ **Quarkus Framework**: Framework Java supersónico y subatómico
- ✅ **MySQL 8**: Base de datos relacional robusta
- ✅ **Hibernate ORM con Panache**: Simplifica el código de persistencia
- ✅ **Swagger UI**: Documentación interactiva de la API
- ✅ **Validación de datos**: Usando Bean Validation
- ✅ **Manejo global de excepciones**: Respuestas de error consistentes
- ✅ **Docker Compose**: Para configuración fácil de MySQL

## Estructura del proyecto

```
src/
├── main/java/com/ejemplo/
│   ├── controller/        # Controladores REST
│   ├── service/          # Lógica de negocio
│   ├── repository/       # Capa de acceso a datos
│   ├── entity/           # Entidades JPA
│   ├── dto/             # Objetos de transferencia de datos
│   ├── exception/       # Manejo de excepciones
│   └── config/          # Configuraciones
└── main/resources/
    ├── application.properties
    └── import.sql       # Datos de prueba
```

## Configuración e instalación

### 1. Prerequisitos

- Java 21+
- Maven 3.8+
- Docker y Docker Compose (opcional)

### 2. Base de datos

#### Opción A: Usando Docker Compose (Recomendado)
```bash
# Iniciar MySQL y Adminer
docker-compose up -d

# Para detener los servicios
docker-compose down
```

#### Opción B: MySQL local
- Instalar MySQL 8.0
- Crear base de datos: `CREATE DATABASE crud_db;`
- Configurar credenciales en `application.properties`

### 3. Ejecución

#### Modo desarrollo (con live reload)
```bash
./mvnw compile quarkus:dev
```

#### Modo producción
```bash
./mvnw package
java -jar target/$(ls target | grep runner.jar)
```

## API Endpoints

La aplicación expone los siguientes endpoints:

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/users` | Obtener todos los usuarios |
| GET | `/users/{id}` | Obtener usuario por ID |
| GET | `/users/email/{email}` | Buscar usuario por email |
| POST | `/users` | Crear nuevo usuario |
| PUT | `/users/{id}` | Actualizar usuario |
| DELETE | `/users/{id}` | Eliminar usuario |

### Ejemplo de payload para crear/actualizar usuario:

```json
{
  "name": "Juan Pérez",
  "email": "juan.perez@email.com",
  "phone": "555-0101",
  "city": "Ciudad de México"
}
```

## Documentación de la API

Una vez que la aplicación esté ejecutándose, puedes acceder a:

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/q/openapi

## Herramientas de desarrollo

### Adminer (Administrador de BD web)
Si usas Docker Compose, Adminer estará disponible en:
- **URL**: http://localhost:8081
- **Sistema**: MySQL
- **Servidor**: mysql
- **Usuario**: root
- **Contraseña**: test123
- **Base de datos**: crud_db

### Dev UI de Quarkus
En modo desarrollo, la Dev UI está disponible en:
- **URL**: http://localhost:8080/q/dev

## Configuración

### Variables de entorno importantes

```properties
# Base de datos
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/crud_db?serverTimezone=UTC
quarkus.datasource.username=root
quarkus.datasource.password=test123

# Servidor
quarkus.http.port=8080

# Swagger
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
```

## Manejo de errores

La aplicación incluye manejo global de excepciones que retorna respuestas consistentes:

- **400 Bad Request**: Errores de validación
- **404 Not Found**: Recurso no encontrado  
- **409 Conflict**: Reglas de negocio (ej: email duplicado)
- **500 Internal Server Error**: Errores del servidor

Ejemplo de respuesta de error:
```json
{
  "message": "Ya existe un usuario con el email: juan@email.com",
  "status": 409,
  "timestamp": "2024-01-15T10:30:00",
  "path": "/users",
  "details": null
}
```

## Datos de prueba

La aplicación incluye datos de prueba que se cargan automáticamente:
- Juan Pérez (juan.perez@email.com)
- María García (maria.garcia@email.com)
- Carlos López (carlos.lopez@email.com)
- Ana Rodríguez (ana.rodriguez@email.com)
- Luis Martínez (luis.martinez@email.com)

## Testing

```bash
# Ejecutar tests
./mvnw test

# Ejecutar con perfil de test específico
./mvnw test -Dquarkus.test.profile=test
```

## Construcción nativa

Para crear un ejecutable nativo:

```bash
./mvnw package -Dnative
```

O usando contenedor:
```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```
