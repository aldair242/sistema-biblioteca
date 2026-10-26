package com.biblioteca.dao;

import com.biblioteca.model.Multa;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Multa.
 */
public interface MultaDAO extends GenericDAO<Multa> {

    /**
     * Lista todas las multas en estado PENDIENTE de un cliente.
     *
     * @param clienteId Identificador del cliente
     * @return Lista de multas pendientes de pago
     */
    List<Multa> listarPendientesPorCliente(int clienteId);

    /**
     * Busca la multa generada a partir de un préstamo específico.
     *
     * @param prestamoId Identificador del préstamo
     * @return Optional con la multa si existe
     */
    Optional<Multa> buscarPorPrestamo(int prestamoId);

    /**
     * Lista el historial de multas de un cliente (pendientes y pagadas).
     *
     * @param clienteId Identificador del cliente
     * @return Lista de multas del cliente
     */
    List<Multa> listarPorCliente(int clienteId);
}

