# API Catálogo de Productos y Favoritos (TP1)

Esta es una API RESTful desarrollada con Spring Boot y Java, diseñada para consumir un catálogo de productos externo (DummyJSON) y gestionar listas de productos favoritos.

## Arquitectura y Tecnologías
*   **Java 25 / Spring Boot 4.1**
*   **Spring Web MVC:** Manejo de rutas y peticiones HTTP.
*   **RestClient:** Cliente HTTP moderno para consumo de APIs externas.
*   **Bean Validation:** Validación robusta de datos de entrada (DTOs).
*   **Swagger / OpenAPI 3:** Documentación interactiva de la API.
*   **Spring Security + JWT:** Autenticación y autorización (TP3).
*   **PostgreSQL + JPA + Flyway:** Persistencia y migraciones (TP2).
*   **Testcontainers + JUnit 5 + Mockito:** Pruebas (TP3).
*   **Arquitectura en Capas / Hexagonal:** Controller, Service, Repository (puerto y adaptador), Model/DTO.

## Requisitos Previos
*   JDK 25 instalado.
*   Docker (para la base de datos local y para las pruebas de integración).
*   Maven (o usar el Wrapper de Maven incluido en el proyecto).

## Instalación y Ejecución

1. Clonar el repositorio.
2. Navegar a la raíz del proyecto.
3. Levantar la base de datos (ver sección TP2) y ejecutar la aplicación con el Maven Wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
4. La documentación interactiva queda en `http://localhost:8080/swagger-ui.html`.

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

# TP3 - Seguridad, pruebas e integración continua

## Autenticación y autorización

La API usa **JWT** (stateless). El flujo es:

1. `POST /api/auth/registro` crea un usuario con rol `USER`. La contraseña se guarda hasheada con BCrypt, nunca en texto plano. Si el email ya existe responde `409`.
2. `POST /api/auth/login` valida las credenciales y devuelve un token firmado que lleva el id y el rol del usuario. Credenciales inválidas responden `401`.
3. Las demás requests envían `Authorization: Bearer <token>`. Un filtro (`JwtAuthenticationFilter`) valida el token y deja al usuario disponible para el resto del request.

| Ruta | Acceso |
|---|---|
| `/api/auth/**`, `/api/productos/**`, `/health`, `/ping`, Swagger | Público |
| `/api/listas/**`, `/api/favoritos/**` | Requiere token válido (si no, `401`) |

| Situación | Respuesta |
|---|---|
| Sin token, token inválido o vencido | `401 Unauthorized` |
| Lista de otro usuario (siendo `USER`) | `403 Forbidden` |
| Lista de otro usuario (siendo `ADMIN`) | `200 OK` |
| Lista que no existe | `404 Not Found` |

Un `USER` solo ve y modifica sus propias listas (y los favoritos que cuelgan de ellas). Un `ADMIN` puede ver las de cualquiera. Al crear una lista, el dueño es siempre el usuario autenticado, nunca un dato que mande el cliente en el body.

### ¿Dónde vive la regla de autorización y por qué? (Consigna 4.5)

La regla "esta lista tiene que ser tuya, salvo que seas ADMIN" vive en la **capa de servicio**, en `ListaService.validarPropiedad`. `FavoritoService` no la repite: antes de operar sobre favoritos llama a `ListaService.obtenerListaPorId`, que valida existencia (`404`) y propiedad (`403`).

La elegí ahí y no en el Controller por estas razones:

*   **Es una regla de negocio, no de HTTP.** Si mañana la misma operación se invoca desde otro controller, un job o un test, la regla se sigue aplicando. En un Controller habría que copiarla en cada endpoint y es fácil olvidarse de uno (de hecho, los favoritos colgaban de la lista y estaban sin proteger hasta centralizarla).
*   **Necesita datos que el Controller no tiene.** Para saber de quién es una lista hay que cargarla de la base de datos. Eso es trabajo del servicio, que ya la tiene en mano.
*   **Es testeable sin levantar Spring MVC.** `ListaServiceTest` prueba la regla con Mockito: lista ajena lanza `AccessDeniedException`, `ADMIN` pasa.
*   **El Controller queda fino:** solo traduce HTTP a llamadas al servicio.

Tampoco la puse en el filtro JWT ni en `SecurityConfig`: ahí solo se sabe *quién* es el usuario y qué rutas son públicas, pero no a quién pertenece cada recurso. Por eso el reparto queda así: el **filtro** autentica (¿quién sos?), `SecurityConfig` decide qué rutas exigen token, y el **servicio** autoriza sobre el recurso concreto (¿esto es tuyo?). Las excepciones se traducen a `403`/`404`/`409` en un único lugar, `GlobalExceptionHandler`.

## Variables de entorno

Los datos sensibles se leen del entorno y tienen un valor por defecto para que funcione en local sin configurar nada:

