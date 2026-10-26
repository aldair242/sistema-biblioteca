package com.biblioteca.view;

import com.biblioteca.model.Usuario;
import com.biblioteca.security.SesionActual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Componente superior común que muestra el usuario autenticado, su rol y el botón de cerrar sesión.
 */
public class BarraSuperior extends JPanel {

    public BarraSuperior(JFrame ventanaActual, Runnable accionCerrarSesion) {
        setLayout(new BorderLayout(15, 10));
        setBackground(new Color(240, 243, 246));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        Usuario usuario = SesionActual.getUsuario();
        String infoUsuario = "Usuario: " + (usuario != null ? usuario.getNombre() + " (" + usuario.getUsername() + ")" : "Invitado");
        String rolTexto = "ROL: " + (usuario != null ? usuario.getRol().name() : "DESCONOCIDO");

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelInfo.setOpaque(false);

        JLabel lblUsuario = new JLabel(infoUsuario);
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsuario.setForeground(new Color(33, 37, 41));

        JLabel lblRol = new JLabel(rolTexto);
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRol.setOpaque(true);
        lblRol.setBackground(new Color(13, 110, 253));
        lblRol.setForeground(Color.WHITE);
        lblRol.setBorder(new EmptyBorder(3, 8, 3, 8));

        panelInfo.add(lblUsuario);
        panelInfo.add(lblRol);
        add(panelInfo, BorderLayout.WEST);

        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(ventanaActual,
                    "¿Está seguro de que desea cerrar la sesión actual?",
                    "Confirmar Cierre de Sesión",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                accionCerrarSesion.run();
            }
        });

        add(btnLogout, BorderLayout.EAST);
    }
}

