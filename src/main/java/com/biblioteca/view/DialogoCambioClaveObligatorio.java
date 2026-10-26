package com.biblioteca.view;

import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.security.SesionActual;
import com.biblioteca.security.Validador;
import com.biblioteca.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Diálogo modal para cambio obligatorio de contraseña en el primer inicio de sesión.
 */
public class DialogoCambioClaveObligatorio extends JDialog {

    private final AuthService authService;
    private JPasswordField txtActual;
    private JPasswordField txtNueva;
    private JPasswordField txtConfirmar;
    private boolean claveCambiadaExitosamente = false;

    public DialogoCambioClaveObligatorio(Frame parent, AuthService authService) {
        super(parent, "Cambio Obligatorio de Contraseña", true);
        this.authService = authService;
        initComponents();
    }

    private void initComponents() {
        setSize(440, 360);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Mensaje superior
        JLabel lblMensaje = new JLabel("<html><b>Es necesario actualizar su contraseña</b><br/>" +
                "Por motivos de seguridad, debe cambiar su clave temporal antes de continuar.</html>");
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblMensaje, BorderLayout.NORTH);

        // Formulario
        JPanel panelForm = new JPanel(new GridLayout(6, 1, 5, 5));

        panelForm.add(new JLabel("Contraseña actual (temporal):"));
        txtActual = new JPasswordField();
        panelForm.add(txtActual);

        panelForm.add(new JLabel("Nueva contraseña (mínimo 8 caracteres, mayúscula, minúscula y número):"));
        txtNueva = new JPasswordField();
        panelForm.add(txtNueva);

        panelForm.add(new JLabel("Confirmar nueva contraseña:"));
        txtConfirmar = new JPasswordField();
        panelForm.add(txtConfirmar);

        panel.add(panelForm, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = new JButton("Cancelar (Salir)");
        JButton btnGuardar = new JButton("Actualizar Contraseña");
        btnGuardar.setBackground(new Color(13, 110, 253));
        btnGuardar.setForeground(Color.WHITE);

        btnCancelar.addActionListener(e -> {
            authService.logout();
            claveCambiadaExitosamente = false;
            dispose();
        });

        btnGuardar.addActionListener(e -> procesarCambio());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void procesarCambio() {
        String actual = new String(txtActual.getPassword());
        String nueva = new String(txtNueva.getPassword());
        String confirmar = new String(txtConfirmar.getPassword());

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nueva.equals(confirmar)) {
            JOptionPane.showMessageDialog(this, "La confirmación no coincide con la nueva contraseña.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Validador.validarPasswordFuerte(nueva);
            int usuarioId = SesionActual.getUsuario().getId();
            authService.cambiarClave(usuarioId, actual, nueva);

            JOptionPane.showMessageDialog(this, "Contraseña actualizada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            claveCambiadaExitosamente = true;
            dispose();

        } catch (ValidacionException | AutenticacionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al cambiar contraseña", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isClaveCambiadaExitosamente() {
        return claveCambiadaExitosamente;
    }
}

