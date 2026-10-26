package com.biblioteca.view.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Fábrica de íconos vectoriales de alta definición generados mediante Java2D.
 * Garantiza nitidez en cualquier escala y pantalla sin dependencias externas.
 */
public final class Iconos {

    private Iconos() {}

    public static Icon crearLibro(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Portada izquierda y derecha
            float pad = size * 0.15f;
            float mid = w / 2f;
            // Curva lomo libro
            Path2D p = new Path2D.Float();
            p.moveTo(pad, pad + 2);
            p.quadTo(mid * 0.7f, pad - 2, mid, pad + 3);
            p.quadTo(w - mid * 0.7f, pad - 2, w - pad, pad + 2);
            p.lineTo(w - pad, h - pad);
            p.quadTo(w - mid * 0.7f, h - pad - 4, mid, h - pad + 1);
            p.quadTo(mid * 0.7f, h - pad - 4, pad, h - pad);
            p.closePath();
            g2.draw(p);
            // Línea central (lomo)
            g2.draw(new Line2D.Float(mid, pad + 3, mid, h - pad + 1));
        });
    }

    public static Icon crearUsuario(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float mid = w / 2f;
            float r = size * 0.22f;
            // Cabeza
            g2.draw(new Ellipse2D.Float(mid - r, size * 0.12f, r * 2, r * 2));
            // Hombros / torso
            Arc2D cuerpo = new Arc2D.Float(size * 0.15f, size * 0.45f, size * 0.7f, size * 0.7f, 0, 180, Arc2D.OPEN);
            g2.draw(cuerpo);
        });
    }

    public static Icon crearPrestamo(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Cuaderno
            float pad = size * 0.2f;
            g2.draw(new RoundRectangle2D.Float(pad, pad, size * 0.5f, size * 0.6f, 4, 4));
            // Flecha saliente
            float ax = size * 0.55f;
            float ay = size * 0.5f;
            g2.draw(new Line2D.Float(ax, ay, size * 0.85f, ay));
            g2.draw(new Line2D.Float(size * 0.72f, ay - size * 0.12f, size * 0.85f, ay));
            g2.draw(new Line2D.Float(size * 0.72f, ay + size * 0.12f, size * 0.85f, ay));
        });
    }

    public static Icon crearDevolucion(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Flecha circular de retorno
            Arc2D arc = new Arc2D.Float(size * 0.18f, size * 0.18f, size * 0.64f, size * 0.64f, 40, 270, Arc2D.OPEN);
            g2.draw(arc);
            float ax = size * 0.18f + size * 0.15f;
            float ay = size * 0.22f;
            g2.draw(new Line2D.Float(ax - 3, ay - 4, ax - 3, ay + 4));
            g2.draw(new Line2D.Float(ax - 3, ay - 4, ax + 5, ay - 4));
        });
    }

    public static Icon crearMulta(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Círculo moneda
            g2.draw(new Ellipse2D.Float(size * 0.15f, size * 0.15f, size * 0.7f, size * 0.7f));
            // Signo S/ o $
            float midX = w / 2f;
            g2.draw(new Line2D.Float(midX, size * 0.28f, midX, size * 0.72f));
            Path2D s = new Path2D.Float();
            s.moveTo(midX + size * 0.12f, size * 0.38f);
            s.quadTo(midX, size * 0.32f, midX - size * 0.10f, size * 0.42f);
            s.quadTo(midX, size * 0.52f, midX + size * 0.10f, size * 0.58f);
            s.quadTo(midX, size * 0.68f, midX - size * 0.12f, size * 0.62f);
            g2.draw(s);
        });
    }

    public static Icon crearReserva(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Cinta marcapáginas o calendario
            Path2D p = new Path2D.Float();
            float pad = size * 0.25f;
            p.moveTo(pad, size * 0.15f);
            p.lineTo(w - pad, size * 0.15f);
            p.lineTo(w - pad, size * 0.85f);
            p.lineTo(w / 2f, size * 0.65f);
            p.lineTo(pad, size * 0.85f);
            p.closePath();
            g2.draw(p);
        });
    }

    public static Icon crearConfiguracion(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float mid = w / 2f;
            float r = size * 0.22f;
            g2.draw(new Ellipse2D.Float(mid - r, mid - r, r * 2, r * 2));
            // Dientes del engranaje
            int teeth = 6;
            for (int i = 0; i < teeth; i++) {
                double ang = i * (Math.PI / 3);
                float x1 = (float) (mid + Math.cos(ang) * (r + 1));
                float y1 = (float) (mid + Math.sin(ang) * (r + 1));
                float x2 = (float) (mid + Math.cos(ang) * (r + size * 0.14f));
                float y2 = (float) (mid + Math.sin(ang) * (r + size * 0.14f));
                g2.draw(new Line2D.Float(x1, y1, x2, y2));
            }
        });
    }

    public static Icon crearReportes(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Ejes
            g2.draw(new Line2D.Float(size * 0.18f, size * 0.15f, size * 0.18f, size * 0.85f));
            g2.draw(new Line2D.Float(size * 0.18f, size * 0.85f, size * 0.85f, size * 0.85f));
            // Barras
            g2.draw(new Line2D.Float(size * 0.35f, size * 0.85f, size * 0.35f, size * 0.50f));
            g2.draw(new Line2D.Float(size * 0.55f, size * 0.85f, size * 0.55f, size * 0.30f));
            g2.draw(new Line2D.Float(size * 0.75f, size * 0.85f, size * 0.75f, size * 0.40f));
        });
    }

    public static Icon crearDashboard(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float s = size * 0.28f;
            g2.draw(new RoundRectangle2D.Float(size * 0.15f, size * 0.15f, s, s, 3, 3));
            g2.draw(new RoundRectangle2D.Float(size * 0.55f, size * 0.15f, s, s, 3, 3));
            g2.draw(new RoundRectangle2D.Float(size * 0.15f, size * 0.55f, s, s, 3, 3));
            g2.draw(new RoundRectangle2D.Float(size * 0.55f, size * 0.55f, s, s, 3, 3));
        });
    }

    public static Icon crearCerrarSesion(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Puerta
            Path2D p = new Path2D.Float();
            p.moveTo(size * 0.55f, size * 0.15f);
            p.lineTo(size * 0.25f, size * 0.15f);
            p.lineTo(size * 0.25f, size * 0.85f);
            p.lineTo(size * 0.55f, size * 0.85f);
            g2.draw(p);
            // Flecha salida
            float my = h / 2f;
            g2.draw(new Line2D.Float(size * 0.45f, my, size * 0.85f, my));
            g2.draw(new Line2D.Float(size * 0.72f, my - size * 0.12f, size * 0.85f, my));
            g2.draw(new Line2D.Float(size * 0.72f, my + size * 0.12f, size * 0.85f, my));
        });
    }

    public static Icon crearBuscar(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float r = size * 0.26f;
            g2.draw(new Ellipse2D.Float(size * 0.18f, size * 0.18f, r * 2, r * 2));
            float x1 = size * 0.18f + r * 1.7f;
            float y1 = size * 0.18f + r * 1.7f;
            g2.draw(new Line2D.Float(x1, y1, size * 0.82f, size * 0.82f));
        });
    }

    public static Icon crearOjo(int size, Color color, boolean abierto) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float midY = h / 2f;
            // Forma de ojo
            Path2D o = new Path2D.Float();
            o.moveTo(size * 0.12f, midY);
            o.quadTo(w / 2f, size * 0.15f, size * 0.88f, midY);
            o.quadTo(w / 2f, size * 0.85f, size * 0.12f, midY);
            g2.draw(o);
            if (abierto) {
                float r = size * 0.14f;
                g2.fill(new Ellipse2D.Float(w / 2f - r, midY - r, r * 2, r * 2));
            } else {
                // Barra diagonal (ojo tachado)
                g2.draw(new Line2D.Float(size * 0.2f, size * 0.8f, size * 0.8f, size * 0.2f));
            }
        });
    }

    public static Icon crearMas(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.8f, size / 8f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(w / 2f, size * 0.2f, w / 2f, size * 0.8f));
            g2.draw(new Line2D.Float(size * 0.2f, h / 2f, size * 0.8f, h / 2f));
        });
    }

    public static Icon crearCheck(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(2f, size / 7f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D p = new Path2D.Float();
            p.moveTo(size * 0.22f, h * 0.52f);
            p.lineTo(size * 0.44f, h * 0.72f);
            p.lineTo(size * 0.78f, h * 0.28f);
            g2.draw(p);
        });
    }

    public static Icon crearEliminar(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Tapa
            g2.draw(new Line2D.Float(size * 0.2f, size * 0.28f, size * 0.8f, size * 0.28f));
            g2.draw(new Line2D.Float(size * 0.4f, size * 0.28f, size * 0.4f, size * 0.2f));
            g2.draw(new Line2D.Float(size * 0.4f, size * 0.2f, size * 0.6f, size * 0.2f));
            g2.draw(new Line2D.Float(size * 0.6f, size * 0.2f, size * 0.6f, size * 0.28f));
            // Bote
            Path2D b = new Path2D.Float();
            b.moveTo(size * 0.26f, size * 0.28f);
            b.lineTo(size * 0.32f, size * 0.85f);
            b.lineTo(size * 0.68f, size * 0.85f);
            b.lineTo(size * 0.74f, size * 0.28f);
            g2.draw(b);
        });
    }

    public static Icon crearAlerta(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D.Float t = new Path2D.Float();
            t.moveTo(size * 0.50f, size * 0.12f);
            t.lineTo(size * 0.88f, size * 0.85f);
            t.lineTo(size * 0.12f, size * 0.85f);
            t.closePath();
            g2.draw(t);
            g2.drawLine((int)(size * 0.50f), (int)(size * 0.38f), (int)(size * 0.50f), (int)(size * 0.58f));
            g2.fillOval((int)(size * 0.46f), (int)(size * 0.68f), (int)Math.max(2, size * 0.08f), (int)Math.max(2, size * 0.08f));
        });
    }

    public static Icon crearActualizar(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(Math.max(1.5f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Arc2D a1 = new Arc2D.Float(size * 0.18f, size * 0.18f, size * 0.64f, size * 0.64f, 20, 140, Arc2D.OPEN);
            Arc2D a2 = new Arc2D.Float(size * 0.18f, size * 0.18f, size * 0.64f, size * 0.64f, 200, 140, Arc2D.OPEN);
            g2.draw(a1);
            g2.draw(a2);
        });
    }

    /**
     * Genera la imagen para el icono de la ventana principal (Application Icon).
     */
    public static Image crearAppLogoImage(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo redondeado color primario
        g2.setColor(Tema.COLOR_PRIMARIO);
        g2.fill(new RoundRectangle2D.Float(0, 0, size, size, size * 0.25f, size * 0.25f));

        // Libro blanco en el centro
        Icon iconLibro = crearLibro((int)(size * 0.65f), Color.WHITE);
        int offset = (int)(size * 0.175f);
        iconLibro.paintIcon(null, g2, offset, offset);

        g2.dispose();
        return img;
    }

    // Clase auxiliar para pintar vectores en memoria
    private static class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final VectorDrawer drawer;

        public VectorIcon(int width, int height, VectorDrawer drawer) {
            this.width = width;
            this.height = height;
            this.drawer = drawer;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            drawer.draw(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }

    @FunctionalInterface
    private interface VectorDrawer {
        void draw(Graphics2D g2, int width, int height);
    }
}

