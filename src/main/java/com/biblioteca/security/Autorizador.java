package com.biblioteca.security;

import com.biblioteca.exception.AccesoDenegadoException;
import com.biblioteca.model.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Mecanismo de autorización y control de acceso basado en roles.
 */
public final class Autorizador {

    private static final Logger logger = LoggerFactory.getLogger(Autorizador.class);

    private Autorizador() {
    }

    /**
     * Valida que el usuario en sesión posea al menos uno de los roles requeridos.
     * Si no está autenticado o no posee el rol, lanza AccesoDenegadoException.
     *
     * @param rolesPermitidos Uno o más roles autorizados para la operación
     * @throws AccesoDenegadoException si el usuario no tiene permiso
     */
    public static void requerir(Rol... rolesPermitidos) {
        if (!SesionActual.estaAutenticado()) {
            logger.warn("Intento de acceso denegado: no existe una sesión autenticada.");
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta operación.");
        }

        if (!SesionActual.tieneRol(rolesPermitidos)) {
            String username = SesionActual.getUsuario().getUsername();
            Rol rolActual = SesionActual.getUsuario().getRol();
            logger.warn("Acceso denegado para usuario '{}' con rol {}. Roles requeridos: {}",
                    username, rolActual, Arrays.toString(rolesPermitidos));
            throw new AccesoDenegadoException("Acceso denegado: su rol no tiene privilegios para realizar esta acción.");
        }
    }
}

