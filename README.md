# API-recipes 🍳

API RESTful para gestión de recetas de cocina con sistema de autenticación y características sociales.

## 📑 Tabla de Contenidos
- [Descripción](#-descripción)
- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Documentación API](#-documentación-api)
- [Seguridad](#-seguridad)


## 📝 Descripción
API-recipes es una aplicación backend robusta desarrollada con Spring Boot que proporciona una plataforma completa para la gestión de recetas de cocina. Incluye funcionalidades como autenticación de usuarios, gestión de recetas, ingredientes, categorías y un sistema de favoritos.

## ✨ Características
- **Gestión de Usuarios**
  - Registro y autenticación
  - Sistema de roles y permisos
  - Gestión de perfiles
  - Restablecimiento de contraseña vía email
  - Cambio de email y contraseña

- **Gestión de Recetas**
  - CRUD completo de recetas
  - Asociación de ingredientes
  - Categorización
  - Sistema de favoritos

- **Gestión de Ingredientes**
  - Catálogo de ingredientes
  - Medidas y cantidades
  - Asociación con recetas

- **Categorización**
  - Gestión de categorías
  - Clasificación de recetas

- **Sistema de Favoritos**
  - Marcar/desmarcar favoritos
  - Listado de favoritos por usuario

## 🛠️ Tecnologías
- **Framework**: Spring Boot 3.2.4
- **Lenguaje**: Java 21
- **Base de Datos**: MySQL
- **Seguridad**: Spring Security + JWT
- **Documentación**: 
  - SpringDoc OpenAPI (Swagger)
  - JavaDoc
- **Herramientas**:
  - Maven
  - Lombok
  - MapStruct
  - JPA/Hibernate
- **Email**: JavaMailSender para notificaciones

## 📂 Estructura del Proyecto
```
src/main/java/api_recipes/
├── controllers/         # Controladores REST
├── models/             # Entidades JPA
├── repository/         # Repositorios
├── services/          # Lógica de negocio
├── security/          # Configuración de seguridad
├── payload/           # DTOs
├── mapper/            # Mappers
└── exceptions/        # Manejo de excepciones
```

## 📋 Requisitos Previos
- Java 21 o superior
- MySQL 8.0 o superior
- Maven 3.6 o superior
- Servidor SMTP para envío de emails

## 🚀 Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/sandraamore95/API-recipes.git
cd API-recipes
```

2. **Configurar la base de datos**
```sql
CREATE DATABASE recipes_db;
```

3. **Configurar application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/recipes_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Configuración de email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email
spring.mail.password=tu_contraseña
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

4. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

## ⚙️ Configuración
### Variables de Entorno
```properties
JWT_SECRET=tu_clave_secreta
JWT_EXPIRATION=86400000
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email
MAIL_PASSWORD=tu_contraseña
APP_BASE_URL=http://localhost:8080
```

## 📚 Documentación API

### Swagger UI
La documentación interactiva de la API está disponible a través de Swagger UI en:
```
http://localhost:8080/swagger-ui.html
```

### JavaDoc
La documentación del código está disponible en:
```
target/site/apidocs/index.html
```

Para generar la documentación JavaDoc:
```bash
mvn javadoc:javadoc
```

### Endpoints Principales

#### Autenticación
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/login` - Inicio de sesión
- `POST /api/auth/refresh` - Refrescar token
- `POST /api/auth/forgot-password` - Solicitar restablecimiento de contraseña
- `POST /api/auth/reset-password` - Restablecer contraseña con token

#### Cuenta
- `PATCH /api/account/change-email` - Cambiar email
- `PATCH /api/account/change-password` - Cambiar contraseña

#### Recetas
- `GET /api/recipes` - Listar recetas
- `POST /api/recipes` - Crear receta
- `GET /api/recipes/{id}` - Obtener receta
- `PUT /api/recipes/{id}` - Actualizar receta
- `PATCH /api/recipes/{id}/upload-image` - Actualizar imagen receta
- `DELETE /api/recipes/{id}` - Eliminar receta

#### Ingredientes
- `GET /api/ingredients` - Buscar ingredientes
- `POST /api/ingredients` - Crear ingrediente
- `PUT /api/ingredients/{id}` - Actualizar ingrediente
- `PATCH /api/ingredients/{id}/upload-image` - Actualizar imagen ingrediente
- `PUT /api/disable/{id}` - Desabilitar ingrediente
- `PUT /api/enable/{id}` - Habilitar ingrediente

#### Favoritos
- `GET /api/favorites` - Listar favoritos
- `POST /api/favorites/{recipeId}` - Agregar a favoritos
- `DELETE /api/favorites/{recipeId}` - Eliminar de favoritos

## 🔒 Seguridad
- Autenticación basada en JWT
- Roles de usuario: ADMIN, USER
- Protección contra CSRF
- Validación de tokens
- Encriptación de contraseñas
- Sistema de restablecimiento de contraseña seguro
  - Tokens únicos y expirables (24 horas)
  - Notificación por email
  - Validación de tokens




