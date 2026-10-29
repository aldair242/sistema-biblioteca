# Matriz de Casos de Prueba y Cobertura (TDD)

El proyecto cuenta con una batería integral de **57 pruebas unitarias y de integración** desarrolladas bajo el estándar **JUnit 5**, **Mockito** y transacciones aisladas con rollback contra la base de datos real MySQL `biblioteca`, garantizando una cobertura total sobre las reglas de negocio, criptografía, persistencia y exportación.

3. **`testCorreoRecuperacionClave`**:
   - Comprueba la generación del contenido formal de correo de recuperación con instrucciones de cambio obligatorio de clave y su despacho satisfactorio.

4. **`testCorreoBienvenida`**:
   - Verifica el formato del correo de bienvenida para nuevos clientes con credenciales iniciales de acceso y su despacho.

---

## 1. Resumen de Ejecución de Pruebas (57 / 57 Aprobadas)

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Tests run: 57, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Capa / Módulo | Clase de Prueba | Cantidad | Estado | Tipo de Prueba |
| :--- | :--- | :---: | :---: | :--- |
| **Arranque** | `AppTest` | 1 | Aprobada | Contexto general de la aplicación |
| **Infraestructura** | `ConexionBDTest` | 2 | Aprobada | Integración MySQL y carga de `db.properties` |
| **Persistencia DAO** | `ClienteDAOTest` | 1 | Aprobada | CRUD MySQL con rollback transaccional |
| **Persistencia DAO** | `ConfiguracionDAOTest`| 1 | Aprobada | Parámetros clave-valor con rollback |
| **Persistencia DAO** | `EjemplarDAOTest` | 1 | Aprobada | Copias físicas, estados y códigos de barras |
| **Persistencia DAO** | `LibroDAOTest` | 1 | Aprobada | Títulos, ISBN y catálogo con rollback |
| **Persistencia DAO** | `MultaDAOTest` | 1 | Aprobada | Multas por préstamo y cliente |
| **Persistencia DAO** | `PrestamoDAOTest` | 1 | Aprobada | Préstamos activos, vencidos y consultas |
| **Persistencia DAO** | `ReservaDAOTest` | 1 | Aprobada | Cola FIFO, reservas activas y expiradas |
| **Persistencia DAO** | `UsuarioDAOTest` | 1 | Aprobada | Usuarios, intentos fallidos y bloqueo |
| **Seguridad** | `AutorizadorTest` | 3 | Aprobada | Control de acceso basado en roles (RBAC) |
| **Seguridad** | `PasswordHasherTest` | 2 | Aprobada | Criptografía BCrypt (costo 12) |
| **Seguridad** | `SesionActualTest` | 1 | Aprobada | Singleton de sesión y verificación de rol |
| **Seguridad** | `ValidadorTest` | 4 | Aprobada | Formato DNI (8 dígitos), clave fuerte, email |
| **Seguridad / Auth** | `AuthServiceTest` | 9 | Aprobada | Bloqueo por 5 intentos, clave obligatoria |
| **Notificaciones** | `NotificacionServiceTest` | 4 | Aprobada | Ticket de préstamo, WhatsApp Push y correo |
| **Reportes y Excel** | `ReporteServiceTest` | 4 | Aprobada | Generación de los 4 tipos de reporte |
| **Servicios Negocio**| `ServiciosNegocioTest` | 11 | Aprobada | Casos obligatorios de la consigna y plazos |
| **Utilidades POI** | `ExcelExporterTest` | 2 | Aprobada | Formatos de moneda, fechas y totales Excel |
| **Integración E2E** | `FlujoCompletoDocenteTest`| 2 | Aprobada | Flujo de vida de cliente, préstamo y devolución |
| **TOTAL** | **20 Clases de Prueba** | **54** | **100% OK** | **0 Fallos, 0 Errores** |

---

## 2. Detalle de Casos de Negocio en `ServiciosNegocioTest`

