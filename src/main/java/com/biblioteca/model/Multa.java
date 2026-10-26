package com.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa una multa aplicada por retraso en la devolución de un préstamo.
 */
public class Multa {

    private Integer id;
    private Integer prestamoId;
    private int diasRetraso;
    private BigDecimal tarifaAplicada;
    private BigDecimal monto;
    private EstadoMulta estado;
    private LocalDate fechaPago;

    public Multa() {
        this.tarifaAplicada = BigDecimal.ZERO;
        this.monto = BigDecimal.ZERO;
        this.estado = EstadoMulta.PENDIENTE;
    }

    public Multa(Integer id, Integer prestamoId, int diasRetraso, BigDecimal tarifaAplicada,
                 BigDecimal monto, EstadoMulta estado, LocalDate fechaPago) {
        this.id = id;
        this.prestamoId = prestamoId;
        this.diasRetraso = diasRetraso;
        this.tarifaAplicada = tarifaAplicada != null ? tarifaAplicada : BigDecimal.ZERO;
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.estado = estado != null ? estado : EstadoMulta.PENDIENTE;
        this.fechaPago = fechaPago;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(Integer prestamoId) {
        this.prestamoId = prestamoId;
    }

    public int getDiasRetraso() {
        return diasRetraso;
    }

    public void setDiasRetraso(int diasRetraso) {
        this.diasRetraso = diasRetraso;
    }

    public BigDecimal getTarifaAplicada() {
        return tarifaAplicada;
    }

    public void setTarifaAplicada(BigDecimal tarifaAplicada) {
        this.tarifaAplicada = tarifaAplicada;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public EstadoMulta getEstado() {
        return estado;
    }

    public void setEstado(EstadoMulta estado) {
        this.estado = estado;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multa multa = (Multa) o;
        return Objects.equals(id, multa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Multa{" +
                "id=" + id +
                ", prestamoId=" + prestamoId +
                ", diasRetraso=" + diasRetraso +
                ", tarifaAplicada=" + tarifaAplicada +
                ", monto=" + monto +
                ", estado=" + estado +
                ", fechaPago=" + fechaPago +
                '}';
    }
}

