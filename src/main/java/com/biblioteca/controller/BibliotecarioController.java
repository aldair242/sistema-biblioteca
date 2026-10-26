package com.biblioteca.controller;

import com.biblioteca.model.*;
import com.biblioteca.service.*;
import com.biblioteca.view.BibliotecarioView;

import javax.swing.JOptionPane;
import java.util.List;

/**
 * Controlador para la interfaz del BIBLIOTECARIO.
 */
public class BibliotecarioController {

    private final BibliotecarioView view;
    private final AuthController authController;
    private final ServiceFactory sf;

    public BibliotecarioController(BibliotecarioView view, AuthController authController, ServiceFactory sf) {
        this.view = view;
        this.authController = authController;
        this.sf = sf;
        this.view.setController(this);
    }

    public void iniciar() {
        cargarClientes();
        cargarCatalogo();
        cargarPrestamos();
        cargarReservas();
        cargarMultas(true);
        view.setVisible(true);
    }

    // --- CLIENTES ---
    public void cargarClientes() {
        List<Cliente> clientes = sf.getClienteService().listarTodos();
        view.cargarTablaClientes(clientes);
    }

    public void registrarCliente(String dni, String nombres, String apellidos, String email, String telefono) {
        try {
            Cliente nuevo = new Cliente(null, null, dni.trim(), nombres.trim(), apellidos.trim(), email.trim(), telefono.trim(), null);
            String claveTemporal = sf.getClienteService().registrarCliente(nuevo);

            view.mostrarModalClaveTemporal(nuevo.getDni(), nuevo.getNombreCompleto(), claveTemporal);
            view.limpiarFormCliente();
            cargarClientes();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    public void editarCliente(int id, String dni, String nombres, String apellidos, String email, String telefono) {
        try {
            Cliente c = sf.getClienteService().buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            c.setDni(dni.trim());
            c.setNombres(nombres.trim());
            c.setApellidos(apellidos.trim());
            c.setEmail(email.trim());
            c.setTelefono(telefono.trim());

            sf.getClienteService().editarCliente(c);
            view.mostrarMensaje("Información del cliente actualizada con éxito.");
            cargarClientes();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    public void toggleActivoCliente(int clienteId) {
        try {
            Cliente c = sf.getClienteService().buscarPorId(clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            Usuario u = sf.getUsuarioDAO().buscarPorId(c.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            boolean nuevo = !u.isActivo();
            sf.getClienteService().cambiarEstadoActivo(clienteId, nuevo);
            view.mostrarMensaje("Estado del cliente cambiado a: " + (nuevo ? "ACTIVO" : "INACTIVO"));
            cargarClientes();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    public void buscarClientes(String texto) {
        List<Cliente> res = sf.getClienteService().buscarClientes(texto);
        view.cargarTablaClientes(res);
    }

    // --- CATÁLOGO ---
    public void cargarCatalogo() {
        List<Libro> libros = sf.getCatalogoService().listarTodosLibros();
        view.cargarTablaCatLibros(libros, id -> sf.getCatalogoService().contarDisponibles(id));
    }

    public void buscarCatalogo(String texto) {
        List<Libro> libros = sf.getCatalogoService().buscarLibros(texto);
        view.cargarTablaCatLibros(libros, id -> sf.getCatalogoService().contarDisponibles(id));
    }

    public void cargarEjemplaresDeLibro(int libroId) {
        List<Ejemplar> ejemplares = sf.getCatalogoService().listarEjemplaresPorLibro(libroId);
        view.cargarTablaCatEjemplares(ejemplares);
    }

    public void cambiarEstadoEjemplarDialog(int ejemplarId) {
        EstadoEjemplar[] opciones = {EstadoEjemplar.DISPONIBLE, EstadoEjemplar.EN_MANTENIMIENTO, EstadoEjemplar.BAJA};
        EstadoEjemplar elegido = (EstadoEjemplar) JOptionPane.showInputDialog(
                view,
                "Seleccione el nuevo estado del ejemplar:",
                "Estado del Ejemplar",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        if (elegido != null) {
            try {
                sf.getCatalogoService().cambiarEstadoEjemplar(ejemplarId, elegido);
                view.mostrarMensaje("Estado de ejemplar actualizado.");
                cargarCatalogo();
            } catch (Exception ex) {
                view.mostrarError(ex.getMessage());
            }
        }
    }

    // --- PRÉSTAMOS ---
    public void cargarPrestamos() {
        List<Prestamo> prestamos = sf.getPrestamoService().listarActivosYVencidos();
        view.cargarTablaPrestamos(prestamos);
    }

    public void registrarPrestamo(String dni, String codigoBarras) {
        try {
            Prestamo p = sf.getPrestamoService().registrarPrestamo(dni, codigoBarras);
            String comprobante = "<html><body>" +
                    "<h3>Préstamo Registrado con Éxito</h3>" +
                    "<b>ID Préstamo:</b> " + p.getId() + "<br/>" +
                    "<b>Fecha de Préstamo:</b> " + p.getFechaPrestamo() + "<br/>" +
                    "<b>Fecha de Vencimiento:</b> <span style='color:red; font-weight:bold;'>" + p.getFechaVencimiento() + "</span><br/>" +
                    "<p><i>El cliente debe devolver el ejemplar a más tardar en la fecha indicada para evitar multas.</i></p>" +
                    "</body></html>";
            JOptionPane.showMessageDialog(view, comprobante, "Comprobante de Préstamo", JOptionPane.INFORMATION_MESSAGE);

            cargarPrestamos();
            cargarCatalogo();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    // --- DEVOLUCIONES ---
    public void registrarDevolucion(String codigoBarras) {
        try {
            PrestamoService.ResultadoDevolucion res = sf.getPrestamoService().registrarDevolucion(codigoBarras);
            view.mostrarResultadoDevolucion(res);
            cargarPrestamos();
            cargarCatalogo();
            cargarMultas(false);
            cargarReservas();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    // --- RESERVAS ---
    public void cargarReservas() {
        List<Reserva> reservas = sf.getReservaService().listarTodas();
        view.cargarTablaReservas(reservas);
    }

    public void crearReserva(String dni, String libroIdStr) {
        try {
            int libroId = Integer.parseInt(libroIdStr.trim());
            Cliente c = sf.getClienteService().buscarPorDni(dni.trim())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con DNI " + dni));

            Reserva r = sf.getReservaService().crearReserva(c.getId(), libroId);
            view.mostrarMensaje("Reserva en cola FIFO creada con ID " + r.getId());
            cargarReservas();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    public void cancelarReserva(int reservaId) {
        try {
            sf.getReservaService().cancelarReserva(reservaId);
            view.mostrarMensaje("Reserva cancelada exitosamente.");
            cargarReservas();
            cargarCatalogo();
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    // --- MULTAS ---
    public void cargarMultas(boolean soloPendientes) {
        List<Multa> multas = soloPendientes ?
                sf.getMultaService().listarTodas().stream().filter(m -> m.getEstado() == EstadoMulta.PENDIENTE).toList()
                : sf.getMultaService().listarTodas();
        view.cargarTablaMultas(multas);
    }

    public void cobrarMulta(int multaId) {
        try {
            sf.getMultaService().cobrar(multaId);
            view.mostrarMensaje("Multa cobrada exitosamente.");
            cargarMultas(false);
        } catch (Exception ex) {
            view.mostrarError(ex.getMessage());
        }
    }

    public void cerrarSesion() {
        authController.cerrarSesion(view);
    }
}

