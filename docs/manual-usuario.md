# Manual de Usuario - Sistema de Gestión de Biblioteca

Guía paso a paso para la operación completa del **Sistema de Gestión de Biblioteca**, con diseño visual moderno, pantallas completas por defecto, navegación lateral interactiva, gráficos analíticos y notificaciones multicanal (Push WhatsApp y Correo).

---

## 1. Inicio del Sistema y Control de Acceso

### 1.1 Ejecución de la Aplicación
Puede iniciar el sistema mediante cualquiera de las siguientes opciones:
- **Mediante el archivo JAR empaquetado**:
  ```bash
  java -jar target/biblioteca-1.0.0.jar
  ```
- **A través de Maven**:
  ```bash
  mvn exec:java -Dexec.mainClass="com.biblioteca.App"
  ```

### 1.2 Pantalla de Inicio de Sesión (`LoginView`)
Al arrancar, se desplegará una pantalla dividida (**Split-Screen**):
- **Panel Izquierdo**: Marca corporativa "BIBLIOTECA CI", ícono heráldico y descripción del sistema.
- **Tarjeta Central Derecha**: Formulario estilizado con diseño estable:
  1. Ingrese su nombre de usuario o DNI en el campo **Usuario o DNI**.
  2. Ingrese su contraseña en el campo **Contraseña**.
  3. Puede pulsar el botón de ojo a la derecha para ver u ocultar los caracteres.
  4. Presione **Ingresar al Sistema** o presione la tecla `Enter`.
- **Recuperación de Contraseña por Correo**:
  - Si olvidó su contraseña, pulse el enlace **«¿Olvidaste tu contraseña? Restablecer aquí»**.
  - Ingrese el correo electrónico registrado con su cuenta de lector en el diálogo modal.
  - El sistema validará su correo, generará una clave temporal y la despachará por correo electrónico.
  - Al iniciar sesión con esta clave temporal, el sistema abrirá automáticamente el diálogo de cambio obligatorio para configurar su contraseña definitiva.
- **Mecanismos de Protección**:
  - Si ingresa credenciales inválidas, aparecerá un banner rojo explicativo dentro de un espacio reservado sin desplazar ningún campo.
  - Si acumula 5 intentos fallidos consecutivos, la cuenta se bloqueará automáticamente por 5 minutos contra ataques de fuerza bruta.
  - Si es el primer ingreso con una clave provisional, se abrirá de inmediato el diálogo para establecer una nueva contraseña personal obligatoria.

---

## 2. Manual del Rol ADMINISTRADOR

Al iniciar sesión como `admin`, la ventana **Panel de Administración** se abrirá automáticamente en **pantalla completa** con la barra lateral de navegación:

```
[ 88 Inicio ]   [ 8 Bibliotecarios ]   [ 📖 Catálogo y Copias ]   [ ⚙️ Configuración ]   [ 📊 Reportes Excel ]
```

### 2.1 Sección «Inicio» (Dashboard)
- Visualice de un vistazo las 4 tarjetas de métricas institucionales:
  - **Total Títulos en Catálogo**: Cantidad de obras registradas.
  - **Total de Ejemplares Físicos**: Copias físicas en estantería.
  - **Préstamos en Curso**: Total de libros prestados a lectores.
  - **Tarifa Vigente de Mora**: Costo diario configurado por día de retraso.

### 2.2 Sección «Bibliotecarios»
- **Registrar Nuevo Bibliotecario**:
  1. En el panel lateral izquierdo, ingrese nombre de usuario (ej. `biblio2`), nombre completo y contraseña inicial.
  2. Presione **+ Registrar Bibliotecario**. El registro aparecerá en la tabla.
- **Modificar / Cambiar Estado**:
  1. Seleccione un bibliotecario de la tabla central.
  2. Presione **Activar / Desactivar** para suspender o rehabilitar su acceso.
  3. Presione **Restablecer Contraseña** para asignarle una nueva clave de acceso.

