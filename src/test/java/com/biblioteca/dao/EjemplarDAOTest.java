package com.biblioteca.dao;

import com.biblioteca.dao.impl.EjemplarDAOImpl;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.EstadoEjemplar;
import com.biblioteca.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EjemplarDAOTest extends BaseDAOTest {

    private final EjemplarDAO ejemplarDAO = new EjemplarDAOImpl();
    private final LibroDAO libroDAO = new LibroDAOImpl();

    @Test
    @DisplayName("CRUD de Ejemplar y consultas específicas en transacción")
    void testCrudYConsultasEjemplar() {
        String isbn = "978" + (long) (Math.random() * 8999999999L + 1000000000L);
        Libro libro = libroDAO.guardar(new Libro(null, isbn, "Libro Ejemplares", "Autor", "Ciencia", "Editorial", 2024));

        String codBarras = "EJ-" + UUID.randomUUID().toString().substring(0, 10);
        Ejemplar ejemplar = new Ejemplar(null, libro.getId(), codBarras, EstadoEjemplar.DISPONIBLE);

        // 1. Guardar
        Ejemplar guardado = ejemplarDAO.guardar(ejemplar);
        assertNotNull(guardado.getId());

        // 2. Buscar por ID
        Optional<Ejemplar> optId = ejemplarDAO.buscarPorId(guardado.getId());
        assertTrue(optId.isPresent());
        assertEquals(codBarras, optId.get().getCodigoBarras());

        // 3. Buscar por código de barras
        Optional<Ejemplar> optCod = ejemplarDAO.buscarPorCodigoBarras(codBarras);
        assertTrue(optCod.isPresent());
        assertEquals(EstadoEjemplar.DISPONIBLE, optCod.get().getEstado());

        // 4. Listar por libro
        List<Ejemplar> listaLibro = ejemplarDAO.listarPorLibro(libro.getId());
        assertFalse(listaLibro.isEmpty());

        // 5. Contar disponibles y buscar disponible
        int disponibles = ejemplarDAO.contarDisponibles(libro.getId());
        assertTrue(disponibles >= 1);

        Optional<Ejemplar> disp = ejemplarDAO.buscarDisponiblePorLibro(libro.getId());
        assertTrue(disp.isPresent());

        // 6. Actualizar estado
        ejemplarDAO.actualizarEstado(guardado.getId(), EstadoEjemplar.EN_MANTENIMIENTO);
        Ejemplar actualizado = ejemplarDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(EstadoEjemplar.EN_MANTENIMIENTO, actualizado.getEstado());

        // 7. Eliminar
        ejemplarDAO.eliminar(guardado.getId());
        assertTrue(ejemplarDAO.buscarPorId(guardado.getId()).isEmpty());
    }
}

