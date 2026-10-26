package com.biblioteca.exception;

/**
 * Excepción lanzada al ocurrir fallos en las operaciones de acceso a datos (DAO).
 * Encapsula errores de SQL y base de datos sin exponer detalles técnicos sensibles al usuario.
 */
public class DaoException extends RuntimeException {

    public DaoException(String mensaje) {
        super(mensaje);
    }

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

