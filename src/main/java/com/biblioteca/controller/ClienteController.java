package com.biblioteca.controller;

import com.biblioteca.model.Cliente;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;
import com.biblioteca.security.SesionActual;
import com.biblioteca.service.ServiceFactory;
import com.biblioteca.util.ManejadorExcepciones;
import com.biblioteca.view.ClienteView;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la interfaz del CLIENTE (lector).
 */
public class ClienteController {

    private final ClienteView view;
    private final AuthController authController;
    private final ServiceFactory sf;
    private Integer clienteIdActual;

    public ClienteController(ClienteView view, AuthController authController, ServiceFactory sf) {
        this.view = view;
        this.authController = authController;
        this.sf = sf;
        this.view.setController(this);
    }

    public void iniciar() {
        // Resolver el cliente asociado a la cuenta en sesión
        if (SesionActual.estaAutenticado()) {
            int usuarioId = SesionActual.getUsuario().getId();
            Optional<Cliente> optCli = sf.getClienteService().buscarPorUsuarioId(usuarioId);
            optCli.ifPresent(c -> this.clienteIdActual = c.getId());
        }

        cargarCatalogo();
        cargarPrestamos();
        cargarReservas();
        cargarMultas();
        view.setVisible(true);
    }

    public void cargarCatalogo() {
        List<Libro> libros = sf.getCatalogoService().listarTodosLibros();
        view.cargarTablaCatalogo(libros, id -> sf.getCatalogoService().contarDisponibles(id));
    }

    public void buscarLibros(String texto) {
        List<Libro> libros = sf.getCatalogoService().buscarLibros(texto);
        view.cargarTablaCatalogo(libros, id -> sf.getCatalogoService().contarDisponibles(id));
    }

    public void solicitarReserva(int libroId) {
        if (clienteIdActual == null) {
            view.mostrarError("No se pudo identificar el perfil de cliente asociado a esta sesión.");
            return;
        }

        try {
            Reserva r = sf.getReservaService().crearReserva(clienteIdActual, libroId);
            view.mostrarMensaje("Reserva solicitada exitosamente con ID " + r.getId() +
                    ". Se le notificará cuando un ejemplar esté disponible.");
            cargarCatalogo();
            cargarReservas();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "solicitud de reserva", ex);
        }
    }

    public void cargarPrestamos() {
        if (clienteIdActual != null) {
            List<Prestamo> prestamos = sf.getPrestamoService().listarPorCliente(clienteIdActual);
            view.cargarTablaPrestamos(prestamos);
        }
    }

    public void renovarPrestamo(int prestamoId) {
        try {
            sf.getPrestamoService().renovar(prestamoId);
            view.mostrarMensaje("Préstamo renovado exitosamente por un periodo adicional.");
            cargarPrestamos();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "renovación de préstamo", ex);
        }
    }

    public void cargarReservas() {
        if (clienteIdActual != null) {
            List<Reserva> reservas = sf.getReservaService().listarPorCliente(clienteIdActual);
            view.cargarTablaReservas(reservas);
        }
    }

    public void cancelarReserva(int reservaId) {
        try {
            sf.getReservaService().cancelarReserva(reservaId);
            view.mostrarMensaje("Reserva cancelada correctamente.");
            cargarReservas();
            cargarCatalogo();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "cancelación de reserva", ex);
        }
    }

    public void cargarMultas() {
        if (clienteIdActual != null) {
            List<Multa> multas = sf.getMultaService().listarPorCliente(clienteIdActual);
            view.cargarTablaMultas(multas);
        }
    }

    public void cambiarContrasena(String actual, String nueva, String confirmar) {
        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            view.mostrarAdvertencia("Complete todos los campos del formulario.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            view.mostrarAdvertencia("La confirmación de la contraseña no coincide.");
            return;
        }

        try {
            int usuarioId = SesionActual.getUsuario().getId();
            sf.getAuthService().cambiarClave(usuarioId, actual, nueva);
            view.mostrarMensaje("Su contraseña ha sido actualizada con éxito.");
            view.limpiarFormSeguridad();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "cambio de contraseña", ex);
        }
    }

    public void cerrarSesion() {
        authController.cerrarSesion(view);
    }
}
