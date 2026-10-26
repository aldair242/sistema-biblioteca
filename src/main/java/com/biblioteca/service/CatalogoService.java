package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.dao.ReservaDAO;
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

        GestorTransaccion.ejecutar(() -> {
            for (Ejemplar ej : ejemplares) {
                ejemplarDAO.eliminar(ej.getId());
            }
            libroDAO.eliminar(libroId);
            logger.info("Libro ID {} y sus ejemplares eliminados del catálogo", libroId);
        });
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

