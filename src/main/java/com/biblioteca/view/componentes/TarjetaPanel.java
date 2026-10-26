package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Panel contenedor con apariencia de tarjeta moderna: fondo blanco,
 * esquinas redondeadas, borde sutil y sombra difusa.
 */
public class TarjetaPanel extends JPanel {

    private boolean conSombra = true;

    public TarjetaPanel() {
        this(new BorderLayout());
    }

    public TarjetaPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
    }

    public void setConSombra(boolean conSombra) {
        this.conSombra = conSombra;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (conSombra) {
            // Sombra suave difuminada
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(2, 4, w - 4, h - 4, Tema.ARCO_TARJETA + 2, Tema.ARCO_TARJETA + 2);
            g2.setColor(new Color(0, 0, 0, 6));
            g2.fillRoundRect(1, 2, w - 2, h - 2, Tema.ARCO_TARJETA + 1, Tema.ARCO_TARJETA + 1);
        }

        // Fondo de la tarjeta
        g2.setColor(Tema.COLOR_TARJETA);
        g2.fillRoundRect(0, 0, w - 1, h - 1, Tema.ARCO_TARJETA, Tema.ARCO_TARJETA);

        // Borde sutil
        g2.setColor(Tema.COLOR_BORDE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, Tema.ARCO_TARJETA, Tema.ARCO_TARJETA);

        g2.dispose();
        super.paintComponent(g);
    }
}
