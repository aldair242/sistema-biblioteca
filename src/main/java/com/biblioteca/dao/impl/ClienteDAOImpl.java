package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.ClienteDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para ClienteDAO.
 */
public class ClienteDAOImpl implements ClienteDAO {

    private static final Logger logger = LoggerFactory.getLogger(ClienteDAOImpl.class);

    @Override
    public Cliente guardar(Cliente cliente) {
        String sql = "INSERT INTO cliente (usuario_id, dni, nombres, apellidos, email, telefono, fecha_registro) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, cliente.getUsuarioId());
            ps.setString(2, cliente.getDni());
            ps.setString(3, cliente.getNombres());
            ps.setString(4, cliente.getApellidos());
            ps.setString(5, cliente.getEmail());
            ps.setString(6, cliente.getTelefono());
            ps.setDate(7, Date.valueOf(cliente.getFechaRegistro() != null ? cliente.getFechaRegistro() : java.time.LocalDate.now()));

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar el cliente en la base de datos.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cliente.setId(generatedKeys.getInt(1));
                }
            }
            return cliente;

        } catch (SQLException e) {
            logger.error("Error al guardar cliente DNI '{}': {}", cliente.getDni(), e.getMessage());
            throw new DaoException("Error al registrar cliente en la base de datos.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT * FROM cliente WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar cliente por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al buscar cliente por identificador.", e);
        }
    }

    @Override
    public List<Cliente> listar() {
        String sql = "SELECT * FROM cliente ORDER BY apellidos, nombres ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Cliente> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearCliente(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar clientes: {}", e.getMessage());
            throw new DaoException("Error al listar los clientes.", e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET dni = ?, nombres = ?, apellidos = ?, email = ?, telefono = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, cliente.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cliente.getDni());
            ps.setString(2, cliente.getNombres());
            ps.setString(3, cliente.getApellidos());
            ps.setString(4, cliente.getEmail());
            ps.setString(5, cliente.getTelefono());
            ps.setInt(6, cliente.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar cliente id {}: {}", cliente.getId(), e.getMessage());
            throw new DaoException("Error al actualizar la información del cliente.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar cliente id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar el cliente de la base de datos.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorDni(String dni) {
        String sql = "SELECT * FROM cliente WHERE dni = ?";
        logger.debug("Ejecutando SQL: {} con dni={}", sql, dni);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar cliente por DNI '{}': {}", dni, e.getMessage());
            throw new DaoException("Error al consultar cliente por DNI.", e);
        }
    }

    @Override
    public List<Cliente> buscarPorTexto(String texto) {
        String sql = "SELECT * FROM cliente WHERE dni LIKE ? OR nombres LIKE ? OR apellidos LIKE ? ORDER BY apellidos, nombres ASC";
        logger.debug("Ejecutando SQL: {} con criterio='{}'", sql, texto);

        List<Cliente> lista = new ArrayList<>();
        String patron = "%" + (texto != null ? texto.trim() : "") + "%";

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCliente(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al buscar clientes por texto '{}': {}", texto, e.getMessage());
            throw new DaoException("Error al realizar búsqueda de clientes.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorUsuarioId(int usuarioId) {
        String sql = "SELECT * FROM cliente WHERE usuario_id = ?";
        logger.debug("Ejecutando SQL: {} con usuario_id={}", sql, usuarioId);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar cliente por usuario_id {}: {}", usuarioId, e.getMessage());
            throw new DaoException("Error al consultar perfil del cliente.", e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        String sql = "SELECT * FROM cliente WHERE LOWER(email) = LOWER(?)";
        logger.debug("Ejecutando SQL: {} con email='{}'", sql, email);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCliente(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar cliente por email '{}': {}", email, e.getMessage());
            throw new DaoException("Error al buscar cliente por correo electrónico.", e);
        }
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setUsuarioId(rs.getInt("usuario_id"));
        c.setDni(rs.getString("dni"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        Date f = rs.getDate("fecha_registro");
        if (f != null) {
            c.setFechaRegistro(f.toLocalDate());
        }
        return c;
    }
}

