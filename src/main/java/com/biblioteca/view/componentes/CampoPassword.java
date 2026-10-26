package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Campo de contraseña moderno con placeholder, estado de validación y botón de mostrar/ocultar.
 */
public class CampoPassword extends JPanel {

    private final JPasswordField passwordField;
    private final JButton btnToggle;
    private final Border bordeNormal;
    private final Border bordeError;
    private boolean mostrandoPassword = false;
    private boolean estadoError = false;

    public CampoPassword() {
        this("Contraseña");
    }

    public CampoPassword(String placeholder) {
        setLayout(new BorderLayout(5, 0));
        setOpaque(false);

        passwordField = new JPasswordField();
        passwordField.setFont(Tema.FUENTE_NORMAL);
        passwordField.setForeground(Tema.COLOR_TEXTO);
        passwordField.setCaretColor(Tema.COLOR_PRIMARIO);
        passwordField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        passwordField.putClientProperty("JTextField.placeholderText", placeholder);

        btnToggle = new JButton(Iconos.crearOjo(18, Tema.COLOR_TEXTO_SECUNDARIO, false));
        btnToggle.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        btnToggle.setContentAreaFilled(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setToolTipText("Mostrar/Ocultar contraseña");

        btnToggle.addActionListener(e -> toggleVisibilidad());

        add(passwordField, BorderLayout.CENTER);
        add(btnToggle, BorderLayout.EAST);

        bordeNormal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 8)
        );

        bordeError = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_PELIGRO, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 7)
        );

        setBorder(bordeNormal);
    }

    private void toggleVisibilidad() {
        mostrandoPassword = !mostrandoPassword;
        if (mostrandoPassword) {
            passwordField.setEchoChar((char) 0);
            btnToggle.setIcon(Iconos.crearOjo(18, Tema.COLOR_PRIMARIO, true));
        } else {
            passwordField.setEchoChar('•');
            btnToggle.setIcon(Iconos.crearOjo(18, Tema.COLOR_TEXTO_SECUNDARIO, false));
        }
        passwordField.requestFocusInWindow();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public String getPasswordString() {
        return new String(passwordField.getPassword());
    }

    public void setText(String text) {
        passwordField.setText(text);
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    @Override
    public boolean requestFocusInWindow() {
        return passwordField.requestFocusInWindow();
    }

    public void mostrarError() {
        this.estadoError = true;
        setBorder(bordeError);
        repaint();
    }

    public void limpiarError() {
        this.estadoError = false;
        setBorder(bordeNormal);
        repaint();
    }
}