### 2.3 Sección «Catálogo y Copias»
- **Buscador en Tiempo Real**:
  - Escriba en la caja de búsqueda superior por título, autor, ISBN o categoría y presione **Buscar** para filtrar al instante. Presione **Ver Todos** para restablecer la lista completa.
- **Formulario Colapsable**:
  - Puede pulsar **▲ Ocultar Formulario** para colapsar los campos de registro y ganar espacio vertical en pantalla completa, o **▼ Mostrar Formulario** para desplegarlo.
- **Registrar Nuevo Título**:
  1. Ingrese el código ISBN (ej. `978-0132350884`), Título, Autor, Categoría, Editorial, Año de publicación y la cantidad de copias iniciales a crear.
  2. Presione **+ Registrar Libro**. El sistema generará el libro y sus ejemplares físicos con códigos de barra secuenciales (ej. `L0001-001`, `L0001-002`).
- **Gestionar Ejemplares de un Libro**:
  1. Haga clic en un libro de la tabla superior.
  2. En la tabla inferior aparecerán todas sus copias físicas con su código de barras y estado actual.
  3. Puede presionar **+ Agregar Ejemplares** para sumar copias adicionales al título seleccionado.
  4. Seleccione una copia y presione **Cambiar Estado** para alternar entre `DISPONIBLE`, `EN_MANTENIMIENTO` o `BAJA`.

### 2.4 Sección «Configuración»
- Edite los 4 parámetros operativos del sistema:
  - **Tarifa de Multa por Día (S/)**: Costo en Soles por cada día de mora.
  - **Días Estándar de Préstamo**: Duración normal concedida.
  - **Límite Máximo de Préstamos**: Tope de libros prestados en simultáneo por lector.
  - **Días de Retención de Reserva**: Días de espera de un ejemplar devuelto para su recojo.
- Presione **Guardar Parámetros** y confirme los cambios en el diálogo modal.

### 2.5 Sección «Reportes Excel y Gráficos Estadísticos»
- Seleccione el tipo de reporte en el selector superior:
  1. *Préstamos Activos y Vencidos*
  2. *Multas Pendientes y Pagadas*
  3. *Libros Más Prestados (Top 10)*
  4. *Inventario de Ejemplares por Estado*
- **Visualización en 3 Pestañas Modernas**:
  - **📊 Visualización Gráfica**: Muestra el gráfico analítico en Java2D con barras verticales u horizontales, líneas guía de escala, badges de valores numéricos, porcentajes de cumplimiento y tarjetas de resumen KPI.
  - **📋 Tabla Detallada**: Muestra las columnas y registros completos con formato tabular limpio y filas alternadas.
  - **📑 Vista Dividida (Ambas)**: Presenta la gráfica analítica a la izquierda y la tabla de datos a la derecha al mismo tiempo, ideal para pantallas completas panorámicas.
- **Exportación a Excel (.xlsx)**:
  - Presione **Exportar a Excel (.xlsx)**, elija la carpeta y guarde el reporte profesional con estilos Apache POI. Si el archivo ya existe, el sistema le pedirá confirmación antes de sobrescribir.

---

## 3. Manual del Rol BIBLIOTECARIO

Al iniciar sesión como `biblio`, se abrirá el panel operativo con las herramientas de mostrador:

### 3.1 Módulo «Préstamos»
- **Emisión de Préstamos Paso a Paso**:
  1. Ingrese el **DNI del Lector** (8 dígitos).
  2. Ingrese o escanee el **Código de Barras del Ejemplar** (ej. `L0001-001`).
  3. Seleccione el **Plazo de Devolución**:
     - *1 día (Alta demanda / Express)*: Para libros con mucha rotación.
     - *3 días (Fin de semana / Reserva)*: Préstamos cortos prioritarios.
     - *7 días (Estándar recomendado)*: Préstamo regular.
     - *14 días (Investigación)*: Préstamo extendido.
  4. Presione **Emitir Préstamo**.
