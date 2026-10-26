package com.biblioteca.service;

import com.biblioteca.dao.ConfiguracionDAO;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Configuracion;
import com.biblioteca.model.Rol;
import com.biblioteca.security.Autorizador;
import com.biblioteca.security.SesionActual;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de parámetros globales del sistema.
 */
public class ConfiguracionService {

    private static final Logger logger = LoggerFactory.getLogger(ConfiguracionService.class);

    public static final String CLAVE_MULTA_POR_DIA = "MULTA_POR_DIA";
    public static final String CLAVE_DIAS_PRESTAMO = "DIAS_PRESTAMO";
    public static final String CLAVE_MAX_PRESTAMOS = "MAX_PRESTAMOS";
    public static final String CLAVE_DIAS_RETENCION_RESERVA = "DIAS_RETENCION_RESERVA";

    private final ConfiguracionDAO configuracionDAO;

    public ConfiguracionService(ConfiguracionDAO configuracionDAO) {
        this.configuracionDAO = configuracionDAO;
    }

    public BigDecimal obtenerMultaPorDia() {
        return configuracionDAO.obtener(CLAVE_MULTA_POR_DIA)
                .map(v -> {
                    try {
                        return new BigDecimal(v);
                    } catch (NumberFormatException e) {
                        return new BigDecimal("1.00");
                    }
                })
                .orElse(new BigDecimal("1.00"));
    }

    public int obtenerDiasPrestamo() {
        return configuracionDAO.obtener(CLAVE_DIAS_PRESTAMO)
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return 7;
                    }
                })
                .orElse(7);
    }

    public int obtenerMaxPrestamos() {
        return configuracionDAO.obtener(CLAVE_MAX_PRESTAMOS)
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return 3;
                    }
                })
                .orElse(3);
    }

    public int obtenerDiasRetencionReserva() {
        return configuracionDAO.obtener(CLAVE_DIAS_RETENCION_RESERVA)
                .map(v -> {
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return 3;
                    }
                })
                .orElse(3);
    }

    /**
     * Actualiza el valor de un parámetro global. Operación restringida exclusivamente al ADMIN.
     */
    public void actualizar(String clave, String valor) {
        Autorizador.requerir(Rol.ADMIN);

        if (StringUtils.isBlank(clave) || StringUtils.isBlank(valor)) {
            throw new ValidacionException("La clave y el valor de configuración no pueden estar vacíos.");
        }

        try {
            BigDecimal num = new BigDecimal(valor.trim());
            if (num.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidacionException("El valor de configuración debe ser un número positivo mayor a cero.");
            }
        } catch (NumberFormatException e) {
            throw new ValidacionException("El valor ingresado debe ser numérico.");
        }

        String valorAnterior = configuracionDAO.obtener(clave).orElse("(no definido)");
        configuracionDAO.guardar(clave.trim(), valor.trim());

        String usuario = SesionActual.estaAutenticado() ? SesionActual.getUsuario().getUsername() : "ADMIN";
        logger.info("Configuración '{}' modificada por usuario '{}': valor anterior='{}', nuevo valor='{}'",
                clave, usuario, valorAnterior, valor.trim());
    }

    public List<Configuracion> listarTodas() {
        return configuracionDAO.listar();
    }

    public Map<String, String> listarComoMapa() {
        return configuracionDAO.listarComoMapa();
    }
}

