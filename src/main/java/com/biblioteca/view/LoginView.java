package com.biblioteca.view;

import com.biblioteca.controller.AuthController;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Ventana de inicio de sesión rediseñada con pantalla dividida (Split Screen),
 * panel de marca corporativo, tarjeta flotante de acceso y mensajes de error en línea estables.
 */
public class LoginView extends JFrame {

    private CampoTexto txtUsuario;
    private CampoPassword txtPassword;
    private BotonPrimario btnIngresar;
    private CardLayout cardErrorLayout;
    private JPanel panelContenedorError;
    private JPanel panelErrorBanner;
    private JLabel lblMensajeError;
    private AuthController controller;

    public LoginView() {
        Tema.aplicar();
        initComponents();
    }

    public void setController(AuthController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Acceso al Sistema");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setMinimumSize(new Dimension(850, 520));
        setLocationRelativeTo(null);
        setIconImage(Iconos.crearAppLogoImage(64));

        // División horizontal: panel de presentación y formulario
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(Tema.COLOR_FONDO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Panel Izquierdo: Branding / Presentación
        gbc.gridx = 0;
        gbc.weightx = 0.42;
        panelPrincipal.add(crearPanelBranding(), gbc);


        gbc.gridx = 1;
        gbc.weightx = 0.58;
        panelPrincipal.add(crearPanelFormulario(), gbc);

        setContentPane(panelPrincipal);

        // Atajo Enter para iniciar sesión
        getRootPane().setDefaultButton(btnIngresar);
    }

    private JPanel crearPanelBranding() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradiente elegante de Azul Índigo a Azul Profundo
                GradientPaint gp = new GradientPaint(
                        0, 0, Tema.COLOR_PRIMARIO,
                        getWidth(), getHeight(), Color.decode("#1A237E")
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Efectos gráficos decorativos
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillOval(-50, -50, 300, 300);
                g2.fillOval(getWidth() - 180, getHeight() - 180, 260, 260);

                g2.dispose();
            }
        };

        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 35, 40, 35));

        // Centro con Ícono grande y textos
        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.setOpaque(false);

        JLabel lblIcono = new JLabel(Iconos.crearLibro(72, Color.WHITE));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSistema = new JLabel("BIBLIOTECA CI");
        lblSistema.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 26));
        lblSistema.setForeground(Color.WHITE);
        lblSistema.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistema Integral de Gestión y Catálogo");
        lblSub.setFont(Tema.FUENTE_SECCION);
        lblSub.setForeground(new Color(224, 231, 255));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html><center style='color:#E2E8F0; font-size:12px; line-height: 1.5;'>" +
                "Gestión ágil de préstamos, devoluciones automatizadas con cálculo de mora y control de reservas FIFO en tiempo real." +
                "</center></html>");
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelCentro.add(Box.createVerticalGlue());
        panelCentro.add(lblIcono);
        panelCentro.add(Box.createVerticalStrut(20));
        panelCentro.add(lblSistema);
        panelCentro.add(Box.createVerticalStrut(8));
        panelCentro.add(lblSub);
        panelCentro.add(Box.createVerticalStrut(18));
        panelCentro.add(lblDesc);
        panelCentro.add(Box.createVerticalGlue());

        panel.add(panelCentro, BorderLayout.CENTER);

        JLabel lblFooter = new JLabel("Universidad Tecnológica del Perú • 2026", SwingConstants.CENTER);
        lblFooter.setFont(Tema.FUENTE_PEQUENA);
        lblFooter.setForeground(new Color(197, 202, 233));
        panel.add(lblFooter, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panelContenedor = new JPanel(new GridBagLayout());
        panelContenedor.setBackground(Tema.COLOR_FONDO);

        TarjetaPanel tarjeta = new TarjetaPanel(new GridBagLayout());
        tarjeta.setPreferredSize(new Dimension(420, 540));
        tarjeta.setMinimumSize(new Dimension(380, 490));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(32, 36, 32, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel lblBienvenido = new JLabel("Iniciar Sesión");
        lblBienvenido.setFont(Tema.FUENTE_TITULO);
        lblBienvenido.setForeground(Tema.COLOR_TEXTO);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        tarjeta.add(lblBienvenido, gbc);

        JLabel lblInstruccion = new JLabel("Ingrese sus credenciales de usuario o DNI");
        lblInstruccion.setFont(Tema.FUENTE_PEQUENA);
        lblInstruccion.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        tarjeta.add(lblInstruccion, gbc);

        // Ranura con CardLayout y altura fija para alertas sin desplazar campos
        cardErrorLayout = new CardLayout();
        panelContenedorError = new JPanel(cardErrorLayout);
        panelContenedorError.setOpaque(false);
        panelContenedorError.setPreferredSize(new Dimension(340, 42));
        panelContenedorError.setMinimumSize(new Dimension(300, 42));
        panelContenedorError.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel panelVacio = new JPanel();
        panelVacio.setOpaque(false);
        panelContenedorError.add(panelVacio, "VACIO");

        panelErrorBanner = new JPanel(new BorderLayout(8, 0));
        panelErrorBanner.setBackground(Tema.COLOR_PELIGRO_CLARO);
        panelErrorBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_PELIGRO, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblIconoError = new JLabel(Iconos.crearAlerta(18, Tema.COLOR_PELIGRO));
        panelErrorBanner.add(lblIconoError, BorderLayout.WEST);

        lblMensajeError = new JLabel("Credenciales inválidas");
        lblMensajeError.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblMensajeError.setForeground(Tema.COLOR_PELIGRO);
        panelErrorBanner.add(lblMensajeError, BorderLayout.CENTER);

        panelContenedorError.add(panelErrorBanner, "ERROR");
        cardErrorLayout.show(panelContenedorError, "VACIO");

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        tarjeta.add(panelContenedorError, gbc);

        JLabel lblUser = new JLabel("Usuario o DNI:");
        lblUser.setFont(Tema.FUENTE_SECCION);
        lblUser.setForeground(Tema.COLOR_TEXTO);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 6, 0);
        tarjeta.add(lblUser, gbc);

        txtUsuario = new CampoTexto("Ej. admin, biblio o DNI del cliente");
        txtUsuario.setPreferredSize(new Dimension(340, 40));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        tarjeta.add(txtUsuario, gbc);

        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(Tema.FUENTE_SECCION);
        lblPass.setForeground(Tema.COLOR_TEXTO);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 6, 0);
        tarjeta.add(lblPass, gbc);

        txtPassword = new CampoPassword("Contraseña");
        txtPassword.setPreferredSize(new Dimension(340, 40));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 8, 0);
        tarjeta.add(txtPassword, gbc);

        // Enlace para recuperación de contraseña
        JPanel panelOlvido = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelOlvido.setOpaque(false);
        JButton btnOlvido = new JButton("¿Olvidaste tu contraseña? Restablecer aquí");
        btnOlvido.setFont(Tema.FUENTE_PEQUENA_BOLD);
        btnOlvido.setForeground(Tema.COLOR_PRIMARIO);
        btnOlvido.setBorderPainted(false);
        btnOlvido.setContentAreaFilled(false);
        btnOlvido.setFocusPainted(false);
        btnOlvido.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOlvido.addActionListener(e -> {
            limpiarBannerError();
            DialogoRecuperarClave dialogo = new DialogoRecuperarClave(this, controller);
            dialogo.setVisible(true);
        });
        panelOlvido.add(btnOlvido);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 16, 0);
        tarjeta.add(panelOlvido, gbc);

        btnIngresar = new BotonPrimario("Ingresar al Sistema", Iconos.crearCheck(18, Color.WHITE));
        btnIngresar.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 15));
        btnIngresar.setPreferredSize(new Dimension(340, 44));
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        tarjeta.add(btnIngresar, gbc);

        btnIngresar.addActionListener(e -> {
            limpiarBannerError();
            if (controller != null) {
                controller.procesarLogin(txtUsuario.getText(), txtPassword.getPasswordString());
            }
        });

        gbc.gridy = 9;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        tarjeta.add(Box.createGlue(), gbc);

        panelContenedor.add(tarjeta);
        return panelContenedor;
    }

    public void mostrarError(String mensaje) {
        lblMensajeError.setText(mensaje);
        if (cardErrorLayout != null && panelContenedorError != null) {
            cardErrorLayout.show(panelContenedorError, "ERROR");
        }
        txtUsuario.mostrarError();
        txtPassword.mostrarError();
        panelContenedorError.revalidate();
        panelContenedorError.repaint();
    }

    public void limpiarBannerError() {
        if (cardErrorLayout != null && panelContenedorError != null) {
            cardErrorLayout.show(panelContenedorError, "VACIO");
        }
        txtUsuario.limpiarError();
        txtPassword.limpiarError();
        if (panelContenedorError != null) {
            panelContenedorError.revalidate();
            panelContenedorError.repaint();
        }
    }

    public void mostrarMensaje(String mensaje) {
        DialogoModerno.mostrarMensaje(this, mensaje, "Información", DialogoModerno.TipoDialogo.EXITO);
    }

    public void setUsuario(String usuario) {
        if (txtUsuario != null && usuario != null) {
            txtUsuario.setText(usuario);
        }
    }

    public void limpiarClave() {
        txtPassword.setText("");
        txtPassword.requestFocusInWindow();
    }
}
