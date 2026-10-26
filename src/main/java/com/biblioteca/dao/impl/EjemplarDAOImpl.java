package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.EstadoEjemplar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para EjemplarDAO.
 */
public class EjemplarDAOImpl implements EjemplarDAO {

    private static final Logger logger = LoggerFactory.getLogger(EjemplarDAOImpl.class);

    @Override
    public Ejemplar guardar(Ejemplar ejemplar) {
        String sql = "INSERT INTO ejemplar (libro_id, codigo_barras, estado) VALUES (?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ejemplar.getLibroId());
            ps.setString(2, ejemplar.getCodigoBarras());
            ps.setString(3, ejemplar.getEstado().name());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar el ejemplar.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ejemplar.setId(generatedKeys.getInt(1));
                }
            }
            return ejemplar;

        } catch (SQLException e) {
            logger.error("Error al guardar ejemplar '{}': {}", ejemplar.getCodigoBarras(), e.getMessage());
            throw new DaoException("Error al registrar el ejemplar en la base de datos.", e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorId(int id) {
        String sql = "SELECT * FROM ejemplar WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEjemplar(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar ejemplar por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al buscar ejemplar por identificador.", e);
        }
    }

    @Override
    public List<Ejemplar> listar() {
        String sql = "SELECT * FROM ejemplar ORDER BY codigo_barras ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Ejemplar> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearEjemplar(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar ejemplares: {}", e.getMessage());
            throw new DaoException("Error al listar los ejemplares.", e);
        }
    }

    @Override
    public void actualizar(Ejemplar ejemplar) {
        String sql = "UPDATE ejemplar SET libro_id = ?, codigo_barras = ?, estado = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, ejemplar.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ejemplar.getLibroId());
            ps.setString(2, ejemplar.getCodigoBarras());
            ps.setString(3, ejemplar.getEstado().name());
            ps.setInt(4, ejemplar.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar ejemplar id {}: {}", ejemplar.getId(), e.getMessage());
            throw new DaoException("Error al actualizar el ejemplar.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM ejemplar WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar ejemplar id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar ejemplar.", e);
        }
    }

    @Override
    public List<Ejemplar> listarPorLibro(int libroId) {
        String sql = "SELECT * FROM ejemplar WHERE libro_id = ? ORDER BY codigo_barras ASC";
        logger.debug("Ejecutando SQL: {} con libro_id={}", sql, libroId);

        List<Ejemplar> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEjemplar(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar ejemplares por libro id {}: {}", libroId, e.getMessage());
            throw new DaoException("Error al consultar ejemplares del libro.", e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarPorCodigoBarras(String codigoBarras) {
        String sql = "SELECT * FROM ejemplar WHERE codigo_barras = ?";
        logger.debug("Ejecutando SQL: {} con codigo={}", sql, codigoBarras);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoBarras);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEjemplar(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar ejemplar por código '{}': {}", codigoBarras, e.getMessage());
            throw new DaoException("Error al consultar ejemplar por código de barras.", e);
        }
    }

    @Override
    public int contarDisponibles(int libroId) {
        String sql = "SELECT COUNT(*) FROM ejemplar WHERE libro_id = ? AND estado = 'DISPONIBLE'";
        logger.debug("Ejecutando SQL: {} con libro_id={}", sql, libroId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar disponibles para libro id {}: {}", libroId, e.getMessage());
            throw new DaoException("Error al verificar disponibilidad de ejemplares.", e);
        }
    }

    @Override
    public Optional<Ejemplar> buscarDisponiblePorLibro(int libroId) {
        String sql = "SELECT * FROM ejemplar WHERE libro_id = ? AND estado = 'DISPONIBLE' ORDER BY id ASC LIMIT 1";
        logger.debug("Ejecutando SQL: {} con libro_id={}", sql, libroId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEjemplar(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar ejemplar disponible para libro {}: {}", libroId, e.getMessage());
            throw new DaoException("Error al obtener un ejemplar disponible.", e);
        }
    }

    @Override
    public void actualizarEstado(int id, EstadoEjemplar nuevoEstado) {
        String sql = "UPDATE ejemplar SET estado = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}, estado={}", sql, id, nuevoEstado);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar estado de ejemplar id {}: {}", id, e.getMessage());
            throw new DaoException("Error al cambiar estado del ejemplar.", e);
        }
    }

    private Ejemplar mapearEjemplar(ResultSet rs) throws SQLException {
        Ejemplar e = new Ejemplar();
        e.setId(rs.getInt("id"));
        e.setLibroId(rs.getInt("libro_id"));
        e.setCodigoBarras(rs.getString("codigo_barras"));
        e.setEstado(EstadoEjemplar.valueOf(rs.getString("estado")));
        return e;
    }
}

