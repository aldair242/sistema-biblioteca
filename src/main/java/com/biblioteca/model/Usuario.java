package com.biblioteca.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa a un usuario del sistema (ADMIN, BIBLIOTECARIO o CLIENTE).
 */
public class Usuario {

    private Integer id;
    private String username;
    private String passwordHash;
    private Rol rol;
    private String nombre;
    private boolean activo;
    private boolean debeCambiarClave;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    public Usuario() {
        this.activo = true;
        this.debeCambiarClave = false;
        this.intentosFallidos = 0;
    }

    public Usuario(Integer id, String username, String passwordHash, Rol rol, String nombre,
                   boolean activo, boolean debeCambiarClave, int intentosFallidos, LocalDateTime bloqueadoHasta) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.nombre = nombre;
        this.activo = activo;
        this.debeCambiarClave = debeCambiarClave;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isDebeCambiarClave() {
        return debeCambiarClave;
    }

    public void setDebeCambiarClave(boolean debeCambiarClave) {
        this.debeCambiarClave = debeCambiarClave;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id) || (username != null && Objects.equals(username, usuario.username));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    /**
     * NUNCA incluye passwordHash en la representación de texto por seguridad.
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", rol=" + rol +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                ", debeCambiarClave=" + debeCambiarClave +
                ", intentosFallidos=" + intentosFallidos +
                ", bloqueadoHasta=" + bloqueadoHasta +
                '}';
    }
}

