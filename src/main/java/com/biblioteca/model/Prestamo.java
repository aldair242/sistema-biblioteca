package com.biblioteca.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa una operación de préstamo de un ejemplar a un cliente.
 */
public class Prestamo {

    private Integer id;
    private Integer clienteId;
    private Integer ejemplarId;
    private Integer bibliotecarioId;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private LocalDate fechaDevolucion;
    private EstadoPrestamo estado;
    private int renovaciones;

    public Prestamo() {
        this.estado = EstadoPrestamo.ACTIVO;
        this.renovaciones = 0;
    }

    public Prestamo(Integer id, Integer clienteId, Integer ejemplarId, Integer bibliotecarioId,
                    LocalDate fechaPrestamo, LocalDate fechaVencimiento, LocalDate fechaDevolucion,
                    EstadoPrestamo estado, int renovaciones) {
        this.id = id;
        this.clienteId = clienteId;
        this.ejemplarId = ejemplarId;
        this.bibliotecarioId = bibliotecarioId;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado != null ? estado : EstadoPrestamo.ACTIVO;
        this.renovaciones = renovaciones;
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

    public Integer getEjemplarId() {
        return ejemplarId;
    }

    public void setEjemplarId(Integer ejemplarId) {
        this.ejemplarId = ejemplarId;
    }

    public Integer getBibliotecarioId() {
        return bibliotecarioId;
    }

    public void setBibliotecarioId(Integer bibliotecarioId) {
        this.bibliotecarioId = bibliotecarioId;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public int getRenovaciones() {
        return renovaciones;
    }

    public void setRenovaciones(int renovaciones) {
        this.renovaciones = renovaciones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(id, prestamo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", ejemplarId=" + ejemplarId +
                ", bibliotecarioId=" + bibliotecarioId +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaVencimiento=" + fechaVencimiento +
                ", fechaDevolucion=" + fechaDevolucion +
                ", estado=" + estado +
                ", renovaciones=" + renovaciones +
                '}';
    }
}

