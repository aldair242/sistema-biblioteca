package com.biblioteca.security;

import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SesionActualTest {

    @BeforeEach
    @AfterEach
    void limpiarSesion() {
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("Debe gestionar la sesión actual y verificar roles correctamente")
    void testGestionSesionYRoles() {
        assertFalse(SesionActual.estaAutenticado());
        assertNull(SesionActual.getUsuario());
        assertFalse(SesionActual.tieneRol(Rol.ADMIN));

        Usuario u = new Usuario(1, "biblio_demo", "hash", Rol.BIBLIOTECARIO, "Bibliotecario", true, false, 0, null);
        SesionActual.iniciarSesion(u);

        assertTrue(SesionActual.estaAutenticado());
        assertEquals(u, SesionActual.getUsuario());
        assertTrue(SesionActual.tieneRol(Rol.BIBLIOTECARIO));
        assertTrue(SesionActual.tieneRol(Rol.ADMIN, Rol.BIBLIOTECARIO));
        assertFalse(SesionActual.tieneRol(Rol.ADMIN));
        assertFalse(SesionActual.tieneRol(Rol.CLIENTE));

        SesionActual.cerrarSesion();
        assertFalse(SesionActual.estaAutenticado());
        assertNull(SesionActual.getUsuario());
    }
}

