package com.biblioteca;

import com.biblioteca.config.DatabaseCheck;
import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.model.*;
import com.biblioteca.security.SesionActual;
import com.biblioteca.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de verificación integral docente (Prompt 11):
 * Recorre el flujo completo de negocio de punta a punta contra la base de datos real:
 * 1. Login ADMIN y actualización de tarifa de multa a 2.00.
 * 2. Login BIBLIOTECARIO y registro de nuevo cliente con credencial temporal.
 * 3. Registro de libro con 2 ejemplares.
 * 4. Registro de préstamo de ejemplar.
 * 5. Simulación de retraso en devolución y cálculo de multa con tarifa 2.00.
 * 6. Cobranza y liquidación de multa.
 * 7. Agotamiento de ejemplares y prueba de cola de reserva FIFO.
 * 8. Login CLIENTE, validación de debe_cambiar_clave y cambio de clave exitoso.
 */
@DisplayName("Verificación Final Docente - Recorrido Integral de Negocio")
public class FlujoCompletoDocenteTest {

    private ServiceFactory sf;

    @BeforeEach
    void setUp() throws Exception {
        Assumptions.assumeTrue(DatabaseCheck.verificarConexion(), "MySQL no está disponible para la prueba integral");
        sf = ServiceFactory.getInstancia();
        GestorTransaccion.iniciarTransaccionManual();
    }

