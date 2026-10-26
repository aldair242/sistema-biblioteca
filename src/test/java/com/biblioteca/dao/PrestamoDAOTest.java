package com.biblioteca.dao;

import com.biblioteca.dao.impl.*;
import com.biblioteca.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PrestamoDAOTest extends BaseDAOTest {

    private final PrestamoDAO prestamoDAO = new PrestamoDAOImpl();
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();

    @Test
    @DisplayName("CRUD de Préstamo y consultas específicas en transacción")
    void testCrudYConsultasPrestamo() {
        // Crear bibliotecario
        Usuario biblio = usuarioDAO.guardar(new Usuario(null, "bib_" + UUID.randomUUID().toString().substring(0, 8),
                "$2a$10$hash", Rol.BIBLIOTECARIO, "Biblio Test", true, false, 0, null));

        // Crear cliente
        Usuario userCli = usuarioDAO.guardar(new Usuario(null, "ucli_" + UUID.randomUUID().toString().substring(0, 8),
                "$2a$10$hash", Rol.CLIENTE, "Lector Test", true, false, 0, null));
        String dni = String.format("%08d", (int) (Math.random() * 89999999 + 10000000));
        Cliente cliente = clienteDAO.guardar(new Cliente(null, userCli.getId(), dni, "Lector", "Test", "lec@test.com", "987000111", LocalDate.now()));

        // Crear libro y ejemplar
        String isbn = "978" + (long) (Math.random() * 8999999999L + 1000000000L);
        Libro libro = libroDAO.guardar(new Libro(null, isbn, "Libro Prestamos", "Autor", "Ficcion", "Editorial", 2023));
        Ejemplar ejemplar = ejemplarDAO.guardar(new Ejemplar(null, libro.getId(), "BAR-" + UUID.randomUUID().toString().substring(0, 8), EstadoEjemplar.PRESTADO));

        LocalDate hoy = LocalDate.now();
        Prestamo prestamo = new Prestamo(null, cliente.getId(), ejemplar.getId(), biblio.getId(),
                hoy, hoy.plusDays(7), null, EstadoPrestamo.ACTIVO, 0);

        // 1. Guardar
        Prestamo guardado = prestamoDAO.guardar(prestamo);
        assertNotNull(guardado.getId());

        // 2. Buscar por ID
        Optional<Prestamo> optId = prestamoDAO.buscarPorId(guardado.getId());
        assertTrue(optId.isPresent());
        assertEquals(EstadoPrestamo.ACTIVO, optId.get().getEstado());

        // 3. Buscar activo por ejemplar
        Optional<Prestamo> optActivo = prestamoDAO.buscarActivoPorEjemplar(ejemplar.getId());
        assertTrue(optActivo.isPresent());
        assertEquals(guardado.getId(), optActivo.get().getId());

        // 4. Listar activos por cliente y contar
        List<Prestamo> activos = prestamoDAO.listarActivosPorCliente(cliente.getId());
        assertFalse(activos.isEmpty());
        assertEquals(1, prestamoDAO.contarActivosPorCliente(cliente.getId()));

        // 5. Listar por cliente
        List<Prestamo> historial = prestamoDAO.listarPorCliente(cliente.getId());
        assertFalse(historial.isEmpty());

        // 6. Actualizar devolución
        guardado.setEstado(EstadoPrestamo.DEVUELTO);
        guardado.setFechaDevolucion(hoy);
        prestamoDAO.actualizar(guardado);
        Prestamo devuelto = prestamoDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(EstadoPrestamo.DEVUELTO, devuelto.getEstado());
        assertEquals(hoy, devuelto.getFechaDevolucion());

        // 7. Eliminar
        prestamoDAO.eliminar(guardado.getId());
        assertTrue(prestamoDAO.buscarPorId(guardado.getId()).isEmpty());
    }
}

