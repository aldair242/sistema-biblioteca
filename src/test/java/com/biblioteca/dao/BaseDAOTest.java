package com.biblioteca.dao;

import com.biblioteca.config.ConexionBD;
import com.biblioteca.config.GestorTransaccion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * Clase base para pruebas de integración de la capa DAO contra MySQL.
 * Inicia una transacción antes de cada prueba y la revierte en el @AfterEach
 * para garantizar el aislamiento y evitar ensuciar los datos de la base de datos.
 */
public abstract class BaseDAOTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected boolean baseDeDatosDisponible = false;

    @BeforeEach
    void iniciarTransaccion() {
        try {
            try (Connection conn = ConexionBD.obtenerConexion()) {
                baseDeDatosDisponible = conn != null && !conn.isClosed() && conn.isValid(2);
            }
            if (baseDeDatosDisponible) {
                GestorTransaccion.iniciarTransaccionManual();
            }
        } catch (Exception e) {
            baseDeDatosDisponible = false;
            logger.warn("Conexión a MySQL no disponible para pruebas DAO: {}", e.getMessage());
        }

        Assumptions.assumeTrue(baseDeDatosDisponible,
                "Se omite la prueba: el servidor MySQL o las credenciales en db.properties no están configuradas.");
    }

    @AfterEach
    void revertirTransaccion() {
        if (baseDeDatosDisponible) {
            GestorTransaccion.revertirTransaccionManual();
        }
    }
}

