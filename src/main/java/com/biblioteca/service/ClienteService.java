package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.ClienteDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Cliente;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.security.Autorizador;
import com.biblioteca.security.PasswordHasher;
import com.biblioteca.security.Validador;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de clientes y cuentas asociadas de lectores.
 */
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteDAO clienteDAO;
    private final UsuarioDAO usuarioDAO;

    public ClienteService(ClienteDAO clienteDAO, UsuarioDAO usuarioDAO) {
        this.clienteDAO = clienteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Registra un nuevo cliente y genera automáticamente su cuenta de usuario con contraseña temporal.
     * Solo permitido para personal con rol BIBLIOTECARIO o ADMIN.
     *
     * @param cliente Datos del cliente a registrar
     * @return Contraseña temporal generada para entregar al cliente
     */
    public String registrarCliente(Cliente cliente) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        // Validaciones
        Validador.validarDni(cliente.getDni());
        Validador.validarEmail(cliente.getEmail());
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            Validador.validarTelefono(cliente.getTelefono());
        }

        if (clienteDAO.buscarPorDni(cliente.getDni().trim()).isPresent()) {
            throw new ValidacionException("Ya existe un cliente registrado con el DNI " + cliente.getDni());
        }

        if (usuarioDAO.buscarPorUsername(cliente.getDni().trim()).isPresent()) {
            throw new ValidacionException("Ya existe una cuenta con el nombre de usuario " + cliente.getDni());
        }

        // Generar contraseña temporal segura (mínimo 8 caracteres: mayúscula, minúscula, número)
        String claveTemporal = generarClaveTemporal();
        String hash = PasswordHasher.hashear(claveTemporal);

        return GestorTransaccion.ejecutar(() -> {
            // 1. Crear usuario asociado
            Usuario usuario = new Usuario();
            usuario.setUsername(cliente.getDni().trim());
            usuario.setPasswordHash(hash);
            usuario.setRol(Rol.CLIENTE);
            usuario.setNombre(cliente.getNombreCompleto());
            usuario.setActivo(true);
            usuario.setDebeCambiarClave(true);
            usuario.setIntentosFallidos(0);

            Usuario usuarioGuardado = usuarioDAO.guardar(usuario);

            // 2. Crear cliente vinculado al usuario
            cliente.setUsuarioId(usuarioGuardado.getId());
            cliente.setFechaRegistro(LocalDate.now());
            clienteDAO.guardar(cliente);

            logger.info("Cliente registrado con éxito: DNI '{}', ID Cliente: {}", cliente.getDni(), cliente.getId());
            return claveTemporal;
        });
    }

    public void editarCliente(Cliente cliente) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        Validador.validarDni(cliente.getDni());
        Validador.validarEmail(cliente.getEmail());
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            Validador.validarTelefono(cliente.getTelefono());
        }

        // Verificar si el DNI pertenece a otro cliente
        Optional<Cliente> existente = clienteDAO.buscarPorDni(cliente.getDni().trim());
        if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
            throw new ValidacionException("El DNI " + cliente.getDni() + " ya está asignado a otro cliente.");
        }

        clienteDAO.actualizar(cliente);
        logger.info("Datos de cliente ID {} actualizados correctamente", cliente.getId());
    }

    public List<Cliente> buscarClientes(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return clienteDAO.listar();
        }
        return clienteDAO.buscarPorTexto(texto.trim());
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.listar();
    }

    public Optional<Cliente> buscarPorId(int id) {
        return clienteDAO.buscarPorId(id);
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return Optional.empty();
        }
        return clienteDAO.buscarPorDni(dni.trim());
    }

    public Optional<Cliente> buscarPorUsuarioId(int usuarioId) {
        return clienteDAO.buscarPorUsuarioId(usuarioId);
    }

    public void cambiarEstadoActivo(int clienteId, boolean activo) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Cliente c = clienteDAO.buscarPorId(clienteId)
                .orElseThrow(() -> new ValidacionException("Cliente no encontrado con ID " + clienteId));

        usuarioDAO.cambiarEstadoActivo(c.getUsuarioId(), activo);
        logger.info("Estado del cliente ID {} (Usuario ID {}) modificado a activo={}", clienteId, c.getUsuarioId(), activo);
    }

    private String generarClaveTemporal() {
        // Garantizar al menos 1 mayúscula, 1 minúscula y 1 número (mínimo 8 caracteres)
        String upper = RandomStringUtils.random(2, "ABCDEFGHJKLMNPQRSTUVWXYZ");
        String lower = RandomStringUtils.random(4, "abcdefghijkmnpqrstuvwxyz");
        String numbers = RandomStringUtils.random(2, "23456789");
        return upper + lower + numbers;
    }
}

