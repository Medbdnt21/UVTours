# Práctica 1 – SCRUM: Historias de Usuario (UvTours)

**Proyecto:** UvTours – API REST (Spring Boot) + SPA (React) para una agencia de viajes.

**Herramienta:** Jira (plantilla Scrum). **Fecha de creación de las historias:** 30/09/2026.

**Responsables:** `Administrador` (tareas de back-office / admin) y `Cliente` (tareas del usuario final).

---

## 1. Epics

| Epic | Agrupa |
|---|---|
| **E1. Acceso y usuarios** | Registro, login, roles, JWT |
| **E2. Gestión de viajes** | Viajes simples, circuitos, descuento luna de miel |
| **E3. Gestión de clientes** | Alta/edición/búsqueda de clientes |
| **E4. Puntos de venta** | Tiendas y empleados |
| **E5. Actividades** | CRUD de actividades, máquina de estados, lista del día |
| **E6. Compras** | Compra online y venta en mostrador, acompañantes |
| **E7. Inscripciones** | Inscripción y anulación de actividades |
| **E8. Interfaz de usuario** | Tareas de definición de interfaces (Sprint 1) |
| **E9. Infraestructura técnica** | Control de versiones y entorno (Sprint 1) |
---

## 2. Historias de usuario

### HU01 – Registrarse e iniciar sesión

- **Epic:** E1 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación (planning poker):** 5 · **Valor:** 9 · **Prioridad:** Alta

- **Descripción:** Como **cliente**, quiero **registrarme con email y contraseña e iniciar sesión para obtener un token que me permita usar la aplicación de forma personalizada**.

- **Criterios de aceptación:**

  1. Dado que completo registro con email válido, contraseña ≥ 6 caracteres y nombre, cuando envío el formulario, entonces la cuenta se crea con rol CLIENTE y recibo un JWT.

  2. Dado que el email ya está registrado, cuando envío el registro, entonces aparece el error "El email ya está registrado" (400).

  3. Dado que introduzco credenciales correctas, cuando pulso "Entrar", entonces obtengo el token y entro en mi área.

  4. Dado que introduzco credenciales incorrectas, cuando pulso "Entrar", entonces aparece "Credenciales inválidas" (401).

  5. Dado que me registro indicando un DNI que ya existe como cliente, entonces mi cuenta queda vinculada a ese cliente.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de Login/Registro **(Sprint 1)**

  - [ ] Definir el modelo de datos Usuario/Rol

  - [ ] Repositorio/DAO de usuarios

  - [ ] Servicio de negocio (registro, normalización de email, BCrypt)

  - [ ] Controlador `POST /api/auth/register`, `POST /api/auth/login`

  - [ ] Tests unitarios de dominio/servicio

---

### HU02 – Consultar el catálogo de viajes

- **Epic:** E2 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 9 · **Prioridad:** Alta

- **Descripción:** Como **cliente**, quiero **ver la lista de viajes disponibles con su precio y descuento para poder elegir el que más me interesa**.

- **Criterios de aceptación:**

  1. Dado que abro la página de inicio sin iniciar sesión, cuando se cargan los datos, entonces veo todos los viajes (simples y circuitos) con nombre, fechas y precio.

  2. Dado que un viaje es de luna de miel con descuento, entonces el precio original aparece tachado junto al precio final.

  3. Dado que no hay viajes registrados, entonces se muestra un mensaje de "no hay viajes disponibles".

  4. Dado que el servicio falla, entonces se muestra un error comprensible y la página no se rompe.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica del catálogo (Home) **(Sprint 1)**

  - [ ] Servicio de consulta de viajes

  - [ ] Controlador `GET /api/viajes` (acceso público)

  - [ ] Tests del controlador

---

### HU03 – Dar de alta un viaje simple

- **Epic:** E2 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 5 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **administrador**, quiero **dar de alta un viaje simple con nombre, destino, fechas y precio para ponerlo a la venta**.

