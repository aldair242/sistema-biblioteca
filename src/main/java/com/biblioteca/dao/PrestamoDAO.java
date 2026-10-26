package com.biblioteca.dao;

import com.biblioteca.model.Prestamo;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Prestamo.
 */
public interface PrestamoDAO extends GenericDAO<Prestamo> {

    /**
     * Busca si existe un préstamo activo para un ejemplar específico.
     *
     * @param ejemplarId Identificador del ejemplar
     * @return Optional con el préstamo activo si existe
     */
    Optional<Prestamo> buscarActivoPorEjemplar(int ejemplarId);

    /**
     * Lista todos los préstamos activos de un cliente.
     *
     * @param clienteId Identificador del cliente
     * @return Lista de préstamos activos
     */
    List<Prestamo> listarActivosPorCliente(int clienteId);

    /**
     * Cuenta el número total de préstamos actualmente activos de un cliente.
     *
     * @param clienteId Identificador del cliente
     * @return Número de préstamos activos
     */
    int contarActivosPorCliente(int clienteId);

    /**
     * Lista todos los préstamos en estado ACTIVO o VENCIDO cuya fecha de vencimiento ya expiró.
     *
     * @return Lista de préstamos vencidos
     */
    List<Prestamo> listarVencidos();

    /**
     * Lista el historial completo de préstamos de un cliente (activos, devueltos y vencidos).
     *
     * @param clienteId Identificador del cliente
     * @return Lista de préstamos del cliente
     */
    List<Prestamo> listarPorCliente(int clienteId);
}

