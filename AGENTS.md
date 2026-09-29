# UvTours

Spring Boot 4.1.0 / Spring Framework 7 REST API for a tour agency. Java 21, Maven, MySQL, JPA, Lombok. Base package `com.Dev.UvTours`; layered `controller → service → repository → entity`, with DTOs at the API boundary. Domain naming is Spanish (methods, messages, DTO fields).

## Commands
- Use the wrapper: `./mvnw` (committed, LF). Java 21 required.
- `./mvnw clean test` — 177 tests, all green. Prefer `clean` here: a plain `mvn test` can silently skip recompiling stale test sources after source edits (we hit an out-of-date compiled class). `contextLoads` (`@SpringBootTest`) needs a running MySQL; the JPA slice tests (`@DataJpaTest`) run on embedded H2 and do not.
- Run: `./mvnw spring-boot:run` — needs a running MySQL.

## Test imports (Boot 4)
Tests are fixed to Spring Boot 4.1.0 locations. When writing NEW tests use these, NOT the old Spring Boot 2/3 names (they do not exist here — verified against the jars):
- `com.fasterxml.jackson.databind.ObjectMapper` → `tools.jackson.databind.ObjectMapper` (Jackson 3)
- `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest` → `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`
- `org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager` → `org.springframework.boot.jpa.test.autoconfigure.TestEntityManager`
- `@MockitoBean` stays at `org.springframework.test.context.bean.override.mockito.MockitoBean`.

## Data model
- `@Inheritance(JOINED)`: abstract `Viaje` → `ViajeSimple` (single destination) and `Circuito` (many-to-many `viajesSimples`). `ViajeDTO.tipoViaje` is the string discriminator `"SIMPLE"`/`"CIRCUITO"`.
- `Circuito.precio` is auto-computed as the sum of its `ViajeSimple`s; create circuits with `viajesSimplesIds`. `actualizarViaje` re-computes it too (and clears `descuentoLunaMiel` when `esLunaMiel` is turned off).
- `Tienda` 1..* `Empleado`; `Compra` links `Cliente` + `Viaje` + optional `Tienda`/`Empleado`, with `acompanantes` (travel companions) and `compraOnline` (defaults to false; null → treated as in-store purchase).
- `Inscripcion` = `Cliente` + `Actividad` (unique per pair). Only allowed while the activity is `PLANIFICADA`; enforced in `InscripcionService` (the `@Scheduled` job and the entity constraint back it up).
- `ActividadDelDia` is the persisted daily 09:00 snapshot (one row per planned activity of the day); the `@Scheduled` job replaces it via `deleteByFecha` + `saveAll`.
- No Flyway/Liquibase — `spring.jpa.hibernate.ddl-auto=update` auto-creates/alters the schema from entities.
- Entity subclasses extending a Lombok `@Data` parent need `@EqualsAndHashCode(callSuper = true)` (Lombok otherwise warns on generated equals/hashCode).

## DB and runtime
- MySQL connection lives (with credentials) in `src/main/resources/application.properties`, which is **not versioned** (copy it from `application.properties.example` and fill it in): DB `uvtours_db` at `localhost:3306`. `@SpringBootTest`'s `contextLoads` connects to this DB — startup fails without MySQL up.
- `@DataJpaTest` replaces the datasource with embedded H2 (`com.h2database:h2`, test scope in `pom.xml`); it never touches MySQL.
- Scheduling is on: `@EnableScheduling`, and `ActividadService.generarListaActividadesDelDia()` runs `@Scheduled(cron = "0 0 9 * * ?")` daily at 09:00 and persists the snapshot; consult it via `GET /api/actividades/hoy?fecha=YYYY-MM-DD`.

## API and error conventions
- REST under `/api/*`: `/api/viajes`, `/api/compras`, `/api/clientes`, `/api/actividades` (+ `GET /hoy`), `/api/inscripciones`, `/api/tiendas`, `/api/empleados`.
- Errors go through `GlobalExceptionHandler`: `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400, `IllegalStateException` → 409, `MethodArgumentNotValidException` → 400. Throw these instead of building error responses yourself.
- Test style follows existing suites: service tests `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks`; controller tests `@WebMvcTest` + `@MockitoBean` + `MockMvc`; repository tests `@DataJpaTest` + `TestEntityManager`.
- Bean validation is in place on `ClienteDTO` (`@NotBlank` on `dni`) triggered by `@Valid` on `@RequestBody`; validation failures return 400 via `GlobalExceptionHandler`'s `MethodArgumentNotValidException` handler.

## Boot 4 artifact names
- The web starter is `spring-boot-starter-webmvc` (not `spring-boot-starter-web`). Test infra uses modular `-test` starters (e.g. `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`), not `spring-boot-starter-test` alone.