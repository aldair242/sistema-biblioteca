# Manual de Usuario - Sistema de Gestión de Biblioteca

Guía paso a paso para la operación del sistema según los tres roles de usuario admitidos.

---

## 1. Inicio de Sesión y Control de Acceso

1. Ejecute la aplicación mediante `java -jar target/biblioteca-1.0.0.jar` o desde VS Code.
2. En la pantalla de **Acceso al Sistema**:
   - Ingrese su nombre de usuario o DNI.
   - Ingrese su contraseña.
   - Presione **Ingresar**.
3. **Mecanismo de Seguridad Anti-Fuerza Bruta**:
   - Si se ingresan 5 contraseñas erróneas consecutivas, la cuenta quedará suspendida temporalmente por 5 minutos.
4. **Cambio Obligatorio de Contraseña**:
   - Si es el primer ingreso de un cliente (o una cuenta con contraseña provisional), se presentará inmediatamente el diálogo modal obligatorio para establecer una nueva clave personal segura.

---

## 2. Operación del Rol ADMINISTRADOR

Al autenticarse como `ADMIN`, se habilitará la ventana **Panel de Administración** con 4 pestañas:

### 2.1 Pestaña «Bibliotecarios»
- **Registrar Personal**: En el panel lateral izquierdo, ingrese nombre de usuario, nombre completo y contraseña. Haga clic en **Guardar Bibliotecario**.
- **Activar / Desactivar**: Seleccione un bibliotecario de la tabla y presione **Activar / Desactivar** para suspender o restaurar su acceso sin eliminar el historial.

### 2.2 Pestaña «Catálogo de Libros y Ejemplares»
- **Añadir Título y Copias**: Complete los datos bibliográficos (ISBN, Título, Autor, Categoría, Editorial, Año) e indique la cantidad de ejemplares físicos a crear automáticamente. Presione **Guardar Libro**.
- **Consultar Copias**: Seleccione un libro en la tabla superior para visualizar todas sus copias físicas en la tabla inferior, identificadas por su código de barras individual (ej. `L0001-001`).
- **Mantenimiento y Baja**: Seleccione un ejemplar y presione **Modificar Estado** para alternar entre `DISPONIBLE`, `EN_MANTENIMIENTO` o `BAJA`.
- **Eliminar Libro**: Permite retirar un título del catálogo si ninguna de sus copias se encuentra en préstamo activo.

### 2.3 Pestaña «Configuración Global»
- Permite actualizar los parámetros del sistema:
  - **Multa por Día (S/)**: Tarifa monetaria por cada día de mora.
  - **Días de Préstamo**: Duración predeterminada del préstamo.
  - **Máximo de Préstamos por Lector**: Límite de préstamos simultáneos.
  - **Días de Retención de Reserva**: Plazo de reserva para recojo.
- Presione **Guardar Parámetros** y confirme en el cuadro de diálogo emergente.

### 2.4 Pestaña «Reportes y Exportación a Excel»
- Seleccione el reporte deseado en la lista desplegable:
  1. *Préstamos Activos y Vencidos*
  2. *Multas Pendientes y Pagadas*
  3. *Top 10 Libros Más Prestados*
  4. *Inventario de Ejemplares por Estado*
- La tabla cargará inmediatamente los datos en pantalla.
- Presione **Exportar a Excel (.xlsx)** para seleccionar la carpeta de destino. Si el archivo ya existe, el sistema solicitará confirmación antes de sobrescribir.

---

## 3. Operación del Rol BIBLIOTECARIO

Al autenticarse como `BIBLIOTECARIO`, se abrirá el panel operacional de atención:

### 3.1 Pestaña «Gestión de Clientes»
- **Registro**: Ingrese DNI (8 dígitos), nombres, apellidos, correo y teléfono. Al registrar, el sistema generará una contraseña temporal aleatoria y la mostrará en un diálogo emergente seguro para entregar al lector.
- **Búsqueda**: Ingrese el DNI o apellido en el buscador para filtrar en tiempo real.

### 3.2 Pestaña «Préstamos»
- **Registrar Préstamo**: Ingrese el DNI del cliente y el código de barras del libro (ej. `L0001-001`).
- Presione **Registrar Préstamo**. El sistema validará automáticamente que el cliente no tenga multas impagas, no supere el tope de préstamos y que el libro esté disponible.
- Se emitirá un comprobante visual con la fecha exacta de devolución.

### 3.3 Pestaña «Devoluciones»
- Ingrese el código de barras del libro devuelto y presione **Procesar Devolución**.
- El sistema calculará automáticamente los días de desfase y el monto de la multa si hubo retraso.
- Si corresponde una multa, se habilitará el botón **Cobrar Multa Inmediatamente** para emitir el comprobante de cancelación.
- Si el título contaba con reservas en cola, el sistema notificará que el ejemplar quedó retenido para el siguiente lector.

### 3.4 Pestaña «Reservas»
- Permite registrar a un lector en la lista de espera cuando un libro no cuenta con stock disponible.
- Muestra la posición en la cola FIFO y permite cancelaciones a solicitud del usuario.

### 3.5 Pestaña «Multas»
- Lista todas las sanciones registradas (pendientes y canceladas).
- Permite seleccionar una multa pendiente y asentar el pago correspondiente.

---

## 4. Operación del Rol CLIENTE

Al iniciar sesión como cliente lector (con su DNI y clave personal):

### 4.1 Pestaña «Catálogo y Búsqueda»
- Busque libros por título, autor o categoría.
- Visualice en tiempo real la cantidad de ejemplares disponibles.
- Si un libro tiene 0 disponibles, se habilitará el botón **Reservar Libro** para incorporarse a la cola de espera.

### 4.2 Pestaña «Mis Préstamos»
- Muestra los préstamos actuales del cliente con sus fechas límites.
- Los préstamos vencidos se destacan visualmente en color rojo.
- Si no hay cola de reservas para el libro, se puede solicitar una renovación por un nuevo período.

### 4.3 Pestaña «Mis Reservas»
- Permite consultar el estado de sus reservas (`PENDIENTE`, `LISTA`) y cancelarlas voluntariamente.

### 4.4 Pestaña «Mis Multas»
- Muestra el historial de sanciones, días de retraso y estado de cobro.

### 4.5 Pestaña «Cambiar Contraseña»
- Formulario seguro para renovar la contraseña personal verificando la clave anterior.
