-- biblioteca.sql | MySQL 8.0+
CREATE DATABASE IF NOT EXISTS biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca;

CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol ENUM('ADMIN','BIBLIOTECARIO','CLIENTE') NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    debe_cambiar_clave TINYINT(1) NOT NULL DEFAULT 0,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta DATETIME NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL UNIQUE,
    dni CHAR(8) NOT NULL UNIQUE,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    email VARCHAR(120),
    telefono VARCHAR(15),
    fecha_registro DATE NOT NULL,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS libro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    titulo VARCHAR(200) NOT NULL,
    autor VARCHAR(150) NOT NULL,
    categoria VARCHAR(80),
    editorial VARCHAR(100),
    anio INT,
    INDEX idx_libro_titulo (titulo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ejemplar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    libro_id INT NOT NULL,
    codigo_barras VARCHAR(30) NOT NULL UNIQUE,
    estado ENUM('DISPONIBLE','PRESTADO','RESERVADO','EN_MANTENIMIENTO','BAJA') NOT NULL DEFAULT 'DISPONIBLE',
    CONSTRAINT fk_ejemplar_libro FOREIGN KEY (libro_id) REFERENCES libro(id),
    INDEX idx_ejemplar_libro_estado (libro_id, estado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS prestamo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    ejemplar_id INT NOT NULL,
    bibliotecario_id INT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    fecha_devolucion DATE NULL,
    estado ENUM('ACTIVO','DEVUELTO','VENCIDO') NOT NULL DEFAULT 'ACTIVO',
    renovaciones INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_prestamo_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_prestamo_ejemplar FOREIGN KEY (ejemplar_id) REFERENCES ejemplar(id),
    CONSTRAINT fk_prestamo_bibliotecario FOREIGN KEY (bibliotecario_id) REFERENCES usuario(id),
    INDEX idx_prestamo_cliente_estado (cliente_id, estado),
    INDEX idx_prestamo_ejemplar_estado (ejemplar_id, estado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS reserva (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    libro_id INT NOT NULL,
    ejemplar_id INT NULL,
    fecha_reserva DATETIME NOT NULL,
    fecha_expiracion DATE NULL,
    estado ENUM('PENDIENTE','LISTA','CUMPLIDA','EXPIRADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_reserva_libro FOREIGN KEY (libro_id) REFERENCES libro(id),
    CONSTRAINT fk_reserva_ejemplar FOREIGN KEY (ejemplar_id) REFERENCES ejemplar(id),
    INDEX idx_reserva_libro_estado (libro_id, estado, fecha_reserva)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS multa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL UNIQUE,
    dias_retraso INT NOT NULL,
    tarifa_aplicada DECIMAL(10,2) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    estado ENUM('PENDIENTE','PAGADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_pago DATE NULL,
    CONSTRAINT fk_multa_prestamo FOREIGN KEY (prestamo_id) REFERENCES prestamo(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS configuracion (
    clave VARCHAR(50) PRIMARY KEY,
    valor VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Usuario de la aplicacion con permisos minimos (CAMBIA LA CLAVE)
CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost' IDENTIFIED BY 'Cambiar_Esta_Clave_123!';
GRANT SELECT, INSERT, UPDATE, DELETE ON biblioteca.* TO 'biblioteca_app'@'localhost';
FLUSH PRIVILEGES;

-- Parametros editables por el ADMIN
INSERT IGNORE INTO configuracion(clave, valor) VALUES
('MULTA_POR_DIA', '1.00'), ('DIAS_PRESTAMO', '7'),
('MAX_PRESTAMOS', '3'), ('DIAS_RETENCION_RESERVA', '3');

-- Usuarios demo (hash BCrypt)
INSERT IGNORE INTO usuario(username, password_hash, rol, nombre) VALUES
('admin', '$2a$10$u87b9FlgJ6JeHjBEsIVSROXFW6SVAmvfssuduVKyfOaZLyIOD6S/G', 'ADMIN', 'Administrador del Sistema'),
('biblio', '$2a$10$NvtcqStjIZdeGCyrBAc0Xuz6eemr2r0vscFe0J9BwUqTp/oEcaBie', 'BIBLIOTECARIO', 'Bibliotecario Demo'),
('12345678', '$2a$10$ABThFn.PC7jAPZUZlKoi8.IgRMJgYWHlIt5exrA12UHR3A/zWqutW', 'CLIENTE', 'Ana Torres Quispe');

INSERT IGNORE INTO cliente(usuario_id, dni, nombres, apellidos, email, telefono, fecha_registro)
SELECT id, '12345678', 'Ana', 'Torres Quispe', 'ana.torres@example.com', '987654321', CURDATE()
FROM usuario WHERE username = '12345678';

-- Catalogo demo
INSERT IGNORE INTO libro(isbn, titulo, autor, categoria, editorial, anio) VALUES
('9780132350884', 'Clean Code', 'Robert C. Martin', 'Programacion', 'Prentice Hall', 2008),
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Programacion', 'Addison-Wesley', 2018),
('9780201633610', 'Design Patterns', 'Erich Gamma', 'Ingenieria de Software', 'Addison-Wesley', 1994),
('9780321125217', 'Domain-Driven Design', 'Eric Evans', 'Ingenieria de Software', 'Addison-Wesley', 2003),
('9780135957059', 'The Pragmatic Programmer', 'Andrew Hunt', 'Programacion', 'Addison-Wesley', 2019),
('9780307474728', 'Cien anios de soledad', 'Gabriel Garcia Marquez', 'Novela', 'Sudamericana', 1967),
('9788437604947', 'Don Quijote de la Mancha', 'Miguel de Cervantes', 'Clasicos', 'Catedra', 1605),
('9786124142222', 'La ciudad y los perros', 'Mario Vargas Llosa', 'Novela', 'Alfaguara', 1963);

-- 2 o 3 ejemplares por libro, con codigo L0001-001, L0001-002, ...
INSERT IGNORE INTO ejemplar(libro_id, codigo_barras, estado)
SELECT l.id, CONCAT('L', LPAD(l.id, 4, '0'), '-', LPAD(n.n, 3, '0')), 'DISPONIBLE'
FROM libro l CROSS JOIN (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3) n
WHERE n.n <= 2 + (l.id MOD 2);

