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
    private CatalogoService catalogoService;

    @BeforeEach
    void setUp() {
        configuracionService = new ConfiguracionService(configuracionDAO);
        multaService = new MultaService(multaDAO, configuracionService);
        reservaService = new ReservaService(reservaDAO, ejemplarDAO, configuracionService);
        prestamoService = new PrestamoService(prestamoDAO, ejemplarDAO, clienteDAO, multaDAO, reservaDAO,
                multaService, reservaService, configuracionService);
        clienteService = new ClienteService(clienteDAO, usuarioDAO, prestamoDAO, multaDAO, reservaDAO);
        catalogoService = new CatalogoService(libroDAO, ejemplarDAO, prestamoDAO, reservaDAO);

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
        SesionActual.iniciarSesion(new Usuario(1, "admin", "hash", Rol.ADMIN, "Admin", true, false, 0, null));

        configuracionService.actualizar("MULTA_POR_DIA", "3.00");
        verify(configuracionDAO).guardar("MULTA_POR_DIA", "3.00");

        when(configuracionDAO.obtener("MULTA_POR_DIA")).thenReturn(Optional.of("3.00"));

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(1, 1, 1, 1, hoy.minusDays(9), hoy.minusDays(2), null, EstadoPrestamo.ACTIVO, 0);

        BigDecimal multa = multaService.calcularMulta(prestamo, hoy);
        assertEquals(0, new BigDecimal("6.00").compareTo(multa), "2 días de retraso x nueva tarifa 3.00 debe dar 6.00");
    }

    @Test
    @DisplayName("Caso 4: Límite de 3 préstamos activos bloquea un cuarto intento")
    void testLimiteDePrestamosActivosBloqueaNuevoPrestamo() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        String dni = "77889900";
        Cliente cliente = new Cliente(10, 100, dni, "Juan", "Pérez", "juan@mail.com", "999888777", LocalDate.now());
        when(clienteDAO.buscarPorDni(dni)).thenReturn(Optional.of(cliente));

        when(configuracionDAO.obtener("MAX_PRESTAMOS")).thenReturn(Optional.of("3"));
        List<Prestamo> listaTres = List.of(
                new Prestamo(1, 10, 1, 1, LocalDate.now(), LocalDate.now().plusDays(5), null, EstadoPrestamo.ACTIVO, 0),
                new Prestamo(2, 10, 2, 1, LocalDate.now(), LocalDate.now().plusDays(5), null, EstadoPrestamo.ACTIVO, 0),
                new Prestamo(3, 10, 3, 1, LocalDate.now(), LocalDate.now().plusDays(5), null, EstadoPrestamo.ACTIVO, 0)
        );
        when(prestamoDAO.listarActivosPorCliente(10)).thenReturn(listaTres);

        ValidacionException ex = assertThrows(ValidacionException.class, () ->
                prestamoService.registrarPrestamo(dni, "L0001-001"));

        assertTrue(ex.getMessage().contains("límite máximo permitido"), "Debe notificar superación de límite");
    }

    @Test
    @DisplayName("Caso 5: Cliente con multa pendiente bloqueado para nuevos préstamos")
    void testClienteConMultaPendienteBloqueadoParaPrestar() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        String dni = "88990011";
        Cliente cliente = new Cliente(20, 200, dni, "María", "López", "maria@mail.com", "988776655", LocalDate.now());
        when(clienteDAO.buscarPorDni(dni)).thenReturn(Optional.of(cliente));

        List<Multa> multasPendientes = Collections.singletonList(
                new Multa(1, 10, 2, new BigDecimal("2.00"), new BigDecimal("4.00"), EstadoMulta.PENDIENTE, null)
        );
        when(multaDAO.listarPendientesPorCliente(20)).thenReturn(multasPendientes);

        ValidacionException ex = assertThrows(ValidacionException.class, () ->
                prestamoService.registrarPrestamo(dni, "L0002-001"));

        assertTrue(ex.getMessage().contains("pendiente(s) de pago"), "Debe bloquear si tiene multas impagas");
    }

    @Test
    @DisplayName("Caso 6: Cola FIFO de reservas asigna ejemplar devuelto al primer cliente")
    void testColaFifoReservasAsignaAlPrimero() {
        int libroId = 5;
        Ejemplar ejemplar = new Ejemplar(10, libroId, "L0005-001", EstadoEjemplar.PRESTADO);

        when(ejemplarDAO.contarDisponibles(libroId)).thenReturn(0);
        when(reservaDAO.buscarReservaActivaClienteLibro(anyInt(), eq(libroId))).thenReturn(Optional.empty());

        when(reservaDAO.guardar(any(Reserva.class))).thenAnswer(i -> {
            Reserva r = i.getArgument(0);
            r.setId(r.getClienteId() * 10);
            return r;
        });

        Reserva resCliente1 = reservaService.crearReserva(101, libroId);
        Reserva resCliente2 = reservaService.crearReserva(102, libroId);

        assertEquals(1010, resCliente1.getId());
        assertEquals(1020, resCliente2.getId());

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
        SesionActual.iniciarSesion(new Usuario(5, "cliente_lector", "hash", Rol.CLIENTE, "Lector", true, false, 0, null));

        assertThrows(AccesoDenegadoException.class,
                () -> prestamoService.registrarPrestamo("12345678", "L0001-001"));

        assertThrows(AccesoDenegadoException.class,
                () -> prestamoService.registrarDevolucion("L0001-001"));

        assertThrows(AccesoDenegadoException.class,
                () -> clienteService.registrarCliente(new Cliente()));

        assertThrows(AccesoDenegadoException.class,
                () -> multaService.cobrar(1));
    }

    @Test
    @DisplayName("Caso 8: Intentar eliminar cliente con préstamos activos lanza ValidacionException")
    void testEliminarClienteConPrestamosActivosLanzaExcepcion() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        int clienteId = 15;
        Cliente c = new Cliente(clienteId, 150, "12345678", "Pedro", "Gómez", "pedro@mail.com", "999111222", LocalDate.now());
        when(clienteDAO.buscarPorId(clienteId)).thenReturn(Optional.of(c));
        when(prestamoDAO.contarActivosPorCliente(clienteId)).thenReturn(1);

        ValidacionException ex = assertThrows(ValidacionException.class, () ->
                clienteService.eliminarCliente(clienteId));

        assertTrue(ex.getMessage().contains("préstamo(s) activo(s)"), "Debe impedir eliminar cliente con préstamos activos");
    }

    @Test
    @DisplayName("Caso 9: Restablecer clave temporal asigna nueva clave y activa debe_cambiar_clave")
    void testRestablecerClaveTemporal() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        int clienteId = 25;
        Cliente c = new Cliente(clienteId, 250, "87654321", "Ana", "Torres", "ana@mail.com", "988222333", LocalDate.now());
        when(clienteDAO.buscarPorId(clienteId)).thenReturn(Optional.of(c));

        String claveTemp = clienteService.restablecerClaveTemporal(clienteId);

        assertNotNull(claveTemp, "La clave temporal no debe ser nula");
        assertEquals(8, claveTemp.length(), "La clave temporal generada debe tener 8 caracteres");
        verify(usuarioDAO).cambiarClave(eq(250), anyString(), eq(true));
    }

    @Test
    @DisplayName("Caso 10: Edición de cliente valida DNI duplicado de otro cliente")
    void testEditarClienteValidaDniDuplicado() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        Cliente c1 = new Cliente(1, 10, "11223344", "Carlos", "Ruiz", "carlos@mail.com", "977111222", LocalDate.now());
        Cliente c2 = new Cliente(2, 20, "11223344", "Otro", "Usuario", "otro@mail.com", "977333444", LocalDate.now());

        when(clienteDAO.buscarPorDni("11223344")).thenReturn(Optional.of(c2));

        ValidacionException ex = assertThrows(ValidacionException.class, () ->
                clienteService.editarCliente(c1));

        assertTrue(ex.getMessage().contains("ya está asignado a otro cliente"));
    }

    @Test
    @DisplayName("Caso 11: Registrar préstamo con selección de días personalizados (1, 3 o 7 días)")
    void testRegistrarPrestamoConDiasPersonalizados() {
        SesionActual.iniciarSesion(new Usuario(2, "biblio", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null));

        String dni = "77889900";
        Cliente cliente = new Cliente(30, 300, dni, "Laura", "Vega", "laura@mail.com", "911223344", LocalDate.now());
        when(clienteDAO.buscarPorDni(dni)).thenReturn(Optional.of(cliente));
        when(multaDAO.listarPendientesPorCliente(30)).thenReturn(Collections.emptyList());
        when(prestamoDAO.listarActivosPorCliente(30)).thenReturn(Collections.emptyList());
        when(configuracionDAO.obtener("MAX_PRESTAMOS")).thenReturn(Optional.of("3"));

        Ejemplar ejemplar = new Ejemplar(15, 3, "L0003-001", EstadoEjemplar.DISPONIBLE);
        when(ejemplarDAO.buscarPorCodigoBarras("L0003-001")).thenReturn(Optional.of(ejemplar));

        LocalDate hoy = LocalDate.now();
        when(prestamoDAO.guardar(any(Prestamo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Probar con 3 días (Fin de semana / alta demanda)
        Prestamo p3 = prestamoService.registrarPrestamo(dni, "L0003-001", 3);
        assertNotNull(p3);
        assertEquals(hoy.plusDays(3), p3.getFechaVencimiento(), "El préstamo debe vencer exactamente en 3 días");

        // Probar con 1 día (Express / alta demanda)
        Prestamo p1 = prestamoService.registrarPrestamo(dni, "L0003-001", 1);
        assertEquals(hoy.plusDays(1), p1.getFechaVencimiento(), "El préstamo express debe vencer exactamente en 1 día");

        // Probar con 7 días (Estándar)
        Prestamo p7 = prestamoService.registrarPrestamo(dni, "L0003-001", 7);
        assertEquals(hoy.plusDays(7), p7.getFechaVencimiento(), "El préstamo estándar debe vencer exactamente en 7 días");
    }
}