- **Comprobante Digital y Notificación Inmediata**:
  - Se abrirá automáticamente el **Ticket de Préstamo**:
    - Exhibe los datos del lector, obra y **la fecha límite de devolución requerida en grande y resaltada en color rojo**.
    - Botón **📱 Enviar WhatsApp / SMS**: Abre WhatsApp Web o prepara el mensaje para el número móvil del cliente con el recordatorio y la fecha límite exacta.
    - Botón **✉️ Enviar Correo Confirmación**: Envía la confirmación al correo del lector.
    - Botón **📋 Copiar Texto del Ticket**: Copia el ticket al portapapeles para su uso o impresión.
- **Renovación de Préstamo**:
  - Seleccione un préstamo activo de la tabla y presione **Renovar Préstamo Seleccionado** (máximo 1 renovación y solo si no hay reservas en cola).

### 3.2 Módulo «Devoluciones»
1. Ingrese el código de barras del libro devuelto y presione **Procesar Devolución**.
2. El sistema verifica la fecha pactada:
   - Si se devolvió a tiempo, se confirma la recepción y el libro queda disponible (o asignado a una reserva).
   - Si hubo retraso, se calcula la multa en el acto ($días \times S/ por día$).
3. Si se generó una multa, se habilita inmediatamente el botón **Cobrar Multa Generada** para registrar el pago en ese instante.

### 3.3 Módulo «Clientes» (Lectores)
- **Registrar Nuevo Lector**:
  1. Complete DNI, nombres, apellidos, correo y teléfono móvil.
  2. Presione **Guardar Cliente**.
  3. Aparecerá una ventana emergente mostrando la **Contraseña Temporal** generada aleatoriamente. Entréguesela al lector; al ingresar por primera vez el sistema le solicitará cambiarla.
- **Editar y Buscar Lectores**:
  - Utilice el buscador para filtrar clientes por DNI o apellidos.
  - Seleccione un cliente en la tabla para cargar sus datos en el formulario y actualizarlos.

### 3.4 Módulo «Reservas»
- Consulte la cola de espera de títulos agotados ordenada por llegada (FIFO).
- Puede registrar una nueva reserva para un lector o cancelar una reserva a solicitud del cliente.

### 3.5 Módulo «Multas»
- Visualice la lista de multas pendientes y canceladas.
- Seleccione cualquier multa pendiente y presione **Cobrar Multa Seleccionada** para liquidarla y dejar al cliente libre de penalizaciones.

---

## 4. Manual del Rol CLIENTE (Lector)

Al ingresar con el DNI y la contraseña personal:

### 4.1 «Inicio / Mi Resumen»
- Visualice sus métricas personales: cantidad de préstamos activos, reservas en espera y multas pendientes.

### 4.2 «Catálogo Bibliográfico»
- Busque libros por título o autor y filtre por categoría.
- **Selector de Vista**: Alterne entre **Vista Cuadrícula** (tarjetas visuales de libros) o **Vista Tabla** (listado compacto).
- Si un libro tiene `0 disponibles`, se activará el botón **Reservar Libro** para unirse a la cola de espera FIFO.

### 4.3 «Mis Préstamos»
- Revise las fechas de devolución de los libros que tiene en su poder.
- **Alerta Visual**: Si un préstamo se encuentra vencido, se destacará automáticamente con un badge de advertencia en color rojo.
- Si cumple las condiciones, puede presionar **Renovar Préstamo** para extender su plazo.

### 4.4 «Mis Reservas»
- Consulte si sus reservas se encuentran en espera (`PENDIENTE`) o listas para recojo en biblioteca (`LISTA`).
- Puede cancelar voluntariamente una reserva si ya no la necesita.

### 4.5 «Mis Multas»
- Revise su historial de sanciones, días de demora y confirmación de pagos efectuados.

### 4.6 «Cambiar Contraseña»
- Formulario de seguridad para actualizar su clave personal ingresando la contraseña actual y la nueva clave robusta.
