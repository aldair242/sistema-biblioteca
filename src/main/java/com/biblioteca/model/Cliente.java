package com.biblioteca.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa a un cliente/lector de la biblioteca.
 */
public class Cliente {

    private Integer id;
    private Integer usuarioId;
    private String dni;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private LocalDate fechaRegistro;

    public Cliente() {
        this.fechaRegistro = LocalDate.now();
    }

    public Cliente(Integer id, Integer usuarioId, String dni, String nombres, String apellidos,
                   String email, String telefono, LocalDate fechaRegistro) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : LocalDate.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id) || (dni != null && Objects.equals(dni, cliente.dni));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dni);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", dni='" + dni + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}

