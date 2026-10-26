# Reglas de Negocio del Sistema de Biblioteca

Este documento detalla exhaustivamente las políticas y reglas de negocio implementadas en la capa de servicios (`com.biblioteca.service`), las cuales se ejecutan con independencia de la interfaz gráfica y se auditan mediante pruebas unitarias con TDD.

---

## 1. Parámetros Globales del Sistema

1. Los parámetros operativos se almacenan en la tabla `configuracion` y son gestionados **única y exclusivamente por usuarios con rol `ADMIN`**:
   - `MULTA_POR_DIA`: Tarifa en Soles aplicada por cada día calendario de retraso.
   - `DIAS_PRESTAMO`: Plazo de préstamo otorgado al registrar un préstamo o renovación.
   - `MAX_PRESTAMOS`: Cantidad máxima de préstamos activos simultáneos que un cliente puede tener.
   - `DIAS_RETENCION_RESERVA`: Días que un ejemplar reservado se retiene en estado `RESERVADO` para que el cliente lo recoja.
2. Cualquier intento de modificación de parámetros por un rol distinto arroja `AccesoDenegadoException`.
3. Todos los valores numéricos deben ser estrictamente positivos y se auditan registrando el usuario ejecutor, valor anterior y nuevo valor en los logs.

---

## 2. Política de Préstamos

Para que un préstamo sea aprobado y registrado satisfactoriamente en `PrestamoService.registrarPrestamo`, deben cumplirse concurrentemente las siguientes 4 condiciones:

1. **Cero multas pendientes**: El cliente no debe tener ninguna multa en estado `PENDIENTE`. Si existe al menos una, el préstamo se bloquea inmediatamente.
2. **Cero préstamos vencidos**: El cliente no debe poseer ningún préstamo activo cuya `fecha_vencimiento` sea anterior a la fecha actual (`LocalDate.now()`).
3. **Límite de préstamos activos**: El total de préstamos en estado `ACTIVO` del cliente debe ser estrictamente menor que `MAX_PRESTAMOS`.
4. **Disponibilidad del ejemplar**:
   - El ejemplar debe encontrarse en estado `DISPONIBLE`, **O BIEN**
   - El ejemplar puede estar en estado `RESERVADO`, siempre y cuando la reserva `LISTA` pertenezca al mismo cliente que solicita el préstamo. Al completarse el préstamo, la reserva pasa automáticamente a estado `CUMPLIDA`.

---

## 3. Política de Devoluciones y Cálculo de Multas

1. Al registrar la devolución de un ejemplar:
   - Se identifica el préstamo activo asociado al ejemplar mediante su código de barras.
   - Se compara la fecha actual con la fecha pactada de vencimiento:
     $$\text{Días de retraso} = \text{fechaDevolución} - \text{fechaVencimiento}$$
   - Si $\text{días de retraso} > 0$:
     $$\text{Monto de la multa} = \text{días de retraso} \times \text{MULTA\_POR\_DIA vigente al devolver}$$
     Se genera y persiste un registro en la tabla `multa` con estado `PENDIENTE` y la tarifa diaria aplicada en ese momento exacto.
   - Si no hay retraso ($\text{días de retraso} \le 0$), el monto de la multa es `0.00` y no se genera ninguna multa.
2. El préstamo pasa a estado `DEVUELTO` con su `fecha_devolucion` registrada.
3. Transición del ejemplar devuelto:
   - Si existen reservas en cola (`PENDIENTE`) para ese título, la primera reserva según el orden de llegada (FIFO) se promueve a estado `LISTA`, se le asigna la `fecha_expiracion = hoy + DIAS_RETENCION_RESERVA` y el ejemplar pasa a estado `RESERVADO`.
   - Si no hay cola de reservas para el libro, el ejemplar pasa a estado `DISPONIBLE`.

---

## 4. Política de Renovaciones de Préstamo

1. Un préstamo puede renovarse por un período adicional equivalente a `DIAS_PRESTAMO`.
2. Restricciones:
   - Se permite un **máximo de 1 renovación** por préstamo (`renovaciones < 1`).
   - No se puede renovar si el préstamo ya se encuentra vencido.
   - No se puede renovar si existen reservas activas en cola para el libro correspondiente.

---

## 5. Ciclo de Vida de las Reservas (Cola FIFO)

1. Una reserva solo puede registrarse si **no existen ejemplares disponibles** del libro (`contarDisponibles == 0`).
2. Un cliente no puede tener dos reservas activas sobre el mismo título de libro.
3. Máquina de estados de la reserva:
   - `PENDIENTE`: Cliente aguardando en la cola de espera FIFO.
   - `LISTA`: Se produjo la devolución de una copia y se le asigna al cliente con fecha límite de recojo.
   - `CUMPLIDA`: El cliente se presentó y recogió su ejemplar formalizando el préstamo.
   - `EXPIRADA`: El cliente no recogió el libro dentro de los días de retención. El sistema cancela la reserva y transfiere el ejemplar a la siguiente reserva en la cola (o lo devuelve a `DISPONIBLE` si la cola quedó vacía).
   - `CANCELADA`: El usuario o bibliotecario desiste formalmente de la solicitud.

---

## 6. Registro de Clientes y Credenciales

1. Al dar de alta un cliente, el sistema valida la unicidad y el formato del DNI (8 dígitos exactos).
2. Se genera de forma automática una cuenta de usuario vinculada:
   - `username`: DNI del cliente.
   - `password_hash`: Contraseña aleatoria segura hasheada con BCrypt (costo 12).
   - `debe_cambiar_clave`: Se establece en `true`.
3. La contraseña temporal se entrega y muestra **una única vez** en la interfaz para que el bibliotecario se la facilite al cliente.
4. En el primer inicio de sesión del cliente, la interfaz detecta la bandera `debe_cambiar_clave` y bloquea la navegación hasta que el usuario establezca una contraseña personal que cumpla con los requisitos de robustez.
