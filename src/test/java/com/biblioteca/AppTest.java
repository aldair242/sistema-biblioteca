package com.biblioteca;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppTest {

    @Test
    @DisplayName("Verificar que la clase principal App existe")
    void testAppInstantiation() {
        App app = new App();
        assertNotNull(app, "La instancia de App no debe ser nula");
    }
}

