package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;
import java.awt.Color;
import javax.swing.Icon;

/**
 * Botón rojo para acciones destructivas, cancelaciones o eliminaciones.
 */
public class BotonPeligro extends BotonEstilizado {

    public BotonPeligro(String texto) {
        super(texto, Tema.COLOR_PELIGRO, Tema.COLOR_PELIGRO_OSCURO, Color.decode("#7F0000"), Color.WHITE);
    }

    public BotonPeligro(String texto, Icon icono) {
        this(texto);
        setIcon(icono);
    }
}
