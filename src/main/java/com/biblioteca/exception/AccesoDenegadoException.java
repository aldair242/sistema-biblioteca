package com.biblioteca.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una operación o recurso
 * para el cual no posee los privilegios o roles requeridos.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}

