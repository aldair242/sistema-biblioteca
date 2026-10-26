package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.ReservaDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.EstadoReserva;
import com.biblioteca.model.Reserva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para ReservaDAO.
 */
public class ReservaDAOImpl implements ReservaDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReservaDAOImpl.class);

    @Override
    public Reserva guardar(Reserva reserva) {
        String sql = "INSERT INTO reserva (cliente_id, libro_id, ejemplar_id, fecha_reserva, fecha_expiracion, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reserva.getClienteId());
            ps.setInt(2, reserva.getLibroId());

            if (reserva.getEjemplarId() != null) {
                ps.setInt(3, reserva.getEjemplarId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setTimestamp(4, Timestamp.valueOf(reserva.getFechaReserva() != null ? reserva.getFechaReserva() : java.time.LocalDateTime.now()));

            if (reserva.getFechaExpiracion() != null) {
                ps.setDate(5, Date.valueOf(reserva.getFechaExpiracion()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setString(6, reserva.getEstado().name());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar la reserva.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reserva.setId(generatedKeys.getInt(1));
                }
            }
            return reserva;

        } catch (SQLException e) {
            logger.error("Error al guardar reserva: {}", e.getMessage());
            throw new DaoException("Error al registrar la reserva en la base de datos.", e);
        }
    }

    @Override
    public Optional<Reserva> buscarPorId(int id) {
        String sql = "SELECT * FROM reserva WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearReserva(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar reserva por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al consultar reserva por identificador.", e);
        }
    }

    @Override
    public List<Reserva> listar() {
        String sql = "SELECT * FROM reserva ORDER BY fecha_reserva DESC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar reservas: {}", e.getMessage());
            throw new DaoException("Error al obtener la lista de reservas.", e);
        }
    }

    @Override
    public void actualizar(Reserva reserva) {
        String sql = "UPDATE reserva SET cliente_id = ?, libro_id = ?, ejemplar_id = ?, " +
                "fecha_reserva = ?, fecha_expiracion = ?, estado = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, reserva.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reserva.getClienteId());
            ps.setInt(2, reserva.getLibroId());

            if (reserva.getEjemplarId() != null) {
                ps.setInt(3, reserva.getEjemplarId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setTimestamp(4, Timestamp.valueOf(reserva.getFechaReserva()));

            if (reserva.getFechaExpiracion() != null) {
                ps.setDate(5, Date.valueOf(reserva.getFechaExpiracion()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.setString(6, reserva.getEstado().name());
            ps.setInt(7, reserva.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar reserva id {}: {}", reserva.getId(), e.getMessage());
            throw new DaoException("Error al actualizar la reserva.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM reserva WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar reserva id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar la reserva.", e);
        }
    }

    @Override
    public List<Reserva> listarColaPorLibro(int libroId) {
        String sql = "SELECT * FROM reserva WHERE libro_id = ? AND estado = 'PENDIENTE' ORDER BY fecha_reserva ASC";
        logger.debug("Ejecutando SQL: {} con libroId={}", sql, libroId);

        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al obtener cola FIFO de reservas para libro {}: {}", libroId, e.getMessage());
            throw new DaoException("Error al listar cola de reservas del libro.", e);
        }
    }

    @Override
    public List<Reserva> listarPorCliente(int clienteId) {
        String sql = "SELECT * FROM reserva WHERE cliente_id = ? ORDER BY fecha_reserva DESC";
        logger.debug("Ejecutando SQL: {} con clienteId={}", sql, clienteId);

        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar reservas del cliente {}: {}", clienteId, e.getMessage());
            throw new DaoException("Error al consultar reservas del cliente.", e);
        }
    }

    @Override
    public List<Reserva> listarListasPorLibro(int libroId) {
        String sql = "SELECT * FROM reserva WHERE libro_id = ? AND estado = 'LISTA' ORDER BY fecha_reserva ASC";
        logger.debug("Ejecutando SQL: {} con libroId={}", sql, libroId);

        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, libroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReserva(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar reservas listas para libro {}: {}", libroId, e.getMessage());
            throw new DaoException("Error al listar reservas listas.", e);
        }
    }

    @Override
    public List<Reserva> listarExpiradas() {
        String sql = "SELECT * FROM reserva WHERE estado = 'LISTA' AND fecha_expiracion < CURDATE() ORDER BY fecha_expiracion ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Reserva> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearReserva(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar reservas expiradas: {}", e.getMessage());
            throw new DaoException("Error al consultar reservas expiradas.", e);
        }
    }

    @Override
    public Optional<Reserva> buscarReservaActivaClienteLibro(int clienteId, int libroId) {
        String sql = "SELECT * FROM reserva WHERE cliente_id = ? AND libro_id = ? AND estado IN ('PENDIENTE', 'LISTA') " +
                "ORDER BY fecha_reserva DESC LIMIT 1";
        logger.debug("Ejecutando SQL: {} con clienteId={}, libroId={}", sql, clienteId, libroId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            ps.setInt(2, libroId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearReserva(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al verificar reserva activa de cliente {} para libro {}: {}", clienteId, libroId, e.getMessage());
            throw new DaoException("Error al validar reserva previa del libro.", e);
        }
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setClienteId(rs.getInt("cliente_id"));
        r.setLibroId(rs.getInt("libro_id"));
        int ejemplarId = rs.getInt("ejemplar_id");
        if (!rs.wasNull()) {
            r.setEjemplarId(ejemplarId);
        }
        Timestamp ts = rs.getTimestamp("fecha_reserva");
        if (ts != null) {
            r.setFechaReserva(ts.toLocalDateTime());
        }
        Date exp = rs.getDate("fecha_expiracion");
        if (exp != null) {
            r.setFechaExpiracion(exp.toLocalDate());
        }
        r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        return r;
    }
}

