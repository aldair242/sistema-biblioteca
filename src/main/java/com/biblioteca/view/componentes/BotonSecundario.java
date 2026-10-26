package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;
import java.awt.Color;
import javax.swing.Icon;

/**
 * Botón secundario con tono claro y texto primario.
 */
public class BotonSecundario extends BotonEstilizado {

    public BotonSecundario(String texto) {
        super(texto, Tema.COLOR_PRIMARIO_CLARO, Color.decode("#D7DAF4"), Color.decode("#C5CAE9"), Tema.COLOR_PRIMARIO_OSCURO);
    }

    public BotonSecundario(String texto, Icon icono) {
        this(texto);
        setIcon(icono);
    }
}

