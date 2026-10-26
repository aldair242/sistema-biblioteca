package com.biblioteca.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa la reserva de un libro en cola FIFO.
 */
public class Reserva {

    private Integer id;
    private Integer clienteId;
    private Integer libroId;
    private Integer ejemplarId;
    private LocalDateTime fechaReserva;
    private LocalDate fechaExpiracion;
    private EstadoReserva estado;

    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
    }

    public Reserva(Integer id, Integer clienteId, Integer libroId, Integer ejemplarId,
                   LocalDateTime fechaReserva, LocalDate fechaExpiracion, EstadoReserva estado) {
        this.id = id;
        this.clienteId = clienteId;
        this.libroId = libroId;
        this.ejemplarId = ejemplarId;
        this.fechaReserva = fechaReserva != null ? fechaReserva : LocalDateTime.now();
        this.fechaExpiracion = fechaExpiracion;
        this.estado = estado != null ? estado : EstadoReserva.PENDIENTE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getLibroId() {
        return libroId;
    }

    public void setLibroId(Integer libroId) {
        this.libroId = libroId;
    }

    public Integer getEjemplarId() {
        return ejemplarId;
    }

    public void setEjemplarId(Integer ejemplarId) {
        this.ejemplarId = ejemplarId;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public LocalDate getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDate fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", libroId=" + libroId +
                ", ejemplarId=" + ejemplarId +
                ", fechaReserva=" + fechaReserva +
                ", fechaExpiracion=" + fechaExpiracion +
                ", estado=" + estado +
                '}';
    }
}

