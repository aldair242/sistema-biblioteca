# Manual de Usuario - Sistema de Gestión de Biblioteca

Guía paso a paso para la operación del sistema según los tres roles de usuario admitidos, con el nuevo sistema de diseño moderno, barra de navegación lateral y componentes visuales ergonómicos.

---

## 1. Inicio de Sesión y Control de Acceso

1. Ejecute la aplicación mediante `java -jar target/biblioteca-1.0.0.jar` o desde su IDE.
2. En la pantalla dividida (**Split-Screen**) de **Acceso al Sistema**:
   - En el panel derecho de la tarjeta central, ingrese su nombre de usuario o DNI.
   - Ingrese su contraseña (puede activar el botón con forma de ojo para alternar la visibilidad del texto).
   - Presione **Ingresar** o presione la tecla `Enter`.
3. **Mecanismo de Seguridad Anti-Fuerza Bruta**:
   - Si se ingresan 5 contraseñas erróneas consecutivas, la cuenta quedará suspendida temporalmente por 5 minutos y se mostrará un banner rojo explicativo.
4. **Cambio Obligatorio de Contraseña**:
   - Si es el primer ingreso de un cliente (o una cuenta con contraseña provisional), se presentará inmediatamente el diálogo modal obligatorio para establecer una nueva clave personal segura.

---

## 2. Operación del Rol ADMINISTRADOR

Al autenticarse como `ADMIN`, se habilitará la ventana **Panel de Administración** con una barra lateral de navegación interactiva y 5 secciones:

### 2.1 Módulo «Dashboard»
- Tarjetas de métricas KPI en tiempo real:
  - **Total Libros Registrados** en catálogo.
  - **Total de Ejemplares Físicos** en inventario.
  - **Préstamos en Curso** atendidos.
  - **Multas Pendientes de Cobro**.

### 2.2 Módulo «Bibliotecarios»
- **Registrar Personal**: En el formulario dentro de la tarjeta izquierda, ingrese nombre de usuario, nombre completo y contraseña. Haga clic en **Guardar Bibliotecario**.
- **Activar / Desactivar**: Seleccione un bibliotecario de la tabla y presione el botón para suspender o restaurar su acceso sin comprometer la auditoría.
- **Editar / Restablecer Clave**: Permite actualizar el nombre del bibliotecario o generar una clave provisional temporal.

### 2.3 Módulo «Catálogo de Libros y Ejemplares»
- **Añadir Título y Copias**: Complete los datos bibliográficos (ISBN, Título, Autor, Categoría, Editorial, Año) e indique la cantidad de ejemplares físicos a crear automáticamente. Presione **Guardar Libro**.
- **Consultar Copias**: Seleccione un libro en la tabla superior para visualizar todas sus copias físicas en la tabla inferior, identificadas por su código de barras individual (ej. `L0001-001`).
- **Mantenimiento y Baja**: Seleccione un ejemplar y presione **Modificar Estado** para alternar entre `DISPONIBLE`, `EN_MANTENIMIENTO` o `BAJA`.
- **Eliminar Libro**: Permite retirar un título del catálogo si ninguna de sus copias se encuentra en préstamo activo.

### 2.4 Módulo «Configuración Global»
- Permite actualizar los parámetros del sistema en tarjetas visuales:
  - **Multa por Día (S/)**: Tarifa monetaria por cada día de mora.
  - **Días de Préstamo**: Duración predeterminada del préstamo.
  - **Máximo de Préstamos por Lector**: Límite de préstamos simultáneos.
  - **Días de Retención de Reserva**: Plazo de reserva para recojo.
- Presione **Guardar Parámetros** y confirme en el cuadro de diálogo modal.

### 2.5 Módulo «Reportes y Exportación a Excel»
- Seleccione el reporte deseado en la lista desplegable:
  1. *Préstamos Activos y Vencidos*
  2. *Multas Pendientes y Pagadas*
  3. *Top 10 Libros Más Prestados*
  4. *Inventario de Ejemplares por Estado*
- La tabla cargará inmediatamente los datos en pantalla con filas de 36 px de alto y chips visuales de colores.
- Presione **Exportar a Excel (.xlsx)** para seleccionar la carpeta de destino. Si el archivo ya existe, el sistema solicitará confirmación antes de sobrescribir.