| Variable | Uso | Valor por defecto (solo local) |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/tp2_db` |
| `DB_USER` | Usuario de la base | `postgres` |
| `DB_PASSWORD` | Contraseña de la base | `admin` |
| `JWT_SECRET` | Secreto para firmar los tokens | clave de desarrollo |

En un entorno real (producción) hay que definir siempre `JWT_SECRET` con un valor propio y largo.

## Cómo correr los tests

Hay dos tipos de pruebas, distinguidas por el sufijo del nombre de la clase:

*   **Unitarias (`*Test`)**, ejecutadas por Surefire: `ListaServiceTest`, `AuthServiceTest`. Usan Mockito, sin base de datos.
*   **De integración (`*IT`)**, ejecutadas por Failsafe: `AuthAndListaIntegrationIT`. Levantan un PostgreSQL real con **Testcontainers** y Flyway aplica todas las migraciones contra esa base limpia en cada ejecución.

`DemoApplicationTests` (verifica que el contexto de Spring arranca) también usa Testcontainers.

**Requiere Docker corriendo.**

```bash
./mvnw verify          # compila y corre TODO (unitarias + integración)
./mvnw test            # solo la fase de Surefire (*Test y DemoApplicationTests)
```

En Windows con Docker Desktop, si Testcontainers no encuentra Docker, exponer el daemon en *Settings → General → Expose daemon on tcp://localhost:2375 without TLS* y definir antes de correr:

```powershell
$env:DOCKER_HOST="tcp://127.0.0.1:2375"
$env:TESTCONTAINERS_RYUK_DISABLED="true"
.\mvnw.cmd clean verify
```

Los reportes quedan en `target/surefire-reports` y `target/failsafe-reports`.

## Integración continua (GitHub Actions)

El workflow `.github/workflows/ci.yml` se ejecuta en cada push (y pull request). Corre en un entorno limpio (`ubuntu-latest`, sin PostgreSQL local), instala Java 25 y ejecuta `./mvnw clean verify`. Como GitHub Actions ya trae Docker, Testcontainers levanta su propia base y las migraciones corren contra una base vacía.

**Cómo leer el resultado:**

1. En el repositorio, pestaña **Actions**, abrir la corrida más reciente.
2. Tilde verde: todo pasó. Cruz roja: algo falló.
3. Abrir el job `build` y el paso *Compilar y ejecutar pruebas*. Buscar `<<< ERROR` o `<<< FAILURE` para ver qué test falló y por qué.

**Verificación:** se rompió un test a propósito (commit `test: romper un test a propósito...`) y la corrida quedó en rojo; luego se revirtió y volvió a verde.

## Despliegue en la Nube (Render)



La aplicación ha sido desplegada exitosamente en **Render** utilizando un entorno basado en contenedores Docker y conectada a una base de datos PostgreSQL remota gestionada en la misma plataforma.



### Pasos de Despliegue Realizados

1. **Creación del servicio:** Se configuró un nuevo *Web Service* en Render vinculado a la rama `main` del repositorio de GitHub (`api-catalogo-favoritos`).

2. **Entorno Docker:** Se utilizó un `Dockerfile` optimizado en dos etapas (compilación con Maven y ejecución con Java 21 JRE).

3. **Configuración de Variables de Entorno en el Dashboard:**

   - `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<host-remoto>:5432/<nombre_db>` (configurado con el prefijo JDBC obligatorio).

   - `SPRING_DATASOURCE_USERNAME`: Usuario provisto por PostgreSQL en Render.

   - `SPRING_DATASOURCE_PASSWORD`: Contraseña segura provista por la base de datos remota.

   - `JWT_SECRET`: Clave de firma simétrica para la autenticación sin estado.

   - `SPRING_PROFILES_ACTIVE`: `prod`

4. **Empaquetado y Migraciones:** Gracias al `spring-boot-maven-plugin`, el `.jar` ejecutable se genera con el servidor Tomcat embebido, permitiendo que Flyway ejecute automáticamente las migraciones (`V1` a `V6`) sobre la base de datos remota al iniciar.



### Evidencias del Despliegue y Pruebas

*   **URL Pública del Servicio:** [https://api-catalogo-favoritos.onrender.com](https://api-catalogo-favoritos.onrender.com)

*   **Endpoint de Salud (`GET /health`):** Responde con éxito (`200 OK`) desde la URL pública, confirmando que el servidor web se encuentra operativo y respondiendo peticiones.

*   **Control de Acceso y Seguridad:**

    *   **Sin Token / Token Inválido:** Las rutas protegidas (ej. `/api/listas`) devuelven estrictamente un código **`401 Unauthorized`**, protegiendo los recursos privados.

    *   **Con Token Válido:** Al autenticarse previamente en `/api/auth/login` y enviar el header `Authorization: Bearer <token>`, el servidor procesa la petición devolviendo un código **`200 OK`**.

*   **Documentación Interactiva:** Accesible públicamente en [https://api-catalogo-favoritos.onrender.com/swagger-ui.html](https://api-catalogo-favoritos.onrender.com/swagger-ui.html).