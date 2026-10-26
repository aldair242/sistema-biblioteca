package com.biblioteca.service;

import com.biblioteca.config.GestorTransaccion;
import com.biblioteca.dao.*;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.Cliente;
import com.biblioteca.model.Multa;
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
 * Servicio encargado de la administración de clientes y lectores de la biblioteca.
 * Aplica principios SOLID y control de acceso basado en roles (RBAC).
 */
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteDAO clienteDAO;
    private final UsuarioDAO usuarioDAO;
    private final PrestamoDAO prestamoDAO;
    private final MultaDAO multaDAO;
    private final ReservaDAO reservaDAO;

    public ClienteService(ClienteDAO clienteDAO, UsuarioDAO usuarioDAO) {
        this(clienteDAO, usuarioDAO, null, null, null);
    }

    public ClienteService(ClienteDAO clienteDAO,
                          UsuarioDAO usuarioDAO,
                          PrestamoDAO prestamoDAO,
                          MultaDAO multaDAO,
                          ReservaDAO reservaDAO) {
        this.clienteDAO = clienteDAO;
        this.usuarioDAO = usuarioDAO;
        this.prestamoDAO = prestamoDAO;
        this.multaDAO = multaDAO;
        this.reservaDAO = reservaDAO;
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

            // 2. Crear cliente vinculado
            cliente.setUsuarioId(usuarioGuardado.getId());
            cliente.setFechaRegistro(LocalDate.now());
            clienteDAO.guardar(cliente);

            logger.info("Cliente registrado con éxito: DNI '{}', ID Cliente: {}", cliente.getDni(), cliente.getId());
            return claveTemporal;
        });
    }

    /**
     * Edita los datos personales de un cliente y sincroniza su nombre y DNI con la cuenta de usuario.
     *
     * @param cliente Datos actualizados del cliente
     */
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

        GestorTransaccion.ejecutar(() -> {
            clienteDAO.actualizar(cliente);

            // Sincronizar el nombre de usuario (DNI) y el nombre completo en la tabla usuario
            if (cliente.getUsuarioId() != null) {
                usuarioDAO.buscarPorId(cliente.getUsuarioId()).ifPresent(u -> {
                    u.setUsername(cliente.getDni().trim());
                    u.setNombre(cliente.getNombreCompleto());
                    usuarioDAO.actualizar(u);
                });
            }
            logger.info("Datos del cliente ID {} actualizados correctamente", cliente.getId());
            return null;
        });
    }

    /**
     * Elimina a un cliente de la base de datos si no tiene préstamos activos, multas pendientes ni historial protegido.
     *
     * @param clienteId Identificador del cliente
     */
    public void eliminarCliente(int clienteId) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        Cliente cliente = clienteDAO.buscarPorId(clienteId)
                .orElseThrow(() -> new ValidacionException("Cliente no encontrado con ID " + clienteId));

        if (prestamoDAO != null) {
            int prestamosActivos = prestamoDAO.contarActivosPorCliente(clienteId);
            if (prestamosActivos > 0) {
                throw new ValidacionException("No se puede eliminar el cliente porque tiene " + prestamosActivos + " préstamo(s) activo(s).");
            }
            if (!prestamoDAO.listarPorCliente(clienteId).isEmpty()) {
                throw new ValidacionException("El cliente cuenta con historial de préstamos en la biblioteca. " +
                        "Por integridad de auditoría no se puede eliminar físicamente; utilice 'Activar / Desactivar' para suspender su cuenta.");
            }
        }

        if (multaDAO != null) {
            List<Multa> multasPendientes = multaDAO.listarPendientesPorCliente(clienteId);
            if (!multasPendientes.isEmpty()) {
                throw new ValidacionException("No se puede eliminar el cliente porque tiene multas pendientes de pago.");
            }
            if (!multaDAO.listarPorCliente(clienteId).isEmpty()) {
                throw new ValidacionException("El cliente cuenta con historial de multas registrado. " +
                        "Utilice 'Activar / Desactivar' para suspender su cuenta.");
            }
        }

        if (reservaDAO != null && !reservaDAO.listarPorCliente(clienteId).isEmpty()) {
            throw new ValidacionException("El cliente cuenta con historial de reservas en el sistema. " +
                    "Utilice 'Activar / Desactivar' para suspender su cuenta.");
        }

        GestorTransaccion.ejecutar(() -> {
            clienteDAO.eliminar(clienteId);
            if (cliente.getUsuarioId() != null) {
                usuarioDAO.eliminar(cliente.getUsuarioId());
            }
            logger.info("Cliente ID {} y su usuario asociado eliminados exitosamente", clienteId);
            return null;
        });
    }

    /**
     * Restablece la contraseña de un cliente generando una nueva clave temporal obligatoria.
     *
     * @param clienteId Identificador del cliente
     * @return Nueva contraseña temporal generada
     */
    public String restablecerClaveTemporal(int clienteId) {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);

        Cliente cliente = clienteDAO.buscarPorId(clienteId)
                .orElseThrow(() -> new ValidacionException("Cliente no encontrado con ID " + clienteId));

        String claveTemporal = generarClaveTemporal();
        String hash = PasswordHasher.hashear(claveTemporal);

        usuarioDAO.cambiarClave(cliente.getUsuarioId(), hash, true);
        logger.info("Contraseña temporal restablecida para cliente ID {} (DNI '{}')", clienteId, cliente.getDni());
        return claveTemporal;
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
        String upper = RandomStringUtils.random(2, "ABCDEFGHJKLMNPQRSTUVWXYZ");
        String lower = RandomStringUtils.random(4, "abcdefghijkmnpqrstuvwxyz");
        String numbers = RandomStringUtils.random(2, "23456789");
        return upper + lower + numbers;
    }
}
