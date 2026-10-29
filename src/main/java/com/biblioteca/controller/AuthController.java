package com.biblioteca.controller;

import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.ResultadoRecuperacion;
import com.biblioteca.model.Rol;
import com.biblioteca.model.Usuario;
import com.biblioteca.service.AuthService;
import com.biblioteca.service.ServiceFactory;
import com.biblioteca.util.ManejadorExcepciones;
import com.biblioteca.view.*;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Controlador de autenticación y navegación según el rol del usuario.
 */
public class AuthController {

    private final AuthService authService;
    private final LoginView loginView;

    public AuthController(LoginView loginView, AuthService authService) {
        this.loginView = loginView;
        this.authService = authService;
        this.loginView.setController(this);
    }

    public void iniciar() {
        loginView.setVisible(true);
    }

    public void procesarLogin(String username, String password) {
        try {
            Usuario usuario = authService.login(username, password);

            // Verificar cambio obligatorio de contraseña en primer acceso
            if (usuario.isDebeCambiarClave()) {
                DialogoCambioClaveObligatorio dialogo = new DialogoCambioClaveObligatorio(loginView, authService);
                dialogo.setVisible(true);

                if (!dialogo.isClaveCambiadaExitosamente()) {
                    loginView.limpiarClave();
                    return;
                }
            }

            // Ocultar login y abrir ventana según rol
            loginView.setVisible(false);
            abrirVentanaSegunRol(usuario.getRol());

        } catch (AutenticacionException ex) {
            loginView.mostrarError(ex.getMessage());
            loginView.limpiarClave();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(loginView, "autenticación de usuario", ex);
            loginView.limpiarClave();
        }
    }

    /**
     * Procesa la solicitud de recuperación de clave enviando una contraseña temporal al correo registrado.
     *
     * @param email   Correo del lector
     * @param dialogo Diálogo de interfaz de recuperación
     */
    public void procesarRecuperacionClave(String email, DialogoRecuperarClave dialogo) {
        try {
            ServiceFactory sf = ServiceFactory.getInstancia();
            ResultadoRecuperacion res = sf.getClienteService().restablecerClavePorEmail(email);

            sf.getNotificacionService().enviarCorreoRecuperacionClave(
                    res.cliente().getEmail(),
                    res.cliente().getNombreCompleto(),
                    res.usuario().getUsername(),
                    res.claveTemporal()
            );

            dialogo.mostrarExito(res);
            loginView.setUsuario(res.usuario().getUsername());
            loginView.limpiarClave();

        } catch (ValidacionException ex) {
            dialogo.mostrarError(ex.getMessage());
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(dialogo, "recuperación de contraseña", ex);
        }
    }

    private void abrirVentanaSegunRol(Rol rol) {
        ServiceFactory sf = ServiceFactory.getInstancia();

        switch (rol) {
            case ADMIN -> {
                AdminView adminView = new AdminView();
                AdminController adminController = new AdminController(adminView, this, sf);
                adminController.iniciar();
            }
            case BIBLIOTECARIO -> {
                BibliotecarioView biblioView = new BibliotecarioView();
                BibliotecarioController biblioController = new BibliotecarioController(biblioView, this, sf);
                biblioController.iniciar();
            }
            case CLIENTE -> {
                ClienteView clienteView = new ClienteView();
                ClienteController clienteController = new ClienteController(clienteView, this, sf);
                clienteController.iniciar();
            }
        }
    }

    public void cerrarSesion(JFrame ventanaActual) {
        authService.logout();
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            loginView.limpiarClave();
            loginView.setVisible(true);
        });
    }
}

