package com.biblioteca.dao;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para operaciones CRUD de acceso a datos.
 *
 * @param <T> Tipo de entidad de dominio
 */
public interface GenericDAO<T> {

    /**
     * Guarda una nueva entidad en la base de datos y asigna su clave primaria generada.
     *
     * @param entidad Entidad a guardar
     * @return Entidad persistida con su ID
     */
    T guardar(T entidad);

    /**
     * Busca una entidad por su identificador primario.
     *
     * @param id Identificador numérico
     * @return Optional con la entidad si existe, o vacío
     */
    Optional<T> buscarPorId(int id);

    /**
     * Obtiene el listado de todas las entidades registradas.
     *
     * @return Lista de entidades
     */
    List<T> listar();

    /**
     * Actualiza el estado de una entidad existente en la base de datos.
     *
     * @param entidad Entidad con datos actualizados
     */
    void actualizar(T entidad);

    /**
     * Elimina físicamente una entidad por su identificador.
     *
     * @param id Identificador numérico
     */
    void eliminar(int id);
}

