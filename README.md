# UvTours

API REST y aplicación web para una agencia de viajes: catálogo de viajes y circuitos,
tiendas con empleados, compras, inscripciones a actividades y panel de administración.

- **Backend:** Spring Boot 4.1.0 (Java 21, Maven), JPA/Hibernate + MySQL, Spring Security con JWT.
- **Frontend:** React 19 + Vite 8 + Tailwind CSS 4.

## Estructura

```
├── src/main/java/com/Dev/UvTours   # Backend (controller → service → repository → entity)
├── src/main/resources              # application.properties
├── src/test                        # 177 tests (servicio, controller, repositorio, seguridad)
└── frontend/                       # SPA React (Vite)
```

Capas del backend: `controller` (REST) → `service` (lógica) → `repository` (Spring Data)
→ `entity` (JPA), con DTOs en la capa de API y manejo de errores centralizado en
`exception/GlobalExceptionHandler`.

## Requisitos

- Java 21 y MySQL 8 (en local)
- Node.js 20+ y npm

## Base de datos

```sql
CREATE DATABASE uvtours_db;
CREATE USER 'uvtours_user'@'localhost' IDENTIFIED BY '<TU_PASSWORD_MYSQL>';
GRANT ALL PRIVILEGES ON uvtours_db.* TO 'uvtours_user'@'localhost';
FLUSH PRIVILEGES;
```

El esquema se crea y actualiza automáticamente (`spring.jpa.hibernate.ddl-auto=update`);
no hay Flyway ni Liquibase.

## Configuración

`application.properties` **no está versionado** (contiene credenciales). Antes de
arrancar el backend, cópiala desde la plantilla y rellena tus valores:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

| Propiedad | Descripción |
|---|---|
| `spring.datasource.*` | Usuario/contraseña de MySQL (`uvtours_db` en `localhost:3306`) |
| `jwt.secret` | Firma del JWT, ≥ 32 bytes. Genera la tuya (`openssl rand -base64 48`) |
| `app.admin.*` | Admin sembrado al arrancar; se usa en el login (`admin@uvtours.com` por defecto) |

## Ejecutar el backend

```bash
./mvnw spring-boot:run      # http://localhost:8080
```

## Ejecutar el frontend

```bash
cd frontend
npm install
npm run dev                 # http://localhost:5173 (hace proxy de /api a :8080)
```

Build de producción: `npm run build` → `frontend/dist/` (no se versiona).

## Tests

```bash
./mvnw clean test
```

Usa `clean`: sin él, Maven puede reutilizar clases de test obsoletas.
Los tests `@DataJpaTest` corren sobre H2 embebido; `contextLoads` (`@SpringBootTest`)
necesita MySQL en marcha.

## Credenciales

No hay ninguna credencial versionada: viven solo en tu `application.properties`
local (ignorado por git) y en las variables de entorno que definas.

| Uso | Dónde se configura |
|---|---|
| MySQL | `spring.datasource.username` / `spring.datasource.password` |
| Firma JWT | `jwt.secret` |
| Admin de arranque | `app.admin.email` / `app.admin.password` → el email es el que usas en el login |

> Ningún valor de la plantilla debe llevarse a producción sin cambiarlo; para
> desplegar, prefiere variables de entorno sobre los valores por defecto.

## API

Todas las rutas están bajo `/api`. Autenticación por `Authorization: Bearer <JWT>`.

| Método y ruta | Descripción | Acceso |
|---|---|---|
| `POST /api/auth/register` | Registro de cliente | público |
| `POST /api/auth/login` | Login → JWT | público |
| `POST /api/auth/usuarios` | Crear usuario (rol a elegir) | ADMIN |
| `GET /api/auth/me` | Usuario del token | autenticado |
| `GET /api/viajes` | Catálogo de viajes y circuitos | público |
| `POST/PUT/DELETE /api/viajes[/{id}]` | Gestión de viajes | ADMIN |
| `GET /api/actividades` · `GET /api/actividades/hoy?fecha=` | Actividades y snapshot diario | público |
| `POST/PUT/DELETE /api/actividades[/{id}]` | Gestión de actividades | ADMIN |
| `PATCH /api/actividades/{id}/estado` | Cambiar estado de la actividad | ADMIN, EMPLEADO |
| `GET/POST/PUT/DELETE /api/tiendas/**` | Tiendas | ADMIN (escritura), ADMIN/EMPLEADO (lectura) |
| `GET/POST/PUT/DELETE /api/empleados/**` | Empleados | ADMIN (escritura), ADMIN/EMPLEADO (lectura) |
| `GET/POST/PUT/DELETE /api/clientes/**` | Clientes | ADMIN, EMPLEADO |
| `GET/POST /api/compras` · `GET /api/compras/{id}` | Compras | ADMIN, EMPLEADO |
| `GET /api/compras/mias` · `POST /api/compras/mias` | Compras del cliente autenticado | CLIENTE |
| `GET/POST /api/inscripciones` · `DELETE /api/inscripciones/{id}` | Inscripciones | ADMIN, EMPLEADO |
| `GET /api/inscripciones/mias` · `POST .../mias` · `DELETE .../mias/{id}` | Inscripciones propias | CLIENTE |

Errores: `ResourceNotFoundException` → 404, `IllegalArgumentException` → 400,
`IllegalStateException` → 409, validación de bean → 400. Respuesta con
`{ timestamp, message, status }`.

## Modelo de datos

- `@Inheritance(JOINED)`: `Viaje` (abstracto) → `ViajeSimple` (un destino) y `Circuito`
  (muchos a muchos con `ViajeSimple`). `ViajeDTO.tipoViaje` es el discriminador
  `"SIMPLE"` / `"CIRCUITO"` y `Circuito.precio` se calcula como suma de sus viajes simples.
- `Tienda` 1..* `Empleado`; `Compra` relaciona `Cliente` + `Viaje` (+ `Tienda`/`Empleado`
  opcionales), con `acompanantes` y flag `compraOnline`.
- `Inscripcion` = `Cliente` + `Actividad` (único por par), solo permitido mientras la
  actividad esté `PLANIFICADA`.
- `ActividadDelDia`: instantánea diaria a las 09:00 generada por un job
  `@Scheduled` (`ActividadService.generarListaActividadesDelDia`).

## Seguridad

- JWT stateless (HS256) + `SecurityConfig` con reglas por método y rol
  (`ADMIN`, `EMPLEADO`, `CLIENTE`).
- Login opcional con OAuth2 (GitHub u otro proveedor): ver comentarios en
  `application.properties` (`app.oauth2.enabled`, registrations de Spring Security).
  `OAuth2LoginSuccessHandler` emite nuestro JWT al volver del proveedor.
