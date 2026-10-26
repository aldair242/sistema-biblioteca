package com.biblioteca.service;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.security.SesionActual;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String HASH_PASSWORD123 = "$2a$12$xQV7mQTCtw8GO5d9JNUCBOGmMoKJDNbMoP7wn83IBwrxUNKdcAdQC";
    private static final String HASH_CLAVE_CORRECTA = "$2a$12$LwBkFvBFWmDx/4KhMeXc.uonQBM1wZIcXA/oj.74ibYDnmEGjX2tG";
    private static final String HASH_CLAVE_VIEJA = "$2a$12$W5u3Nz1pPuGlO9Ebz3zVj.jN58kASg15MPZb6SFeh.Y69jWSZ84xS";

    @Mock
    private UsuarioDAO usuarioDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioDAO);
        SesionActual.cerrarSesion();
    }

    @AfterEach
    void tearDown() {
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("Login exitoso con credenciales correctas")
    void testLoginExitoso() {
        Usuario u = new Usuario(1, "admin", HASH_PASSWORD123, Rol.ADMIN, "Admin Demo", true, false, 0, null);

        when(usuarioDAO.buscarPorUsername("admin")).thenReturn(Optional.of(u));

        Usuario autenticado = authService.login("admin", "Password123!");

        assertNotNull(autenticado);
        assertEquals("admin", autenticado.getUsername());
        assertTrue(SesionActual.estaAutenticado());
        assertEquals("admin", SesionActual.getUsuario().getUsername());
    }

    @Test
    @DisplayName("Login falla con mensaje genérico si el usuario no existe")
    void testLoginUsuarioNoExisteLanzaExcepcionGenerica() {
        when(usuarioDAO.buscarPorUsername("inexistente")).thenReturn(Optional.empty());

        AutenticacionException ex = assertThrows(AutenticacionException.class,
                () -> authService.login("inexistente", "CualquierClave123!"));

        assertTrue(ex.getMessage().toLowerCase().contains("incorrecto") || ex.getMessage().toLowerCase().contains("inválid"),
                "El mensaje debe ser genérico sin revelar si el usuario existe");
        assertFalse(SesionActual.estaAutenticado());
    }

    @Test
    @DisplayName("Login falla con contraseña incorrecta e incrementa intentos fallidos")
    void testLoginClaveIncorrectaIncrementaIntentos() {
        Usuario u = new Usuario(1, "biblio", HASH_CLAVE_CORRECTA, Rol.BIBLIOTECARIO, "Biblio Demo", true, false, 2, null);

        when(usuarioDAO.buscarPorUsername("biblio")).thenReturn(Optional.of(u));

        assertThrows(AutenticacionException.class, () -> authService.login("biblio", "ClaveErronea1!"));

        verify(usuarioDAO).actualizarIntentosFallidos(eq(1), eq(3), isNull());
        assertFalse(SesionActual.estaAutenticado());
    }

    @Test
    @DisplayName("Bloqueo de 5 minutos al alcanzar 5 intentos fallidos")
    void testBloqueoAlQuintoIntentoFallido() {
        Usuario u = new Usuario(1, "biblio", HASH_CLAVE_CORRECTA, Rol.BIBLIOTECARIO, "Biblio Demo", true, false, 4, null);

        when(usuarioDAO.buscarPorUsername("biblio")).thenReturn(Optional.of(u));

        assertThrows(AutenticacionException.class, () -> authService.login("biblio", "ClaveErronea1!"));

        verify(usuarioDAO).actualizarIntentosFallidos(eq(1), eq(5), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Rechazar login si la cuenta está actualmente bloqueada")
    void testRechazarLoginSiEstaBloqueado() {
        LocalDateTime bloqueoVigente = LocalDateTime.now().plusMinutes(4);
        Usuario u = new Usuario(1, "biblio", HASH_CLAVE_CORRECTA, Rol.BIBLIOTECARIO, "Biblio Demo", true, false, 5, bloqueoVigente);

        when(usuarioDAO.buscarPorUsername("biblio")).thenReturn(Optional.of(u));

        AutenticacionException ex = assertThrows(AutenticacionException.class,
                () -> authService.login("biblio", "ClaveCorrecta123!"));

        assertTrue(ex.getMessage().toLowerCase().contains("bloquead"));
        assertFalse(SesionActual.estaAutenticado());
    }

    @Test
    @DisplayName("Rechazar login si el usuario está inactivo")
    void testRechazarLoginSiUsuarioInactivo() {
        Usuario u = new Usuario(1, "inactivo", HASH_CLAVE_CORRECTA, Rol.CLIENTE, "Inactivo Demo", false, false, 0, null);

        when(usuarioDAO.buscarPorUsername("inactivo")).thenReturn(Optional.of(u));

        assertThrows(AutenticacionException.class, () -> authService.login("inactivo", "ClaveCorrecta123!"));
        assertFalse(SesionActual.estaAutenticado());
    }

    @Test
    @DisplayName("Cambiar contraseña exitosamente validando clave actual y fuerza de nueva clave")
    void testCambiarClaveExitoso() {
        Usuario u = new Usuario(1, "cliente1", HASH_CLAVE_VIEJA, Rol.CLIENTE, "Cliente 1", true, true, 0, null);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(u));

        authService.cambiarClave(1, "ClaveVieja123!", "NuevaClave2026!");

        verify(usuarioDAO).cambiarClave(eq(1), anyString(), eq(false));
    }

    @Test
    @DisplayName("Cambiar contraseña falla si la clave actual es incorrecta")
    void testCambiarClaveFallaSiActualEsIncorrecta() {
        Usuario u = new Usuario(1, "cliente1", HASH_CLAVE_VIEJA, Rol.CLIENTE, "Cliente 1", true, true, 0, null);

        when(usuarioDAO.buscarPorId(1)).thenReturn(Optional.of(u));

        assertThrows(AutenticacionException.class,
                () -> authService.cambiarClave(1, "ClaveEquivocada1!", "NuevaClave2026!"));

        verify(usuarioDAO, never()).cambiarClave(anyInt(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("Cambiar contraseña falla si la nueva clave no cumple criterios de seguridad")
    void testCambiarClaveFallaSiNuevaEsDebil() {
        assertThrows(ValidacionException.class,
                () -> authService.cambiarClave(1, "ClaveVieja123!", "debil"));
    }
}

