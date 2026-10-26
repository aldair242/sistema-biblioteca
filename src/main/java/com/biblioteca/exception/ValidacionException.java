package com.biblioteca.exception;

/**
 * Excepción lanzada cuando los datos de entrada no cumplen con las reglas o formatos requeridos.
 */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}

