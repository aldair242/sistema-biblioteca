package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.*;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.*;
import com.biblioteca.security.Autorizador;
import com.biblioteca.security.SesionActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de control de operaciones de préstamos, renovaciones, devoluciones y vencimientos.
 */
public class PrestamoService {

    private static final Logger logger = LoggerFactory.getLogger(PrestamoService.class);

    private final PrestamoDAO prestamoDAO;
    private final EjemplarDAO ejemplarDAO;
    private final ClienteDAO clienteDAO;
    private final MultaDAO multaDAO;
    private final ReservaDAO reservaDAO;
    private final MultaService multaService;
    private final ReservaService reservaService;
    private final ConfiguracionService configuracionService;

    public PrestamoService(PrestamoDAO prestamoDAO, EjemplarDAO ejemplarDAO, ClienteDAO clienteDAO,
                           MultaDAO multaDAO, ReservaDAO reservaDAO, MultaService multaService,
                           ReservaService reservaService, ConfiguracionService configuracionService) {
        this.prestamoDAO = prestamoDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.clienteDAO = clienteDAO;
        this.multaDAO = multaDAO;
        this.reservaDAO = reservaDAO;
        this.multaService = multaService;
        this.reservaService = reservaService;
        this.configuracionService = configuracionService;
    }

    /**
     * Registra un nuevo préstamo validando todas las reglas de negocio exigidas:
     * 1. Sin multas pendientes.
     * 2. Sin préstamos vencidos.
     * 3. No haber alcanzado el límite máximo de préstamos permitidos.
     * 4. Ejemplar DISPONIBLE o RESERVADO por este mismo cliente con reserva LISTA (que pasa a CUMPLIDA).
     *
     * @param dniCliente   DNI del cliente solicitante
     * @param codigoBarras Código de barras del ejemplar
     * @return Préstamo creado y persistido
     */
    public Prestamo registrarPrestamo(String dniCliente, String codigoBarras) {
        return registrarPrestamo(dniCliente, codigoBarras, null);
    }

    /**
     * Registra un nuevo préstamo permitiendo seleccionar los días de duración (1, 3, 7 días o política).
     *
     * @param dniCliente DNI del cliente
     * @param codigoBarras Código de barras del ejemplar
     * @param diasPrestamoPersonalizado Días de préstamo opcionales (1, 3, 7, 14 o null para valor por defecto)
     * @return Préstamo creado y persistido
     */
    public Prestamo registrarPrestamo(String dniCliente, String codigoBarras, Integer diasPrestamoPersonalizado) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        if (dniCliente == null || dniCliente.trim().isEmpty() || codigoBarras == null || codigoBarras.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar el DNI del cliente y el código de barras del ejemplar.");
        }

        Cliente cliente = clienteDAO.buscarPorDni(dniCliente.trim())
                .orElseThrow(() -> new ValidacionException("No existe un cliente registrado con el DNI " + dniCliente));

        // 1. Validar multas pendientes
        List<Multa> multasPendientes = multaDAO.listarPendientesPorCliente(cliente.getId());
        if (!multasPendientes.isEmpty()) {
            throw new ValidacionException("El cliente tiene " + multasPendientes.size() +
                    " multa(s) pendiente(s) de pago. Debe cancelarlas antes de solicitar un nuevo préstamo.");
        }

        // 2. Validar préstamos vencidos
        List<Prestamo> activos = prestamoDAO.listarActivosPorCliente(cliente.getId());
        LocalDate hoy = LocalDate.now();
        for (Prestamo p : activos) {
            if (p.getFechaVencimiento().isBefore(hoy)) {
                throw new ValidacionException("El cliente tiene préstamos vencidos pendientes de devolución.");
            }
        }

        // 3. Validar límite máximo de préstamos
        int maxPrestamos = configuracionService.obtenerMaxPrestamos();
        if (activos.size() >= maxPrestamos) {
            throw new ValidacionException("El cliente ya alcanzó el límite máximo permitido de préstamos activos (" + maxPrestamos + ").");
        }

        // 4. Validar ejemplar
        Ejemplar ejemplar = ejemplarDAO.buscarPorCodigoBarras(codigoBarras.trim())
                .orElseThrow(() -> new ValidacionException("No se encontró ningún ejemplar con el código de barras " + codigoBarras));

