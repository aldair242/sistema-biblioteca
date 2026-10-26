package com.biblioteca.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad de seguridad para generación y verificación de contraseñas con BCrypt.
 * Aplica un factor de costo computacional de 12 (cost 12) según lo exigido.
 */
public final class PasswordHasher {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    private PasswordHasher() {
    }

    /**
     * Genera un hash seguro BCrypt con salt de costo 12 a partir de la contraseña en texto plano.
     *
     * @param password Contraseña a hashear
     * @return Hash seguro en formato BCrypt
     */
    public static String hashear(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula ni vacía.");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado.
     *
     * @param password Contraseña ingresada
     * @param hash     Hash BCrypt almacenado
     * @return true si coincide, false si no o si alguno es nulo
     */
    public static boolean verificar(String password, String hash) {
        if (password == null || hash == null || hash.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

