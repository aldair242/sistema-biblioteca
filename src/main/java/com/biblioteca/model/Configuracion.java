package com.biblioteca.model;

import java.util.Objects;

/**
 * Entidad para los parámetros globales del sistema configurables por el ADMIN.
 */
public class Configuracion {

    private String clave;
    private String valor;

    public Configuracion() {
    }

    public Configuracion(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuracion that = (Configuracion) o;
        return Objects.equals(clave, that.clave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clave);
    }

    @Override
    public String toString() {
        return "Configuracion{" +
                "clave='" + clave + '\'' +
                ", valor='" + valor + '\'' +
                '}';
    }
}

