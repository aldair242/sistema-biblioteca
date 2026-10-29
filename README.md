# Sistema de Gestión de Préstamos y Devoluciones de Biblioteca

Sistema de escritorio integral desarrollado en **Java 17** con arquitectura **MVC**, patrón **DAO**, principios **SOLID**, seguridad criptográfica **BCrypt** y desarrollo guiado por pruebas (**TDD**). Administra préstamos, devoluciones, cálculo automatizado de multas por mora, cola de reservas FIFO, control de ejemplares físicos, generación de reportes en Excel (.xlsx) y control de acceso basado en roles (**ADMIN**, **BIBLIOTECARIO**, **CLIENTE**).

---

## 📌 Requisitos del Sistema

- **Java Development Kit (JDK)**: Versión 17 o superior (compatible con Java 21).
- **Apache Maven**: Versión 3.8.0 o superior.
- **MySQL Server**: Versión 8.0 o superior.
- **Entorno de Desarrollo**: Visual Studio Code con *Extension Pack for Java*

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
- [**Casos de Prueba (TDD)**](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/docs/casos-de-prueba.md): Resumen de las 57 pruebas JUnit 5 + Mockito y pruebas con rollback.

---

## 📦 Integración de las 4 Librerías Requeridas por la Rúbrica

1. **Google Guava (`33.2.1-jre`)**: `Multimap` para agrupamiento multinivel en reportes de inventario, validaciones con `Preconditions` y medición de tiempos con `Stopwatch`.
2. **Apache POI (`5.2.5`)**: Creación de libros Excel `.xlsx` estilizados con bordes, cabeceras con color institucional, formato de moneda peruana (`S/ #,##0.00`), fechas y totales generales.
3. **Apache Commons (`Lang3:3.14.0`, `IO:2.16.1`)**: Validación de cadenas con `StringUtils` y `Validate`; normalización y comprobación segura de rutas de archivos con `FilenameUtils`.
4. **Logback Classic (`1.5.6`)**: Sistema de logging estructurado con appenders en consola y archivo rotativo diario (`logs/biblioteca.log`), asegurando total confidencialidad de contraseñas.

---

## 🖥️ Sistema de Diseño Visual y Experiencia de Usuario (Java Swing + FlatLaf)

La interfaz gráfica del sistema ha sido rediseñada integralmente bajo un **Sistema de Diseño Moderno** profesional, ergonómico y cohesivo, implementado en `com.biblioteca.view.theme.Tema` y componentes visuales reutilizables:

### Paleta Cromática y Tokens de Diseño
- **Primario**: Azul Índigo (`#3F51B5`), utilizado en identidad de marca, navegación activa y acciones primarias.
- **Acento / Éxito**: Verde bosque (`#2E7D32`), para confirmaciones, guardado y préstamos exitosos.
- **Peligro / Vencidos**: Rojo carmesí (`#C62828`), para moras, bajas y eliminaciones.
- **Advertencia**: Ámbar cálido (`#F9A825`), para alertas y estados en mantenimiento.
- **Superficie y Fondo**: Fondo general (`#F5F7FA`) con tarjetas blancas puras (`#FFFFFF`) con bordes sutiles y sombra flotante.
- **Tipografía y Geometría**: Tipografía fluida con fallback (`Segoe UI`, `Inter`, `SansSerif`), esquinas redondeadas consistentes (radio arc de 12 a 14 px) y scrollbars minimalistas.

### Componentes Reutilizables de Alta Fidelidad (`com.biblioteca.view.componentes`)
- `BarraNavegacionLateral`: Barra lateral oscura (`#1E293B`) con isotipo, botones interactivos con indicador de selección índigo y conmutación fluida mediante `CardLayout`.
- `BarraSuperiorModerna`: Cabecera unificada con título/subtítulo dinámico, avatar circular con iniciales del usuario, badge de rol y botón de cierre de sesión con confirmación.
- `TarjetaPanel`: Tarjeta blanca con bordes redondeados, borde suave (`#E2E8F0`) y efecto de elevación.
- `TarjetaMetrica`: KPI Cards para dashboards que muestran métricas cuantitativas clave en tiempo real con íconos vectoriales en círculos tintados.
- `TablaEstilizada`: Tablas modernas sin líneas verticales, altura de fila ergonómica (36 px), cabecera estilizada, renderizador de chips/pastillas de colores para estados (`DISPONIBLE`, `PRESTADO`, `ACTIVO`, `DEVUELTO`, `VENCIDO`, `PAGADA`, `PENDIENTE`, `RESERVADO`, `LISTA`, `BAJA`) y mensajes amigables cuando no hay registros.
- `CampoTexto` y `CampoPassword`: Entradas con esquinas redondeadas, placeholder contextual, validación visual de error con contorno carmesí y botón de visualización conmutable de contraseñas.
- `DialogoModerno`: Modales personalizados con íconos vectoriales SVG/2D, atajos `Enter`/`Escape` y botones estilizados que sustituyen a los `JOptionPane` planos tradicionales.
- `Iconos`: Fábrica de íconos vectoriales Java2D con renderizado anti-aliased nítido en cualquier resolución.

### Pantallas Renovadas
1. **Acceso al Sistema (`LoginView`)**:
   - Diseño split-screen profesional: panel izquierdo con gradiente institucional índigo, ilustración y resumen de funcionalidades; panel derecho con tarjeta flotante de autenticación.
   - Visor conmutable de contraseña (ícono de ojo), banner de error no intrusivo y atajo `Enter`.
   - Diálogo modal con diseño oscuro-claro para cambio forzoso de credenciales temporales.
2. **Panel de Administración (`AdminView`)**:
   - Navegación lateral con 5 vistas: **Dashboard**, **Bibliotecarios**, **Catálogo**, **Configuración** y **Reportes**.
   - Dashboard administrativo con 4 tarjetas KPI (Total Libros, Total Ejemplares, Préstamos en Curso, Multas Pendientes).
   - Gestión integral de bibliotecarios, catálogo maestro-detalle y exportación de reportes a Excel con selector de rutas seguro.
3. **Panel de Atención de Biblioteca (`BibliotecarioView`)**:
   - Navegación lateral: **Dashboard Operativo**, **Préstamos**, **Devoluciones**, **Clientes**, **Catálogo**, **Reservas** y **Multas**.
   - Dashboard con contadores en tiempo real para atención ágil.
   - Panel de Devoluciones con tarjeta de comprobante detallado, cálculo en vivo de moras y liquidación de sanciones.
4. **Portal del Lector (`ClienteView`)**:
   - Navegación lateral: **Mi Panel**, **Explorar Catálogo**, **Mis Préstamos**, **Mis Reservas**, **Mis Multas** y **Seguridad**.
   - Explorador de catálogo con selector de vista dual: **Cuadrícula de tarjetas de libros** o **Tabla tabular detallada**, con buscador en vivo y filtro por categorías.
   - Gestión autónoma de reservas y renovaciones de préstamos vigentes con un solo clic.

---
