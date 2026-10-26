package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para UsuarioDAO.
 */
public class UsuarioDAOImpl implements UsuarioDAO {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);

    @Override
    public Usuario guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (username, password_hash, rol, nombre, activo, debe_cambiar_clave, intentos_fallidos, bloqueado_hasta) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            ps.setString(4, usuario.getNombre());
            ps.setBoolean(5, usuario.isActivo());
            ps.setBoolean(6, usuario.isDebeCambiarClave());
            ps.setInt(7, usuario.getIntentosFallidos());

            if (usuario.getBloqueadoHasta() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(usuario.getBloqueadoHasta()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar el usuario en la base de datos.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getInt(1));
                }
            }
            return usuario;

        } catch (SQLException e) {
            logger.error("Error al guardar usuario '{}': {}", usuario.getUsername(), e.getMessage());
            throw new DaoException("Error al registrar el usuario en la base de datos.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar usuario por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al buscar el usuario.", e);
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql = "SELECT * FROM usuario ORDER BY id ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearUsuario(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar usuarios: {}", e.getMessage());
            throw new DaoException("Error al listar los usuarios del sistema.", e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET username = ?, password_hash = ?, rol = ?, nombre = ?, activo = ?, " +
                "debe_cambiar_clave = ?, intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} para id={}", sql, usuario.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usuario.getUsername());
            ps.setString(2, usuario.getPasswordHash());
            ps.setString(3, usuario.getRol().name());
            ps.setString(4, usuario.getNombre());
            ps.setBoolean(5, usuario.isActivo());
            ps.setBoolean(6, usuario.isDebeCambiarClave());
            ps.setInt(7, usuario.getIntentosFallidos());

            if (usuario.getBloqueadoHasta() != null) {
                ps.setTimestamp(8, Timestamp.valueOf(usuario.getBloqueadoHasta()));
            } else {
                ps.setNull(8, Types.TIMESTAMP);
            }
            ps.setInt(9, usuario.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar usuario con id {}: {}", usuario.getId(), e.getMessage());
            throw new DaoException("Error al actualizar datos del usuario.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar usuario id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar el usuario.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        String sql = "SELECT * FROM usuario WHERE username = ?";
        logger.debug("Ejecutando SQL: {} con username={}", sql, username);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar usuario por username '{}': {}", username, e.getMessage());
            throw new DaoException("Error al buscar usuario por nombre de cuenta.", e);
        }
    }

    @Override
    public void actualizarIntentosFallidos(int id, int intentos, LocalDateTime bloqueadoHasta) {
        String sql = "UPDATE usuario SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}, intentos={}", sql, id, intentos);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, intentos);
            if (bloqueadoHasta != null) {
                ps.setTimestamp(2, Timestamp.valueOf(bloqueadoHasta));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }
            ps.setInt(3, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar intentos fallidos de usuario id {}: {}", id, e.getMessage());
            throw new DaoException("Error al actualizar intentos de acceso del usuario.", e);
        }
    }

    @Override
    public void cambiarClave(int id, String nuevoPasswordHash, boolean debeCambiarClave) {
        String sql = "UPDATE usuario SET password_hash = ?, debe_cambiar_clave = ?, intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id = ?";
        logger.debug("Ejecutando SQL: {} para id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoPasswordHash);
            ps.setBoolean(2, debeCambiarClave);
            ps.setInt(3, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al cambiar contraseña de usuario id {}: {}", id, e.getMessage());
            throw new DaoException("Error al modificar la contraseña del usuario.", e);
        }
    }

    @Override
    public void cambiarEstadoActivo(int id, boolean activo) {
        String sql = "UPDATE usuario SET activo = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}, activo={}", sql, id, activo);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, id);

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al cambiar estado activo de usuario id {}: {}", id, e.getMessage());
            throw new DaoException("Error al cambiar estado del usuario.", e);
        }
    }

    @Override
    public List<Usuario> listarPorRol(Rol rol) {
        String sql = "SELECT * FROM usuario WHERE rol = ? ORDER BY nombre ASC";
        logger.debug("Ejecutando SQL: {} con rol={}", sql, rol);

        List<Usuario> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rol.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearUsuario(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar usuarios por rol {}: {}", rol, e.getMessage());
            throw new DaoException("Error al listar usuarios por rol.", e);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setNombre(rs.getString("nombre"));
        u.setActivo(rs.getBoolean("activo"));
        u.setDebeCambiarClave(rs.getBoolean("debe_cambiar_clave"));
        u.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        Timestamp ts = rs.getTimestamp("bloqueado_hasta");
        if (ts != null) {
            u.setBloqueadoHasta(ts.toLocalDateTime());
        }
        return u;
    }
}

