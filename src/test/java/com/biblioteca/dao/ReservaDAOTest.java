package com.biblioteca.dao;

import com.biblioteca.dao.impl.*;
import com.biblioteca.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservaDAOTest extends BaseDAOTest {

    private final ReservaDAO reservaDAO = new ReservaDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    @Test
    @DisplayName("CRUD de Reserva y consultas FIFO en transacción")
    void testCrudYConsultasReserva() {
        Usuario userCli = usuarioDAO.guardar(new Usuario(null, "rescli_" + UUID.randomUUID().toString().substring(0, 8),
                "$2a$10$hash", Rol.CLIENTE, "Cliente Reserva", true, false, 0, null));
        String dni = String.format("%08d", (int) (Math.random() * 89999999 + 10000000));
        Cliente cliente = clienteDAO.guardar(new Cliente(null, userCli.getId(), dni, "Cliente", "Reserva", "res@test.com", "987222333", LocalDate.now()));

        String isbn = "978" + (long) (Math.random() * 8999999999L + 1000000000L);
        Libro libro = libroDAO.guardar(new Libro(null, isbn, "Libro Reservado", "Autor", "Ensayo", "Editorial", 2022));

        Reserva reserva = new Reserva(null, cliente.getId(), libro.getId(), null, LocalDateTime.now(), null, EstadoReserva.PENDIENTE);

        // 1. Guardar
        Reserva guardada = reservaDAO.guardar(reserva);
        assertNotNull(guardada.getId());

        // 2. Buscar por ID
        Optional<Reserva> optId = reservaDAO.buscarPorId(guardada.getId());
        assertTrue(optId.isPresent());
        assertEquals(EstadoReserva.PENDIENTE, optId.get().getEstado());

        // 3. Listar cola FIFO
        List<Reserva> cola = reservaDAO.listarColaPorLibro(libro.getId());
        assertFalse(cola.isEmpty());
        assertEquals(guardada.getId(), cola.get(0).getId());

        // 4. Buscar activa de cliente para el libro
        Optional<Reserva> activa = reservaDAO.buscarReservaActivaClienteLibro(cliente.getId(), libro.getId());
        assertTrue(activa.isPresent());

        // 5. Listar por cliente
        List<Reserva> porCliente = reservaDAO.listarPorCliente(cliente.getId());
        assertFalse(porCliente.isEmpty());

        // 6. Actualizar a LISTA con fecha expiración
        guardada.setEstado(EstadoReserva.LISTA);
        guardada.setFechaExpiracion(LocalDate.now().plusDays(3));
        reservaDAO.actualizar(guardada);
        Reserva actualizada = reservaDAO.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(EstadoReserva.LISTA, actualizada.getEstado());

        // 7. Eliminar
        reservaDAO.eliminar(guardada.getId());
        assertTrue(reservaDAO.buscarPorId(guardada.getId()).isEmpty());
    }
}

