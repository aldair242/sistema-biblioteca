package com.biblioteca.service;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.model.Usuario;
import com.biblioteca.security.PasswordHasher;
import com.biblioteca.security.SesionActual;
import com.biblioteca.security.Validador;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de autenticación, control de sesiones y cambio de contraseñas.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 5;

    private final UsuarioDAO usuarioDAO;

    /**
     * Inyección de dependencias por constructor (SOLID).
     *
     * @param usuarioDAO DAO para operaciones sobre usuarios
     */
    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Autentica a un usuario verificando credenciales, estado activo y bloqueo temporal por intentos fallidos.
     * NUNCA registra contraseñas en logs ni revela si el usuario existe o no ante fallas.
     *
     * @param username Nombre de cuenta de usuario
     * @param clave    Contraseña en texto plano
     * @return Usuario autenticado con sesión iniciada
     * @throws AutenticacionException Si las credenciales son incorrectas o la cuenta está bloqueada/inactiva
     */
    public Usuario login(String username, String clave) {
        if (username == null || username.trim().isEmpty() || clave == null || clave.isEmpty()) {
            logger.warn("Intento de inicio de sesión con campos vacíos");
            throw new AutenticacionException("Usuario o contraseña incorrectos.");
        }

        String userTrim = username.trim();
        Optional<Usuario> optUser = usuarioDAO.buscarPorUsername(userTrim);

        if (optUser.isEmpty()) {
            logger.warn("Fallo de autenticación: usuario inexistente o credenciales inválidas para '{}'", userTrim);
            throw new AutenticacionException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = optUser.get();

        if (!usuario.isActivo()) {
            logger.warn("Intento de inicio de sesión con cuenta inactiva: '{}'", userTrim);
            throw new AutenticacionException("La cuenta de usuario se encuentra deshabilitada.");
        }

        // Validación de bloqueo por intentos fallidos
        if (usuario.getBloqueadoHasta() != null) {
            if (usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                logger.warn("Intento de login en cuenta bloqueada temporalmente: '{}'", userTrim);
                throw new AutenticacionException("Cuenta bloqueada temporalmente por demasiados intentos fallidos. Intente más tarde.");
            } else {
                // El bloqueo ya expiró: restablecer intentos
                usuario.setIntentosFallidos(0);
                usuario.setBloqueadoHasta(null);
                usuarioDAO.actualizarIntentosFallidos(usuario.getId(), 0, null);
            }
        }

        boolean coincide = PasswordHasher.verificar(clave, usuario.getPasswordHash());

        if (!coincide) {
            int nuevosIntentos = usuario.getIntentosFallidos() + 1;
            LocalDateTime bloqueoHasta = null;

            if (nuevosIntentos >= MAX_INTENTOS_FALLIDOS) {
                bloqueoHasta = LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO);
                logger.warn("Usuario '{}' bloqueado por {} minutos tras {} intentos fallidos consecutivas",
                        userTrim, MINUTOS_BLOQUEO, MAX_INTENTOS_FALLIDOS);
            } else {
                logger.warn("Contraseña incorrecta para usuario '{}' (intento {} de {})",
                        userTrim, nuevosIntentos, MAX_INTENTOS_FALLIDOS);
            }

            usuarioDAO.actualizarIntentosFallidos(usuario.getId(), nuevosIntentos, bloqueoHasta);
            throw new AutenticacionException("Usuario o contraseña incorrectos.");
        }

        // Login exitoso: restablecer intentos fallidos acumulados si existían
        if (usuario.getIntentosFallidos() > 0 || usuario.getBloqueadoHasta() != null) {
            usuarioDAO.actualizarIntentosFallidos(usuario.getId(), 0, null);
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
        }

        SesionActual.iniciarSesion(usuario);
        logger.info("Usuario '{}' ha iniciado sesión exitosamente con rol {}", userTrim, usuario.getRol());

        return usuario;
    }

    /**
     * Cambia la contraseña de un usuario validando la clave actual y la fortaleza de la nueva.
     *
     * @param usuarioId   Identificador del usuario
     * @param claveActual Contraseña actual
     * @param claveNueva  Nueva contraseña que debe cumplir criterios de seguridad
     */
    public void cambiarClave(int usuarioId, String claveActual, String claveNueva) {
        Validador.validarPasswordFuerte(claveNueva);

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId)
                .orElseThrow(() -> new AutenticacionException("No se encontró el usuario especificado."));

        if (!PasswordHasher.verificar(claveActual, usuario.getPasswordHash())) {
            logger.warn("Intento fallido de cambio de clave para usuario id {}: contraseña actual errónea", usuarioId);
            throw new AutenticacionException("La contraseña actual ingresada es incorrecta.");
        }

        String nuevoHash = PasswordHasher.hashear(claveNueva);
        usuarioDAO.cambiarClave(usuarioId, nuevoHash, false);

        if (SesionActual.estaAutenticado() && usuarioId == SesionActual.getUsuario().getId()) {
            SesionActual.getUsuario().setPasswordHash(nuevoHash);
            SesionActual.getUsuario().setDebeCambiarClave(false);
        }

        logger.info("Contraseña actualizada exitosamente para usuario '{}' (id: {})", usuario.getUsername(), usuarioId);
    }

    /**
     * Cierra la sesión activa en el sistema.
     */
    public void logout() {
        if (SesionActual.estaAutenticado()) {
            logger.info("Cerrando sesión para usuario '{}'", SesionActual.getUsuario().getUsername());
            SesionActual.cerrarSesion();
        }
    }
}

