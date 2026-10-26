package com.biblioteca.dao;

import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LibroDAOTest extends BaseDAOTest {

    private final LibroDAO libroDAO = new LibroDAOImpl();

    @Test
    @DisplayName("CRUD de Libro y consultas específicas en transacción")
    void testCrudYConsultasLibro() {
        String isbn = "978" + (long) (Math.random() * 8999999999L + 1000000000L);
        Libro libro = new Libro(null, isbn, "Libro de Prueba TDD", "Autor Experto", "Tecnologia", "Editorial UTP", 2026);

        // 1. Guardar
        Libro guardado = libroDAO.guardar(libro);
        assertNotNull(guardado.getId());

        // 2. Buscar por ID
        Optional<Libro> optId = libroDAO.buscarPorId(guardado.getId());
        assertTrue(optId.isPresent());
        assertEquals("Libro de Prueba TDD", optId.get().getTitulo());

        // 3. Buscar por ISBN
        Optional<Libro> optIsbn = libroDAO.buscarPorIsbn(isbn);
        assertTrue(optIsbn.isPresent());
        assertEquals("Autor Experto", optIsbn.get().getAutor());

        // 4. Buscar por Criterio
        List<Libro> encontrados = libroDAO.buscarPorCriterio("Prueba TDD");
        assertFalse(encontrados.isEmpty());

        // 5. Listar más prestados
        List<Libro> masPrestados = libroDAO.listarMasPrestados(5);
        assertNotNull(masPrestados);

        // 6. Actualizar
        guardado.setEditorial("Editorial Renovada");
        guardado.setAnio(2027);
        libroDAO.actualizar(guardado);
        Libro actualizado = libroDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("Editorial Renovada", actualizado.getEditorial());
        assertEquals(2027, actualizado.getAnio());

        // 7. Eliminar
        libroDAO.eliminar(guardado.getId());
        assertTrue(libroDAO.buscarPorId(guardado.getId()).isEmpty());
    }
}

