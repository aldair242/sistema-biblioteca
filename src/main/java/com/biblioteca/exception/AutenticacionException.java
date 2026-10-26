package com.biblioteca.exception;

/**
 * Excepción lanzada cuando fallan las credenciales o el estado de autenticación de un usuario.
 */
public class AutenticacionException extends RuntimeException {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}

