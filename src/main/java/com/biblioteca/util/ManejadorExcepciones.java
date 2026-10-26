package com.biblioteca.util;

import com.biblioteca.exception.AccesoDenegadoException;
import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.ValidacionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Utilidad centralizada para el manejo uniforme de excepciones en la capa de presentación (GUI).
 * Presenta mensajes descriptivos y amigables al usuario final mientras registra la traza
 * técnica detallada en el sistema de logs (Logback), evitando exponer detalles internos de la base de datos.
 */
public final class ManejadorExcepciones {

    private static final Logger logger = LoggerFactory.getLogger(ManejadorExcepciones.class);

    private ManejadorExcepciones() {
    }

    /**
     * Procesa y clasifica una excepción arrojada durante la ejecución de una acción del usuario.
     *
     * @param padre     Componente visual contenedor o padre del diálogo
     * @param operacion Nombre descriptivo de la operación (ej. "registro de préstamo", "cobro de multa")
     * @param ex        Excepción o error capturado
     */
    public static void manejar(Component padre, String operacion, Throwable ex) {
        if (ex instanceof ValidacionException ve) {
            logger.warn("Validación de negocio insatisfecha en '{}': {}", operacion, ve.getMessage());
            JOptionPane.showMessageDialog(padre, ve.getMessage(), "Validación de Datos", JOptionPane.WARNING_MESSAGE);
        } else if (ex instanceof AccesoDenegadoException ade) {
            logger.warn("Intento de acceso denegado en '{}': {}", operacion, ade.getMessage());
            JOptionPane.showMessageDialog(padre, ade.getMessage(), "Acceso Restringido", JOptionPane.WARNING_MESSAGE);
        } else if (ex instanceof AutenticacionException ae) {
            logger.warn("Fallo de autenticación en '{}': {}", operacion, ae.getMessage());
            JOptionPane.showMessageDialog(padre, ae.getMessage(), "Autenticación", JOptionPane.ERROR_MESSAGE);
        } else if (ex instanceof DaoException de) {
            logger.error("Error en capa de persistencia/DAO durante '{}': {}", operacion, de.getMessage(), de);
            JOptionPane.showMessageDialog(padre,
                    "Ha ocurrido un error al comunicarse con la base de datos.\n" +
                            "Por favor contacte al administrador si el problema persiste.",
                    "Error de Sistema", JOptionPane.ERROR_MESSAGE);
        } else {
            logger.error("Excepción inesperada durante '{}': {}", operacion, ex.getMessage(), ex);
            String mensaje = ex.getMessage() != null && !ex.getMessage().isBlank()
                    ? ex.getMessage()
                    : "Error interno del sistema.";
            JOptionPane.showMessageDialog(padre,
                    "Ocurrió un error inesperado al procesar la operación:\n" + mensaje,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
