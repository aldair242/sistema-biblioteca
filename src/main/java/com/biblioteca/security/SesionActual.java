package com.biblioteca.security;

import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;

import java.util.Optional;

/**
 * Mantiene el estado del usuario actualmente autenticado en la sesión de la aplicación (Singleton).
 */
public final class SesionActual {

    private static Usuario usuarioAutenticado;

    private SesionActual() {
    }

    /**
     * Establece el usuario autenticado en la sesión.
     *
     * @param usuario Usuario que inició sesión
     */
    public static synchronized void iniciarSesion(Usuario usuario) {
        usuarioAutenticado = usuario;
    }

    /**
     * Cierra la sesión activa actual.
     */
    public static synchronized void cerrarSesion() {
        usuarioAutenticado = null;
    }

    /**
     * Obtiene el usuario autenticado en la sesión actual.
     *
     * @return Usuario autenticado o null si no hay sesión
     */
    public static synchronized Usuario getUsuario() {
        return usuarioAutenticado;
    }

    /**
     * Obtiene el usuario autenticado envuelto en un Optional.
     */
    public static synchronized Optional<Usuario> getUsuarioActual() {
        return Optional.ofNullable(usuarioAutenticado);
    }

    /**
     * Comprueba si existe una sesión de usuario activa.
     */
    public static synchronized boolean estaAutenticado() {
        return usuarioAutenticado != null;
    }

    /**
     * Verifica si el usuario autenticado posee alguno de los roles indicados.
     *
     * @param rolesPermitidos Lista de roles a validar
     * @return true si el usuario tiene al menos uno de los roles, false en caso contrario
     */
    public static synchronized boolean tieneRol(Rol... rolesPermitidos) {
        if (usuarioAutenticado == null || rolesPermitidos == null) {
            return false;
        }
        for (Rol rol : rolesPermitidos) {
            if (usuarioAutenticado.getRol() == rol) {
                return true;
            }
        }
        return false;
    }
}