        Integer reservaIdACumplir = null;

        if (ejemplar.getEstado() == EstadoEjemplar.DISPONIBLE) {
            // Permitido directamente
        } else if (ejemplar.getEstado() == EstadoEjemplar.RESERVADO) {
            // Verificar si este ejemplar está reservado para este cliente con reserva LISTA
            Optional<Reserva> reservaCliente = reservaDAO.buscarReservaActivaClienteLibro(cliente.getId(), ejemplar.getLibroId());
            if (reservaCliente.isPresent() && reservaCliente.get().getEstado() == EstadoReserva.LISTA
                    && ejemplar.getId().equals(reservaCliente.get().getEjemplarId())) {
                reservaIdACumplir = reservaCliente.get().getId();
            } else {
                throw new ValidacionException("El ejemplar se encuentra RESERVADO para otro lector en espera de recojo.");
            }
        } else {
            throw new ValidacionException("El ejemplar no se encuentra disponible para préstamo (estado: " + ejemplar.getEstado() + ").");
        }

        int diasPrestamo = (diasPrestamoPersonalizado != null && diasPrestamoPersonalizado > 0)
                ? diasPrestamoPersonalizado
                : configuracionService.obtenerDiasPrestamo();
        LocalDate fechaVencimiento = hoy.plusDays(diasPrestamo);

        int bibliotecarioId = SesionActual.estaAutenticado() ? SesionActual.getUsuario().getId() : 1;
        final Integer idReservaCumplir = reservaIdACumplir;

