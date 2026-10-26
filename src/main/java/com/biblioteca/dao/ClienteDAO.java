package com.biblioteca.dao;

import com.biblioteca.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Cliente.
 */
public interface ClienteDAO extends GenericDAO<Cliente> {

    /**
     * Busca un cliente por su número único de DNI (8 dígitos).
     *
     * @param dni DNI del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> buscarPorDni(String dni);

    /**
     * Busca clientes por coincidencia de texto en DNI, nombres o apellidos.
     *
     * @param texto Criterio de búsqueda
     * @return Lista de clientes coincidentes
     */
    List<Cliente> buscarPorTexto(String texto);

    /**
     * Busca el registro de cliente asociado a una cuenta de usuario.
     *
     * @param usuarioId Identificador de usuario
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> buscarPorUsuarioId(int usuarioId);
}

