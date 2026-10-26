package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;
import java.awt.Color;
import javax.swing.Icon;

/**
 * Botón de acento verde para acciones positivas y guardado.
 */
public class BotonAcento extends BotonEstilizado {

    public BotonAcento(String texto) {
        super(texto, Tema.COLOR_ACENTO, Tema.COLOR_ACENTO_OSCURO, Color.decode("#0D47A1"), Color.WHITE);
    }

    public BotonAcento(String texto, Icon icono) {
        this(texto);
        setIcon(icono);
    }
}
