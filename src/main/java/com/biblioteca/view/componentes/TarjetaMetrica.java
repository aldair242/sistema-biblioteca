package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;

/**
 * Tarjeta de métrica para dashboards ejecutivos (KPIs).
 * Muestra un número grande, etiqueta, icono ilustrativo y color temático.
 */
public class TarjetaMetrica extends TarjetaPanel {

    private final JLabel lblValor;
    private final JLabel lblTitulo;
    private final JLabel lblIcono;

    public TarjetaMetrica(String titulo, String valorInicial, Icon icono, Color colorAcento) {
        super(new BorderLayout(15, 0));
        setPreferredSize(new Dimension(230, 105));
        setMinimumSize(new Dimension(200, 105));

        // Panel de Textos (Izquierda)
        JPanel panelTextos = new JPanel(new GridLayout(2, 1, 2, 4));
        panelTextos.setOpaque(false);

        lblTitulo = new JLabel(titulo.toUpperCase());
        lblTitulo.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblTitulo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        lblValor = new JLabel(valorInicial);
        lblValor.setFont(Tema.FUENTE_METRICA);
        lblValor.setForeground(colorAcento != null ? colorAcento : Tema.COLOR_PRIMARIO);

        panelTextos.add(lblTitulo);
        panelTextos.add(lblValor);
        add(panelTextos, BorderLayout.CENTER);

        // Panel de Ícono con fondo circular decorativo
        lblIcono = new JLabel(icono);
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcono.setPreferredSize(new Dimension(50, 50));

        JPanel panelIconoWrap = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fondoIcono = colorAcento != null ?
                        new Color(colorAcento.getRed(), colorAcento.getGreen(), colorAcento.getBlue(), 25) :
                        Tema.COLOR_PRIMARIO_CLARO;
                g2.setColor(fondoIcono);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panelIconoWrap.setOpaque(false);
        panelIconoWrap.setPreferredSize(new Dimension(50, 50));
        panelIconoWrap.add(lblIcono);

        add(panelIconoWrap, BorderLayout.EAST);
    }

    public void setValor(String valor) {
        this.lblValor.setText(valor);
    }

    public void setTitulo(String titulo) {
        this.lblTitulo.setText(titulo.toUpperCase());
    }
}

