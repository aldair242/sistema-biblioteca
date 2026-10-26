package com.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.sql.Connection;

/**
 * Validador del estado de la conexión a la base de datos al inicio de la aplicación.
 */
public final class DatabaseCheck {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCheck.class);

    private DatabaseCheck() {
    }

    /**
     * Verifica la disponibilidad de la conexión a MySQL.
     * Si falla, muestra un mensaje amigable al usuario y registra el error técnico en los logs.
     *
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public static boolean verificarConexion() {
        return verificarConexion(null);
    }

    /**
     * Verifica la disponibilidad de la conexión a MySQL asociando el diálogo a un componente padre.
     *
     * @param padre Componente visual padre para el JOptionPane (puede ser null)
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public static boolean verificarConexion(Component padre) {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            boolean valida = conn != null && !conn.isClosed() && conn.isValid(3);
            if (valida) {
                logger.info("Verificación de conexión a la base de datos MySQL exitosa.");
                return true;
            }
        } catch (Exception e) {
            logger.error("Detalle técnico del fallo al conectar a MySQL: {}", e.getMessage(), e);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        padre,
                        "No se pudo establecer conexión con la base de datos de la biblioteca.\n" +
                                "Por favor, verifique que el servicio de base de datos se encuentre en ejecución y configurado correctamente.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return false;
    }
}

