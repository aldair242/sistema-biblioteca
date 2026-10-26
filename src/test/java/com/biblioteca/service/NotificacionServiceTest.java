package com.biblioteca.service;

import com.biblioteca.dao.ClienteDAO;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private EjemplarDAO ejemplarDAO;

    @Mock
    private LibroDAO libroDAO;

    @Mock
    private ConfiguracionService configuracionService;

    private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        notificacionService = new NotificacionService(clienteDAO, ejemplarDAO, libroDAO, configuracionService);
    }

    @Test
    @DisplayName("Generar ticket de préstamo con datos completos y cálculo de plazo")
    void testGenerarTicketExitoso() {
        LocalDate hoy = LocalDate.now();
        LocalDate vence = hoy.plusDays(3);

        Prestamo p = new Prestamo(10, 1, 2, 1, hoy, vence, null, EstadoPrestamo.ACTIVO, 0);
        Cliente c = new Cliente(1, 10, "12345678", "Carlos", "Perez", "carlos@correo.com", "987654321", LocalDate.now());
        Ejemplar ej = new Ejemplar(2, 5, "L0005-001", EstadoEjemplar.PRESTADO);
        Libro l = new Libro(5, "978-0132350884", "Clean Code", "Robert C. Martin", "Software", "Prentice Hall", 2008);

        when(clienteDAO.buscarPorId(1)).thenReturn(Optional.of(c));
        when(ejemplarDAO.buscarPorId(2)).thenReturn(Optional.of(ej));
        when(libroDAO.buscarPorId(5)).thenReturn(Optional.of(l));
        when(configuracionService.obtenerMultaPorDia()).thenReturn(new BigDecimal("3.00"));

        TicketPrestamo ticket = notificacionService.generarTicket(p);

        assertNotNull(ticket);
        assertEquals(10, ticket.getPrestamoId());
        assertEquals("Carlos Perez", ticket.getClienteNombre());
        assertEquals("12345678", ticket.getClienteDni());
        assertEquals("987654321", ticket.getClienteTelefono());
        assertEquals("carlos@correo.com", ticket.getClienteEmail());
        assertEquals("Clean Code", ticket.getLibroTitulo());
        assertEquals("L0005-001", ticket.getCodigoBarras());
        assertEquals(3, ticket.getDiasOtorgados());
        assertEquals(vence, ticket.getFechaVencimiento());
        assertEquals(new BigDecimal("3.00"), ticket.getTarifaMultaPorDia());
    }

    @Test
    @DisplayName("Mensaje push de WhatsApp contiene fecha límite de devolución y libro")
    void testMensajePushWhatsApp() {
        LocalDate hoy = LocalDate.now();
        LocalDate vence = hoy.plusDays(7);
        TicketPrestamo ticket = new TicketPrestamo(
                20, "Ana Gómez", "87654321", "912345678", "ana@correo.com",
                "El Quijote", "Miguel de Cervantes", "Novela", "L0002-001",
                hoy, vence, 7, new BigDecimal("2.50")
        );

        String push = notificacionService.generarMensajePushWhatsApp(ticket);

        assertNotNull(push);
        assertTrue(push.contains("Ana Gómez"));
        assertTrue(push.contains("El Quijote"));
        assertTrue(push.contains("FECHA LÍMITE"));
        assertTrue(push.contains("L0002-001"));
        assertTrue(push.contains("7 día(s)"));
    }

    @Test
    @DisplayName("Cuerpo de correo contiene datos detallados de devolución y términos")
    void testContenidoCorreo() {
        LocalDate hoy = LocalDate.now();
        LocalDate vence = hoy.plusDays(1);
        TicketPrestamo ticket = new TicketPrestamo(
                30, "Luis Morales", "76543210", "998877665", "luis@correo.com",
                "Patrones de Diseño", "Gang of Four", "Computación", "L0010-001",
                hoy, vence, 1, new BigDecimal("2.50")
        );

        String correo = notificacionService.generarContenidoCorreo(ticket);

        assertNotNull(correo);
        assertTrue(correo.contains("Luis Morales"));
        assertTrue(correo.contains("Patrones de Diseño"));
        assertTrue(correo.contains("FECHA LÍMITE DE DEVOLUCIÓN"));
        assertTrue(correo.contains("1 días"));
    }

    @Test
    @DisplayName("Despacho de push y correo no lanzan excepción y retornan confirmación")
    void testDespachoPushYCorreo() {
        LocalDate hoy = LocalDate.now();
        TicketPrestamo ticket = new TicketPrestamo(
                40, "Maria Ramos", "11223344", "999888777", "maria@correo.com",
                "Clean Architecture", "Robert C. Martin", "Software", "L0020-001",
                hoy, hoy.plusDays(3), 3, new BigDecimal("2.50")
        );

        assertTrue(notificacionService.enviarPushWhatsApp(ticket));
        assertTrue(notificacionService.enviarCorreo(ticket));
    }
}
