package com.biblioteca.dao;

import com.biblioteca.dao.impl.ClienteDAOImpl;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.Cliente;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClienteDAOTest extends BaseDAOTest {

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    @Test
    @DisplayName("CRUD de Cliente y consultas específicas en transacción")
    void testCrudYConsultasCliente() {
        String dni = String.format("%08d", (int) (Math.random() * 89999999 + 10000000));
        String username = "cli_" + UUID.randomUUID().toString().substring(0, 8);

        // Crear usuario para la FK
        Usuario user = new Usuario(null, username, "$2a$10$hash...", Rol.CLIENTE, "Juan Perez", true, true, 0, null);
        user = usuarioDAO.guardar(user);

        Cliente nuevo = new Cliente(null, user.getId(), dni, "Juan Carlos", "Pérez López", "juan.perez@test.com", "987111222", LocalDate.now());

        // 1. Guardar
        Cliente guardado = clienteDAO.guardar(nuevo);
        assertNotNull(guardado.getId());

        // 2. Buscar por ID
        Optional<Cliente> optId = clienteDAO.buscarPorId(guardado.getId());
        assertTrue(optId.isPresent());
        assertEquals(dni, optId.get().getDni());

        // 3. Buscar por DNI
        Optional<Cliente> optDni = clienteDAO.buscarPorDni(dni);
        assertTrue(optDni.isPresent());
        assertEquals("Juan Carlos", optDni.get().getNombres());

        // 4. Buscar por texto
        List<Cliente> textoResult = clienteDAO.buscarPorTexto("Pérez");
        assertFalse(textoResult.isEmpty());

        // 5. Buscar por Usuario ID
        Optional<Cliente> optUser = clienteDAO.buscarPorUsuarioId(user.getId());
        assertTrue(optUser.isPresent());
        assertEquals(guardado.getId(), optUser.get().getId());

        // 6. Actualizar
        guardado.setTelefono("999888777");
        guardado.setEmail("actualizado@test.com");
        clienteDAO.actualizar(guardado);
        Cliente actualizado = clienteDAO.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals("999888777", actualizado.getTelefono());
        assertEquals("actualizado@test.com", actualizado.getEmail());

        // 7. Eliminar
        clienteDAO.eliminar(guardado.getId());
        assertTrue(clienteDAO.buscarPorId(guardado.getId()).isEmpty());
    }
}

