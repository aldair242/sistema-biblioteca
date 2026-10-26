package com.biblioteca.dao;

import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de acceso a datos para la entidad Usuario.
 */
public interface UsuarioDAO extends GenericDAO<Usuario> {

    /**
     * Busca un usuario por su nombre de usuario único.
     *
     * @param username Nombre de usuario para autenticación
     * @return Optional con el usuario si existe, o vacío
     */
    Optional<Usuario> buscarPorUsername(String username);

    /**
     * Actualiza el contador de intentos fallidos y la fecha de bloqueo de una cuenta.
     *
     * @param id Identificador del usuario
     * @param intentos Número de intentos fallidos acumulados
     * @param bloqueadoHasta Fecha y hora de expiración del bloqueo (o null si no está bloqueado)
     */
    void actualizarIntentosFallidos(int id, int intentos, LocalDateTime bloqueadoHasta);

    /**
     * Actualiza el hash de la contraseña y la bandera de cambio obligatorio.
     *
     * @param id Identificador del usuario
     * @param nuevoPasswordHash Nuevo hash seguro con BCrypt
     * @param debeCambiarClave Indica si debe cambiarla en el próximo inicio
     */
    void cambiarClave(int id, String nuevoPasswordHash, boolean debeCambiarClave);

    /**
     * Modifica el estado activo/inactivo de un usuario.
     *
     * @param id Identificador del usuario
     * @param activo true para activo, false para deshabilitado
     */
    void cambiarEstadoActivo(int id, boolean activo);

    /**
     * Lista usuarios filtrando por rol específico.
     *
     * @param rol Rol a filtrar
     * @return Lista de usuarios del rol especificado
     */
    List<Usuario> listarPorRol(Rol rol);
}

