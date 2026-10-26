# Matriz de Casos de Prueba y Cobertura (TDD)

El proyecto cuenta con una batería integral de **46 pruebas unitarias y de integración** desarrolladas bajo el estándar **JUnit 5** y **Mockito**, garantizando la cobertura funcional de las reglas de negocio, la seguridad criptográfica, la persistencia JDBC y la generación de reportes con Apache POI.

---

## 1. Resumen de Ejecución de Pruebas

| Capa / Módulo | Clase de Prueba | Pruebas | Estado | Tipo de Prueba |
| :--- | :--- | :---: | :---: | :--- |
| **Infraestructura** | `ConexionBDTest` | 2 | Aprobada | Integración MySQL |
| **Infraestructura** | `AppTest` | 1 | Aprobada | Integración Contexto |
| **Modelo** | `ModelTest` | 1 | Aprobada | Unitaria (POJOs y enums) |
| **Seguridad** | `PasswordHasherTest` | 3 | Aprobada | Criptografía BCrypt (costo 12) |
| **Seguridad** | `ValidadorTest` | 4 | Aprobada | Formatos DNI, clave, email |
| **Seguridad** | `SesionActualTest` | 3 | Aprobada | Singleton y RBAC |
| **Seguridad** | `AutorizadorTest` | 2 | Aprobada | Excepciones de rol |
| **Seguridad / Auth** | `AuthServiceTest` | 9 | Aprobada | Bloqueo por 5 intentos, clave |
| **Persistencia DAO** | `ClienteDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `ConfiguracionDAOTest`| 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `EjemplarDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `LibroDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `MultaDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `PrestamoDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `ReservaDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Persistencia DAO** | `UsuarioDAOTest` | 1 | Aprobada | MySQL con rollback |
| **Servicios de Negocio**| `ServiciosNegocioTest`| 7 | Aprobada | Reglas TDD obligatorias |
| **Reportes y POI** | `ExcelExporterTest` | 2 | Aprobada | Formatos POI y totales |
| **Reportes y Negocio** | `ReporteServiceTest` | 4 | Aprobada | Generación de 4 reportes |
| **Integración Docente**| `FlujoCompletoDocenteTest`| 1 | Aprobada | Flujo completo de negocio |
| **TOTAL** | **21 Clases de Prueba** | **46** | **100% OK** | **0 Fallos, 0 Errores** |

---

## 2. Detalle de Casos de Prueba Obligatorios de la Rúbrica

En la clase `ServiciosNegocioTest` se verifican formalmente los 7 casos críticos requeridos por la consigna académica:

### Caso 1: Devolución a tiempo (sin multa)
- **Escenario**: Préstamo con vencimiento posterior a la fecha de entrega.
- **Resultado Esperado**: Préstamo pasa a `DEVUELTO`, monto de multa = `0.00` y no se inserta ninguna multa.

### Caso 2: Devolución con retraso y tarifa de multa
- **Escenario**: Préstamo con 5 días de desfase y tarifa de `S/ 2.00` por día.
- **Resultado Esperado**: Multa generada en estado `PENDIENTE` por un valor exacto de `S/ 10.00` (`5 * 2.00`).

### Caso 3: Cambio de tarifa de configuración
- **Escenario**: El rol `ADMIN` actualiza `MULTA_POR_DIA` de `1.00` a `3.00`. Se procesa una devolución con retraso de 2 días.
- **Resultado Esperado**: La nueva multa se calcula aplicando la tarifa actualizada de `S/ 3.00`, arrojando `S/ 6.00`.

### Caso 4: Bloqueo de préstamo por multas pendientes
- **Escenario**: Cliente con al menos una multa no cancelada en estado `PENDIENTE` solicita un nuevo préstamo.
- **Resultado Esperado**: Se arroja `ValidacionException` impidiendo el registro del préstamo.

### Caso 5: Límite máximo de préstamos activos
- **Escenario**: El parámetro `MAX_PRESTAMOS` es 3. El cliente posee actualmente 3 préstamos activos e intenta solicitar un cuarto.
- **Resultado Esperado**: Se arroja `ValidacionException` notificando que alcanzó el cupo máximo permitido.

### Caso 6: Flujo completo de reserva con cola FIFO (2 Clientes)
- **Escenario**: Libro sin copias disponibles. El cliente A reserva (primero en cola) y luego el cliente B reserva (segundo en cola).
- **Proceso**:
  1. Se devuelve un ejemplar físico.
  2. El ejemplar se asigna al cliente A (`LISTA`), retenido por 3 días.
  3. El cliente A formaliza el préstamo (la reserva pasa a `CUMPLIDA`).
  4. Al devolver el libro nuevamente, se asigna al cliente B.
- **Resultado Esperado**: Secuencia FIFO estricta y transiciones de estado correctas (`PENDIENTE` -> `LISTA` -> `CUMPLIDA`).

### Caso 7: Control de acceso RBAC
- **Escenario**: Usuario con rol `CLIENTE` intenta invocar métodos de `BIBLIOTECARIO` o `ADMIN` (ej. `registrarPrestamo`, `cobrarMulta`, `actualizarConfiguracion`).
- **Resultado Esperado**: `Autorizador` arroja `AccesoDenegadoException` protegiendo las operaciones sensibles en la capa de negocio.

---

## 3. Pruebas de Transaccionalidad en Capa DAO

Todas las pruebas en el paquete `com.biblioteca.dao` heredan de `BaseDAOTest`:
- En `@BeforeEach` se inicia una conexión JDBC dedicada con `conn.setAutoCommit(false)`.
- El `GestorTransaccion` asocia la conexión al hilo de prueba.
- Se ejecutan operaciones CRUD reales contra la base MySQL `biblioteca`.
- En `@AfterEach` se invoca `conn.rollback()` y se cierra la conexión.
- **Garantía**: Los datos de prueba jamás ensucian la base de datos de producción o desarrollo.
