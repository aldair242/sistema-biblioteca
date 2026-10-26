package com.biblioteca.config;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConexionBDTest {

    private boolean isDbReachable() {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Verificar que ConexionBD conecta a MySQL y existen las 8 tablas del esquema")
    void testConexionYTablasExistentes() throws SQLException {
        Assumptions.assumeTrue(isDbReachable(),
                "Se omite: Credenciales en db.properties no coinciden o MySQL no está disponible.");

        try (Connection conn = ConexionBD.obtenerConexion()) {
            assertNotNull(conn, "La conexión a la base de datos no debe ser nula");
            assertFalse(conn.isClosed(), "La conexión debe encontrarse abierta");
            assertTrue(conn.isValid(3), "La conexión a MySQL debe ser válida");

            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> tablasExistentes = new HashSet<>();

            try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String nombreTabla = rs.getString("TABLE_NAME");
                    if (nombreTabla != null) {
                        tablasExistentes.add(nombreTabla.toLowerCase());
                    }
                }
            }

            String[] tablasRequeridas = {
                    "usuario",
                    "cliente",
                    "libro",
                    "ejemplar",
                    "prestamo",
                    "reserva",
                    "multa",
                    "configuracion"
            };

            for (String tabla : tablasRequeridas) {
                assertTrue(tablasExistentes.contains(tabla.toLowerCase()),
                        "La tabla '" + tabla + "' debe existir en la base de datos MySQL");
            }
        }
    }

    @Test
    @DisplayName("Verificar que DatabaseCheck reporta conexión exitosa")
    void testDatabaseCheck() {
        Assumptions.assumeTrue(isDbReachable(),
                "Se omite: Credenciales en db.properties no coinciden o MySQL no está disponible.");
        boolean conexionValida = DatabaseCheck.verificarConexion();
        assertTrue(conexionValida, "DatabaseCheck debe retornar true con conexión activa");
    }
}

