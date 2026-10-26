# Modelo de Datos de la Biblioteca

El sistema utiliza **MySQL 8** con motor de almacenamiento transaccional **InnoDB** y codificación de caracteres `utf8mb4_unicode_ci`. La integridad referencial se garantiza mediante claves foráneas con restricciones estrictas de integridad.

---

## 1. Diagrama Entidad-Relación (Mermaid)

```mermaid
erDiagram
    USUARIO ||--o| CLIENTE : "perfil lector"
    USUARIO ||--o{ PRESTAMO : "registrado por (bibliotecario)"
    CLIENTE ||--o{ PRESTAMO : "solicita"
    CLIENTE ||--o{ RESERVA : "crea"
    LIBRO ||--|{ EJEMPLAR : "posee copias"
    LIBRO ||--o{ RESERVA : "es reservado"
    EJEMPLAR ||--o{ PRESTAMO : "se presta"
    EJEMPLAR ||--o| RESERVA : "asignado para recojo"
    PRESTAMO ||--o| MULTA : "genera por mora"

    USUARIO {
        int id PK
        varchar(50) username UK
        varchar(100) password_hash
        enum rol
        varchar(120) nombre
        tinyint(1) activo
        tinyint(1) debe_cambiar_clave
        int intentos_fallidos
        datetime bloqueado_hasta
    }

    CLIENTE {
        int id PK
        int usuario_id FK,UK
        char(8) dni UK
        varchar(80) nombres
        varchar(80) apellidos
        varchar(120) email
        varchar(15) telefono
        date fecha_registro
    }

    LIBRO {
        int id PK
        varchar(20) isbn UK
        varchar(200) titulo
        varchar(150) autor
        varchar(80) categoria
        varchar(100) editorial
        int anio
    }

    EJEMPLAR {
        int id PK
        int libro_id FK
        varchar(30) codigo_barras UK
        enum estado
    }

    PRESTAMO {
        int id PK
        int cliente_id FK
        int ejemplar_id FK
        int bibliotecario_id FK
        date fecha_prestamo
        date fecha_vencimiento
        date fecha_devolucion
        enum estado
        int renovaciones
    }

    RESERVA {
        int id PK
        int cliente_id FK
        int libro_id FK
        int ejemplar_id FK
        datetime fecha_reserva
        date fecha_expiracion
        enum estado
    }

    MULTA {
        int id PK
        int prestamo_id FK,UK
        int dias_retraso
        decimal(10,2) tarifa_aplicada
        decimal(10,2) monto
        enum estado
        date fecha_pago
    }

    CONFIGURACION {
        varchar(50) clave PK
        varchar(100) valor
    }
```

---

## 2. Diccionario de Tablas y Enums

### 2.1 Tabla `usuario`
Almacena las credenciales y el estado de acceso de todos los usuarios del sistema.
- **`rol`**: `ENUM('ADMIN', 'BIBLIOTECARIO', 'CLIENTE')`.
- **`password_hash`**: Hash seguro BCrypt (longitud estándar 60 caracteres).
- **`debe_cambiar_clave`**: Indicador para obligar al usuario a renovar su contraseña en el primer ingreso.
- **`intentos_fallidos`** y **`bloqueado_hasta`**: Control de seguridad contra ataques de fuerza bruta (bloqueo por 5 minutos tras 5 intentos erróneos).

### 2.2 Tabla `cliente`
Almacena el perfil personal de los lectores de la biblioteca. Cada registro está vinculado uno a uno con un registro de la tabla `usuario` (`usuario_id`).
- **`dni`**: Clave natural única de 8 caracteres numéricos.

### 2.3 Tabla `libro`
Almacena el catálogo de títulos bibliográficos.
- **`isbn`**: Código comercial o estándar único del libro.

### 2.4 Tabla `ejemplar`
Representa cada unidad física tangible en las estanterías de la biblioteca.
- **`estado`**: `ENUM('DISPONIBLE', 'PRESTADO', 'RESERVADO', 'EN_MANTENIMIENTO', 'BAJA')`.

### 2.5 Tabla `prestamo`
Registra el ciclo de vida de un préstamo.
- **`estado`**: `ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO')`.
- **`renovaciones`**: Contador de extensiones permitidas (máximo 1 renovación si no hay reservas en cola).

### 2.6 Tabla `reserva`
Implementa una cola de espera FIFO cuando todos los ejemplares de un libro están ocupados.
- **`estado`**: `ENUM('PENDIENTE', 'LISTA', 'CUMPLIDA', 'EXPIRADA', 'CANCELADA')`.
  - `PENDIENTE`: Cliente en cola de espera.
  - `LISTA`: Ejemplar devuelto y apartado para este cliente durante el plazo de retención.
  - `CUMPLIDA`: El cliente recogió el libro y se registró el préstamo.
  - `EXPIRADA`: El cliente no recogió el libro dentro de los días permitidos; el ejemplar se pasa al siguiente en cola o se libera.
  - `CANCELADA`: El cliente o bibliotecario desistió de la reserva.

### 2.7 Tabla `multa`
Registra sanciones pecuniarias generadas automáticamente al registrar una devolución fuera de la fecha límite.
- **`tarifa_aplicada`**: Tarifa diaria vigente al momento exacto de la devolución.
- **`monto`**: `dias_retraso * tarifa_aplicada`.
- **`estado`**: `ENUM('PENDIENTE', 'PAGADA')`.

### 2.8 Tabla `configuracion`
Tabla clave-valor para parámetros dinámicos del sistema modificables solo por el rol `ADMIN`:
- `MULTA_POR_DIA`: Tarifa en Soles por día de mora (por defecto `1.00`).
- `DIAS_PRESTAMO`: Días de duración estándar de un préstamo (por defecto `7`).
- `MAX_PRESTAMOS`: Límite máximo de préstamos activos simultáneos por cliente (por defecto `3`).
- `DIAS_RETENCION_RESERVA`: Días que un ejemplar devuelto espera al lector de una reserva antes de expirar (por defecto `3`).
