package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.MultaDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.EstadoMulta;
import com.biblioteca.model.Multa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para MultaDAO.
 */
public class MultaDAOImpl implements MultaDAO {

    private static final Logger logger = LoggerFactory.getLogger(MultaDAOImpl.class);

    @Override
    public Multa guardar(Multa multa) {
        String sql = "INSERT INTO multa (prestamo_id, dias_retraso, tarifa_aplicada, monto, estado, fecha_pago) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, multa.getPrestamoId());
            ps.setInt(2, multa.getDiasRetraso());
            ps.setBigDecimal(3, multa.getTarifaAplicada());
            ps.setBigDecimal(4, multa.getMonto());
            ps.setString(5, multa.getEstado().name());

            if (multa.getFechaPago() != null) {
                ps.setDate(6, Date.valueOf(multa.getFechaPago()));
            } else {
                ps.setNull(6, Types.DATE);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar la multa.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    multa.setId(generatedKeys.getInt(1));
                }
            }
            return multa;

        } catch (SQLException e) {
            logger.error("Error al guardar multa para préstamo id {}: {}", multa.getPrestamoId(), e.getMessage());
            throw new DaoException("Error al registrar la multa en el sistema.", e);
        }
    }

    @Override
    public Optional<Multa> buscarPorId(int id) {
        String sql = "SELECT * FROM multa WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMulta(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar multa id {}: {}", id, e.getMessage());
            throw new DaoException("Error al consultar multa por identificador.", e);
        }
    }

    @Override
    public List<Multa> listar() {
        String sql = "SELECT * FROM multa ORDER BY id DESC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Multa> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMulta(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar multas: {}", e.getMessage());
            throw new DaoException("Error al consultar el listado de multas.", e);
        }
    }

    @Override
    public void actualizar(Multa multa) {
        String sql = "UPDATE multa SET prestamo_id = ?, dias_retraso = ?, tarifa_aplicada = ?, " +
                "monto = ?, estado = ?, fecha_pago = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, multa.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, multa.getPrestamoId());
            ps.setInt(2, multa.getDiasRetraso());
            ps.setBigDecimal(3, multa.getTarifaAplicada());
            ps.setBigDecimal(4, multa.getMonto());
            ps.setString(5, multa.getEstado().name());

            if (multa.getFechaPago() != null) {
                ps.setDate(6, Date.valueOf(multa.getFechaPago()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setInt(7, multa.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar multa id {}: {}", multa.getId(), e.getMessage());
            throw new DaoException("Error al actualizar la información de la multa.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM multa WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar multa id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar multa.", e);
        }
    }

    @Override
    public List<Multa> listarPendientesPorCliente(int clienteId) {
        String sql = "SELECT m.* FROM multa m " +
                "JOIN prestamo p ON p.id = m.prestamo_id " +
                "WHERE p.cliente_id = ? AND m.estado = 'PENDIENTE' " +
                "ORDER BY m.id DESC";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        List<Multa> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMulta(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar multas pendientes del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al consultar multas pendientes del cliente.", e);
        }
    }

    @Override
    public Optional<Multa> buscarPorPrestamo(int prestamoId) {
        String sql = "SELECT * FROM multa WHERE prestamo_id = ?";
        logger.debug("Ejecutando SQL: {} con prestamoId={}", sql, prestamoId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, prestamoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMulta(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar multa por prestamo id {}: {}", prestamoId, e.getMessage());
            throw new DaoException("Error al consultar multa asociada al préstamo.", e);
        }
    }

    @Override
    public List<Multa> listarPorCliente(int clienteId) {
        String sql = "SELECT m.* FROM multa m " +
                "JOIN prestamo p ON p.id = m.prestamo_id " +
                "WHERE p.cliente_id = ? " +
                "ORDER BY m.id DESC";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        List<Multa> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMulta(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar historial de multas del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al consultar el historial de multas del cliente.", e);
        }
    }

    private Multa mapearMulta(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setId(rs.getInt("id"));
        m.setPrestamoId(rs.getInt("prestamo_id"));
        m.setDiasRetraso(rs.getInt("dias_retraso"));
        m.setTarifaAplicada(rs.getBigDecimal("tarifa_aplicada"));
        m.setMonto(rs.getBigDecimal("monto"));
        m.setEstado(EstadoMulta.valueOf(rs.getString("estado")));
        Date fp = rs.getDate("fecha_pago");
        if (fp != null) {
            m.setFechaPago(fp.toLocalDate());
        }
        return m;
    }
}

