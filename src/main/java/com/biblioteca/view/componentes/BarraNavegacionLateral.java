package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Barra de navegación lateral moderna con estética slate/indigo para navegación por CardLayout.
 */
public class BarraNavegacionLateral extends JPanel {

    private final JPanel panelItems;
    private final List<ItemNav> items = new ArrayList<>();
    private final Consumer<String> onSeleccion;
    private String claveSeleccionada;

    public BarraNavegacionLateral(String tituloRol, Consumer<String> onSeleccion) {
        this.onSeleccion = onSeleccion;
        setLayout(new BorderLayout());
        setBackground(Tema.COLOR_SIDEBAR);
        setPreferredSize(new Dimension(230, 0));
        setMinimumSize(new Dimension(210, 0));

        // Encabezado con Logo y Título
        JPanel panelHeader = new JPanel(new BorderLayout(12, 5));
        panelHeader.setOpaque(false);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));

        JLabel lblLogo = new JLabel(Iconos.crearLibro(28, Color.WHITE));
        panelHeader.add(lblLogo, BorderLayout.WEST);

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);

        JLabel lblApp = new JLabel("BIBLIOTECA CI");
        lblApp.setFont(Tema.FUENTE_SECCION);
        lblApp.setForeground(Color.WHITE);

        JLabel lblRol = new JLabel(tituloRol.toUpperCase());
        lblRol.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblRol.setForeground(new Color(148, 163, 184)); // Slate light

        panelTitulos.add(lblApp);
        panelTitulos.add(lblRol);
        panelHeader.add(panelTitulos, BorderLayout.CENTER);

        add(panelHeader, BorderLayout.NORTH);

        // Contenedor de Items de Menú
        panelItems = new JPanel();
        panelItems.setLayout(new BoxLayout(panelItems, BoxLayout.Y_AXIS));
        panelItems.setOpaque(false);
        panelItems.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(panelItems);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Pie de barra lateral (versión del sistema)
        JPanel panelFooter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFooter.setOpaque(false);
        panelFooter.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel lblVersion = new JLabel("v1.0.0 • UTP 2026");
        lblVersion.setFont(Tema.FUENTE_PEQUENA);
        lblVersion.setForeground(new Color(100, 116, 139));
        panelFooter.add(lblVersion);

        add(panelFooter, BorderLayout.SOUTH);
    }

    public void agregarItem(String clave, String texto, Icon icono) {
        ItemNav item = new ItemNav(clave, texto, icono);
        items.add(item);
        panelItems.add(item);
        panelItems.add(Box.createVerticalStrut(6));

        if (items.size() == 1) {
            seleccionar(clave);
        }
    }

    public void seleccionar(String clave) {
        this.claveSeleccionada = clave;
        for (ItemNav item : items) {
            item.actualizarEstado(item.clave.equals(clave));
        }
        if (onSeleccion != null) {
            onSeleccion.accept(clave);
        }
    }

    private class ItemNav extends JPanel {
        private final String clave;
        private final JLabel lblTexto;
        private final JLabel lblIcono;
        private boolean activo = false;
        private boolean hover = false;

        public ItemNav(String clave, String texto, Icon icono) {
            this.clave = clave;
            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(200, 42));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

            lblIcono = new JLabel(icono);
            add(lblIcono, BorderLayout.WEST);

            lblTexto = new JLabel(texto);
            lblTexto.setFont(Tema.FUENTE_NORMAL);
            lblTexto.setForeground(new Color(203, 213, 225));
            add(lblTexto, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!activo) {
                        hover = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    seleccionar(clave);
                }
            });
        }

        public void actualizarEstado(boolean esActivo) {
            this.activo = esActivo;
            this.hover = false;
            if (activo) {
                lblTexto.setForeground(Color.WHITE);
                lblTexto.setFont(Tema.FUENTE_NORMAL_BOLD);
            } else {
                lblTexto.setForeground(new Color(203, 213, 225));
                lblTexto.setFont(Tema.FUENTE_NORMAL);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (activo) {
                g2.setColor(Tema.COLOR_SIDEBAR_ACTIVO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hover) {
                g2.setColor(Tema.COLOR_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
