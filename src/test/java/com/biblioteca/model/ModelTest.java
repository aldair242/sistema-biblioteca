package com.biblioteca.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    @DisplayName("Usuario NUNCA debe incluir passwordHash en toString()")
    void testUsuarioToStringNoExponeHash() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setUsername("admin");
        u.setPasswordHash("$2a$10$u87b9FlgJ6JeHjBEsIVSROXFW6SVAmvfssuduVKyfOaZLyIOD6S/G");
        u.setRol(Rol.ADMIN);
        u.setNombre("Administrador");

        String str = u.toString();
        assertFalse(str.contains(u.getPasswordHash()), "El toString de Usuario jamás debe contener el hash de contraseña");
        assertTrue(str.contains("admin"));
        assertTrue(str.contains("ADMIN"));
    }

    @Test
    @DisplayName("Pruebas de inicialización de entidades de dominio")
    void testEntidadesModel() {
        Cliente c = new Cliente(1, 2, "12345678", "Ana", "Torres", "ana@example.com", "987654321", LocalDate.now());
        assertEquals("12345678", c.getDni());
        assertEquals("Ana Torres", c.getNombreCompleto());

        Libro l = new Libro(1, "9780132350884", "Clean Code", "Robert C. Martin", "Programacion", "Prentice Hall", 2008);
        assertEquals("9780132350884", l.getIsbn());

        Ejemplar e = new Ejemplar(1, 1, "L0001-001", EstadoEjemplar.DISPONIBLE);
        assertEquals(EstadoEjemplar.DISPONIBLE, e.getEstado());

        Prestamo p = new Prestamo(1, 1, 1, 1, LocalDate.now(), LocalDate.now().plusDays(7), null, EstadoPrestamo.ACTIVO, 0);
        assertEquals(EstadoPrestamo.ACTIVO, p.getEstado());

        Reserva r = new Reserva(1, 1, 1, null, LocalDateTime.now(), null, EstadoReserva.PENDIENTE);
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());

        Multa m = new Multa(1, 1, 3, new BigDecimal("1.00"), new BigDecimal("3.00"), EstadoMulta.PENDIENTE, null);
        assertEquals(new BigDecimal("3.00"), m.getMonto());

        Configuracion cfg = new Configuracion("MULTA_POR_DIA", "1.00");
        assertEquals("MULTA_POR_DIA", cfg.getClave());
    }
}

