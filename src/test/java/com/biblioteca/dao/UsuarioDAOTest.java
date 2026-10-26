package com.biblioteca.dao;

import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDAOTest extends BaseDAOTest {

    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Test
    @DisplayName("CRUD de Usuario y consultas específicas en transacción")
    void testCrudYConsultasUsuario() {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "test_" + randomSuffix;

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPasswordHash("$2a$10$e8Z4N...dummyhash");
        nuevo.setRol(Rol.BIBLIOTECARIO);
        nuevo.setNombre("Usuario Prueba " + randomSuffix);
        nuevo.setActivo(true);
        nuevo.setDebeCambiarClave(true);
        nuevo.setIntentosFallidos(0);

        // 1. Guardar
        Usuario guardado = usuarioDAO.guardar(nuevo);
        assertNotNull(guardado.getId(), "El ID debe generarse automáticamente");

        // 2. Buscar por ID
        Optional<Usuario> optId = usuarioDAO.buscarPorId(guardado.getId());
        assertTrue(optId.isPresent());
        assertEquals(username, optId.get().getUsername());

        // 3. Buscar por Username
        Optional<Usuario> optUser = usuarioDAO.buscarPorUsername(username);
        assertTrue(optUser.isPresent());
        assertEquals(Rol.BIBLIOTECARIO, optUser.get().getRol());

        // 4. Actualizar intentos fallidos
        LocalDateTime bloqueo = LocalDateTime.now().plusMinutes(5);
        usuarioDAO.actualizarIntentosFallidos(guardado.getId(), 3, bloqueo);
        Usuario actualizado = usuarioDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(3, actualizado.getIntentosFallidos());

        // 5. Cambiar clave
        usuarioDAO.cambiarClave(guardado.getId(), "$2a$10$newhash...", false);
        actualizado = usuarioDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("$2a$10$newhash...", actualizado.getPasswordHash());
        assertFalse(actualizado.isDebeCambiarClave());
        assertEquals(0, actualizado.getIntentosFallidos());

        // 6. Listar por rol
        List<Usuario> bibliotecarios = usuarioDAO.listarPorRol(Rol.BIBLIOTECARIO);
        assertFalse(bibliotecarios.isEmpty());

        // 7. Eliminar
        usuarioDAO.eliminar(guardado.getId());
        Optional<Usuario> optEliminado = usuarioDAO.buscarPorId(guardado.getId());
        assertTrue(optEliminado.isEmpty());
    }
}

