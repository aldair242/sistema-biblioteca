package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.dao.ReservaDAO;
import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.EstadoEjemplar;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Rol;
import com.biblioteca.security.Autorizador;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión del catálogo bibliográfico y control de ejemplares físicos.
 */
public class CatalogoService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogoService.class);

    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;
    private final PrestamoDAO prestamoDAO;
    private final ReservaDAO reservaDAO;

    public CatalogoService(LibroDAO libroDAO, EjemplarDAO ejemplarDAO, PrestamoDAO prestamoDAO, ReservaDAO reservaDAO) {
        this.libroDAO = libroDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.prestamoDAO = prestamoDAO;
        this.reservaDAO = reservaDAO;
    }

    /**
     * Registra un libro en el catálogo junto con una cantidad inicial de ejemplares físicos.
     * Operación transaccional.
     *
     * @param libro Entidad Libro a registrar
     * @param cantidadEjemplares Cantidad de ejemplares iniciales a generar
     * @return Libro persistido
     */
    public Libro registrarLibro(Libro libro, int cantidadEjemplares) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Preconditions.checkNotNull(libro, "El libro no puede ser nulo.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(libro.getTitulo()), "El título del libro es obligatorio.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(libro.getIsbn()), "El ISBN del libro es obligatorio.");
        Preconditions.checkArgument(cantidadEjemplares >= 1, "Debe registrar al menos 1 ejemplar inicial.");

        if (libroDAO.buscarPorIsbn(libro.getIsbn().trim()).isPresent()) {
            throw new ValidacionException("Ya existe un libro con el ISBN " + libro.getIsbn());
        }

        return GestorTransaccion.ejecutar(() -> {
            Libro libroGuardado = libroDAO.guardar(libro);

            // Generar ejemplares con código único L{libro_id:04d}-{n:03d}
            for (int i = 1; i <= cantidadEjemplares; i++) {
                String codigoBarras = String.format("L%04d-%03d", libroGuardado.getId(), i);
                Ejemplar ejemplar = new Ejemplar(null, libroGuardado.getId(), codigoBarras, EstadoEjemplar.DISPONIBLE);
                ejemplarDAO.guardar(ejemplar);
            }

            logger.info("Libro '{}' registrado con {} ejemplares", libroGuardado.getTitulo(), cantidadEjemplares);
            return libroGuardado;
        });
    }

    public void actualizarLibro(Libro libro) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Preconditions.checkNotNull(libro, "El libro no puede ser nulo.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(libro.getTitulo()), "El título del libro es obligatorio.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(libro.getIsbn()), "El ISBN del libro es obligatorio.");

        // Verificar si otro libro ya usa el mismo ISBN
        Optional<Libro> existente = libroDAO.buscarPorIsbn(libro.getIsbn().trim());
        if (existente.isPresent() && !existente.get().getId().equals(libro.getId())) {
            throw new ValidacionException("El ISBN " + libro.getIsbn() + " ya está asignado a otro libro en el catálogo.");
        }

        libroDAO.actualizar(libro);
        logger.info("Libro ID {} actualizado correctamente", libro.getId());
    }

    public void eliminarLibro(int libroId) {
        Autorizador.requerir(Rol.ADMIN);

        List<Ejemplar> ejemplares = ejemplarDAO.listarPorLibro(libroId);
        for (Ejemplar ej : ejemplares) {
            if (ej.getEstado() == EstadoEjemplar.PRESTADO) {
                throw new ValidacionException("No se puede eliminar el libro porque tiene ejemplares actualmente prestados.");
            }
        }

        if (!reservaDAO.listarColaPorLibro(libroId).isEmpty()) {
            throw new ValidacionException("No se puede eliminar el libro porque cuenta con reservas pendientes.");
        }

        try {
            GestorTransaccion.ejecutar(() -> {
                for (Ejemplar ej : ejemplares) {
                    ejemplarDAO.eliminar(ej.getId());
                }
                libroDAO.eliminar(libroId);
                logger.info("Libro ID {} y sus ejemplares eliminados del catálogo", libroId);
                return null;
            });
        } catch (DaoException de) {
            logger.warn("No se pudo eliminar físicamente el libro ID {}: {}", libroId, de.getMessage());
            throw new ValidacionException("No se puede eliminar físicamente este libro porque sus ejemplares cuentan con " +
                    "historial de préstamos en el sistema. Cambie el estado de sus ejemplares a 'BAJA'.");
        }
    }

    public Ejemplar agregarEjemplar(int libroId, String codigoBarras) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(codigoBarras), "El código de barras no puede estar vacío.");

        if (ejemplarDAO.buscarPorCodigoBarras(codigoBarras.trim()).isPresent()) {
            throw new ValidacionException("Ya existe un ejemplar registrado con el código de barras " + codigoBarras);
        }

        Ejemplar ejemplar = new Ejemplar(null, libroId, codigoBarras.trim(), EstadoEjemplar.DISPONIBLE);
        return ejemplarDAO.guardar(ejemplar);
    }

    /**
     * Agrega múltiples ejemplares físicos a un libro existente generando códigos de barras secuenciales.
     *
     * @param libroId ID del libro
     * @param cantidad Cantidad de ejemplares a agregar
     * @return Lista de ejemplares creados
     */
    public List<Ejemplar> agregarMultiplesEjemplares(int libroId, int cantidad) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Preconditions.checkArgument(cantidad >= 1, "La cantidad de ejemplares debe ser mayor o igual a 1.");

        Libro libro = libroDAO.buscarPorId(libroId)
                .orElseThrow(() -> new ValidacionException("No se encontró el libro con ID " + libroId));

        return GestorTransaccion.ejecutar(() -> {
            List<Ejemplar> existentes = ejemplarDAO.listarPorLibro(libroId);
            int nextSeq = existentes.size() + 1;
            List<Ejemplar> creados = new ArrayList<>();

            for (int i = 0; i < cantidad; i++) {
                String codigo;
                do {
                    codigo = String.format("L%04d-%03d", libroId, nextSeq++);
                } while (ejemplarDAO.buscarPorCodigoBarras(codigo).isPresent());

                Ejemplar ej = new Ejemplar(null, libroId, codigo, EstadoEjemplar.DISPONIBLE);
                creados.add(ejemplarDAO.guardar(ej));
            }

            logger.info("Se agregaron {} nuevos ejemplares al libro '{}' (ID {})", cantidad, libro.getTitulo(), libroId);
            return creados;
        });
    }

    /**
     * Elimina físicamente un ejemplar si no está prestado y no tiene historial protegido.
     *
     * @param ejemplarId ID del ejemplar a eliminar
     */
    public void eliminarEjemplar(int ejemplarId) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        Ejemplar ej = ejemplarDAO.buscarPorId(ejemplarId)
                .orElseThrow(() -> new ValidacionException("No se encontró el ejemplar con ID " + ejemplarId));

        if (ej.getEstado() == EstadoEjemplar.PRESTADO) {
            throw new ValidacionException("No se puede eliminar un ejemplar que se encuentra actualmente PRESTADO.");
        }

        try {
            ejemplarDAO.eliminar(ejemplarId);
            logger.info("Ejemplar ID {} ('{}') eliminado exitosamente", ejemplarId, ej.getCodigoBarras());
        } catch (DaoException de) {
            logger.warn("No se pudo eliminar físicamente el ejemplar ID {}: {}", ejemplarId, de.getMessage());
            throw new ValidacionException("No se puede eliminar físicamente el ejemplar '" + ej.getCodigoBarras() +
                    "' porque tiene historial de préstamos asociado. Modifique su estado a 'BAJA'.");
        }
    }

    public void cambiarEstadoEjemplar(int ejemplarId, EstadoEjemplar nuevoEstado) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Preconditions.checkNotNull(nuevoEstado, "El nuevo estado no puede ser nulo.");

        Ejemplar ejemplar = ejemplarDAO.buscarPorId(ejemplarId)
                .orElseThrow(() -> new ValidacionException("No se encontró el ejemplar ID " + ejemplarId));

        if (ejemplar.getEstado() == EstadoEjemplar.PRESTADO && (nuevoEstado == EstadoEjemplar.BAJA || nuevoEstado == EstadoEjemplar.EN_MANTENIMIENTO)) {
            throw new ValidacionException("No se puede dar de baja ni pasar a mantenimiento un ejemplar que se encuentra PRESTADO.");
        }

        ejemplarDAO.actualizarEstado(ejemplarId, nuevoEstado);
        logger.info("Estado del ejemplar '{}' (ID {}) actualizado a {}", ejemplar.getCodigoBarras(), ejemplarId, nuevoEstado);
    }

    public List<Libro> buscarLibros(String criterio) {
        if (Strings.isNullOrEmpty(criterio)) {
            return ImmutableList.copyOf(libroDAO.listar());
        }
        return ImmutableList.copyOf(libroDAO.buscarPorCriterio(criterio.trim()));
    }

    public List<Libro> listarTodosLibros() {
        return ImmutableList.copyOf(libroDAO.listar());
    }

    public Optional<Libro> buscarPorId(int id) {
        return libroDAO.buscarPorId(id);
    }

    public Optional<Libro> buscarPorIsbn(String isbn) {
        if (Strings.isNullOrEmpty(isbn)) {
            return Optional.empty();
        }
        return libroDAO.buscarPorIsbn(isbn.trim());
    }

    public List<Ejemplar> listarEjemplaresPorLibro(int libroId) {
        return ImmutableList.copyOf(ejemplarDAO.listarPorLibro(libroId));
    }

    public Optional<Ejemplar> buscarEjemplarPorCodigo(String codigoBarras) {
        if (Strings.isNullOrEmpty(codigoBarras)) {
            return Optional.empty();
        }
        return ejemplarDAO.buscarPorCodigoBarras(codigoBarras.trim());
    }

    public int contarDisponibles(int libroId) {
        return ejemplarDAO.contarDisponibles(libroId);
    }
}
