package com.biblioteca.security;

import com.biblioteca.exception.AccesoDenegadoException;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutorizadorTest {

    @BeforeEach
    @AfterEach
    void limpiarSesion() {
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException si no hay usuario autenticado")
    void testRequerirSinSesionLanzaExcepcion() {
        assertThrows(AccesoDenegadoException.class, () -> Autorizador.requerir(Rol.ADMIN));
    }

    @Test
    @DisplayName("Debe lanzar AccesoDenegadoException si el usuario no tiene el rol exigido")
    void testRequerirRolIncorrectoLanzaExcepcion() {
        Usuario cliente = new Usuario(1, "cliente", "hash", Rol.CLIENTE, "Cliente", true, false, 0, null);
        SesionActual.iniciarSesion(cliente);

        assertThrows(AccesoDenegadoException.class, () -> Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO));
    }

    @Test
    @DisplayName("No debe lanzar excepción si el usuario tiene uno de los roles permitidos")
    void testRequerirRolCorrectoNoLanzaExcepcion() {
        Usuario admin = new Usuario(1, "admin", "hash", Rol.ADMIN, "Admin", true, false, 0, null);
        SesionActual.iniciarSesion(admin);

        assertDoesNotThrow(() -> Autorizador.requerir(Rol.ADMIN));
        assertDoesNotThrow(() -> Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO));
    }
}

