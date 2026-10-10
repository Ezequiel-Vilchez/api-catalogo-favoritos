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

# TP2 - API Catálogo Favoritos (PostgreSQL, JPA, Flyway)

## ¿Cómo levantar la base de datos?
Para ejecutar la base de datos PostgreSQL en modo local, se debe utilizar Docker. Parado en la raíz del proyecto, ejecutar:
`docker-compose up -d`
Una vez levantado el contenedor, al iniciar la aplicación Spring Boot, Flyway se encargará automáticamente de ejecutar las migraciones ubicadas en `src/main/resources/db/migration` para construir el esquema de la base de datos.

## Arquitectura: Puertos y Adaptadores (Punto 4.2)
Al migrar de la persistencia en memoria (TP1) a JPA con PostgreSQL (TP2), **las clases `FavoritoService` y `FavoritoController` no sufrieron modificaciones en su lógica principal**.
Esto fue posible gracias a la Arquitectura Hexagonal. `FavoritoRepository` funciona como un **puerto** (un contrato o interfaz). En el TP1 teníamos un adaptador en memoria, y en el TP2 simplemente lo reemplazamos por el `FavoritoRepositoryAdapter` (que implementa el mismo puerto pero interactúa con JPA). Como el servicio solo conoce la interfaz y no los detalles de implementación, el cambio de infraestructura fue transparente para la lógica de negocio.

## Evolución del Esquema con Flyway (Punto 6.3)
**¿Por qué se crea una migración `V4` nueva en lugar de modificar las anteriores?**
Flyway lleva un registro estricto de las migraciones ejecutadas mediante la tabla `flyway_schema_history` calculando un checksum (una firma) del archivo. Si editamos un archivo ya aplicado (`V1`, `V2` o `V3`), el checksum cambiará, Flyway detectará la inconsistencia y lanzará un error impidiendo que la aplicación arranque. Para evolucionar el esquema de manera segura sin romper entornos que ya aplicaron las primeras migraciones, siempre se debe avanzar hacia adelante creando nuevos archivos (ej: `V4`).

## Transacciones y Propiedades ACID (Punto 7.3)
En el endpoint de mover favoritos utilizamos la anotación `@Transactional`. En términos de las propiedades **ACID**, esto garantiza la **Atomicidad** (todo o nada). 
Si sacáramos el `@Transactional` y tuviéramos que mover 5 favoritos, podría ocurrir que se actualicen los primeros 3 y, por un error de red o base de datos, los últimos 2 fallen. Esto dejaría la base de datos en un estado **inconsistente** (datos a medias). Con `@Transactional`, si la escritura del favorito 4 falla, se hace un *rollback* automático y los favoritos 1, 2 y 3 vuelven a su estado original, asegurando la integridad de la información.