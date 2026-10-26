package com.biblioteca.view;

import com.biblioteca.view.componentes.BarraSuperiorModerna;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * Componente superior común compatible con la versión anterior,
 * que delega el renderizado a la moderna BarraSuperiorModerna.
 */
public class BarraSuperior extends JPanel {

    private final BarraSuperiorModerna moderna;

    public BarraSuperior(JFrame ventanaActual, Runnable accionCerrarSesion) {
        setLayout(new BorderLayout());
        setOpaque(false);
        moderna = new BarraSuperiorModerna(ventanaActual, accionCerrarSesion);
        add(moderna, BorderLayout.CENTER);
    }

    public void setSeccion(String titulo, String subtitulo) {
        moderna.setSeccion(titulo, subtitulo);
    }
}
