package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Campo de texto moderno con soporte de placeholder y validación visual de errores.
 */
public class CampoTexto extends JTextField {

    private final Border bordeNormal;
    private final Border bordeError;
    private boolean estadoError = false;

    public CampoTexto() {
        this("");
    }

    public CampoTexto(String placeholder) {
        this(placeholder, 0);
    }

    public CampoTexto(String placeholder, int columnas) {
        super(columnas);
        setFont(Tema.FUENTE_NORMAL);
        setForeground(Tema.COLOR_TEXTO);
        setCaretColor(Tema.COLOR_PRIMARIO);
        setPlaceholder(placeholder);

        bordeNormal = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );

        bordeError = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_PELIGRO, 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
        );

        setBorder(bordeNormal);
    }

    public void setPlaceholder(String texto) {
        putClientProperty("JTextField.placeholderText", texto);
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

    public boolean tieneError() {
        return estadoError;
    }
}
