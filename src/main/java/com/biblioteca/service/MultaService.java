package com.biblioteca.service;

import com.biblioteca.dao.MultaDAO;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.EstadoMulta;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Rol;
import com.biblioteca.security.Autorizador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de liquidación, consulta y cobranza de multas por retraso.
 */
public class MultaService {

    private static final Logger logger = LoggerFactory.getLogger(MultaService.class);

    private final MultaDAO multaDAO;
    private final ConfiguracionService configuracionService;

    public MultaService(MultaDAO multaDAO, ConfiguracionService configuracionService) {
        this.multaDAO = multaDAO;
        this.configuracionService = configuracionService;
    }

    /**
     * Calcula el monto de la multa por días de retraso utilizando la tarifa vigente.
     * Retorna BigDecimal.ZERO si la devolución fue a tiempo.
     *
     * @param prestamo Préstamo objeto de devolución
     * @param fechaDevolucion Fecha real en la que se devuelve el ejemplar
     * @return Monto monetario calculado en BigDecimal
     */
    public BigDecimal calcularMulta(Prestamo prestamo, LocalDate fechaDevolucion) {
        if (prestamo == null || fechaDevolucion == null) {
            return BigDecimal.ZERO;
        }

        if (!fechaDevolucion.isAfter(prestamo.getFechaVencimiento())) {
            return BigDecimal.ZERO;
        }

        long diasRetraso = ChronoUnit.DAYS.between(prestamo.getFechaVencimiento(), fechaDevolucion);
        if (diasRetraso <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tarifaVigente = configuracionService.obtenerMultaPorDia();
        return tarifaVigente.multiply(BigDecimal.valueOf(diasRetraso));
    }

    /**
     * Registra el cobro de una multa marcando estado PAGADA y fecha de pago.
     * Solo permitido para personal con rol BIBLIOTECARIO o ADMIN.
     *
     * @param multaId Identificador de la multa
     */
    public void cobrar(int multaId) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        Multa multa = multaDAO.buscarPorId(multaId)
                .orElseThrow(() -> new ValidacionException("No se encontró la multa ID " + multaId));

        if (multa.getEstado() == EstadoMulta.PAGADA) {
            throw new ValidacionException("La multa ID " + multaId + " ya se encuentra pagada.");
        }

        multa.setEstado(EstadoMulta.PAGADA);
        multa.setFechaPago(LocalDate.now());
        multaDAO.actualizar(multa);

        logger.info("Cobro de multa registrado exitosamente: ID {}, Monto S/ {}, Fecha: {}",
                multaId, multa.getMonto(), multa.getFechaPago());
    }

    public List<Multa> listarPendientes(int clienteId) {
        return multaDAO.listarPendientesPorCliente(clienteId);
    }

    public List<Multa> listarPorCliente(int clienteId) {
        return multaDAO.listarPorCliente(clienteId);
    }

    public List<Multa> listarTodas() {
        return multaDAO.listar();
    }

    public Optional<Multa> buscarPorId(int id) {
        return multaDAO.buscarPorId(id);
    }

    public Optional<Multa> buscarPorPrestamo(int prestamoId) {
        return multaDAO.buscarPorPrestamo(prestamoId);
    }

    public Multa guardar(Multa multa) {
        return multaDAO.guardar(multa);
    }
}

