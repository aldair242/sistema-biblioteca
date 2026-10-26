package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Tabla moderna con filas alternadas, chips visuales para estados y mensaje de estado vacío.
 */
public class TablaEstilizada extends JTable {

    private String mensajeVacio = "No hay registros disponibles para mostrar.";

    public TablaEstilizada() {
        this(new DefaultTableModel());
    }

    public TablaEstilizada(DefaultTableModel model) {
        super(model);
        configurarEstilo();
    }

    public void setMensajeVacio(String mensaje) {
        this.mensajeVacio = mensaje;
        repaint();
    }

    private void configurarEstilo() {
        setRowHeight(Tema.ALTO_FILA_TABLA);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFont(Tema.FUENTE_NORMAL);
        setForeground(Tema.COLOR_TEXTO);

        setSelectionBackground(Tema.COLOR_PRIMARIO_CLARO);
        setSelectionForeground(Color.decode("#1A237E"));

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setFont(Tema.FUENTE_PEQUENA_BOLD);
        getTableHeader().setBackground(new Color(241, 245, 249));
        getTableHeader().setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.COLOR_BORDE));

        // Renderers por defecto
        setDefaultRenderer(Object.class, new CeldaRenderer());
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);

        if (!isRowSelected(row)) {
            // Filas alternadas suaves
            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
        }

        return c;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Mostrar estado vacío amigable cuando no hay filas
        if (getRowCount() == 0 && mensajeVacio != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(Tema.FUENTE_NORMAL);
            g2.setColor(Tema.COLOR_TEXTO_SECUNDARIO);

            FontMetrics fm = g2.getFontMetrics();
            int strWidth = fm.stringWidth(mensajeVacio);
            int x = Math.max(20, (getWidth() - strWidth) / 2);
            int y = Math.max(50, getHeight() / 3);

            g2.drawString(mensajeVacio, x, y);
            g2.dispose();
        }
    }

    /**
     * Renderer de celdas con soporte de badges/chips para estados de negocio.
     */
    public static class CeldaRenderer extends DefaultTableCellRenderer {

        private final ChipPanel chipPanel = new ChipPanel();

        public CeldaRenderer() {
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String valStr = value != null ? value.toString() : "";

            // Detectar si el valor representa un estado para renderizarlo como chip
            if (esEstadoConocido(valStr)) {
                chipPanel.setEstado(valStr, isSelected);
                return chipPanel;
            }

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(Tema.FUENTE_NORMAL);

            // Alinear números o monedas a la derecha si aplica
            if (valStr.startsWith("S/") || valStr.matches("^-?\\d+$")) {
                setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
            }

            return this;
        }

        private boolean esEstadoConocido(String txt) {
            String u = txt.trim().toUpperCase();
            return u.equals("DISPONIBLE") || u.equals("PRESTADO") || u.equals("RESERVADO") ||
                    u.equals("EN_MANTENIMIENTO") || u.equals("BAJA") || u.equals("ACTIVO") ||
                    u.equals("DEVUELTO") || u.equals("VENCIDO") || u.equals("PENDIENTE") ||
                    u.equals("PAGADA") || u.equals("LISTA") || u.equals("CUMPLIDA") ||
                    u.equals("EXPIRADA") || u.equals("CANCELADA") || u.equals("INACTIVO");
        }
    }

    /**
     * Componente visual tipo chip redondeado para estados.
     */
    private static class ChipPanel extends JPanel {
        private String texto = "";
        private Color colorFondo = Color.LIGHT_GRAY;
        private Color colorTexto = Color.BLACK;

        public ChipPanel() {
            setOpaque(false);
            setLayout(new GridBagLayout());
        }

        public void setEstado(String estado, boolean seleccionado) {
            this.texto = estado.replace('_', ' ');

            String u = estado.trim().toUpperCase();
            switch (u) {
                case "DISPONIBLE", "PAGADA", "ACTIVO", "DEVUELTO", "CUMPLIDA" -> {
                    colorFondo = Tema.COLOR_ACENTO_CLARO;
                    colorTexto = Tema.COLOR_ACENTO;
                }
                case "PRESTADO", "PENDIENTE" -> {
                    colorFondo = Tema.COLOR_ADVERTENCIA_CLARO;
                    colorTexto = Tema.COLOR_ADVERTENCIA_OSCURO;
                }
                case "RESERVADO", "LISTA" -> {
                    colorFondo = Tema.COLOR_INFO_CLARO;
                    colorTexto = Tema.COLOR_INFO;
                }
                case "VENCIDO", "INACTIVO" -> {
                    colorFondo = Tema.COLOR_PELIGRO_CLARO;
                    colorTexto = Tema.COLOR_PELIGRO;
                }
                default -> { // BAJA, EN_MANTENIMIENTO, CANCELADA, EXPIRADA
                    colorFondo = new Color(241, 245, 249);
                    colorTexto = Color.decode("#475569");
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setFont(Tema.FUENTE_PEQUENA_BOLD);
            FontMetrics fm = g2.getFontMetrics();
            int strW = fm.stringWidth(texto);
            int strH = fm.getAscent();

            int chipW = strW + 18;
            int chipH = 22;
            int chipX = Math.max(6, (getWidth() - chipW) / 2);
            int chipY = (getHeight() - chipH) / 2;

            // Fondo del chip
            g2.setColor(colorFondo);
            g2.fillRoundRect(chipX, chipY, chipW, chipH, 12, 12);

            // Borde sutil del chip
            g2.setColor(new Color(colorTexto.getRed(), colorTexto.getGreen(), colorTexto.getBlue(), 40));
            g2.drawRoundRect(chipX, chipY, chipW, chipH, 12, 12);

            // Texto
            g2.setColor(colorTexto);
            g2.drawString(texto, chipX + 9, chipY + ((chipH - fm.getHeight()) / 2) + strH);

            g2.dispose();
        }
    }
}
