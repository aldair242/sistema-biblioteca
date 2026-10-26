package com.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de datos para la emisión y comprobante digital/impreso de ticket de préstamo
 * y envío de notificaciones push / correo electrónico al cliente.
 */
public class TicketPrestamo {

    private int prestamoId;
    private String clienteNombre;
    private String clienteDni;
    private String clienteTelefono;
    private String clienteEmail;
    private String libroTitulo;
    private String libroAutor;
    private String libroCategoria;
    private String codigoBarras;
    private LocalDate fechaPrestamo;
    private LocalDate fechaVencimiento;
    private int diasOtorgados;
    private BigDecimal tarifaMultaPorDia;

    public TicketPrestamo() {
    }

    public TicketPrestamo(int prestamoId, String clienteNombre, String clienteDni,
                          String clienteTelefono, String clienteEmail,
                          String libroTitulo, String libroAutor, String libroCategoria,
                          String codigoBarras, LocalDate fechaPrestamo,
                          LocalDate fechaVencimiento, int diasOtorgados,
                          BigDecimal tarifaMultaPorDia) {
        this.prestamoId = prestamoId;
        this.clienteNombre = clienteNombre;
        this.clienteDni = clienteDni;
        this.clienteTelefono = clienteTelefono;
        this.clienteEmail = clienteEmail;
        this.libroTitulo = libroTitulo;
        this.libroAutor = libroAutor;
        this.libroCategoria = libroCategoria;
        this.codigoBarras = codigoBarras;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaVencimiento = fechaVencimiento;
        this.diasOtorgados = diasOtorgados;
        this.tarifaMultaPorDia = tarifaMultaPorDia;
    }

    public int getPrestamoId() {
        return prestamoId;
    }

    public void setPrestamoId(int prestamoId) {
        this.prestamoId = prestamoId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getLibroTitulo() {
        return libroTitulo;
    }

    public void setLibroTitulo(String libroTitulo) {
        this.libroTitulo = libroTitulo;
    }

    public String getLibroAutor() {
        return libroAutor;
    }

    public void setLibroAutor(String libroAutor) {
        this.libroAutor = libroAutor;
    }

    public String getLibroCategoria() {
        return libroCategoria;
    }

    public void setLibroCategoria(String libroCategoria) {
        this.libroCategoria = libroCategoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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

    public int getDiasOtorgados() {
        return diasOtorgados;
    }

    public void setDiasOtorgados(int diasOtorgados) {
        this.diasOtorgados = diasOtorgados;
    }

    public BigDecimal getTarifaMultaPorDia() {
        return tarifaMultaPorDia;
    }

    public void setTarifaMultaPorDia(BigDecimal tarifaMultaPorDia) {
        this.tarifaMultaPorDia = tarifaMultaPorDia;
    }
}
