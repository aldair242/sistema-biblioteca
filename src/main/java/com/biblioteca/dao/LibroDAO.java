package com.biblioteca.dao;

import com.biblioteca.model.Libro;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Libro.
 */
public interface LibroDAO extends GenericDAO<Libro> {

    /**
     * Busca un libro por su código ISBN único.
     *
     * @param isbn Código ISBN
     * @return Optional con el libro si existe
     */
    Optional<Libro> buscarPorIsbn(String isbn);

    /**
     * Busca libros por coincidencia en título, autor, ISBN o categoría.
     *
     * @param texto Criterio de búsqueda libre
     * @return Lista de libros que coinciden con el criterio
     */
    List<Libro> buscarPorCriterio(String texto);

    /**
     * Obtiene los 10 libros con mayor cantidad histórica de préstamos.
     *
     * @param limite Cantidad máxima a retornar (por ejemplo 10)
     * @return Lista de libros más prestados
     */
    List<Libro> listarMasPrestados(int limite);
}