    @AfterEach
    void tearDown() {
        GestorTransaccion.revertirTransaccionManual();
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("Flujo Completo Docente: Admin -> Config -> Biblio -> Cliente -> Prestamo -> Multa -> Cobro -> Reserva -> Auth")
    void ejecutarRecorridoCompletoDocente() {
        // =========================================================================
        // PASO 1: Login ADMIN -> Cambiar multa por día a 2.00
        // =========================================================================
        Usuario admin = sf.getAuthService().login("admin", "Admin123!");
        assertNotNull(admin);
        assertEquals(Rol.ADMIN, admin.getRol());
        SesionActual.iniciarSesion(admin);

        ConfiguracionService configService = sf.getConfiguracionService();
        configService.actualizar(ConfiguracionService.CLAVE_MULTA_POR_DIA, "2.00");
        assertEquals(new BigDecimal("2.00"), configService.obtenerMultaPorDia(), "La tarifa debe ser 2.00");

        // =========================================================================
        // PASO 2: Login BIBLIOTECARIO -> Registrar cliente con DNI y clave temporal
        // =========================================================================
        Usuario biblio = sf.getAuthService().login("biblio", "Biblio123!");
        assertNotNull(biblio);
        assertEquals(Rol.BIBLIOTECARIO, biblio.getRol());
        SesionActual.iniciarSesion(biblio);

        String dniPrueba = "78965412";
        Cliente nuevoCliente = new Cliente(null, null, dniPrueba, "Carlos", "Mendoza Ramos", "carlos.mendoza@test.com", "999888777", LocalDate.now());
        String claveTemporal = sf.getClienteService().registrarCliente(nuevoCliente);
        assertNotNull(claveTemporal);
        assertTrue(claveTemporal.length() >= 8);

        // =========================================================================
        // PASO 3: Registrar libro con 2 ejemplares
        // =========================================================================
        CatalogoService catService = sf.getCatalogoService();
        String isbn = "9780132350899";
        Libro libro = new Libro(null, isbn, "Refactoring", "Martin Fowler", "Software", "Addison-Wesley", 2019);
        Libro libroGuardado = catService.registrarLibro(libro, 2);
        assertNotNull(libroGuardado.getId());

        List<Ejemplar> ejemplares = catService.listarEjemplaresPorLibro(libroGuardado.getId());
        assertEquals(2, ejemplares.size(), "Debe haberse generado exactamente 2 ejemplares");
        Ejemplar ejemplar1 = ejemplares.get(0);
        Ejemplar ejemplar2 = ejemplares.get(1);
        assertEquals(EstadoEjemplar.DISPONIBLE, ejemplar1.getEstado());
        assertEquals(EstadoEjemplar.DISPONIBLE, ejemplar2.getEstado());

        // =========================================================================
        // PASO 4: Préstamo del ejemplar 1 al cliente
        // =========================================================================
        PrestamoService prestamoService = sf.getPrestamoService();
        Prestamo prestamo = prestamoService.registrarPrestamo(dniPrueba, ejemplar1.getCodigoBarras());
        assertNotNull(prestamo.getId());
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());

        Ejemplar ejemplarPrestado = sf.getEjemplarDAO().buscarPorId(ejemplar1.getId()).orElseThrow();
        assertEquals(EstadoEjemplar.PRESTADO, ejemplarPrestado.getEstado());

        // =========================================================================
        // PASO 5: Forzar vencimiento (hace 4 días) y devolver con retraso (Tarifa 2.00)
        // =========================================================================
        prestamo.setFechaVencimiento(LocalDate.now().minusDays(4));
        sf.getPrestamoDAO().actualizar(prestamo);

        PrestamoService.ResultadoDevolucion resDevolucion = prestamoService.registrarDevolucion(ejemplar1.getCodigoBarras());
        assertNotNull(resDevolucion);
        assertEquals(EstadoPrestamo.DEVUELTO, resDevolucion.getPrestamo().getEstado());
        assertNotNull(resDevolucion.getMulta(), "Debe haberse generado una multa por retraso");

        Multa multa = resDevolucion.getMulta();
        assertEquals(4, multa.getDiasRetraso(), "Días de retraso esperados: 4");
        assertEquals(new BigDecimal("2.00"), multa.getTarifaAplicada(), "La tarifa aplicada debe ser 2.00");
        // 4 días x 2.00 = S/ 8.00
        assertEquals(new BigDecimal("8.00"), multa.getMonto(), "Monto de multa calculado esperado: 8.00");
        assertEquals(EstadoMulta.PENDIENTE, multa.getEstado());

        // =========================================================================
        // PASO 6: Cobrar multa
        // =========================================================================
        MultaService multaService = sf.getMultaService();
        multaService.cobrar(multa.getId());
        Multa multaCobrada = multaService.buscarPorId(multa.getId()).orElseThrow();
        assertEquals(EstadoMulta.PAGADA, multaCobrada.getEstado());
        assertNotNull(multaCobrada.getFechaPago());

        // =========================================================================
        // PASO 7: Ocupar ejemplar restante y probar cola de reserva FIFO
        // =========================================================================
        // Prestar ejemplar 1 y ejemplar 2 a un cliente demo existente ('12345678')
        // Primero aseguramos que el cliente 78965412 toma ejemplar 1 nuevamente
        prestamoService.registrarPrestamo(dniPrueba, ejemplar1.getCodigoBarras());
        // Y el cliente demo 12345678 toma el ejemplar 2
        prestamoService.registrarPrestamo("12345678", ejemplar2.getCodigoBarras());

        // Ahora el libro tiene 0 disponibles
        assertEquals(0, catService.contarDisponibles(libroGuardado.getId()));

        // Crear reserva para un cliente en espera
        ReservaService reservaService = sf.getReservaService();
        Cliente clienteReserva = sf.getClienteDAO().buscarPorDni("12345678").orElseThrow();
        // Cancelamos cualquier reserva anterior para evitar colisiones
        reservaService.listarPorCliente(clienteReserva.getId()).forEach(r -> reservaService.cancelarReserva(r.getId()));

        Reserva reservaCreada = reservaService.crearReserva(clienteReserva.getId(), libroGuardado.getId());
        assertNotNull(reservaCreada.getId());
        assertEquals(EstadoReserva.PENDIENTE, reservaCreada.getEstado());

        // Devolver ejemplar 1: Debe asignarse inmediatamente a la reserva pendiente
        PrestamoService.ResultadoDevolucion devConCola = prestamoService.registrarDevolucion(ejemplar1.getCodigoBarras());
        assertTrue(devConCola.isAsignadoAReserva(), "El ejemplar debe ser reservado por la cola FIFO");

        Reserva reservaActualizada = sf.getReservaDAO().buscarPorId(reservaCreada.getId()).orElseThrow();
        assertEquals(EstadoReserva.LISTA, reservaActualizada.getEstado(), "La reserva debe pasar a estado LISTA");
        assertNotNull(reservaActualizada.getFechaExpiracion());

        // =========================================================================
        // PASO 8: Login CLIENTE y cambio obligatorio de contraseña
        // =========================================================================
        SesionActual.cerrarSesion();
        Usuario usuarioCliente = sf.getAuthService().login(dniPrueba, claveTemporal);
        assertNotNull(usuarioCliente);
        assertEquals(Rol.CLIENTE, usuarioCliente.getRol());
        assertTrue(usuarioCliente.isDebeCambiarClave(), "El cliente debe tener la bandera debe_cambiar_clave activa");

        // Cambiar clave por una contraseña personal segura
        String nuevaClave = "ClaveSegura2026!";
        sf.getAuthService().cambiarClave(usuarioCliente.getId(), claveTemporal, nuevaClave);

        // Verificar que ahora se puede autenticar con la nueva clave y debeCambiarClave es false
        Usuario clienteActualizado = sf.getAuthService().login(dniPrueba, nuevaClave);
        assertFalse(clienteActualizado.isDebeCambiarClave(), "La bandera debe_cambiar_clave debe ser false tras el cambio");
    }
}