---

## 3. Operación del Rol BIBLIOTECARIO

Al autenticarse como `BIBLIOTECARIO`, se abrirá el panel operacional de atención con barra lateral:

### 3.1 Módulo «Dashboard Operativo»
- Tarjetas de estado con el recuento en tiempo real de préstamos activos, préstamos vencidos que requieren atención inmediata, clientes registrados y reservas pendientes.

### 3.2 Módulo «Préstamos»
- **Registrar Préstamo**: Ingrese el DNI del cliente y el código de barras del libro (ej. `L0001-001`).
- Presione **Registrar Préstamo**. El sistema validará automáticamente que el cliente no tenga multas impagas, no supere el tope de préstamos y que el ejemplar esté disponible.
- Se emitirá un comprobante visual estilizado con la fecha exacta de devolución.
- **Renovación**: Seleccione un préstamo activo de la tabla y presione **Renovar Préstamo** para extender el plazo (siempre que no existan reservas en cola de espera).

### 3.3 Módulo «Devoluciones»
- Ingrese el código de barras del libro devuelto y presione **Procesar Devolución**.
- El sistema calculará automáticamente los días de retraso y el monto de la multa si hubo mora, mostrándolo en una tarjeta de resumen.
- Si corresponde una multa, se habilitará el botón **Cobrar Multa Inmediatamente** para emitir el comprobante de cancelación.
- Si el título contaba con solicitudes en cola de espera, el sistema notificará que el ejemplar quedó automáticamente apartado para el siguiente lector.

### 3.4 Módulo «Gestión de Clientes»
- **Registro**: Ingrese DNI (8 dígitos), nombres, apellidos, correo y teléfono. Al registrar, el sistema generará una contraseña temporal aleatoria y la mostrará en un modal seguro para entregar al lector.
- **Búsqueda**: Ingrese el DNI o apellido en el buscador para filtrar en tiempo real.
- **Modificación**: Permite actualizar datos de contacto del cliente o cambiar su estado de actividad.

### 3.5 Módulo «Catálogo»
- Consulta rápida de títulos, disponibilidad y detalle de copias para asistencia inmediata en mostrador.

### 3.6 Módulo «Reservas»
- Permite registrar a un lector en la lista de espera cuando un libro no cuenta con stock disponible.
- Muestra la posición en la cola FIFO y permite cancelaciones voluntarias a solicitud del usuario.

### 3.7 Módulo «Multas»
- Lista todas las sanciones registradas (pendientes y canceladas) con badges de color.
- Permite seleccionar una multa pendiente y registrar su cobro.

---

## 4. Operación del Rol CLIENTE

Al iniciar sesión como cliente lector (con su DNI y clave personal):

### 4.1 Módulo «Mi Panel»
- Resumen personalizado con tarjetas métricas: préstamos activos, reservas en espera y multas pendientes.

### 4.2 Módulo «Explorar Catálogo»
- **Selector de Vista**: Alterne libremente entre **Vista Cuadrícula** (tarjetas visuales de libros con portada temática) o **Vista Tabla** (datos tabulares compactos).
- Busque libros por título o autor y filtre por categorías temáticas mediante el menú desplegable.
- Si un libro tiene 0 copias disponibles, el botón de reserva se habilitará automáticamente para unirse a la cola de espera.

### 4.3 Módulo «Mis Préstamos»
- Muestra los préstamos actuales del cliente con sus fechas límites.
- Los préstamos vencidos se destacan visualmente con badge carmesí de alerta.
- Si no hay cola de reservas para el libro, se puede solicitar una renovación por un nuevo período con un solo clic.

### 4.4 Módulo «Mis Reservas»
- Permite consultar el estado de sus reservas (`PENDIENTE`, `LISTA` para recojo) y cancelarlas voluntariamente.

### 4.5 Módulo «Mis Multas»
- Muestra el historial de sanciones, días de retraso y estado de cobro con badges visuales.

### 4.6 Módulo «Seguridad»
- Formulario seguro para renovar la contraseña personal con validación en tiempo real de mayúsculas, minúsculas, números y longitud mínima.
