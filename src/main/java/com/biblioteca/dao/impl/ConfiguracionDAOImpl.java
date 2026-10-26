package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.ConfiguracionDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.Configuracion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Implementación JDBC para ConfiguracionDAO.
 */
public class ConfiguracionDAOImpl implements ConfiguracionDAO {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionDAOImpl.class);

    @Override
    public Optional<String> obtener(String clave) {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";
        logger.debug("Ejecutando SQL: {} con clave='{}'", sql, clave);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getString("valor"));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al obtener parámetro de configuración '{}': {}", clave, e.getMessage());
            throw new DaoException("Error al consultar parámetro de configuración.", e);
        }
    }

    @Override
    public void guardar(String clave, String valor) {
        String sql = "INSERT INTO configuracion (clave, valor) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE valor = VALUES(valor)";
        logger.debug("Ejecutando SQL: {} con clave='{}', valor='{}'", sql, clave, valor);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al guardar parámetro de configuración '{}': {}", clave, e.getMessage());
            throw new DaoException("Error al actualizar parámetro de configuración.", e);
        }
    }

    @Override
    public List<Configuracion> listar() {
        String sql = "SELECT clave, valor FROM configuracion ORDER BY clave ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Configuracion> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Configuracion(rs.getString("clave"), rs.getString("valor")));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar configuraciones: {}", e.getMessage());
            throw new DaoException("Error al obtener la lista de parámetros de configuración.", e);
        }
    }

    @Override
    public Map<String, String> listarComoMapa() {
        String sql = "SELECT clave, valor FROM configuracion";
        logger.debug("Ejecutando SQL: {}", sql);

        Map<String, String> mapa = new HashMap<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getString("clave"), rs.getString("valor"));
            }
            return mapa;

        } catch (SQLException e) {
            logger.error("Error al obtener mapa de configuraciones: {}", e.getMessage());
            throw new DaoException("Error al cargar mapa de configuración del sistema.", e);
        }
    }
}

