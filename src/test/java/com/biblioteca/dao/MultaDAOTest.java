package com.biblioteca.dao;

import com.biblioteca.dao.impl.*;
import com.biblioteca.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MultaDAOTest extends BaseDAOTest {

    private final MultaDAO multaDAO = new MultaDAOImpl();
    private final PrestamoDAO prestamoDAO = new PrestamoDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();

    @Test
    @DisplayName("CRUD de Multa y consultas específicas en transacción")
    void testCrudYConsultasMulta() {
        Usuario biblio = usuarioDAO.guardar(new Usuario(null, "mbib_" + UUID.randomUUID().toString().substring(0, 8),
                "$2a$10$hash", Rol.BIBLIOTECARIO, "Biblio Multas", true, false, 0, null));
        Usuario userCli = usuarioDAO.guardar(new Usuario(null, "mcli_" + UUID.randomUUID().toString().substring(0, 8),
                "$2a$10$hash", Rol.CLIENTE, "Cliente Multado", true, false, 0, null));
        String dni = String.format("%08d", (int) (Math.random() * 89999999 + 10000000));
        Cliente cliente = clienteDAO.guardar(new Cliente(null, userCli.getId(), dni, "Cliente", "Multado", "mul@test.com", "987333444", LocalDate.now()));

        String isbn = "978" + (long) (Math.random() * 8999999999L + 1000000000L);
        Libro libro = libroDAO.guardar(new Libro(null, isbn, "Libro Multa", "Autor", "Drama", "Editorial", 2021));
        Ejemplar ejemplar = ejemplarDAO.guardar(new Ejemplar(null, libro.getId(), "MUL-" + UUID.randomUUID().toString().substring(0, 8), EstadoEjemplar.DISPONIBLE));

        LocalDate haceDiezDias = LocalDate.now().minusDays(10);
        Prestamo prestamo = prestamoDAO.guardar(new Prestamo(null, cliente.getId(), ejemplar.getId(), biblio.getId(),
                haceDiezDias, haceDiezDias.plusDays(7), LocalDate.now(), EstadoPrestamo.DEVUELTO, 0));

        Multa multa = new Multa(null, prestamo.getId(), 3, new BigDecimal("1.00"), new BigDecimal("3.00"), EstadoMulta.PENDIENTE, null);

        // 1. Guardar
        Multa guardada = multaDAO.guardar(multa);
        assertNotNull(guardada.getId());

        // 2. Buscar por ID
        Optional<Multa> optId = multaDAO.buscarPorId(guardada.getId());
        assertTrue(optId.isPresent());
        assertEquals(new BigDecimal("3.00"), optId.get().getMonto());

        // 3. Buscar por préstamo
        Optional<Multa> optPres = multaDAO.buscarPorPrestamo(prestamo.getId());
        assertTrue(optPres.isPresent());
        assertEquals(guardada.getId(), optPres.get().getId());

        // 4. Listar pendientes por cliente
        List<Multa> pendientes = multaDAO.listarPendientesPorCliente(cliente.getId());
        assertFalse(pendientes.isEmpty());

        // 5. Listar por cliente
        List<Multa> historial = multaDAO.listarPorCliente(cliente.getId());
        assertFalse(historial.isEmpty());

        // 6. Actualizar a PAGADA
        guardada.setEstado(EstadoMulta.PAGADA);
        guardada.setFechaPago(LocalDate.now());
        multaDAO.actualizar(guardada);
        Multa pagada = multaDAO.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(EstadoMulta.PAGADA, pagada.getEstado());
        assertNotNull(pagada.getFechaPago());

        // 7. Eliminar
        multaDAO.eliminar(guardada.getId());
        assertTrue(multaDAO.buscarPorId(guardada.getId()).isEmpty());
    }
}