        return GestorTransaccion.ejecutar(() -> {
            Prestamo nuevo = new Prestamo(null, cliente.getId(), ejemplar.getId(), bibliotecarioId,
                    hoy, fechaVencimiento, null, EstadoPrestamo.ACTIVO, 0);

            Prestamo prestamoGuardado = prestamoDAO.guardar(nuevo);

            // Cambiar estado del ejemplar a PRESTADO
            ejemplarDAO.actualizarEstado(ejemplar.getId(), EstadoEjemplar.PRESTADO);

            // Si se atendió una reserva LISTA, marcarla como CUMPLIDA
            if (idReservaCumplir != null) {
                reservaService.marcarCumplida(idReservaCumplir);
            }

            logger.info("Préstamo registrado exitosamente: ID {}, Cliente DNI {}, Ejemplar '{}', Vence: {}",
                    prestamoGuardado.getId(), dniCliente, codigoBarras, fechaVencimiento);

            return prestamoGuardado;
        });
    }

    /**
     * Registra la devolución de un ejemplar.
     * Calcula la multa por días de retraso si corresponde y asigna el ejemplar a la cola de reservas o lo deja DISPONIBLE.
     *
     * @param codigoBarras Código de barras del ejemplar devuelto
     * @return Resultado de la devolución con detalles de multa y estado
     */
    public ResultadoDevolucion registrarDevolucion(String codigoBarras) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        if (codigoBarras == null || codigoBarras.trim().isEmpty()) {
            throw new ValidacionException("Debe ingresar el código de barras del ejemplar.");
        }

        Ejemplar ejemplar = ejemplarDAO.buscarPorCodigoBarras(codigoBarras.trim())
                .orElseThrow(() -> new ValidacionException("No existe un ejemplar con código " + codigoBarras));

        Prestamo prestamo = prestamoDAO.buscarActivoPorEjemplar(ejemplar.getId())
                .orElseThrow(() -> new ValidacionException("No existe ningún préstamo activo para el ejemplar con código " + codigoBarras));

        LocalDate hoy = LocalDate.now();
        BigDecimal montoMulta = multaService.calcularMulta(prestamo, hoy);

        return GestorTransaccion.ejecutar(() -> {
            Multa multaGenerada = null;

            if (montoMulta.compareTo(BigDecimal.ZERO) > 0) {
                long diasRetraso = ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), hoy);
                BigDecimal tarifaAplicada = configuracionService.obtenerMultaPorDia();

                multaGenerada = new Multa(null, prestamo.getId(), (int) diasRetraso, tarifaAplicada, montoMulta, EstadoMulta.PENDIENTE, null);
                multaGenerada = multaDAO.guardar(multaGenerada);
                logger.info("Multa generada por retraso: Préstamo ID {}, Días: {}, Monto: S/ {}",
                        prestamo.getId(), diasRetraso, montoMulta);
            }

            prestamo.setEstado(EstadoPrestamo.DEVUELTO);
            prestamo.setFechaDevolucion(hoy);
            prestamoDAO.actualizar(prestamo);

            // Reasignar ejemplar: a la siguiente reserva en cola FIFO o a DISPONIBLE
            boolean reservadoPorCola = reservaService.asignarEjemplarDevuelto(ejemplar);

            logger.info("Devolución registrada exitosamente para ejemplar '{}' (Préstamo ID {}). Multa: S/ {}, Reservado por cola: {}",
                    codigoBarras, prestamo.getId(), montoMulta, reservadoPorCola);

            return new ResultadoDevolucion(prestamo, multaGenerada, reservadoPorCola);
        });
    }

    /**
     * Actualiza el estado de préstamos activos vencidos a estado VENCIDO.
     */
    public void actualizarVencidos() {
        List<Prestamo> vencidos = prestamoDAO.listarVencidos();
        for (Prestamo p : vencidos) {
            if (p.getEstado() == EstadoPrestamo.ACTIVO) {
                p.setEstado(EstadoPrestamo.VENCIDO);
                prestamoDAO.actualizar(p);
                logger.info("Préstamo ID {} actualizado a estado VENCIDO", p.getId());
            }
        }
    }

    /**
     * Renueva el préstamo por un nuevo periodo de préstamo, permitido una sola vez si no hay cola de reservas.
     *
     * @param prestamoId Identificador del préstamo
     */
    public void renovar(int prestamoId) {
        Prestamo prestamo = prestamoDAO.buscarPorId(prestamoId)
                .orElseThrow(() -> new ValidacionException("No se encontró el préstamo ID " + prestamoId));

        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO) {
            throw new ValidacionException("Solo se pueden renovar préstamos en estado ACTIVO (estado actual: " + prestamo.getEstado() + ")");
        }

        if (prestamo.getRenovaciones() >= 1) {
            throw new ValidacionException("El préstamo ya fue renovado una vez (límite máximo alcanzado).");
        }

        if (prestamo.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ValidacionException("No es posible renovar un préstamo vencido.");
        }

        Ejemplar ejemplar = ejemplarDAO.buscarPorId(prestamo.getEjemplarId())
                .orElseThrow(() -> new ValidacionException("Ejemplar no encontrado"));

        if (!reservaDAO.listarColaPorLibro(ejemplar.getLibroId()).isEmpty()) {
            throw new ValidacionException("No se puede renovar: el libro tiene solicitudes de reserva en cola de espera.");
        }

        int dias = configuracionService.obtenerDiasPrestamo();
        prestamo.setFechaVencimiento(prestamo.getFechaVencimiento().plusDays(dias));
        prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);
        prestamoDAO.actualizar(prestamo);

        logger.info("Préstamo ID {} renovado exitosamente. Nueva fecha de vencimiento: {}", prestamoId, prestamo.getFechaVencimiento());
    }

    public List<Prestamo> listarActivosYVencidos() {
        return prestamoDAO.listar().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.VENCIDO)
                .toList();
    }

    public List<Prestamo> listarPorCliente(int clienteId) {
        return prestamoDAO.listarPorCliente(clienteId);
    }

    public List<Prestamo> listarTodos() {
        return prestamoDAO.listar();
    }

    public Optional<Prestamo> buscarPorId(int id) {
        return prestamoDAO.buscarPorId(id);
    }

    /**
     * Objeto de resultado para la devolución de un préstamo.
     */
    public static class ResultadoDevolucion {
        private final Prestamo prestamo;
        private final Multa multa;
        private final boolean asignadoAReserva;

        public ResultadoDevolucion(Prestamo prestamo, Multa multa, boolean asignadoAReserva) {
            this.prestamo = prestamo;
            this.multa = multa;
            this.asignadoAReserva = asignadoAReserva;
        }

        public Prestamo getPrestamo() {
            return prestamo;
        }

        public Multa getMulta() {
            return multa;
        }

        public boolean tieneMulta() {
            return multa != null && multa.getMonto().compareTo(BigDecimal.ZERO) > 0;
        }

        public boolean isAsignadoAReserva() {
            return asignadoAReserva;
        }
    }
}

