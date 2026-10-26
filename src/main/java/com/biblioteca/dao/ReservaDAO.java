package com.biblioteca.dao;

import com.biblioteca.model.Reserva;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Reserva.
 */
public interface ReservaDAO extends GenericDAO<Reserva> {

    /**
     * Lista las reservas en cola FIFO (ordenadas por fecha_reserva ascendente) con estado PENDIENTE.
     *
     * @param libroId Identificador del libro
     * @return Lista en orden FIFO de reservas pendientes
     */
    List<Reserva> listarColaPorLibro(int libroId);

    /**
     * Lista todas las reservas asociadas a un cliente.
     *
     * @param clienteId Identificador del cliente
     * @return Lista de reservas del cliente
     */
    List<Reserva> listarPorCliente(int clienteId);

    /**
     * Lista las reservas de un libro que están en estado LISTA (ejemplar asignado esperando recojo).
     *
     * @param libroId Identificador del libro
     * @return Lista de reservas listas
     */
    List<Reserva> listarListasPorLibro(int libroId);

    /**
     * Lista las reservas en estado LISTA cuyo plazo de retención ha expirado (fecha_expiracion menor a hoy).
     *
     * @return Lista de reservas expiradas
     */
    List<Reserva> listarExpiradas();

    /**
     * Busca si un cliente ya tiene una reserva activa (PENDIENTE o LISTA) para un libro.
     *
     * @param clienteId Identificador del cliente
     * @param libroId Identificador del libro
     * @return Optional con la reserva activa si existe
     */
    Optional<Reserva> buscarReservaActivaClienteLibro(int clienteId, int libroId);
}

