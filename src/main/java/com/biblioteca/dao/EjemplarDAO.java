package com.biblioteca.dao;

import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.EstadoEjemplar;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Ejemplar.
 */
public interface EjemplarDAO extends GenericDAO<Ejemplar> {

    /**
     * Lista todos los ejemplares asociados a un libro determinado.
     *
     * @param libroId Identificador del libro
     * @return Lista de ejemplares del libro
     */
    List<Ejemplar> listarPorLibro(int libroId);

    /**
     * Busca un ejemplar por su código de barras único.
     *
     * @param codigoBarras Código de barras único del ejemplar
     * @return Optional con el ejemplar si existe
     */
    Optional<Ejemplar> buscarPorCodigoBarras(String codigoBarras);

    /**
     * Cuenta la cantidad de ejemplares en estado DISPONIBLE para un libro.
     *
     * @param libroId Identificador del libro
     * @return Cantidad de ejemplares disponibles
     */
    int contarDisponibles(int libroId);

    /**
     * Busca el primer ejemplar en estado DISPONIBLE para un libro.
     *
     * @param libroId Identificador del libro
     * @return Optional con el ejemplar disponible si existe
     */
    Optional<Ejemplar> buscarDisponiblePorLibro(int libroId);

    /**
     * Actualiza únicamente el estado operativo de un ejemplar.
     *
     * @param id Identificador del ejemplar
     * @param nuevoEstado Nuevo estado del ejemplar
     */
    void actualizarEstado(int id, EstadoEjemplar nuevoEstado);
}

