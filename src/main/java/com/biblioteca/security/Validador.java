package com.biblioteca.security;

import com.biblioteca.exception.ValidacionException;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Validador de reglas de formato y seguridad para entradas del sistema.
 * Utiliza utilidades de Apache Commons Lang3 y Google Guava.
 */
public final class Validador {

    private static final Pattern PATRON_DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^\\+?\\d{7,15}$");
    private static final Pattern PATRON_PASSWORD_FUERTE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    private Validador() {
    }

    /**
     * Comprueba si un DNI tiene exactamente 8 dígitos numéricos.
     */
    public static boolean esDniValido(String dni) {
        if (StringUtils.isBlank(dni)) {
            return false;
        }
        return PATRON_DNI.matcher(dni.trim()).matches();
    }

    /**
     * Valida el DNI y lanza ValidacionException si es inválido.
     */
    public static void validarDni(String dni) {
        if (!esDniValido(dni)) {
            throw new ValidacionException("El DNI debe contener exactamente 8 dígitos numéricos.");
        }
    }

    /**
     * Comprueba si una cadena tiene formato válido de correo electrónico.
     */
    public static boolean esEmailValido(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return PATRON_EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Valida el formato de email y lanza ValidacionException si es inválido.
     */
    public static void validarEmail(String email) {
        if (!esEmailValido(email)) {
            throw new ValidacionException("El formato del correo electrónico ingresado no es válido.");
        }
    }

    /**
     * Comprueba si una cadena tiene formato válido de teléfono (7 a 15 dígitos).
     */
    public static boolean esTelefonoValido(String telefono) {
        if (StringUtils.isBlank(telefono)) {
            return false;
        }
        return PATRON_TELEFONO.matcher(telefono.trim()).matches();
    }

    /**
     * Valida el número telefónico y lanza ValidacionException si es inválido.
     */
    public static void validarTelefono(String telefono) {
        if (!esTelefonoValido(telefono)) {
            throw new ValidacionException("El número de teléfono debe contener entre 7 y 15 dígitos numéricos.");
        }
    }

    /**
     * Comprueba si una contraseña cumple la política de seguridad:
     * mínimo 8 caracteres, al menos una mayúscula, una minúscula y un dígito.
     */
    public static boolean esPasswordFuerte(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        return PATRON_PASSWORD_FUERTE.matcher(password).matches();
    }

    /**
     * Valida la fortaleza de la contraseña y lanza ValidacionException si no cumple los requisitos.
     */
    public static void validarPasswordFuerte(String password) {
        Preconditions.checkNotNull(password, "La contraseña no puede ser nula.");
        if (!esPasswordFuerte(password)) {
            throw new ValidacionException("La contraseña debe tener al menos 8 caracteres e incluir al menos una letra mayúscula, una minúscula y un número.");
        }
    }
}

