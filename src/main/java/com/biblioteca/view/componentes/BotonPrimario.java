package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;
import java.awt.Color;
import javax.swing.Icon;

/**
 * Botón principal con color azul índigo.
 */
public class BotonPrimario extends BotonEstilizado {

    public BotonPrimario(String texto) {
        super(texto, Tema.COLOR_PRIMARIO, Tema.COLOR_PRIMARIO_OSCURO, Color.decode("#1A237E"), Color.WHITE);
    }

    public BotonPrimario(String texto, Icon icono) {
        this(texto);
        setIcon(icono);
    }
}
