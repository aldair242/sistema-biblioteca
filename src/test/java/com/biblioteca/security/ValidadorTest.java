package com.biblioteca.security;

import com.biblioteca.exception.ValidacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidadorTest {

    @Test
    @DisplayName("Debe validar DNI de exactamente 8 dígitos numéricos")
    void testValidarDni() {
        assertTrue(Validador.esDniValido("12345678"));
        assertTrue(Validador.esDniValido("87654321"));

        assertFalse(Validador.esDniValido("1234567")); // 7 dígitos
        assertFalse(Validador.esDniValido("123456789")); // 9 dígitos
        assertFalse(Validador.esDniValido("1234567A")); // letras
        assertFalse(Validador.esDniValido(null));
        assertFalse(Validador.esDniValido(""));

        assertDoesNotThrow(() -> Validador.validarDni("12345678"));
        assertThrows(ValidacionException.class, () -> Validador.validarDni("123A"));
    }

    @Test
    @DisplayName("Debe validar formato de correo electrónico")
    void testValidarEmail() {
        assertTrue(Validador.esEmailValido("usuario@dominio.com"));
        assertTrue(Validador.esEmailValido("juan.perez@utp.edu.pe"));

        assertFalse(Validador.esEmailValido("correo-invalido"));
        assertFalse(Validador.esEmailValido("@dominio.com"));
        assertFalse(Validador.esEmailValido(null));
        assertFalse(Validador.esEmailValido(""));

        assertDoesNotThrow(() -> Validador.validarEmail("test@example.com"));
        assertThrows(ValidacionException.class, () -> Validador.validarEmail("invalido"));
    }

    @Test
    @DisplayName("Debe validar formato de teléfono")
    void testValidarTelefono() {
        assertTrue(Validador.esTelefonoValido("987654321"));
        assertTrue(Validador.esTelefonoValido("014567890"));

        assertFalse(Validador.esTelefonoValido("123")); // muy corto
        assertFalse(Validador.esTelefonoValido("987654321abc")); // letras
        assertFalse(Validador.esTelefonoValido(null));

        assertDoesNotThrow(() -> Validador.validarTelefono("987654321"));
        assertThrows(ValidacionException.class, () -> Validador.validarTelefono("abc"));
    }

    @Test
    @DisplayName("Debe validar contraseñas fuertes (mínimo 8 caracteres, mayúscula, minúscula, número)")
    void testValidarPasswordFuerte() {
        assertTrue(Validador.esPasswordFuerte("Admin123!"));
        assertTrue(Validador.esPasswordFuerte("ClaveSegura2026"));

        assertFalse(Validador.esPasswordFuerte("admin")); // corta
        assertFalse(Validador.esPasswordFuerte("admin1234")); // sin mayúscula
        assertFalse(Validador.esPasswordFuerte("ADMIN1234")); // sin minúscula
        assertFalse(Validador.esPasswordFuerte("AdminAdmin")); // sin número
        assertFalse(Validador.esPasswordFuerte(null));

        assertDoesNotThrow(() -> Validador.validarPasswordFuerte("ClaveValida1"));
        assertThrows(ValidacionException.class, () -> Validador.validarPasswordFuerte("debil"));
    }
}