- **Criterios de aceptación:**

  1. Dado que abro "Nuevo viaje" y selecciono tipo SIMPLE, cuando completo nombre, destino, fechaInicio ≤ fechaFin y precio ≥ 0, entonces el viaje se guarda.

  2. Dado que envío un tipo de viaje distinto de SIMPLE/CIRCUITO, entonces aparece "Tipo de viaje no válido" (400).

  3. Dado que el viaje no existe, cuando envío el formulario, entonces recibo 404 en operaciones sobre un id inexistente.

  4. Dado que un usuario sin rol ADMIN intenta crear un viaje, entonces recibe 403.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de gestión de viajes **(Sprint 1)**

  - [ ] Definir el modelo de datos Viaje/ViajeSimple

  - [ ] Repositorio/DAO de viajes

  - [ ] Servicio de negocio (validaciones)

  - [ ] Controlador `POST /api/viajes`

  - [ ] Tests unitarios de dominio/servicio

---

### HU04 – Actualizar y eliminar viajes

- **Epic:** E2 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **modificar los datos de un viaje o eliminarlo para mantener el catálogo actualizado**.

- **Criterios de aceptación:**

  1. Dado que edito un viaje existente, cuando guardo los cambios, entonces los datos quedan actualizados.

  2. Dado que elimino un viaje, cuando confirmo, entonces desaparece del catálogo (204).

  3. Dado que el viaje no existe, entonces recibo 404 "recurso no encontrado".

- **Subtareas:**

  - [ ] Extender la interfaz de gestión de viajes (editar/eliminar) **(Sprint 1)**

  - [ ] Servicio de negocio de actualización/borrado

  - [ ] Controlador `PUT /api/viajes/{id}`, `DELETE /api/viajes/{id}`

  - [ ] Tests del servicio

---

### HU05 – Crear un circuito con viajes simples

- **Epic:** E2 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 8 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **administrador**, quiero **componer un circuito a partir de varios viajes simples para ofrecer rutas combinadas cuyo precio se calcule automáticamente**.

- **Criterios de aceptación:**

  1. Dado que creo un viaje con tipo CIRCUITO y selecciono varios viajes simples, cuando guardo, entonces el circuito se crea con precio = suma de los precios de sus viajes simples.

  2. Dado que modifico un circuito, cuando guardo, entonces el precio se recalcula con la lista vigente de viajes.

  3. Dado que no envío viajes simples, entonces el circuito se crea con precio 0.

  4. Dado que un id de viaje simple no existe, entonces recibo 404.

- **Subtareas:**

  - [ ] Extender la interfaz de viajes para seleccionar viajes de un circuito **(Sprint 1)**

  - [ ] Definir el modelo de datos Circuito (relación muchos-a-muchos)

  - [ ] Repositorio/DAO de circuitos

  - [ ] Servicio de negocio (cálculo de coste total)

  - [ ] Tests unitarios del cálculo de precio

---

### HU06 – Aplicar descuento de luna de miel

- **Epic:** E2 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **marcar un viaje como luna de miel y asignarle un descuento para poder promocionarlo a parejas recién casadas**.

- **Criterios de aceptación:**

  1. Dado que activo "luna de miel" en un viaje y fijo un descuento, cuando guardo, entonces el descuento queda aplicado.

  2. Dado que desactivo la luna de miel, entonces el descuento se pone a 0.

  3. Dado que intento aplicar un descuento a un viaje que no es de luna de miel (PATCH), entonces recibo 400 "El viaje no es de tipo Luna de Miel".

- **Subtareas:**

  - [ ] Añadir el control de descuento en la interfaz de viajes **(Sprint 1)**

  - [ ] Servicio de negocio de descuento

  - [ ] Controlador `PATCH /api/viajes/{id}/descuento-luna-miel`

  - [ ] Tests del servicio

---

### HU07 – Gestionar clientes (CRUD)

- **Epic:** E3 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 5 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **administrador**, quiero **dar de alta, editar y eliminar clientes con DNI, nombre, dirección, email y teléfono para llevar la ficha de la cartera de clientes**.

