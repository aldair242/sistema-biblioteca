package com.biblioteca.view;

import com.biblioteca.controller.AuthController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana de inicio de sesión (LoginView).
 * Vista desacoplada de la lógica de negocio según el patrón MVC.
 */
public class LoginView extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private AuthController controller;

    public LoginView() {
        initComponents();
    }

    public void setController(AuthController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Acceso");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 380);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));
        panelPrincipal.setBackground(new Color(248, 249, 250));

        // Encabezado
        JPanel panelHeader = new JPanel(new GridLayout(2, 1, 5, 5));
        panelHeader.setOpaque(false);
        JLabel lblTitulo = new JLabel("Biblioteca Central", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(33, 37, 41));

        JLabel lblSubtitulo = new JLabel("Ingrese sus credenciales de acceso", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(108, 117, 125));

        panelHeader.add(lblTitulo);
        panelHeader.add(lblSubtitulo);
        panelPrincipal.add(panelHeader, BorderLayout.NORTH);

        // Formulario
        JPanel panelForm = new JPanel(new GridLayout(4, 1, 8, 8));
        panelForm.setOpaque(false);

        JLabel lblUser = new JLabel("Usuario (o DNI para clientes):");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ej. admin, biblio o DNI");

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.putClientProperty("JTextField.placeholderText", "Contraseña");

        panelForm.add(lblUser);
        panelForm.add(txtUsuario);
        panelForm.add(lblPass);
        panelForm.add(txtPassword);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        // Botón Ingresar
        JPanel panelBoton = new JPanel(new GridLayout(1, 1, 10, 10));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(15, 0, 5, 0));

        btnIngresar = new JButton("Iniciar Sesión");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIngresar.setBackground(new Color(13, 110, 253));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIngresar.addActionListener(e -> {
            if (controller != null) {
                controller.procesarLogin(txtUsuario.getText(), new String(txtPassword.getPassword()));
            }
        });

        // Enviar con Enter en cualquier campo
        getRootPane().setDefaultButton(btnIngresar);

        panelBoton.add(btnIngresar);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        setContentPane(panelPrincipal);
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Inicio de Sesión", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public void limpiarClave() {
        txtPassword.setText("");
        txtPassword.requestFocus();
    }
}
