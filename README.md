# API Catálogo de Productos y Favoritos (TP1)

Esta es una API RESTful desarrollada con Spring Boot 3 y Java 21, diseñada para consumir un catálogo de productos externo (DummyJSON) y gestionar una lista local de productos favoritos en memoria.

## Arquitectura y Tecnologías
*   **Java 21 / Spring Boot 3.2+**
*   **Spring Web MVC:** Manejo de rutas y peticiones HTTP.
*   **RestClient:** Cliente HTTP moderno para consumo de APIs externas.
*   **Bean Validation:** Validación robusta de datos de entrada (DTOs).
*   **Swagger / OpenAPI 3:** Documentación interactiva de la API.
*   **Arquitectura en Capas:** Controller, Service, Repository, Model/DTO.

## Requisitos Previos
*   JDK 21 instalado.
*   Maven (o usar el Wrapper de Maven incluido en el proyecto).

## Instalación y Ejecución

1. Clonar el repositorio.
2. Navegar a la raíz del proyecto.
3. Ejecutar la aplicación usando Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run