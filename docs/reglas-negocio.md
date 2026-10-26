# Reglas de Negocio del Sistema de Gestión de Biblioteca

Este documento detalla exhaustivamente las políticas y reglas de negocio implementadas en la capa de servicios (`com.biblioteca.service`). Todas estas reglas operan de manera desacoplada de la interfaz gráfica y están verificadas al 100% mediante pruebas unitarias e integración con JUnit 5.

---

## 1. Parámetros Globales del Sistema

1. **Gestión Exclusiva de Administración**:
   - Los parámetros operativos se almacenan en la tabla `configuracion` y solo pueden ser modificados por usuarios con rol `ADMIN`:
     - `MULTA_POR_DIA`: Tarifa en Soles (S/) aplicada por cada día calendario de retraso.
     - `DIAS_PRESTAMO`: Plazo predeterminado de préstamo en días naturales.
     - `MAX_PRESTAMOS`: Límite máximo de préstamos activos simultáneos que un cliente puede tener.
     - `DIAS_RETENCION_RESERVA`: Plazo en días que un ejemplar reservado se retiene en estado `RESERVADO` para que el cliente acuda a recogerlo.
2. **Autorización y Auditoría**:
   - Cualquier intento de modificación de parámetros por un usuario sin rol `ADMIN` arroja inmediatamente `AccesoDenegadoException`.
   - Todos los valores numéricos deben ser estrictamente positivos ($> 0$).
   - Cada cambio de configuración se audita en Logback registrando: usuario ejecutor, clave del parámetro, valor anterior y nuevo valor asignado.

---

## 2. Política de Préstamos y Plazos de Devolución

Para que un préstamo sea aprobado y registrado satisfactoriamente en `PrestamoService.registrarPrestamo`, deben cumplirse concurrentemente las siguientes **4 condiciones de validación**:

1. **Cero multas pendientes**:
   - El cliente solicitante no debe registrar ninguna multa en estado `PENDIENTE`. Si existe al menos una multa impaga, el préstamo se bloquea arrojando `ValidacionException`.
2. **Cero préstamos vencidos**:
   - El cliente no debe tener ningún préstamo en estado `ACTIVO` cuya `fecha_vencimiento` sea estrictamente anterior a la fecha actual (`LocalDate.now()`).
3. **Límite máximo de préstamos activos**:
   - La cantidad de préstamos activos vigentes del cliente debe ser estrictamente menor que el valor de `MAX_PRESTAMOS`. Si ya posee el tope máximo, el sistema impide un nuevo préstamo.
4. **Disponibilidad del ejemplar físico**:
   - El ejemplar debe encontrarse en estado `DISPONIBLE`, **O BIEN**
   - El ejemplar puede estar en estado `RESERVADO` siempre y cuando la reserva en estado `LISTA` corresponda al mismo cliente que solicita el préstamo. Al completarse el préstamo, la reserva pasa automáticamente a estado `CUMPLIDA`.

### 2.1 Selección de Plazos de Devolución Flexibles
El bibliotecario cuenta con la facultad de seleccionar el plazo del préstamo según la naturaleza de la obra:
- **1 día (Express / Alta Demanda)**: Aplicable a títulos de alta rotación o consulta rápida. La fecha de vencimiento se fija en `hoy + 1 día`.
- **3 días (Fin de Semana / Reserva)**: Aplicable a préstamos especiales de viernes a lunes o títulos con alta reserva. Vencimiento en `hoy + 3 días`.
- **7 días (Estándar)**: Plazo regular recomendado de biblioteca. Vencimiento en `hoy + 7 días`.
- **14 días (Investigación)**: Plazo extendido para docentes o tesistas. Vencimiento en `hoy + 14 días`.
- **Por defecto**: Si no se especifica, se toma el valor configurado en `DIAS_PRESTAMO`.

### 2.2 Emisión de Comprobante / Ticket y Notificaciones
Una vez persistido el préstamo de forma atómica:
1. Se genera un **Ticket Digital de Préstamo** (`TicketPrestamo`) con folio único, datos del cliente, código de barras del ejemplar y **la fecha límite exacta de devolución destacada**.
2. Se ofrece el despacho inmediato de **Notificación Push / WhatsApp** formateada con emojis y mensaje preconfigurado (`wa.me/51{telefono}?text=...`) alertando al lector sobre el vencimiento y la tarifa de mora.
3. Se ofrece el despacho de **Correo Electrónico de Confirmación** con las cláusulas de cuidado del patrimonio.

---

## 3. Política de Devoluciones y Cálculo de Multas

Al registrar la devolución de un ejemplar en `PrestamoService.registrarDevolucion`:

1. **Identificación**: Se localiza el préstamo activo asociado al código de barras del ejemplar devuelto.
2. **Cálculo de Desfase Temporal**:
   $$\text{Días de retraso} = \text{fechaActual} - \text{fechaVencimiento}$$
