package com.biblioteca.controller;

import com.biblioteca.model.*;
import com.biblioteca.service.*;
import com.biblioteca.util.ManejadorExcepciones;
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
            ManejadorExcepciones.manejar(view, "registro de cliente", ex);
        }
    }

    public void editarCliente(int id, String dni, String nombres, String apellidos, String email, String telefono) {
        try {
            Cliente c = sf.getClienteService().buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID " + id));
            c.setDni(dni.trim());
            c.setNombres(nombres.trim());
            c.setApellidos(apellidos.trim());
            c.setEmail(email.trim());
            c.setTelefono(telefono.trim());

            sf.getClienteService().editarCliente(c);
            view.mostrarMensaje("Información del cliente actualizada con éxito.");
            cargarClientes();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "edición de cliente", ex);
        }
    }

    public void eliminarCliente(int id) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de que desea eliminar permanentemente a este cliente?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                sf.getClienteService().eliminarCliente(id);
                view.mostrarMensaje("Cliente eliminado exitosamente del sistema.");
                view.limpiarFormCliente();
                cargarClientes();
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "eliminación de cliente", ex);
            }
        }
    }

    public void restablecerClaveCliente(int id) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Desea restablecer la contraseña de este cliente y generar una nueva clave temporal?",
                "Restablecer Contraseña", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Cliente c = sf.getClienteService().buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
                String claveTemp = sf.getClienteService().restablecerClaveTemporal(id);
                view.mostrarModalClaveTemporal(c.getDni(), c.getNombreCompleto(), claveTemp);
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "restablecimiento de contraseña", ex);
            }
        }
    }

    public void toggleActivoCliente(int clienteId) {
        try {
            Cliente c = sf.getClienteService().buscarPorId(clienteId)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID " + clienteId));
            Usuario u = sf.getUsuarioDAO().buscarPorId(c.getUsuarioId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            boolean nuevo = !u.isActivo();
            sf.getClienteService().cambiarEstadoActivo(clienteId, nuevo);
            view.mostrarMensaje("Estado del cliente cambiado a: " + (nuevo ? "ACTIVO" : "INACTIVO"));
            cargarClientes();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "cambio de estado del cliente", ex);
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
        cambiarEstadoEjemplarDialog(ejemplarId, null);
    }

    public void cambiarEstadoEjemplarDialog(int ejemplarId, Integer libroId) {
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
                if (libroId != null) {
                    cargarEjemplaresDeLibro(libroId);
                }
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "cambio de estado de ejemplar", ex);
            }
        }
    }

    public void agregarEjemplares(int libroId, int cantidad) {
        try {
            List<Ejemplar> nuevos = sf.getCatalogoService().agregarMultiplesEjemplares(libroId, cantidad);
            view.mostrarMensaje("Se agregaron exitosamente " + nuevos.size() + " nuevo(s) ejemplar(es).");
            cargarCatalogo();
            cargarEjemplaresDeLibro(libroId);
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "registro de nuevos ejemplares", ex);
        }
    }

    public void eliminarEjemplar(int ejemplarId, int libroId) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de eliminar físicamente este ejemplar?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                sf.getCatalogoService().eliminarEjemplar(ejemplarId);
                view.mostrarMensaje("Ejemplar eliminado correctamente.");
                cargarCatalogo();
                cargarEjemplaresDeLibro(libroId);
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "eliminación de ejemplar", ex);
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
            String comprobante = "<h3>Préstamo Registrado con Éxito</h3>" +
                    "<b>ID Préstamo:</b> " + p.getId() + "<br/>" +
                    "<b>Fecha de Préstamo:</b> " + p.getFechaPrestamo() + "<br/>" +
                    "<b>Fecha de Vencimiento:</b> <span style='color:#C62828; font-weight:bold;'>" + p.getFechaVencimiento() + "</span><br/>" +
                    "<p><i>El cliente debe devolver el ejemplar a más tardar en la fecha indicada para evitar multas.</i></p>";
            view.mostrarMensaje(comprobante);

            cargarPrestamos();
            cargarCatalogo();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "registro de préstamo", ex);
        }
    }

    public void renovarPrestamo(int prestamoId) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Desea renovar el préstamo #" + prestamoId + " para extender el plazo de devolución?",
                "Confirmar Renovación", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                sf.getPrestamoService().renovar(prestamoId);
                Prestamo renovado = sf.getPrestamoService().buscarPorId(prestamoId).orElse(null);
                String nuevaFecha = (renovado != null) ? renovado.getFechaVencimiento().toString() : "";
                int rens = (renovado != null) ? renovado.getRenovaciones() : 1;
                view.mostrarMensaje("<html><b>Préstamo renovado con éxito.</b><br/>" +
                        "Nueva fecha de vencimiento: <span style='color:#3F51B5; font-weight:bold;'>" +
                        nuevaFecha + "</span> (Renovación #" + rens + ")</html>");
                cargarPrestamos();
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "renovación de préstamo", ex);
            }
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
            ManejadorExcepciones.manejar(view, "registro de devolución", ex);
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
            ManejadorExcepciones.manejar(view, "creación de reserva", ex);
        }
    }

    public void cancelarReserva(int reservaId) {
        try {
            sf.getReservaService().cancelarReserva(reservaId);
            view.mostrarMensaje("Reserva cancelada exitosamente.");
            cargarReservas();
            cargarCatalogo();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "cancelación de reserva", ex);
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
            ManejadorExcepciones.manejar(view, "cobro de multa", ex);
        }
    }

    public void cerrarSesion() {
        authController.cerrarSesion(view);
    }
}
