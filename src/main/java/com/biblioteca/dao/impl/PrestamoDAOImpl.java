package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.EstadoPrestamo;
import com.biblioteca.model.Prestamo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para PrestamoDAO.
 */
public class PrestamoDAOImpl implements PrestamoDAO {

    private static final Logger logger = LoggerFactory.getLogger(PrestamoDAOImpl.class);

    @Override
    public Prestamo guardar(Prestamo prestamo) {
        String sql = "INSERT INTO prestamo (cliente_id, ejemplar_id, bibliotecario_id, fecha_prestamo, " +
                "fecha_vencimiento, fecha_devolucion, estado, renovaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, prestamo.getClienteId());
            ps.setInt(2, prestamo.getEjemplarId());
            ps.setInt(3, prestamo.getBibliotecarioId());
            ps.setDate(4, Date.valueOf(prestamo.getFechaPrestamo()));
            ps.setDate(5, Date.valueOf(prestamo.getFechaVencimiento()));

            if (prestamo.getFechaDevolucion() != null) {
                ps.setDate(6, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, prestamo.getEstado().name());
            ps.setInt(8, prestamo.getRenovaciones());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar el préstamo.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    prestamo.setId(generatedKeys.getInt(1));
                }
            }
            return prestamo;

        } catch (SQLException e) {
            logger.error("Error al guardar préstamo: {}", e.getMessage());
            throw new DaoException("Error al registrar el préstamo en el sistema.", e);
        }
    }

    @Override
    public Optional<Prestamo> buscarPorId(int id) {
        String sql = "SELECT * FROM prestamo WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPrestamo(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar préstamo por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al buscar préstamo por identificador.", e);
        }
    }

    @Override
    public List<Prestamo> listar() {
        String sql = "SELECT * FROM prestamo ORDER BY fecha_prestamo DESC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Prestamo> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar préstamos: {}", e.getMessage());
            throw new DaoException("Error al obtener la lista de préstamos.", e);
        }
    }

    @Override
    public void actualizar(Prestamo prestamo) {
        String sql = "UPDATE prestamo SET cliente_id = ?, ejemplar_id = ?, bibliotecario_id = ?, " +
                "fecha_prestamo = ?, fecha_vencimiento = ?, fecha_devolucion = ?, estado = ?, renovaciones = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, prestamo.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, prestamo.getClienteId());
            ps.setInt(2, prestamo.getEjemplarId());
            ps.setInt(3, prestamo.getBibliotecarioId());
            ps.setDate(4, Date.valueOf(prestamo.getFechaPrestamo()));
            ps.setDate(5, Date.valueOf(prestamo.getFechaVencimiento()));

            if (prestamo.getFechaDevolucion() != null) {
                ps.setDate(6, Date.valueOf(prestamo.getFechaDevolucion()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            ps.setString(7, prestamo.getEstado().name());
            ps.setInt(8, prestamo.getRenovaciones());
            ps.setInt(9, prestamo.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar préstamo id {}: {}", prestamo.getId(), e.getMessage());
            throw new DaoException("Error al actualizar préstamo.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM prestamo WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar préstamo id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar préstamo.", e);
        }
    }

    @Override
    public Optional<Prestamo> buscarActivoPorEjemplar(int ejemplarId) {
        String sql = "SELECT * FROM prestamo WHERE ejemplar_id = ? AND estado IN ('ACTIVO', 'VENCIDO') ORDER BY id DESC LIMIT 1";
        logger.debug("Ejecutando SQL: {} con ejemplarId={}", sql, ejemplarId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ejemplarId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearPrestamo(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar préstamo activo para ejemplar {}: {}", ejemplarId, e.getMessage());
            throw new DaoException("Error al consultar préstamo del ejemplar.", e);
        }
    }

    @Override
    public List<Prestamo> listarActivosPorCliente(int clienteId) {
        String sql = "SELECT * FROM prestamo WHERE cliente_id = ? AND estado IN ('ACTIVO', 'VENCIDO') ORDER BY fecha_prestamo DESC";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        List<Prestamo> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar préstamos activos del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al consultar préstamos activos del cliente.", e);
        }
    }

    @Override
    public int contarActivosPorCliente(int clienteId) {
        String sql = "SELECT COUNT(*) FROM prestamo WHERE cliente_id = ? AND estado IN ('ACTIVO', 'VENCIDO')";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar préstamos activos del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al calcular total de préstamos activos.", e);
        }
    }

    @Override
    public List<Prestamo> listarVencidos() {
        String sql = "SELECT * FROM prestamo WHERE estado IN ('ACTIVO', 'VENCIDO') AND fecha_vencimiento < CURDATE() " +
                "ORDER BY fecha_vencimiento ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Prestamo> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar préstamos vencidos: {}", e.getMessage());
            throw new DaoException("Error al consultar préstamos vencidos.", e);
        }
    }

    @Override
    public List<Prestamo> listarPorCliente(int clienteId) {
        String sql = "SELECT * FROM prestamo WHERE cliente_id = ? ORDER BY fecha_prestamo DESC";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        List<Prestamo> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar historial de préstamos del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al consultar préstamos del cliente.", e);
        }
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setEjemplarId(rs.getInt("ejemplar_id"));
        p.setBibliotecarioId(rs.getInt("bibliotecario_id"));
        Date fp = rs.getDate("fecha_prestamo");
        if (fp != null) p.setFechaPrestamo(fp.toLocalDate());
        Date fv = rs.getDate("fecha_vencimiento");
        if (fv != null) p.setFechaVencimiento(fv.toLocalDate());
        Date fd = rs.getDate("fecha_devolucion");
        if (fd != null) p.setFechaDevolucion(fd.toLocalDate());
        p.setEstado(EstadoPrestamo.valueOf(rs.getString("estado")));
        p.setRenovaciones(rs.getInt("renovaciones"));
        return p;
    }
}