3. **Determinación de Sanción Económica**:
   - Si $\text{días de retraso} > 0$:
     $$\text{Monto de la multa} = \text{días de retraso} \times \text{MULTA\_POR\_DIA vigente al devolver}$$
     Se genera y persiste un registro en la tabla `multa` con estado `PENDIENTE`, los días de retraso exactos y la tarifa diaria aplicada en ese momento.
   - Si $\text{días de retraso} \le 0$: No se genera ninguna multa (monto `0.00`).
4. **Cierre del Préstamo**:
   - El préstamo pasa a estado `DEVUELTO` con su `fecha_devolucion = LocalDate.now()`.
5. **Transición del Ejemplar Devuelto**:
   - Si existen reservas en cola (`PENDIENTE`) para ese título de libro, la primera reserva según el orden de llegada (FIFO) se promueve a estado `LISTA`, se le asigna la `fecha_expiracion = hoy + DIAS_RETENCION_RESERVA` y el ejemplar pasa a estado `RESERVADO`.
   - Si no hay cola de reservas para el libro, el ejemplar pasa inmediatamente a estado `DISPONIBLE`.
6. **Cobro Inmediato**:
   - El bibliotecario puede registrar el cobro de la multa generada en el mismo acto de la devolución, registrando el timestamp de pago y pasando el estado de la multa a `PAGADA`.

---

## 4. Política de Renovaciones de Préstamo

1. Un cliente o bibliotecario puede solicitar la renovación de un préstamo activo para postergar la fecha de devolución en `DIAS_PRESTAMO` adicionales.
2. **Restricciones Mandatorias**:
   - Se permite un **máximo estricto de 1 renovación** por préstamo (`renovaciones < 1`).
   - No se puede renovar si el préstamo ya se encuentra vencido.
   - **Prioridad de cola**: No se puede renovar si existen reservas activas en espera para el libro correspondiente. La renovación se bloquea para no perjudicar a lectores en espera.

---

## 5. Ciclo de Vida de las Reservas (Cola FIFO)

1. **Condición de Entrada**:
   - Una reserva solo puede registrarse si **no existen ejemplares disponibles** del libro (`contarDisponibles == 0`). Si hay copias disponibles en estantería, el sistema exige solicitar el préstamo directo.
2. **Unicidad por Título**:
   - Un cliente no puede mantener dos reservas activas sobre el mismo título literario.
3. **Máquina de Estados de la Reserva**:
   - `PENDIENTE`: El cliente se encuentra en la cola de espera ordenada estrictamente por fecha y hora de solicitud (FIFO).
   - `LISTA`: Se produjo la devolución de una copia y el sistema la apartó en estado `RESERVADO` para este lector con una fecha límite de recojo (`fecha_expiracion`).
   - `CUMPLIDA`: El lector acudió dentro del plazo y se formalizó el préstamo.
   - `EXPIRADA`: El lector no acudió dentro de los días de retención permitidos. El sistema marca la reserva como `EXPIRADA` y automáticamente transfiere el ejemplar a la siguiente reserva en la cola (o lo libera a `DISPONIBLE` si no hay más solicitudes).
   - `CANCELADA`: El cliente o bibliotecario desiste formalmente de la reserva.

---

## 6. Registro de Clientes y Credenciales de Seguridad

1. **Validación de Identidad**:
   - El DNI del cliente debe ser único en todo el sistema y contener exactamente 8 dígitos numéricos.
2. **Generación Automatizada de Usuario**:
   - Al registrar un cliente, se crea de forma concurrente una cuenta de usuario:
     - `username`: DNI del cliente.
     - `password_hash`: Contraseña provisional aleatoria de 8 caracteres alfanuméricos hasheada con **BCrypt (cost 12)**.
     - `rol`: `CLIENTE`.
     - `debe_cambiar_clave`: `true`.
3. **Entrega de Credenciales**:
   - La contraseña temporal se presenta una sola vez en un cuadro de diálogo seguro para ser comunicada al usuario.
4. **Obligación de Cambio de Contraseña**:
   - Al iniciar sesión con `debe_cambiar_clave = true`, el sistema bloquea cualquier otra acción y despliega el diálogo modal de actualización obligatoria de clave. La nueva clave debe satisfacer las políticas de contraseñas robustas (mínimo 8 caracteres, al menos una mayúscula, minúscula y número).

---

## 7. Tareas Automáticas de Mantenimiento al Iniciar el Sistema

Cada vez que el sistema se inicia en [`App.java`](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/src/main/java/com/biblioteca/App.java#L35-L48), tras comprobar la conexión a MySQL se ejecutan automáticamente dos rutinas de mantenimiento:

1. **`actualizarVencidos()`**:
   - Examina todos los préstamos en estado `ACTIVO` cuya fecha límite sea anterior a la fecha actual y actualiza su estado a `VENCIDO`.
2. **`procesarExpiradas()`**:
   - Identifica todas las reservas en estado `LISTA` cuya `fecha_expiracion` haya caducado sin ser recogidas.
   - Las marca como `EXPIRADA` y reasigna los ejemplares a los siguientes lectores en la cola FIFO o los devuelve a `DISPONIBLE`.
