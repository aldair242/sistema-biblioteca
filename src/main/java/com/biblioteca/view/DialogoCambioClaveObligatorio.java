package com.biblioteca.view;

import com.biblioteca.exception.AutenticacionException;
import com.biblioteca.exception.ValidacionException;
import com.biblioteca.security.SesionActual;
import com.biblioteca.security.Validador;
import com.biblioteca.service.AuthService;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Diálogo modal moderno para cambio obligatorio de contraseña en el primer inicio de sesión.
 */
public class DialogoCambioClaveObligatorio extends JDialog {

    private final AuthService authService;
    private CampoPassword txtActual;
    private CampoPassword txtNueva;
    private CampoPassword txtConfirmar;
    private JLabel lblError;
    private boolean claveCambiadaExitosamente = false;

    public DialogoCambioClaveObligatorio(Frame parent, AuthService authService) {
        super(parent, "Actualización Requerida de Contraseña", true);
        this.authService = authService;
        initComponents();
    }

    private void initComponents() {
        setSize(480, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 20, 30));

        // Cabecera con ícono
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelHeader.setOpaque(false);
        panelHeader.add(new JLabel(Iconos.crearConfiguracion(28, Tema.COLOR_PRIMARIO)));

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);
        JLabel lblTitulo = new JLabel("Cambio Obligatorio de Clave");
        lblTitulo.setFont(Tema.FUENTE_SUBTITULO);
        lblTitulo.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Debe actualizar su contraseña temporal para continuar.");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSub);
        panelHeader.add(panelTitulos);
        panel.add(panelHeader);

        panel.add(Box.createVerticalStrut(18));

        // Banner de error si hay fallo
        lblError = new JLabel("");
        lblError.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblError.setForeground(Tema.COLOR_PELIGRO);
        lblError.setVisible(false);
        panel.add(lblError);

        panel.add(Box.createVerticalStrut(10));

        // Campos
        panel.add(crearLabel("Contraseña actual (temporal):"));
        txtActual = new CampoPassword("Ingrese clave temporal");
        txtActual.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(txtActual);

        panel.add(Box.createVerticalStrut(12));
        panel.add(crearLabel("Nueva contraseña (mínimo 8 caracteres, mayúscula y número):"));
        txtNueva = new CampoPassword("Nueva contraseña segura");
        txtNueva.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(txtNueva);

        panel.add(Box.createVerticalStrut(12));
        panel.add(crearLabel("Confirmar nueva contraseña:"));
        txtConfirmar = new CampoPassword("Repita la nueva contraseña");
        txtConfirmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(txtConfirmar);

        add(panel, BorderLayout.CENTER);

        // Barra inferior de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panelBotones.setBackground(new Color(248, 250, 252));
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.COLOR_BORDE));

        BotonSecundario btnCancelar = new BotonSecundario("Cancelar y Salir");
        BotonAcento btnGuardar = new BotonAcento("Actualizar Contraseña", Iconos.crearCheck(16, Color.WHITE));

        btnCancelar.addActionListener(e -> {
            authService.logout();
            claveCambiadaExitosamente = false;
            dispose();
        });

        btnGuardar.addActionListener(e -> procesarCambio());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        add(panelBotones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(e -> {
            authService.logout();
            claveCambiadaExitosamente = false;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FUENTE_SECCION);
        l.setForeground(Tema.COLOR_TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void procesarCambio() {
        String actual = txtActual.getPasswordString();
        String nueva = txtNueva.getPasswordString();
        String confirmar = txtConfirmar.getPasswordString();

        if (actual.isEmpty() || nueva.isEmpty() || confirmar.isEmpty()) {
            mostrarFallo("Debe completar todos los campos requeridos.");
            return;
        }

        if (!nueva.equals(confirmar)) {
            mostrarFallo("La confirmación no coincide con la nueva contraseña.");
            return;
        }

        try {
            Validador.validarPasswordFuerte(nueva);
            int usuarioId = SesionActual.getUsuario().getId();
            authService.cambiarClave(usuarioId, actual, nueva);

            DialogoModerno.mostrarMensaje(this, "Su contraseña ha sido actualizada con éxito.", "Contraseña Actualizada", DialogoModerno.TipoDialogo.EXITO);
            claveCambiadaExitosamente = true;
            dispose();

        } catch (ValidacionException | AutenticacionException ex) {
            mostrarFallo(ex.getMessage());
        }
    }

    private void mostrarFallo(String msj) {
        lblError.setText("• " + msj);
        lblError.setVisible(true);
        txtNueva.mostrarError();
        txtConfirmar.mostrarError();
        revalidate();
        repaint();
    }

    public boolean isClaveCambiadaExitosamente() {
        return claveCambiadaExitosamente;
    }
}
