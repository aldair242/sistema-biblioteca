package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Diálogo modal moderno para mensajes de éxito, advertencia, error y confirmación.
 * Reemplaza estéticamente los diálogos tradicionales de JOptionPane.
 */
public class DialogoModerno extends JDialog {

    public enum TipoDialogo { EXITO, ADVERTENCIA, ERROR, PREGUNTA }

    private boolean confirmado = false;

    private DialogoModerno(Window parent, String titulo, String mensaje, TipoDialogo tipo, boolean esConfirmacion) {
        super(parent, titulo, ModalityType.APPLICATION_MODAL);
        initComponents(mensaje, tipo, esConfirmacion);
    }

    private void initComponents(String mensaje, TipoDialogo tipo, boolean esConfirmacion) {
        setSize(460, 240);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel panelContenido = new JPanel(new BorderLayout(20, 15));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        // Ícono a la izquierda
        Color colorIcono;
        Icon icono;
        switch (tipo) {
            case EXITO -> {
                colorIcono = Tema.COLOR_ACENTO;
                icono = Iconos.crearCheck(36, colorIcono);
            }
            case ADVERTENCIA -> {
                colorIcono = Tema.COLOR_ADVERTENCIA_OSCURO;
                icono = Iconos.crearMulta(36, colorIcono);
            }
            case ERROR -> {
                colorIcono = Tema.COLOR_PELIGRO;
                icono = Iconos.crearEliminar(36, colorIcono);
            }
            default -> {
                colorIcono = Tema.COLOR_PRIMARIO;
                icono = Iconos.crearLibro(36, colorIcono);
            }
        }

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setVerticalAlignment(SwingConstants.TOP);
        panelContenido.add(lblIcono, BorderLayout.WEST);

        // Mensaje central
        JLabel lblMensaje = new JLabel("<html><body style='width: 300px; font-family: Segoe UI, sans-serif; font-size: 13px; color: #212121;'>" +
                mensaje + "</body></html>");
        panelContenido.add(lblMensaje, BorderLayout.CENTER);
        add(panelContenido, BorderLayout.CENTER);

        // Barra inferior de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        panelBotones.setBackground(new Color(248, 250, 252));
        panelBotones.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.COLOR_BORDE));

        if (esConfirmacion) {
            BotonSecundario btnCancelar = new BotonSecundario("Cancelar");
            btnCancelar.addActionListener(e -> {
                confirmado = false;
                dispose();
            });
            panelBotones.add(btnCancelar);

            BotonPrimario btnAceptar = new BotonPrimario("Confirmar");
            btnAceptar.addActionListener(e -> {
                confirmado = true;
                dispose();
            });
            panelBotones.add(btnAceptar);
            getRootPane().setDefaultButton(btnAceptar);
        } else {
            BotonPrimario btnOk = new BotonPrimario("Aceptar");
            btnOk.addActionListener(e -> dispose());
            panelBotones.add(btnOk);
            getRootPane().setDefaultButton(btnOk);
        }

        add(panelBotones, BorderLayout.SOUTH);

        // Atajo: Cerrar con tecla Escape
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public static void mostrarMensaje(Component padre, String mensaje, String titulo, TipoDialogo tipo) {
        Window window = SwingUtilities.getWindowAncestor(padre);
        DialogoModerno dlg = new DialogoModerno(window, titulo, mensaje, tipo, false);
        dlg.setVisible(true);
    }

    public static boolean mostrarConfirmacion(Component padre, String mensaje, String titulo) {
        Window window = SwingUtilities.getWindowAncestor(padre);
        DialogoModerno dlg = new DialogoModerno(window, titulo, mensaje, TipoDialogo.PREGUNTA, true);
        dlg.setVisible(true);
        return dlg.confirmado;
    }
}

