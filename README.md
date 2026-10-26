# Sistema de Gestión de Préstamos y Devoluciones de Biblioteca

Sistema de escritorio integral desarrollado en **Java 17** con arquitectura **MVC**, patrón **DAO**, principios **SOLID**, seguridad criptográfica **BCrypt** y desarrollo guiado por pruebas (**TDD**). Administra préstamos, devoluciones, cálculo automatizado de multas por mora, cola de reservas FIFO, control de ejemplares físicos, generación de reportes en Excel (.xlsx) y control de acceso basado en roles (**ADMIN**, **BIBLIOTECARIO**, **CLIENTE**).

Proyecto académico para la evaluación de **Avance de Proyecto Final 3 (UTP)**.

---

## 📌 Requisitos del Sistema

- **Java Development Kit (JDK)**: Versión 17 o superior (compatible con Java 21).
- **Apache Maven**: Versión 3.8.0 o superior.
- **MySQL Server**: Versión 8.0 o superior.
- **Entorno de Desarrollo**: Visual Studio Code con *Extension Pack for Java* o Antigravity IDE.

---

## ⚙️ Configuración Inicial

### 1. Base de Datos MySQL
1. Inicie el servicio de MySQL 8.
2. Ejecute el script `database/biblioteca.sql` en MySQL Workbench o consola MySQL:
   - Crea la base de datos `biblioteca` con cotejamiento `utf8mb4_unicode_ci`.
   - Crea las 8 tablas relacionales con restricciones de clave foránea e índices optimizados.
   - Crea el usuario de aplicación `biblioteca_app` con permisos mínimos (`SELECT`, `INSERT`, `UPDATE`, `DELETE`).
   - Inserta parámetros de configuración y catálogo demo.

### 2. Configuración de Credenciales
1. Copie el archivo plantilla:
   ```bash
   cp db.properties.example src/main/resources/db.properties
   ```
2. Modifique la clave en `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Lima
   db.user=biblioteca_app
   db.password=Cambiar_Esta_Clave_123!
   ```

---

## 🚀 Compilación y Ejecución

### Ejecutar Todas las Pruebas Unitarias y de Integración (TDD)
```bash
mvn clean test
```

### Empaquetar JAR Ejecutable (Fat JAR con Maven Shade)
```bash
mvn clean package
```

### Ejecutar la Aplicación
```bash
java -jar target/biblioteca-1.0.0.jar
```
O directamente desde Maven:
```bash
mvn compile exec:java -Dexec.mainClass="com.biblioteca.App"
```

### Generar Documentación Javadoc
```bash
mvn javadoc:javadoc
```
*(Los archivos HTML generados se ubican en `target/site/apidocs/index.html`)*

---

## 👥 Usuarios de Prueba

| Rol | Usuario / DNI | Contraseña | Alcance Operativo |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `Admin123!` | CRUD bibliotecarios, catálogo, configuración global (multa, días), reportes Excel. |
| **BIBLIOTECARIO** | `biblio` | `Biblio123!` | Registro de clientes, catálogo, préstamos, devoluciones, reservas, cobro de multas. |
| **CLIENTE** | `12345678` | `Cliente123!` | Consulta de catálogo y stock, reserva de libros, mis préstamos y multas, cambio de clave. |

---

## 📚 Documentación Técnica Detallada

Consulte la carpeta [`docs/`](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs) para ver la especificación completa del sistema:
- [**Arquitectura de Capas**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/arquitectura.md): Diagrama Mermaid, MVC, DAO, SOLID y descripción de las 4 librerías.
- [**Modelo de Datos**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/modelo-datos.md): Diagrama Entidad-Relación Mermaid, tablas, llaves e integridad referencial.
- [**Reglas de Negocio**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/reglas-negocio.md): Fórmulas de cálculo de multas, validaciones de préstamo y cola de reservas FIFO.
- [**Manual de Usuario**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/manual-usuario.md): Guía operativa detallada paso a paso para cada uno de los 3 roles.
- [**Casos de Prueba (TDD)**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/casos-de-prueba.md): Resumen de las 45 pruebas JUnit 5 + Mockito y pruebas con rollback.

---

## 📦 Integración de las 4 Librerías Requeridas por la Rúbrica

1. **Google Guava (`33.2.1-jre`)**: `Multimap` para agrupamiento multinivel en reportes de inventario, validaciones con `Preconditions` y medición de tiempos con `Stopwatch`.
2. **Apache POI (`5.2.5`)**: Creación de libros Excel `.xlsx` estilizados con bordes, cabeceras con color institucional, formato de moneda peruana (`S/ #,##0.00`), fechas y totales generales.
3. **Apache Commons (`Lang3:3.14.0`, `IO:2.16.1`)**: Validación de cadenas con `StringUtils` y `Validate`; normalización y comprobación segura de rutas de archivos con `FilenameUtils`.
4. **Logback Classic (`1.5.6`)**: Sistema de logging estructurado con appenders en consola y archivo rotativo diario (`logs/biblioteca.log`), asegurando total confidencialidad de contraseñas.

---

## 👥 Equipo de Desarrollo y Roles

- **Líder de Proyecto & Arquitecto de Software**: Diseño arquitectónico MVC/DAO/SOLID, configuración Maven, seguridad BCrypt y control transaccional.
- **Especialista en Persistencia & Base de Datos**: Modelado relacional en MySQL 8, DDL/DML, implementación JDBC con PreparedStatement y pruebas DAO con rollback.
- **Desarrollador de Lógica de Negocio & TDD**: Servicios de negocio (Préstamos, Devoluciones, Reservas FIFO, Multas) y pruebas unitarias con JUnit 5/Mockito.
- **Desarrollador Frontend Swing & UX**: Interfaces gráficas Java Swing con FlatLaf, controladores MVC y validación visual.
- **Especialista en Integraciones & QA**: Reportes en Apache POI, utilidades Guava/Commons, logging con Logback y documentación técnica Javadoc.