- **Criterios de aceptación:**

  1. Dado que abro "Nuevo cliente" y completo un DNI obligatorio, cuando guardo, entonces el cliente se crea.

  2. Dado que el DNI ya existe o falta, entonces recibo 400 ("El DNI es obligatorio" / duplicado).

  3. Dado que edito un cliente, entonces puedo cambiar nombre, dirección, email y teléfono, pero **no** el DNI.

  4. Dado que busco un cliente por DNI, entonces aparece su ficha con sus acompañantes.

  5. Dado que elimino un cliente, entonces se borra su ficha (204).

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de clientes **(Sprint 1)**

  - [ ] Definir el modelo de datos Cliente

  - [ ] Repositorio/DAO de clientes

  - [ ] Servicio de negocio (validación de DNI)

  - [ ] Controlador `/api/clientes`

  - [ ] Tests unitarios de dominio/servicio

---

### HU08 – Gestionar tiendas

- **Epic:** E4 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 6 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **dar de alta, editar y eliminar tiendas con dirección, código postal y teléfono para organizar los puntos de venta de la agencia**.

- **Criterios de aceptación:**

  1. Dado que completo dirección, código postal y teléfono, cuando guardo la tienda, entonces se crea.

  2. Dado que intento eliminar una tienda que tiene compras asociadas, entonces recibo 409 "No se puede eliminar una tienda con compras asociadas".

  3. Dado que la tienda no existe, entonces recibo 404.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de tiendas **(Sprint 1)**

  - [ ] Definir el modelo de datos Tienda

  - [ ] Repositorio/DAO de tiendas

  - [ ] Servicio de negocio

  - [ ] Controlador `/api/tiendas`

  - [ ] Tests unitarios de dominio/servicio

---

### HU09 – Gestionar empleados

- **Epic:** E4 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 6 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **dar de alta, editar y eliminar empleados asignándolos a una tienda para saber quién atiende cada punto de venta**.

- **Criterios de aceptación:**

  1. Dado que completo nombre, email, DNI y tienda, cuando guardo, entonces el empleado se crea en esa tienda.

  2. Dado que la tienda indicada no existe, entonces recibo 404.

  3. Dado que intento eliminar un empleado con compras asociadas, entonces recibo 409.

  4. Dado que reasigno un empleado a otra tienda, cuando guardo, entonces queda en la nueva tienda.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de empleados **(Sprint 1)**

  - [ ] Definir el modelo de datos Empleado

  - [ ] Repositorio/DAO de empleados

  - [ ] Servicio de negocio

  - [ ] Controlador `/api/empleados`

  - [ ] Tests unitarios de dominio/servicio

---

### HU10 – Planificar actividades y gestionar su estado

- **Epic:** E5 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 8 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **administrador**, quiero **crear actividades con nombre y fecha y cambiar su estado (planificada, en ejecución, finalizada, cancelada) para controlar la operativa diaria de la agencia**.

- **Criterios de aceptación:**

  1. Dado que creo una actividad, cuando guardo, entonces nace en estado PLANIFICADA aunque se envíe otro estado.

  2. Dado que una actividad está PLANIFICADA, cuando pulso "Iniciar", entonces pasa a EN_EJECUCION; si no estaba planificada, recibo 409.

  3. Dado que está EN_EJECUCION, cuando pulso "Finalizar", entonces pasa a FINALIZADA y registra la fecha de finalización; en otro caso, 409.

  4. Dado que está PLANIFICADA, cuando pulso "Cancelar", entonces pasa a CANCELADA y registra la fecha de cancelación; en otro caso, 409.

  5. Dado que intento editar una actividad EN_EJECUCION o FINALIZADA, entonces recibo 409 "No se puede modificar una actividad en ejecución o finalizada".

  6. Dado que envío un estado inválido, entonces recibo 400 "Estado no válido".

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de actividades con botones de transición **(Sprint 1)**

  - [ ] Definir el modelo de datos Actividad y enum de estados

  - [ ] Repositorio/DAO de actividades

  - [ ] Servicio de negocio (máquina de estados)

  - [ ] Controlador `/api/actividades` y `PATCH /{id}/estado`

  - [ ] Tests unitarios de la máquina de estados

---

### HU11 – Vender un viaje en mostrador (con acompañantes)

- **Epic:** E6 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 8 · **Valor:** 9 · **Prioridad:** Alta

- **Descripción:** Como **empleado de tienda**, quiero **registrar la venta de un viaje a un cliente indicando tienda, empleado y acompañantes para formalizar la reserva presencial**.

