package com.biblioteca.service;

import com.biblioteca.dao.*;
import com.biblioteca.exception.AccesoDenegadoException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.*;
import com.biblioteca.security.SesionActual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiciosNegocioTest {

    @Mock private ConfiguracionDAO configuracionDAO;
    @Mock private PrestamoDAO prestamoDAO;
    @Mock private EjemplarDAO ejemplarDAO;
    @Mock private ClienteDAO clienteDAO;
    @Mock private MultaDAO multaDAO;
    @Mock private ReservaDAO reservaDAO;
    @Mock private LibroDAO libroDAO;
    @Mock private UsuarioDAO usuarioDAO;

    private ConfiguracionService configuracionService;
    private MultaService multaService;
    private ReservaService reservaService;
    private PrestamoService prestamoService;
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        configuracionService = new ConfiguracionService(configuracionDAO);
        multaService = new MultaService(multaDAO, configuracionService);
        reservaService = new ReservaService(reservaDAO, ejemplarDAO, configuracionService);
        prestamoService = new PrestamoService(prestamoDAO, ejemplarDAO, clienteDAO, multaDAO, reservaDAO,
                multaService, reservaService, configuracionService);
        clienteService = new ClienteService(clienteDAO, usuarioDAO);

        SesionActual.cerrarSesion();
    }

    @AfterEach
    void tearDown() {
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("Caso 1: Devolución a tiempo no genera multa (monto 0.00)")
    void testDevolucionATiempoSinMulta() {
        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, 1, hoy.minusDays(5), hoy.plusDays(2), null, EstadoPrestamo.ACTIVO, 0);

        BigDecimal multa = multaService.calcularMulta(prestamo, hoy);
        assertEquals(BigDecimal.ZERO, multa, "La devolución a tiempo debe arrojar multa 0");
    }

    @Test
    @DisplayName("Caso 2: Devolución con 5 días de retraso y tarifa 2.00 genera multa de 10.00")
    void testDevolucionConCincoDiasRetrasoYTarifaDos() {
        when(configuracionDAO.obtener("MULTA_POR_DIA")).thenReturn(Optional.of("2.00"));

        LocalDate hoy = LocalDate.now();
        LocalDate vencimiento = hoy.minusDays(5);
        Prestamo prestamo = new Prestamo(1, 1, 1, 1, hoy.minusDays(12), vencimiento, null, EstadoPrestamo.ACTIVO, 0);

        BigDecimal multa = multaService.calcularMulta(prestamo, hoy);
        assertEquals(0, new BigDecimal("10.00").compareTo(multa), "5 días de retraso x tarifa 2.00 debe ser 10.00");
    }

    @Test
    @DisplayName("Caso 3: Cambio de tarifa vigente por ADMIN y cálculo con nueva tarifa")
    void testCambioDeTarifaYVerificacionNuevaTarifa() {
        // Autenticar como ADMIN
        SesionActual.iniciarSesion(new Usuario(1, "admin", "hash", Rol.ADMIN, "Admin", true, false, 0, null));

        configuracionService.actualizar("MULTA_POR_DIA", "3.00");
        verify(configuracionDAO).guardar("MULTA_POR_DIA", "3.00");

        when(configuracionDAO.obtener("MULTA_POR_DIA")).thenReturn(Optional.of("3.00"));

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, 1, hoy.minusDays(9), hoy.minusDays(2), null, EstadoPrestamo.ACTIVO, 0);

        BigDecimal multa = multaService.calcularMulta(prestamo, hoy);
        assertEquals(0, new BigDecimal("6.00").compareTo(multa), "2 días de retraso con nueva tarifa 3.00 debe ser 6.00");
    }

    @Test
    @DisplayName("Caso 4: Cliente bloqueado para pedir prestado si tiene multas pendientes")
    void testClienteBloqueadoPorMultaPendiente() {
        SesionActual.iniciarSesion(new Usuario(1, "biblio", "hash", Rol.BIBLIOTECARIO, "Biblio", true, false, 0, null));

        Cliente cliente = new Cliente(1, 2, "12345678", "Ana", "Torres", "ana@test.com", "987000111", LocalDate.now());
        when(clienteDAO.buscarPorDni("12345678")).thenReturn(Optional.of(cliente));

        Multa pendiente = new Multa(1, 10, 2, new BigDecimal("1.00"), new BigDecimal("2.00"), EstadoMulta.PENDIENTE, null);
        when(multaDAO.listarPendientesPorCliente(1)).thenReturn(List.of(pendiente));

        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> prestamoService.registrarPrestamo("12345678", "L0001-001"));

        assertTrue(ex.getMessage().toLowerCase().contains("multa"));
    }

    @Test
    @DisplayName("Caso 5: Cliente bloqueado al alcanzar el límite máximo de préstamos")
    void testLimiteDePrestamos() {
        SesionActual.iniciarSesion(new Usuario(1, "biblio", "hash", Rol.BIBLIOTECARIO, "Biblio", true, false, 0, null));

        Cliente cliente = new Cliente(1, 2, "12345678", "Ana", "Torres", "ana@test.com", "987000111", LocalDate.now());
        when(clienteDAO.buscarPorDni("12345678")).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPendientesPorCliente(1)).thenReturn(Collections.emptyList());

        // Máximo 3 configurado
        when(configuracionDAO.obtener("MAX_PRESTAMOS")).thenReturn(Optional.of("3"));

        LocalDate hoy = LocalDate.now();
        List<Prestamo> prestamosActivos = List.of(
                new Prestamo(1, 1, 1, 1, hoy, hoy.plusDays(5), null, EstadoPrestamo.ACTIVO, 0),
                new Prestamo(2, 1, 2, 1, hoy, hoy.plusDays(5), null, EstadoPrestamo.ACTIVO, 0),
                new Prestamo(3, 1, 3, 1, hoy, hoy.plusDays(5), null, EstadoPrestamo.ACTIVO, 0)
        );
        when(prestamoDAO.listarActivosPorCliente(1)).thenReturn(prestamosActivos);

        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> prestamoService.registrarPrestamo("12345678", "L0001-004"));

        assertTrue(ex.getMessage().toLowerCase().contains("límite"));
    }

    @Test
    @DisplayName("Caso 6: Flujo completo de reserva con cola FIFO de 2 clientes")
    void testFlujoCompletoReservaConColaDeDosClientes() {
        int libroId = 5;
        Ejemplar ejemplar = new Ejemplar(10, libroId, "L0005-001", EstadoEjemplar.PRESTADO);

        // Sin ejemplares disponibles
        when(ejemplarDAO.contarDisponibles(libroId)).thenReturn(0);
        when(reservaDAO.buscarReservaActivaClienteLibro(anyInt(), eq(libroId))).thenReturn(Optional.empty());

        when(reservaDAO.guardar(any(Reserva.class))).thenAnswer(i -> {
            Reserva r = i.getArgument(0);
            r.setId(r.getClienteId() * 10);
            return r;
        });

        // Cliente 1 y Cliente 2 reservan
        Reserva resCliente1 = reservaService.crearReserva(101, libroId);
        Reserva resCliente2 = reservaService.crearReserva(102, libroId);

        assertEquals(1010, resCliente1.getId());
        assertEquals(1020, resCliente2.getId());

        // Al devolverse el ejemplar, se consulta la cola FIFO
        List<Reserva> colaFifo = new ArrayList<>();
        colaFifo.add(resCliente1);
        colaFifo.add(resCliente2);
        when(reservaDAO.listarColaPorLibro(libroId)).thenReturn(colaFifo);
        when(configuracionDAO.obtener("DIAS_RETENCION_RESERVA")).thenReturn(Optional.of("3"));

        boolean asignado = reservaService.asignarEjemplarDevuelto(ejemplar);

        assertTrue(asignado, "Debe asignarse a la primera reserva en cola");
        assertEquals(EstadoReserva.LISTA, resCliente1.getEstado(), "La primera reserva pasa a estado LISTA");
        assertEquals(10, resCliente1.getEjemplarId(), "Se le asigna el ejemplar devuelto");
        assertEquals(EstadoEjemplar.RESERVADO, ejemplar.getEstado(), "El ejemplar pasa a RESERVADO");
        assertEquals(EstadoReserva.PENDIENTE, resCliente2.getEstado(), "La segunda reserva permanece PENDIENTE");
    }

    @Test
    @DisplayName("Caso 7: Acceso denegado a operaciones de BIBLIOTECARIO cuando la sesión es de rol CLIENTE")
    void testAccesoDenegadoDeClienteAOperacionesBibliotecario() {
        // Autenticar como CLIENTE
        SesionActual.iniciarSesion(new Usuario(5, "cliente_lector", "hash", Rol.CLIENTE, "Lector", true, false, 0, null));

        // Intento 1: Registrar préstamo
        assertThrows(AccesoDenegadoException.class,
                () -> prestamoService.registrarPrestamo("12345678", "L0001-001"));

        // Intento 2: Registrar devolución
        assertThrows(AccesoDenegadoException.class,
                () -> prestamoService.registrarDevolucion("L0001-001"));

        // Intento 3: Registrar nuevo cliente
        assertThrows(AccesoDenegadoException.class,
                () -> clienteService.registrarCliente(new Cliente()));

        // Intento 4: Cobrar multa
        assertThrows(AccesoDenegadoException.class,
                () -> multaService.cobrar(1));
    }
}

