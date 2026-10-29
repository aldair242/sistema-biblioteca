package com.biblioteca.model;

/**
 * Encapsula el resultado de una solicitud de recuperación de credenciales.
 *
 * @param cliente       Datos del cliente asociado al correo registrado
 * @param usuario       Cuenta de usuario correspondiente
 * @param claveTemporal Contraseña temporal generada para el primer acceso
 */
public record ResultadoRecuperacion(Cliente cliente, Usuario usuario, String claveTemporal) {
}
