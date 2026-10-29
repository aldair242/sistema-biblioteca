package com.biblioteca.view;

import com.biblioteca.controller.AuthController;
import com.biblioteca.model.ResultadoRecuperacion;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

/**
 * Diálogo modal para la recuperación de contraseñas mediante correo electrónico registrado.
 */
public class DialogoRecuperarClave extends JDialog {

    private final AuthController controller;
    private final CardLayout cardLayout;
    private final JPanel panelTarjetas;

    // Componentes del formulario
    private CampoTexto txtEmail;
    private JLabel lblError;
    private BotonPrimario btnEnviar;

    // Componentes de confirmación
    private JLabel lblDestinoEmail;
    private JLabel lblUsuarioDni;
    private JLabel lblClaveGenerada;
    private String claveTemporalActual = "";

    public DialogoRecuperarClave(Frame parent, AuthController controller) {
        super(parent, "Recuperación de Contraseña - Biblioteca CI", true);
        this.controller = controller;

        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);

        cardLayout = new CardLayout();
        panelTarjetas = new JPanel(cardLayout);
        panelTarjetas.setOpaque(false);

        panelTarjetas.add(crearPanelFormulario(), "FORM");
        panelTarjetas.add(crearPanelExito(), "EXITO");

        add(panelTarjetas, BorderLayout.CENTER);
        cardLayout.show(panelTarjetas, "FORM");

        // Cerrar con Escape
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // Cabecera
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pHeader.setOpaque(false);
        pHeader.add(new JLabel(Iconos.crearConfiguracion(26, Tema.COLOR_PRIMARIO)));

        JPanel pTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        pTitulos.setOpaque(false);
        JLabel lblTit = new JLabel("Recuperar Contraseña");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Ingrese el correo registrado con su cuenta de lector.");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        pTitulos.add(lblTit);
        pTitulos.add(lblSub);
        pHeader.add(pTitulos);
        panel.add(pHeader, BorderLayout.NORTH);

        // Cuerpo del formulario
        JPanel pCuerpo = new JPanel();
        pCuerpo.setLayout(new BoxLayout(pCuerpo, BoxLayout.Y_AXIS));
        pCuerpo.setOpaque(false);

        lblError = new JLabel("");
        lblError.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblError.setForeground(Tema.COLOR_PELIGRO);
        lblError.setVisible(false);
        pCuerpo.add(lblError);
        pCuerpo.add(Box.createVerticalStrut(10));

        JLabel lblEmail = new JLabel("Correo Electrónico Registrado:");
        lblEmail.setFont(Tema.FUENTE_SECCION);
        lblEmail.setForeground(Tema.COLOR_TEXTO);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pCuerpo.add(lblEmail);
        pCuerpo.add(Box.createVerticalStrut(6));

        txtEmail = new CampoTexto("ejemplo@correo.com");
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        pCuerpo.add(txtEmail);
        pCuerpo.add(Box.createVerticalStrut(6));

        JLabel lblAyuda = new JLabel("<html><span style='color:#757575; font-size:11px;'>" +
                "Se generará una clave temporal y se enviará a su correo para que pueda iniciar sesión y cambiarla." +
                "</span></html>");
        lblAyuda.setAlignmentX(Component.LEFT_ALIGNMENT);
        pCuerpo.add(lblAyuda);

        panel.add(pCuerpo, BorderLayout.CENTER);

        // Botones inferiores
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBotones.setOpaque(false);

        BotonSecundario btnCancelar = new BotonSecundario("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        btnEnviar = new BotonPrimario("Enviar Clave Temporal", Iconos.crearCheck(16, Color.WHITE));
        btnEnviar.addActionListener(e -> ejecutarRecuperacion());
        txtEmail.addActionListener(e -> ejecutarRecuperacion());

        pBotones.add(btnCancelar);
        pBotones.add(btnEnviar);
        panel.add(pBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelExito() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        // Cabecera de éxito
        JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pHeader.setOpaque(false);
        pHeader.add(new JLabel(Iconos.crearCheck(26, Tema.COLOR_ACENTO)));

        JPanel pTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        pTitulos.setOpaque(false);
        JLabel lblTit = new JLabel("¡Clave Temporal Generada!");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Se enviaron las credenciales de recuperación.");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        pTitulos.add(lblTit);
        pTitulos.add(lblSub);
        pHeader.add(pTitulos);
        panel.add(pHeader, BorderLayout.NORTH);

        // Tarjeta resumen de credenciales
        TarjetaPanel card = new TarjetaPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        lblDestinoEmail = new JLabel("Correo: -");
        lblDestinoEmail.setFont(Tema.FUENTE_NORMAL);
        lblDestinoEmail.setForeground(Tema.COLOR_TEXTO);

        lblUsuarioDni = new JLabel("Usuario (DNI): -");
        lblUsuarioDni.setFont(Tema.FUENTE_NORMAL);
        lblUsuarioDni.setForeground(Tema.COLOR_TEXTO);

        lblClaveGenerada = new JLabel("Clave Temporal: -");
        lblClaveGenerada.setFont(new Font("Consolas", Font.BOLD, 15));
        lblClaveGenerada.setForeground(Tema.COLOR_PRIMARIO);

        JLabel lblAviso = new JLabel("<html><div style='width:340px; color:#555; font-size:11px; margin-top:6px;'>" +
                "• Al iniciar sesión con esta clave temporal, el sistema le exigirá crear su nueva contraseña definitiva." +
                "</div></html>");

        card.add(lblDestinoEmail);
        card.add(Box.createVerticalStrut(6));
        card.add(lblUsuarioDni);
        card.add(Box.createVerticalStrut(6));
        card.add(lblClaveGenerada);
        card.add(Box.createVerticalStrut(4));
        card.add(lblAviso);

        panel.add(card, BorderLayout.CENTER);

        // Botones inferiores de confirmación
        JPanel pBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pBotones.setOpaque(false);

        BotonSecundario btnCopiar = new BotonSecundario("Copiar Clave");
        btnCopiar.addActionListener(e -> {
            if (!claveTemporalActual.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(claveTemporalActual), null);
                btnCopiar.setText("¡Copiada!");
            }
        });

        BotonPrimario btnIrLogin = new BotonPrimario("Ir a Iniciar Sesión", Iconos.crearCheck(16, Color.WHITE));
        btnIrLogin.addActionListener(e -> dispose());

        pBotones.add(btnCopiar);
        pBotones.add(btnIrLogin);
        panel.add(pBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void ejecutarRecuperacion() {
        lblError.setVisible(false);
        txtEmail.limpiarError();
        String email = txtEmail.getText();
        if (controller != null) {
            controller.procesarRecuperacionClave(email, this);
        }
    }

    public void mostrarError(String mensaje) {
        lblError.setText("<html>" + mensaje + "</html>");
        lblError.setVisible(true);
        txtEmail.mostrarError();
    }

    public void mostrarExito(ResultadoRecuperacion res) {
        this.claveTemporalActual = res.claveTemporal();
        lblDestinoEmail.setText("Correo: " + res.cliente().getEmail());
        lblUsuarioDni.setText("Usuario (DNI): " + res.usuario().getUsername());
        lblClaveGenerada.setText("Clave Temporal: " + res.claveTemporal());
        cardLayout.show(panelTarjetas, "EXITO");
    }
}

