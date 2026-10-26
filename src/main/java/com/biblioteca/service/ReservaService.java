package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.ReservaDAO;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.EstadoEjemplar;
import com.biblioteca.model.EstadoReserva;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Rol;
import com.biblioteca.security.Autorizador;
import com.biblioteca.security.SesionActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de reservas de libros bajo cola FIFO y control de expiraciones.
 */
public class ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaDAO reservaDAO;
    private final EjemplarDAO ejemplarDAO;
    private final ConfiguracionService configuracionService;

    public ReservaService(ReservaDAO reservaDAO, EjemplarDAO ejemplarDAO, ConfiguracionService configuracionService) {
        this.reservaDAO = reservaDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.configuracionService = configuracionService;
    }

    /**
     * Registra una reserva para un cliente en la cola FIFO del libro.
     * Solo procede si no hay ejemplares disponibles y el cliente no tiene una reserva activa previa del mismo libro.
     *
     * @param clienteId Identificador del cliente
     * @param libroId Identificador del libro
     * @return Reserva registrada en cola
     */
    public Reserva crearReserva(int clienteId, int libroId) {
        // Validar que no haya ejemplares disponibles
        int disponibles = ejemplarDAO.contarDisponibles(libroId);
        if (disponibles > 0) {
            throw new ValidacionException("No es posible reservar: el libro cuenta con " + disponibles + " ejemplar(es) disponible(s) para préstamo inmediato.");
        }

        // Validar que el cliente no tenga ya una reserva activa para este libro
        Optional<Reserva> activa = reservaDAO.buscarReservaActivaClienteLibro(clienteId, libroId);
        if (activa.isPresent()) {
            throw new ValidacionException("El cliente ya posee una reserva activa (" + activa.get().getEstado() + ") para este libro.");
        }

        Reserva reserva = new Reserva(null, clienteId, libroId, null, LocalDateTime.now(), null, EstadoReserva.PENDIENTE);
        Reserva guardada = reservaDAO.guardar(reserva);
        logger.info("Reserva en cola FIFO creada exitosamente: ID {}, Cliente ID {}, Libro ID {}",
                guardada.getId(), clienteId, libroId);
        return guardada;
    }

    /**
     * Asigna un ejemplar devuelto a la primera reserva en cola FIFO para ese libro.
     * Si no hay cola, coloca el ejemplar en DISPONIBLE.
     *
     * @param ejemplar Ejemplar devuelto
     * @return true si fue asignado a una reserva en cola, false si quedó disponible
     */
    public boolean asignarEjemplarDevuelto(Ejemplar ejemplar) {
        List<Reserva> cola = reservaDAO.listarColaPorLibro(ejemplar.getLibroId());

        if (!cola.isEmpty()) {
            Reserva primera = cola.get(0);
            int diasRetencion = configuracionService.obtenerDiasRetencionReserva();

            primera.setEjemplarId(ejemplar.getId());
            primera.setEstado(EstadoReserva.LISTA);
            primera.setFechaExpiracion(LocalDate.now().plusDays(diasRetencion));
            reservaDAO.actualizar(primera);

            ejemplar.setEstado(EstadoEjemplar.RESERVADO);
            ejemplarDAO.actualizarEstado(ejemplar.getId(), EstadoEjemplar.RESERVADO);

            logger.info("Ejemplar '{}' asignado a la reserva ID {} (Cliente ID {}). Disponible para recojo hasta {}",
                    ejemplar.getCodigoBarras(), primera.getId(), primera.getClienteId(), primera.getFechaExpiracion());
            return true;
        } else {
            ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
            ejemplarDAO.actualizarEstado(ejemplar.getId(), EstadoEjemplar.DISPONIBLE);
            logger.info("Ejemplar '{}' marcado como DISPONIBLE (sin reservas en cola)", ejemplar.getCodigoBarras());
            return false;
        }
    }

    /**
     * Cancela una reserva.
     * Si la reserva estaba LISTA con un ejemplar asignado, libera el ejemplar para la siguiente reserva en cola o DISPONIBLE.
     *
     * @param reservaId Identificador de la reserva
     */
    public void cancelarReserva(int reservaId) {
        Reserva reserva = reservaDAO.buscarPorId(reservaId)
                .orElseThrow(() -> new ValidacionException("No se encontró la reserva ID " + reservaId));

        if (reserva.getEstado() == EstadoReserva.CUMPLIDA || reserva.getEstado() == EstadoReserva.CANCELADA || reserva.getEstado() == EstadoReserva.EXPIRADA) {
            throw new ValidacionException("La reserva ya no se encuentra activa (estado: " + reserva.getEstado() + ")");
        }

        GestorTransaccion.ejecutar(() -> {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaDAO.actualizar(reserva);

            if (reserva.getEjemplarId() != null) {
                // Reasignar el ejemplar que tenía reservado
                ejemplarDAO.buscarPorId(reserva.getEjemplarId()).ifPresent(this::asignarEjemplarDevuelto);
            }
            logger.info("Reserva ID {} cancelada correctamente", reservaId);
        });
    }

    /**
     * Procesa las reservas en estado LISTA cuyo plazo de retención ha expirado.
     * Expira la reserva y traslada el ejemplar al siguiente en cola o lo deja DISPONIBLE.
     */
    public void procesarExpiradas() {
        List<Reserva> expiradas = reservaDAO.listarExpiradas();
        for (Reserva res : expiradas) {
            GestorTransaccion.ejecutar(() -> {
                res.setEstado(EstadoReserva.EXPIRADA);
                reservaDAO.actualizar(res);
                logger.info("Reserva ID {} marcada como EXPIRADA", res.getId());

                if (res.getEjemplarId() != null) {
                    ejemplarDAO.buscarPorId(res.getEjemplarId()).ifPresent(this::asignarEjemplarDevuelto);
                }
            });
        }
    }

    public List<Reserva> listarPorCliente(int clienteId) {
        return reservaDAO.listarPorCliente(clienteId);
    }

    public List<Reserva> listarColaPorLibro(int libroId) {
        return reservaDAO.listarColaPorLibro(libroId);
    }

    public List<Reserva> listarTodas() {
        return reservaDAO.listar();
    }

    public Optional<Reserva> buscarPorId(int id) {
        return reservaDAO.buscarPorId(id);
    }

    public Optional<Reserva> buscarReservaListaParaClienteYLibro(int clienteId, int libroId) {
        return reservaDAO.buscarReservaActivaClienteLibro(clienteId, libroId)
                .filter(r -> r.getEstado() == EstadoReserva.LISTA);
    }

    public void marcarCumplida(int reservaId) {
        reservaDAO.buscarPorId(reservaId).ifPresent(r -> {
            r.setEstado(EstadoReserva.CUMPLIDA);
            reservaDAO.actualizar(r);
            logger.info("Reserva ID {} marcada como CUMPLIDA tras préstamo de ejemplar", reservaId);
        });
    }
}

