package com.biblioteca.model;

import java.util.Objects;

/**
 * Entidad que representa una copia o ejemplar físico individual de un libro.
 */
public class Ejemplar {

    private Integer id;
    private Integer libroId;
    private String codigoBarras;
    private EstadoEjemplar estado;

    public Ejemplar() {
        this.estado = EstadoEjemplar.DISPONIBLE;
    }

    public Ejemplar(Integer id, Integer libroId, String codigoBarras, EstadoEjemplar estado) {
        this.id = id;
        this.libroId = libroId;
        this.codigoBarras = codigoBarras;
        this.estado = estado != null ? estado : EstadoEjemplar.DISPONIBLE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLibroId() {
        return libroId;
    }

    public void setLibroId(Integer libroId) {
        this.libroId = libroId;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public EstadoEjemplar getEstado() {
        return estado;
    }

    public void setEstado(EstadoEjemplar estado) {
        this.estado = estado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return Objects.equals(id, ejemplar.id) || (codigoBarras != null && Objects.equals(codigoBarras, ejemplar.codigoBarras));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, codigoBarras);
    }

    @Override
    public String toString() {
        return "Ejemplar{" +
                "id=" + id +
                ", libroId=" + libroId +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", estado=" + estado +
                '}';
    }
}

