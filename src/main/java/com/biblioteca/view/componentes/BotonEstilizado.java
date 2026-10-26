package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Botón con estilo moderno, esquinas redondeadas y efecto hover.
 */
public class BotonEstilizado extends JButton {

    private final Color colorNormal;
    private final Color colorHover;
    private final Color colorPresionado;
    private boolean mouseEncima = false;

    public BotonEstilizado(String texto, Color normal, Color hover, Color presionado, Color textoColor) {
        super(texto);
        this.colorNormal = normal;
        this.colorHover = hover;
        this.colorPresionado = presionado;

        setFont(Tema.FUENTE_SECCION);
        setForeground(textoColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                mouseEncima = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseEncima = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!isEnabled()) {
            g2.setColor(new Color(203, 213, 225));
        } else if (getModel().isPressed()) {
            g2.setColor(colorPresionado);
        } else if (mouseEncima) {
            g2.setColor(colorHover);
        } else {
            g2.setColor(colorNormal);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Tema.ARCO_COMPONENTE, Tema.ARCO_COMPONENTE);
        g2.dispose();

        super.paintComponent(g);
    }
}

