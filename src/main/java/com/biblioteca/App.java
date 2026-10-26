package com.biblioteca;

import com.biblioteca.config.DatabaseCheck;
import com.biblioteca.controller.AuthController;
import com.biblioteca.service.ServiceFactory;
import com.biblioteca.view.LoginView;
import com.biblioteca.view.theme.Tema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;

/**
 * Clase principal que inicializa la configuración, aplica el sistema de diseño visual,
 * verifica la base de datos y arranca la interfaz gráfica.
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Iniciando Sistema de Gestión de Préstamos y Devoluciones de Biblioteca...");

        // Manejador global para excepciones no capturadas en hilos
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Excepción global no controlada en el hilo '{}': {}", thread.getName(), throwable.getMessage(), throwable);
        });

        
        // Configuración del tema visual y sistema de diseño global
        Tema.aplicar();

        // Ejecución en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // 1. Verificación de conexión a la base de datos MySQL
            boolean conexionOk = DatabaseCheck.verificarConexion();
            if (conexionOk) {
                try {
                    // 2. Tareas automáticas de arranque
                    ServiceFactory sf = ServiceFactory.getInstancia();
                    sf.getPrestamoService().actualizarVencidos();
                    sf.getReservaService().procesarExpiradas();
                    logger.info("Mantenimiento inicial de préstamos vencidos y reservas expiradas ejecutado.");
                } catch (Exception ex) {
                    logger.warn("No se pudo ejecutar el mantenimiento inicial de arranque: {}", ex.getMessage());
                }
            }

            // 3. Inicialización del LoginView y su controlador
            LoginView loginView = new LoginView();
            AuthController authController = new AuthController(loginView, ServiceFactory.getInstancia().getAuthService());
            authController.iniciar();
            logger.info("Aplicación iniciada correctamente con diseño moderno.");
        });
    }
}
