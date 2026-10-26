package com.biblioteca.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    @DisplayName("Debe generar un hash BCrypt válido con costo 12")
    void testHashearPasswordGeneraBCryptValido() {
        String clavePlana = "Admin123!";
        String hash = PasswordHasher.hashear(clavePlana);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$") || hash.startsWith("$2y$12$"),
                "El hash debe comenzar con el prefijo BCrypt y costo 12");
        assertNotEquals(clavePlana, hash);
    }

    @Test
    @DisplayName("Debe verificar correctamente una contraseña contra su hash BCrypt")
    void testVerificarPasswordCorrectaEIncorrecta() {
        String claveCorrecta = "PasswordSegura2026!";
        String hash = PasswordHasher.hashear(claveCorrecta);

        assertTrue(PasswordHasher.verificar(claveCorrecta, hash), "La contraseña correcta debe validar como true");
        assertFalse(PasswordHasher.verificar("ClaveEquivocada", hash), "La contraseña incorrecta debe validar como false");
        assertFalse(PasswordHasher.verificar(null, hash), "Contraseña nula debe retornar false");
        assertFalse(PasswordHasher.verificar(claveCorrecta, null), "Hash nulo debe retornar false");
    }
}

