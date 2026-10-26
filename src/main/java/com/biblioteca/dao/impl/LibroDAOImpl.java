package com.biblioteca.dao.impl;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.model.Libro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC para LibroDAO.
 */
public class LibroDAOImpl implements LibroDAO {

    private static final Logger logger = LoggerFactory.getLogger(LibroDAOImpl.class);

    @Override
    public Libro guardar(Libro libro) {
        String sql = "INSERT INTO libro (isbn, titulo, autor, categoria, editorial, anio) VALUES (?, ?, ?, ?, ?, ?)";
        logger.debug("Ejecutando SQL: {}", sql);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getCategoria());
            ps.setString(5, libro.getEditorial());
            if (libro.getAnio() != null) {
                ps.setInt(6, libro.getAnio());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new DaoException("No se pudo registrar el libro en el catálogo.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    libro.setId(generatedKeys.getInt(1));
                }
            }
            return libro;

        } catch (SQLException e) {
            logger.error("Error al guardar libro ISBN '{}': {}", libro.getIsbn(), e.getMessage());
            throw new DaoException("Error al registrar el libro en la base de datos.", e);
        }
    }

    @Override
    public Optional<Libro> buscarPorId(int id) {
        String sql = "SELECT * FROM libro WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearLibro(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar libro por id {}: {}", id, e.getMessage());
            throw new DaoException("Error al buscar libro por identificador.", e);
        }
    }

    @Override
    public List<Libro> listar() {
        String sql = "SELECT * FROM libro ORDER BY titulo ASC";
        logger.debug("Ejecutando SQL: {}", sql);

        List<Libro> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearLibro(rs));
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al listar libros: {}", e.getMessage());
            throw new DaoException("Error al listar los libros del catálogo.", e);
        }
    }

    @Override
    public void actualizar(Libro libro) {
        String sql = "UPDATE libro SET isbn = ?, titulo = ?, autor = ?, categoria = ?, editorial = ?, anio = ? WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, libro.getId());

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, libro.getIsbn());
            ps.setString(2, libro.getTitulo());
            ps.setString(3, libro.getAutor());
            ps.setString(4, libro.getCategoria());
            ps.setString(5, libro.getEditorial());
            if (libro.getAnio() != null) {
                ps.setInt(6, libro.getAnio());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setInt(7, libro.getId());

            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al actualizar libro id {}: {}", libro.getId(), e.getMessage());
            throw new DaoException("Error al actualizar información del libro.", e);
        }
    }

    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM libro WHERE id = ?";
        logger.debug("Ejecutando SQL: {} con id={}", sql, id);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error al eliminar libro id {}: {}", id, e.getMessage());
            throw new DaoException("Error al eliminar libro del catálogo.", e);
        }
    }

    @Override
    public Optional<Libro> buscarPorIsbn(String isbn) {
        String sql = "SELECT * FROM libro WHERE isbn = ?";
        logger.debug("Ejecutando SQL: {} con isbn={}", sql, isbn);

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearLibro(rs));
                }
            }
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar libro por ISBN '{}': {}", isbn, e.getMessage());
            throw new DaoException("Error al buscar libro por ISBN.", e);
        }
    }

    @Override
    public List<Libro> buscarPorCriterio(String texto) {
        String sql = "SELECT * FROM libro WHERE titulo LIKE ? OR autor LIKE ? OR isbn LIKE ? OR categoria LIKE ? ORDER BY titulo ASC";
        logger.debug("Ejecutando SQL: {} con criterio='{}'", sql, texto);

        List<Libro> lista = new ArrayList<>();
        String patron = "%" + (texto != null ? texto.trim() : "") + "%";

        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            ps.setString(4, patron);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLibro(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al buscar libros con criterio '{}': {}", texto, e.getMessage());
            throw new DaoException("Error al consultar libros por criterio de búsqueda.", e);
        }
    }

    @Override
    public List<Libro> listarMasPrestados(int limite) {
        String sql = "SELECT l.id, l.isbn, l.titulo, l.autor, l.categoria, l.editorial, l.anio, COUNT(p.id) AS total_prestamos " +
                "FROM libro l " +
                "LEFT JOIN ejemplar e ON e.libro_id = l.id " +
                "LEFT JOIN prestamo p ON p.ejemplar_id = e.id " +
                "GROUP BY l.id, l.isbn, l.titulo, l.autor, l.categoria, l.editorial, l.anio " +
                "ORDER BY total_prestamos DESC, l.titulo ASC " +
                "LIMIT ?";
        logger.debug("Ejecutando SQL: {} con limite={}", sql, limite);

        List<Libro> lista = new ArrayList<>();
        try (Connection conn = GestorTransaccion.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearLibro(rs));
                }
            }
            return lista;

        } catch (SQLException e) {
            logger.error("Error al obtener libros más prestados: {}", e.getMessage());
            throw new DaoException("Error al consultar estadísticas de libros más prestados.", e);
        }
    }

    private Libro mapearLibro(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setId(rs.getInt("id"));
        l.setIsbn(rs.getString("isbn"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setCategoria(rs.getString("categoria"));
        l.setEditorial(rs.getString("editorial"));
        int anio = rs.getInt("anio");
        if (!rs.wasNull()) {
            l.setAnio(anio);
        }
        return l;
    }
}