- **Criterios de aceptación:**

  1. Dado que busco al cliente por DNI, selecciono el viaje y la tienda, cuando confirmo la venta, entonces la compra se registra con precio final calculado.

  2. Dado que el viaje es de luna de miel con descuento, entonces precio final = precio del viaje − descuento.

  3. Dado que la venta no es online y falta la tienda o no existe, entonces recibo 404.

  4. Dado que añado acompañantes con nombre y DNI, entonces quedan asociados a la compra.

  5. Dado que un CLIENTE intenta usar este endpoint, entonces recibe 403.

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de nueva venta **(Sprint 1)**

  - [ ] Definir el modelo de datos Compra y Acompañante

  - [ ] Repositorio/DAO de compras

  - [ ] Servicio de negocio (cálculo de precio final, tienda obligatoria)

  - [ ] Controlador `POST /api/compras`

  - [ ] Tests unitarios del cálculo de precio

---

### HU12 – Comprar un viaje online

- **Epic:** E6 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 5 · **Valor:** 10 · **Prioridad:** Alta

- **Descripción:** Como **cliente**, quiero **comprar un viaje desde la web con mi cuenta para reservar sin desplazarme a la tienda**.

- **Criterios de aceptación:**

  1. Dado que estoy autenticado y elijo un viaje, cuando confirmo la compra, entonces se registra como compra online a mi nombre con el precio final (con descuento de luna de miel si aplica).

  2. Dado que mi cuenta no está vinculada a un cliente, entonces recibo 400 "El usuario no está vinculado a un cliente…".

  3. Dado que no estoy autenticado, entonces recibo 401.

  4. Dado que añado acompañantes, entonces quedan guardados con la compra.

- **Subtareas:**

  - [ ] Diseñar el modal de compra online **(Sprint 1)**

  - [ ] Servicio de negocio de compra online

  - [ ] Controlador `POST /api/compras/mias`

  - [ ] Tests del servicio

---

### HU13 – Consultar mis compras

- **Epic:** E6 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 2 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **cliente**, quiero **ver el historial de mis compras con fecha, viaje y precio pagado para comprobar mis reservas**.

- **Criterios de aceptación:**

  1. Dado que entro en "Mi cuenta", cuando se carga la página, entonces veo solo mis compras con fecha, viaje y precio final.

  2. Dado que soy cliente, entonces no puedo ver las compras de otros clientes (403 en el endpoint de administración).

  3. Dado que no tengo compras, entonces veo un mensaje de "aún no tienes compras".

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de "Mi cuenta" **(Sprint 1)**

  - [ ] Servicio de consulta de compras propias

  - [ ] Controlador `GET /api/compras/mias`

  - [ ] Tests del controlador

---

### HU14 – Inscribirme a una actividad

- **Epic:** E7 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 5 · **Valor:** 9 · **Prioridad:** Alta

- **Descripción:** Como **cliente**, quiero **inscribirme en una actividad desde la web para participar en el plan del día**.

- **Criterios de aceptación:**

  1. Dado que veo la actividad en la lista del día y estoy autenticado, cuando pulso "Inscribirme", entonces queda registrada mi inscripción con fecha.

  2. Dado que la actividad no está PLANIFICADA, entonces recibo 409 "No se pueden registrar inscripciones en una actividad no planificada".

  3. Dado que ya estoy inscrito en esa actividad, entonces recibo 400 "El cliente ya está inscrito en la actividad".

  4. Dado que mi cuenta no está vinculada a un cliente, entonces recibo 400.

- **Subtareas:**

  - [ ] Añadir el botón "Inscribirme" en la interfaz de actividades del día **(Sprint 1)**

  - [ ] Definir el modelo de datos Inscripción (restricción única cliente+actividad)

  - [ ] Repositorio/DAO de inscripciones

  - [ ] Servicio de negocio (solo actividades planificadas)

  - [ ] Controlador `POST /api/inscripciones/mias`

  - [ ] Tests unitarios de dominio/servicio

---

### HU15 – Anular una inscripción propia

- **Epic:** E7 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 2 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **cliente**, quiero **anular mi inscripción en una actividad para dejar libre mi sitio si ya no voy a participar**.

