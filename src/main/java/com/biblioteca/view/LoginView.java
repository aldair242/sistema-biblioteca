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
 * panel de marca corporativo, tarjeta flotante de acceso y mensajes de error en línea.
 */
public class LoginView extends JFrame {

    private CampoTexto txtUsuario;
    private CampoPassword txtPassword;
    private BotonPrimario btnIngresar;
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

        // Contenedor principal con división horizontal (Split 42% Izquierda / 58% Derecha)
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBackground(Tema.COLOR_FONDO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Panel Izquierdo: Branding / Presentación
        gbc.gridx = 0;
        gbc.weightx = 0.42;
        panelPrincipal.add(crearPanelBranding(), gbc);

        // Panel Derecho: Formulario de Login Centrado
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

                // Círculos translúcidos decorativos de fondo
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
        lblSub.setForeground(new Color(224, 231, 255)); // Indigo claro
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

        // Pie izquierdo
        JLabel lblFooter = new JLabel("Universidad Tecnológica del Perú • 2026", SwingConstants.CENTER);
        lblFooter.setFont(Tema.FUENTE_PEQUENA);
        lblFooter.setForeground(new Color(197, 202, 233));
        panel.add(lblFooter, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelFormulario() {
        JPanel panelContenedor = new JPanel(new GridBagLayout());
        panelContenedor.setBackground(Tema.COLOR_FONDO);

        // Tarjeta centralizada
        TarjetaPanel tarjeta = new TarjetaPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setPreferredSize(new Dimension(400, 460));
        tarjeta.setMaximumSize(new Dimension(420, 480));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(30, 32, 30, 32));

        // Título de la tarjeta
        JLabel lblBienvenido = new JLabel("Iniciar Sesión");
        lblBienvenido.setFont(Tema.FUENTE_TITULO);
        lblBienvenido.setForeground(Tema.COLOR_TEXTO);
        lblBienvenido.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblInstruccion = new JLabel("Ingrese sus credenciales de usuario o DNI");
        lblInstruccion.setFont(Tema.FUENTE_PEQUENA);
        lblInstruccion.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        lblInstruccion.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Banner de error en línea (inicialmente oculto)
        panelErrorBanner = new JPanel(new BorderLayout(8, 0));
        panelErrorBanner.setBackground(Tema.COLOR_PELIGRO_CLARO);
        panelErrorBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_PELIGRO, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        panelErrorBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        panelErrorBanner.setVisible(false);

        lblMensajeError = new JLabel("Credenciales inválidas");
        lblMensajeError.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblMensajeError.setForeground(Tema.COLOR_PELIGRO);
        panelErrorBanner.add(lblMensajeError, BorderLayout.CENTER);

        // Campo Usuario
        JLabel lblUser = new JLabel("Usuario o DNI:");
        lblUser.setFont(Tema.FUENTE_SECCION);
        lblUser.setForeground(Tema.COLOR_TEXTO);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsuario = new CampoTexto("Ej. admin, biblio o DNI del cliente");
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Campo Contraseña
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(Tema.FUENTE_SECCION);
        lblPass.setForeground(Tema.COLOR_TEXTO);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new CampoPassword("Contraseña");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Botón Ingresar
        btnIngresar = new BotonPrimario("Ingresar al Sistema", Iconos.crearCheck(18, Color.WHITE));
        btnIngresar.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 15));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnIngresar.addActionListener(e -> {
            limpiarBannerError();
            if (controller != null) {
                controller.procesarLogin(txtUsuario.getText(), txtPassword.getPasswordString());
            }
        });

        // Ensamblado en la tarjeta
        tarjeta.add(lblBienvenido);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblInstruccion);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(panelErrorBanner);
        tarjeta.add(Box.createVerticalStrut(12));
        tarjeta.add(lblUser);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtUsuario);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(lblPass);
        tarjeta.add(Box.createVerticalStrut(6));
        tarjeta.add(txtPassword);
        tarjeta.add(Box.createVerticalStrut(24));
        tarjeta.add(btnIngresar);

        panelContenedor.add(tarjeta);
        return panelContenedor;
    }

    public void mostrarError(String mensaje) {
        lblMensajeError.setText(mensaje);
        panelErrorBanner.setVisible(true);
        txtUsuario.mostrarError();
        txtPassword.mostrarError();
        revalidate();
        repaint();
    }

    public void limpiarBannerError() {
        panelErrorBanner.setVisible(false);
        txtUsuario.limpiarError();
        txtPassword.limpiarError();
        revalidate();
        repaint();
    }

    public void mostrarMensaje(String mensaje) {
        DialogoModerno.mostrarMensaje(this, mensaje, "Información", DialogoModerno.TipoDialogo.EXITO);
    }

    public void limpiarClave() {
        txtPassword.setText("");
        txtPassword.requestFocusInWindow();
    }
}
