package com.biblioteca.dao;

import com.biblioteca.model.Configuracion;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la tabla de parámetros del sistema (Configuracion).
 */
public interface ConfiguracionDAO {

    /**
     * Obtiene el valor textual de un parámetro de configuración por su clave.
     *
     * @param clave Clave única del parámetro (ej: MULTA_POR_DIA)
     * @return Optional con el valor si la clave existe
     */
    Optional<String> obtener(String clave);

    /**
     * Guarda o actualiza el valor de un parámetro de configuración.
     *
     * @param clave Clave única del parámetro
     * @param valor Nuevo valor a persistir
     */
    void guardar(String clave, String valor);

    /**
     * Lista todos los parámetros de configuración en forma de entidades.
     *
     * @return Lista de configuraciones
     */
    List<Configuracion> listar();

    /**
     * Lista todos los parámetros de configuración en forma de mapa clave-valor.
     *
     * @return Mapa de clave y valor
     */
    Map<String, String> listarComoMapa();
}