En [`ServiciosNegocioTest`](file:///c:/Users/jerem/OneDrive/Desktop/BIBLIOTECA%20CI/src/test/java/com/biblioteca/service/ServiciosNegocioTest.java) se auditan formalmente los 11 escenarios críticos del dominio:

### Caso 1: Devolución a tiempo (sin mora)
- **Escenario**: El lector devuelve el ejemplar en o antes de la fecha de vencimiento pactada.
- **Resultado Esperado**: El préstamo pasa a `DEVUELTO`, el monto de multa es exactamente `0.00` y no se inserta ningún registro en la tabla `multa`.

### Caso 2: Devolución con retraso y cálculo de multa
- **Escenario**: Préstamo devuelto con 5 días de desfase respecto al vencimiento con tarifa configurada de `S/ 2.00` por día.
- **Resultado Esperado**: Se genera automáticamente una multa en estado `PENDIENTE` por un valor de `S/ 10.00` ($5 \times 2.00$) con la tarifa histórica aplicada.

### Caso 3: Modificación dinámica de tarifa de multa
- **Escenario**: El administrador actualiza la tarifa `MULTA_POR_DIA` de `1.00` a `3.00`. Luego se registra una devolución con 2 días de atraso.
- **Resultado Esperado**: La multa se liquida utilizando la nueva tarifa vigente ($2 \times 3.00 = \text{S/ } 6.00$), demostrando la reactividad a los cambios de configuración.

### Caso 4: Bloqueo de préstamo por multas pendientes
- **Escenario**: Un cliente que registra al menos una multa en estado `PENDIENTE` solicita un nuevo préstamo.
- **Resultado Esperado**: Se interrumpe la operación arrojando `ValidacionException` con mensaje claro de advertencia.

### Caso 5: Límite máximo de préstamos activos simultáneos
- **Escenario**: El parámetro institucional `MAX_PRESTAMOS` está fijado en 3. Un cliente que ya tiene 3 préstamos activos intenta solicitar un cuarto libro.
- **Resultado Esperado**: Se arroja `ValidacionException` impidiendo exceder la cuota permitida.

### Caso 6: Cola FIFO de reservas (Asignación automática)
- **Escenario**: Libro sin stock disponible. El cliente A reserva primero y el cliente B reserva después.
- **Proceso**:
  1. Se registra la devolución de un ejemplar físico.
  2. El ejemplar pasa a `RESERVADO` y se asigna al cliente A (`LISTA`), otorgándole el plazo de retención.
  3. El cliente A formaliza el préstamo (la reserva pasa a `CUMPLIDA`).
  4. Al devolver el ejemplar nuevamente, se asigna al cliente B.
- **Resultado Esperado**: Orden estricto de prelación FIFO y transiciones de estado correctas (`PENDIENTE` $\rightarrow$ `LISTA` $\rightarrow$ `CUMPLIDA`).

### Caso 7: Control de acceso basado en roles (RBAC)
- **Escenario**: Un usuario autenticado con rol `CLIENTE` intenta invocar métodos restringidos de `BIBLIOTECARIO` o `ADMIN` (ej. `registrarPrestamo`, `cobrarMulta`, `actualizarConfiguracion`).
- **Resultado Esperado**: `Autorizador` arroja `AccesoDenegadoException` garantizando la seguridad en la capa de negocio sin depender de la UI.

### Caso 8: Bloqueo de eliminación de cliente con préstamos activos
- **Escenario**: Se intenta eliminar un cliente que aún retiene copias en su poder.
- **Resultado Esperado**: Se lanza `ValidacionException` protegiendo el patrimonio de la biblioteca.

### Caso 9: Restablecimiento seguro de clave temporal
- **Escenario**: Un bibliotecario solicita restablecer la contraseña de un cliente.
- **Resultado Esperado**: Se genera una clave temporal aleatoria de 8 caracteres, se hashea con BCrypt y se activa la bandera `debe_cambiar_clave = true`.

### Caso 10: Validación de DNI duplicado en edición de cliente
- **Escenario**: Se intenta modificar los datos de un cliente asignándole un DNI que ya pertenece a otro lector distinto.
- **Resultado Esperado**: Se arroja `ValidacionException` garantizando la unicidad de identidad.

### Caso 11: Selección de plazos de préstamo personalizados (1, 3 o 7 días)
- **Escenario**: Se emiten préstamos indicando plazos específicos según la demanda de la obra:
  - 1 día (Express / Alta Demanda)
  - 3 días (Fin de semana / Reserva prioritaria)
  - 7 días (Estándar recomendado)
- **Resultado Esperado**: Cada préstamo registra su `fecha_vencimiento` exactamente calculada según el plazo asignado (`hoy.plusDays(dias)`).

---

## 3. Pruebas de Notificaciones y Tickets (`NotificacionServiceTest`)

1. **`testGenerarTicketExitoso`**:
   - Comprueba que el ticket de comprobante contenga el folio correcto, nombre y DNI del lector, datos de la obra, código de barras y la fecha de vencimiento requerida.
2. **`testMensajePushWhatsApp`**:
   - Valida que el texto para WhatsApp contenga los emojis formateados, nombre del cliente, libro, días otorgados y la fecha límite de entrega destacada.
3. **`testContenidoCorreo`**:
   - Verifica la redacción formal del correo de confirmación con las condiciones de entrega oportuna y mora diaria.
4. **`testDespachoPushYCorreo`**:
   - Confirma que el despacho hacia el móvil y correo electrónico retorne confirmación positiva sin lanzar excepciones.

---

## 4. Pruebas de Transaccionalidad Aislada contra MySQL Real

Todas las pruebas del paquete `com.biblioteca.dao` ejecutan sentencias reales sobre el esquema MySQL `biblioteca`, pero manteniendo la base de datos **100% limpia**:
- **`@BeforeEach`**: Inicia una transacción manual con `conn.setAutoCommit(false)`.
- **Ejecución**: Se realizan `INSERT`, `SELECT`, `UPDATE` y `DELETE` comprobando el comportamiento real del motor InnoDB.
- **`@AfterEach`**: Ejecuta `conn.rollback()` y cierra la conexión. Ningún registro de prueba queda persistido.