- **Criterios de aceptación:**

  1. Dado que veo mis inscripciones en "Mi cuenta", cuando pulso "Anular", entonces la inscripción se elimina (204).

  2. Dado que intento anular una inscripción que no es mía, entonces recibo 403 "No puedes anular una inscripción que no es tuya".

  3. Dado que la actividad no está PLANIFICADA, entonces recibo 409.

- **Subtareas:**

  - [ ] Añadir la acción "Anular" en la interfaz de mi cuenta **(Sprint 1)**

  - [ ] Servicio de negocio de anulación

  - [ ] Controlador `DELETE /api/inscripciones/mias/{id}`

  - [ ] Tests del servicio

---

### HU16 – Gestionar inscripciones desde back-office

- **Epic:** E7 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **inscribir clientes a actividades y anular inscripciones desde el back-office para atender altas y bajas presenciales**.

- **Criterios de aceptación:**

  1. Dado que busco al cliente por DNI y selecciono la actividad planificada, cuando confirmo, entonces la inscripción se crea.

  2. Dado que consulto por actividadId o clienteId, entonces aparece la lista correspondiente.

  3. Dado que no indico actividadId ni clienteId, entonces recibo 400 "Debe indicar actividadId o clienteId".

  4. Dado que anulo una inscripción, entonces se elimina (204).

- **Subtareas:**

  - [ ] Diseñar la interfaz gráfica de inscripciones **(Sprint 1)**

  - [ ] Servicio de negocio de inscripciones

  - [ ] Controlador `/api/inscripciones`

  - [ ] Tests del servicio

---

### HU17 – Ver la lista de actividades del día

- **Epic:** E5 · **Responsable:** Cliente · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **cliente**, quiero **ver la lista de actividades de hoy con su estado para planificar mi día y apuntarme a ellas**.

- **Criterios de aceptación:**

  1. Dado que abro la página de inicio, cuando se consulta la lista del día, entonces se muestran las actividades planificadas del día con su estado.

  2. Dado que la consulta incluye una fecha opcional, entonces se devuelven las actividades de esa fecha.

  3. Dado que aún no se ha generado la lista del día, entonces se devuelve una lista vacía sin error.

  4. Dado que la lista es pública, entonces no hace falta iniciar sesión para verla.

- **Subtareas:**

  - [ ] Diseñar el bloque "Actividades de hoy" de la interfaz **(Sprint 1)**

  - [ ] Definir el modelo de datos de la instantánea diaria

  - [ ] Servicio con tarea programada (generación diaria a las 09:00)

  - [ ] Controlador `GET /api/actividades/hoy`

  - [ ] Tests del servicio y del controlador

---

### HU18 – Administrar usuarios y roles

- **Epic:** E1 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 7 · **Prioridad:** Media

- **Descripción:** Como **administrador**, quiero **crear usuarios con rol ADMIN, EMPLEADO o CLIENTE para dar acceso al personal de la agencia**.

- **Criterios de aceptación:**

  1. Dado que completo email, contraseña (≥ 6), nombre y rol, cuando guardo, entonces el usuario se crea con ese rol.

  2. Dado que el email ya existe, entonces recibo 400.

  3. Dado que un usuario sin rol ADMIN intenta crear usuarios, entonces recibe 403.

  4. Dado que inicio sesión como ADMIN, entonces puedo acceder a las secciones de administración.

- **Subtareas:**

  - [ ] Diseñar la interfaz de administración de usuarios **(Sprint 1)**

  - [ ] Servicio de gestión de usuarios/roles

  - [ ] Controlador `POST /api/auth/usuarios`

  - [ ] Tests de autorización

---

## 3. Historias y tareas técnicas del Sprint 1 (obligatorias)

### HT01 – Configurar el sistema de control de versiones** **(historia técnica)\

- **Epic:** E9 · **Responsable:** Administrador · **Fecha de creación:** 30/09/2026

- **Estimación:** 3 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **equipo de desarrollo**, quiero **configurar el repositorio del proyecto con control de versiones y definir la estrategia de ramas para poder trabajar de forma colaborativa y trazable**.

