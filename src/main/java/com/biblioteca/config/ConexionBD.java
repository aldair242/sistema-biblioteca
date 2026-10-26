package com.biblioteca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor de conexiones JDBC para MySQL 8.
 * Lee las credenciales de db.properties desde el classpath y proporciona conexiones activas.
 */
public final class ConexionBD {

    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);
    private static final String ARCHIVO_PROPIEDADES = "db.properties";

    private static String url;
    private static String usuario;
    private static String clave;
    private static boolean inicializado = false;

    private ConexionBD() {
        // Constructor privado para clase utilitaria
    }

    private static synchronized void cargarPropiedades() {
        if (inicializado) {
            return;
        }

        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (input == null) {
                String error = "No se encontró el archivo '" + ARCHIVO_PROPIEDADES + "' en el classpath. " +
                        "Cree dicho archivo en src/main/resources/ copiando el archivo db.properties.example.";
                logger.error(error);
                throw new IllegalStateException(error);
            }

            props.load(input);
            url = props.getProperty("db.url");
            usuario = props.getProperty("db.user");
            clave = props.getProperty("db.password");

            if (url == null || url.trim().isEmpty() ||
                usuario == null || usuario.trim().isEmpty() ||
                clave == null) {
                String error = "El archivo '" + ARCHIVO_PROPIEDADES + "' tiene credenciales incompletas. " +
                        "Asegúrese de definir 'db.url', 'db.user' y 'db.password'.";
                logger.error(error);
                throw new IllegalStateException(error);
            }

            // Registrar driver explícitamente para mayor robustez
            Class.forName("com.mysql.cj.jdbc.Driver");
            inicializado = true;
            logger.info("Configuración de base de datos cargada correctamente. Conectando a {}", url);

        } catch (IOException e) {
            String error = "Error al leer el archivo de propiedades de base de datos: " + ARCHIVO_PROPIEDADES;
            logger.error(error, e);
            throw new IllegalStateException(error, e);
        } catch (ClassNotFoundException e) {
            String error = "No se encontró el controlador JDBC de MySQL (com.mysql.cj.jdbc.Driver)";
            logger.error(error, e);
            throw new IllegalStateException(error, e);
        }
    }

    /**
     * Obtiene una nueva conexión directa a la base de datos MySQL.
     *
     * @return Connection activa
     * @throws SQLException Si ocurre un error al conectar con el servidor
     */
    public static Connection obtenerConexion() throws SQLException {
        if (!inicializado) {
            cargarPropiedades();
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    public static String getUrl() {
        if (!inicializado) {
            cargarPropiedades();
        }
        return url;
    }

    public static String getUsuario() {
        if (!inicializado) {
            cargarPropiedades();
        }
        return usuario;
    }
}