- **Criterios de aceptación:**

  1. Dado que creo el repositorio del proyecto, cuando lo clono, entonces el código fuente (backend y frontend) queda versionado.

  2. Dado que defino las ramas (main/develop/feature), cuando el equipo trabaja, entonces se respetan las convenciones de fusión.

  3. Dado que existe un `.gitignore`/exclusiones, entonces no se versionan secrets (`application.properties` con credenciales) ni artefactos compilados.

  4. Dado que hay un README con instrucciones, entonces un nuevo miembro puede clonar y arrancar el proyecto.

- **Subtareas:**

  - [ ] Crear el repositorio e importar el proyecto

  - [ ] Definir convención de ramas y commits

  - [ ] Configurar ignore de secrets y compilados

  - [ ] Invitar al equipo (y al profesor) al repositorio

### HT02 – Definir las interfaces de usuario** **(tarea técnica obligatoria Sprint 1)\

- **Epic:** E8 · **Responsable:** Administrador (interfaces de back-office) / Cliente (interfaces de usuario final) · **Fecha de creación:** 30/09/2026

- **Estimación:** 8 · **Valor:** 8 · **Prioridad:** Alta

- **Descripción:** Como **equipo de desarrollo**, quiero **definir todas las interfaces de usuario del proyecto antes de empezar a implementar para que cada historia de usuario tenga su maqueta asociada**.

- **Criterios de aceptación:**

  1. Dado que se lista el backlog, cuando reviso el Sprint 1, entonces todas las historias tienen su tarea de interfaz asociada.

  2. Dado que cada maqueta describe campos, acciones y estados de error, entonces el equipo puede implementar sin dudas de diseño.

  3. Dado que existen interfaces para administrador y para cliente, entonces se cubren los dos perfiles de usuario.

- **Subtareas (interfaces, todas en Sprint 1):**

  - [ ] Login / Registro → HU01 **(Cliente)**

  - [ ] Catálogo de viajes (Home) → HU02 **(Cliente)**

  - [ ] Alta/edición de viajes y circuitos → HU03, HU04, HU05, HU06 **(Administrador)**

  - [ ] Gestión de clientes → HU07 **(Administrador)**

  - [ ] Gestión de tiendas → HU08 **(Administrador)**

  - [ ] Gestión de empleados → HU09 **(Administrador)**

  - [ ] Gestión de actividades y estados → HU10 **(Administrador)**

  - [ ] Nueva venta en mostrador → HU11 **(Administrador)**

  - [ ] Modal de compra online → HU12 **(Cliente)**

  - [ ] Mi cuenta (compras e inscripciones) → HU13, HU15 **(Cliente)**

  - [ ] Lista de actividades del día + botón inscribirme → HU14, HU17 **(Cliente)**

  - [ ] Gestión de inscripciones → HU16 **(Administrador)**

  - [ ] Administración de usuarios → HU18 **(Administrador)**

---

## 4. Reparto en los tres sprints

| Sprint | Contenido | Historias | Responsable |
|---|---|---|---|
| **Sprint 1** – "Base y diseño" | Historia técnica de control de versiones + definición de interfaces de usuario | **HT01**, **HT02** (con sus 13 subtareas de interfaz) | Administrador / Cliente |
| **Sprint 2** – "Núcleo de back-office y catálogo" | Acceso, viajes, clientes, puntos de venta y actividades | **HU01, HU02, HU03, HU04, HU05, HU06, HU07, HU08, HU09, HU10** | Cliente (HU01, HU02) · Administrador (HU03–HU10) |
| **Sprint 3** – "Ventas e inscripciones" | Compras, inscripciones, lista del día y usuarios | **HU11, HU12, HU13, HU14, HU15, HU16, HU17, HU18** | Administrador (HU11, HU16, HU18) · Cliente (HU12, HU13, HU14, HU15, HU17) |
**Resumen de asignación por responsable:**

- **Administrador:** HT01, HT02 (interfaces de back-office), HU03, HU04, HU05, HU06, HU07, HU08, HU09, HU10, HU11, HU16, HU18.

- **Cliente:** HT02 (interfaces de usuario final), HU01, HU02, HU12, HU13, HU14, HU15, HU17.
