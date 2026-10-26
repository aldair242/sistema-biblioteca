package com.biblioteca.view.componentes;

import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Componente gráfico interactivo de alta fidelidad basado en Java2D para la
 * visualización analítica de reportes de biblioteca (Préstamos, Multas, Top Libros, Inventario).
 */
public class PanelGraficaReporte extends JPanel {

    private String tituloGrafico = "Visualización Analítica";
    private String subtituloGrafico = "Seleccione un reporte para visualizar gráficos estadísticos";

    // Métricas para tarjetas superiores
    private final JPanel panelKpis;
    private final JLabel lblKpiTotalValor = new JLabel("0");
    private final JLabel lblKpiTotalDesc = new JLabel("Total Registros");
    private final JLabel lblKpi1Valor = new JLabel("0");
    private final JLabel lblKpi1Desc = new JLabel("Métrica Principal");
    private final JLabel lblKpi2Valor = new JLabel("0");
    private final JLabel lblKpi2Desc = new JLabel("Métrica Secundaria");

    // Elementos de datos para el gráfico
    private final List<ElementoDato> datos = new ArrayList<>();
    private boolean esRankingHorizontal = false;
    private int barraSeleccionadaIndex = -1;

    private final CanvasGrafico canvas;

    public PanelGraficaReporte() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Panel Superior: KPIs de Resumen
        panelKpis = new JPanel(new GridLayout(1, 3, 14, 0));
        panelKpis.setOpaque(false);
        panelKpis.setPreferredSize(new Dimension(Integer.MAX_VALUE, 74));

        panelKpis.add(crearTarjetaMiniKpi(lblKpiTotalValor, lblKpiTotalDesc, Tema.COLOR_PRIMARIO));
        panelKpis.add(crearTarjetaMiniKpi(lblKpi1Valor, lblKpi1Desc, Tema.COLOR_ACENTO));
        panelKpis.add(crearTarjetaMiniKpi(lblKpi2Valor, lblKpi2Desc, Tema.COLOR_PELIGRO));

        add(panelKpis, BorderLayout.NORTH);

        // 2. Canvas Java2D de Renderizado de Gráfica
        canvas = new CanvasGrafico();
        add(canvas, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaMiniKpi(JLabel lblValor, JLabel lblDesc, Color colorAcento) {
        JPanel card = new JPanel(new BorderLayout(8, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(Tema.COLOR_BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                // Banda lateral decorativa de color
                g2.setColor(colorAcento);
                g2.fillRoundRect(0, 0, 5, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 14));

        lblValor.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 20));
        lblValor.setForeground(Tema.COLOR_TEXTO);

        lblDesc.setFont(Tema.FUENTE_PEQUENA);
        lblDesc.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        card.add(lblValor, BorderLayout.CENTER);
        card.add(lblDesc, BorderLayout.SOUTH);

        return card;
    }

    /**
     * Procesa los datos del modelo tabular y actualiza las visualizaciones según el reporte.
     */
    public void actualizarDesdeModelo(int tipoReporte, DefaultTableModel model) {
        datos.clear();
        barraSeleccionadaIndex = -1;

        if (model == null || model.getRowCount() == 0) {
            tituloGrafico = "Sin Datos Disponibles";
            subtituloGrafico = "No hay registros cargados para este reporte.";
            lblKpiTotalValor.setText("0");
            lblKpi1Valor.setText("0");
            lblKpi2Valor.setText("0");
            esRankingHorizontal = false;
            canvas.repaint();
            return;
        }

        int totalFilas = model.getRowCount();

        switch (tipoReporte) {
            case 0 -> { // Préstamos Activos y Vencidos
                tituloGrafico = "Distribución de Préstamos Activos vs. Vencidos";
                subtituloGrafico = "Monitoreo del estado de préstamos regulares vs préstamos en mora";
                esRankingHorizontal = false;

                int activos = 0;
                int vencidos = 0;
                int devueltos = 0;

                for (int i = 0; i < totalFilas; i++) {
                    Object estadoObj = model.getValueAt(i, 5);
                    String estado = estadoObj != null ? estadoObj.toString().toUpperCase() : "";
                    if (estado.contains("ACTIVO") || estado.contains("RENOV")) {
                        activos++;
                    } else if (estado.contains("VENC")) {
                        vencidos++;
                    } else if (estado.contains("DEV")) {
                        devueltos++;
                    } else {
                        activos++;
                    }
                }

                datos.add(new ElementoDato("Activos al Día", activos, activos + " préstamos", Tema.COLOR_ACENTO));
                datos.add(new ElementoDato("En Mora (Vencidos)", vencidos, vencidos + " en mora", Tema.COLOR_PELIGRO));
                if (devueltos > 0) {
                    datos.add(new ElementoDato("Devueltos", devueltos, devueltos + " devueltos", Tema.COLOR_PRIMARIO));
                }

                lblKpiTotalDesc.setText("Total Préstamos");
                lblKpiTotalValor.setText(String.valueOf(totalFilas));

                int pctAlDia = totalFilas > 0 ? (activos * 100 / totalFilas) : 0;
                lblKpi1Desc.setText("Al Día (Cumplimiento)");
                lblKpi1Valor.setText(activos + " (" + pctAlDia + "%)");

                int pctMora = totalFilas > 0 ? (vencidos * 100 / totalFilas) : 0;
                lblKpi2Desc.setText("En Mora (Crítico)");
                lblKpi2Valor.setText(vencidos + " (" + pctMora + "%)");
            }
            case 1 -> { // Multas Pendientes y Pagadas
                tituloGrafico = "Comparativa de Multas Pendientes vs. Cobradas";
                subtituloGrafico = "Recaudación económica y montos pendientes de cobro";
                esRankingHorizontal = false;

                int countPendientes = 0;
                int countPagadas = 0;
                double sumPendientes = 0.0;
                double sumPagadas = 0.0;

                for (int i = 0; i < totalFilas; i++) {
                    Object estadoObj = model.getValueAt(i, 5);
                    String estado = estadoObj != null ? estadoObj.toString().toUpperCase() : "";

                    Object montoObj = model.getValueAt(i, 4);
                    double monto = 0.0;
                    if (montoObj != null) {
                        String s = montoObj.toString().replace("S/", "").replace(",", ".").trim();
                        try {
                            monto = Double.parseDouble(s);
                        } catch (Exception ignored) {
                        }
                    }

                    if (estado.contains("PAG")) {
                        countPagadas++;
                        sumPagadas += monto;
                    } else {
                        countPendientes++;
                        sumPendientes += monto;
                    }
                }

                DecimalFormat df = new DecimalFormat("0.00");
                datos.add(new ElementoDato("Multas Pagadas", sumPagadas, "S/ " + df.format(sumPagadas) + " (" + countPagadas + " reg.)", Tema.COLOR_ACENTO));
                datos.add(new ElementoDato("Multas Pendientes", sumPendientes, "S/ " + df.format(sumPendientes) + " (" + countPendientes + " reg.)", Tema.COLOR_ADVERTENCIA));

                lblKpiTotalDesc.setText("Total Multas Generadas");
                lblKpiTotalValor.setText(String.valueOf(totalFilas));

                lblKpi1Desc.setText("Total Recaudado (S/)");
                lblKpi1Valor.setText("S/ " + df.format(sumPagadas));

                lblKpi2Desc.setText("Por Cobrar en Mora");
                lblKpi2Valor.setText("S/ " + df.format(sumPendientes));
            }
            case 2 -> { // Libros Más Prestados (Top 10)
                tituloGrafico = "Ranking de Títulos con Mayor Demanda";
                subtituloGrafico = "Top libros bibliográficos ordenados por índice de circulación";
                esRankingHorizontal = true;

                int maxTop = Math.min(totalFilas, 10);
                Map<String, Integer> categoriasMap = new HashMap<>();

                for (int i = 0; i < maxTop; i++) {
                    String titulo = String.valueOf(model.getValueAt(i, 2));
                    String autor = String.valueOf(model.getValueAt(i, 3));
                    String categoria = String.valueOf(model.getValueAt(i, 4));

                    categoriasMap.put(categoria, categoriasMap.getOrDefault(categoria, 0) + 1);

                    // Puntuación decreciente proporcional al puesto en el Top
                    double valorRank = (maxTop - i) * 10.0;
                    Color colorBarra;
                    if (i == 0) colorBarra = Color.decode("#D4AF37"); // Oro
                    else if (i == 1) colorBarra = Color.decode("#9E9E9E"); // Plata
                    else if (i == 2) colorBarra = Color.decode("#CD7F32"); // Bronce
                    else colorBarra = Tema.COLOR_PRIMARIO;

                    datos.add(new ElementoDato("#" + (i + 1) + " " + titulo, valorRank, autor + " • " + categoria, colorBarra));
                }

                String topCategoria = categoriasMap.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("General");

                lblKpiTotalDesc.setText("Títulos en Ranking");
                lblKpiTotalValor.setText(maxTop + " libros");

                lblKpi1Desc.setText("Top #1 Más Solicitado");
                String libroTop = maxTop > 0 ? String.valueOf(model.getValueAt(0, 2)) : "N/A";
                if (libroTop.length() > 22) libroTop = libroTop.substring(0, 20) + "...";
                lblKpi1Valor.setText(libroTop);

                lblKpi2Desc.setText("Categoría Dominante");
                lblKpi2Valor.setText(topCategoria);
            }
            case 3 -> { // Inventario de Ejemplares por Estado
                tituloGrafico = "Estado Físico del Inventario de Ejemplares";
                subtituloGrafico = "Proporción de copias disponibles, en préstamo, mantenimiento y bajas";
                esRankingHorizontal = false;

                int disponibles = 0;
                int prestados = 0;
                int mantenimiento = 0;
                int bajas = 0;

                for (int i = 0; i < totalFilas; i++) {
                    Object estadoObj = model.getValueAt(i, 3);
                    String estado = estadoObj != null ? estadoObj.toString().toUpperCase() : "";

                    if (estado.contains("DISP")) disponibles++;
                    else if (estado.contains("PREST")) prestados++;
                    else if (estado.contains("MANT")) mantenimiento++;
                    else if (estado.contains("BAJA")) bajas++;
                    else disponibles++;
                }

                datos.add(new ElementoDato("Disponible", disponibles, disponibles + " copias", Tema.COLOR_ACENTO));
                datos.add(new ElementoDato("Prestado", prestados, prestados + " copias", Tema.COLOR_ADVERTENCIA));
                if (mantenimiento > 0 || totalFilas > 0) {
                    datos.add(new ElementoDato("Mantenimiento", mantenimiento, mantenimiento + " copias", Color.decode("#0288D1")));
                }
                if (bajas > 0) {
                    datos.add(new ElementoDato("Dado de Baja", bajas, bajas + " copias", Tema.COLOR_PELIGRO));
                }

                lblKpiTotalDesc.setText("Total Ejemplares");
                lblKpiTotalValor.setText(String.valueOf(totalFilas));

                int pctDisp = totalFilas > 0 ? (disponibles * 100 / totalFilas) : 0;
                lblKpi1Desc.setText("Disponibles para Préstamo");
                lblKpi1Valor.setText(disponibles + " (" + pctDisp + "%)");

                int pctPrest = totalFilas > 0 ? (prestados * 100 / totalFilas) : 0;
                lblKpi2Desc.setText("En Préstamo Activo");
                lblKpi2Valor.setText(prestados + " (" + pctPrest + "%)");
            }
        }

        canvas.repaint();
    }

    // --- CANVAS GRÁFICO INTERNO (JAVA2D) ---
    private class CanvasGrafico extends JComponent {

        public CanvasGrafico() {
            setOpaque(false);
            setToolTipText("");

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    actualizarHover(e.getX(), e.getY());
                }
            });
        }

        private void actualizarHover(int mx, int my) {
            int anterior = barraSeleccionadaIndex;
            barraSeleccionadaIndex = -1;

            if (datos.isEmpty()) return;

            int w = getWidth();
            int h = getHeight();

            if (esRankingHorizontal) {
                int paddingLeft = 180;
                int paddingTop = 60;
                int anchoBarra = w - paddingLeft - 60;
                int altoFila = Math.max(28, (h - paddingTop - 40) / Math.max(1, datos.size()));

                for (int i = 0; i < datos.size(); i++) {
                    int y = paddingTop + i * altoFila;
                    if (mx >= paddingLeft && mx <= paddingLeft + anchoBarra && my >= y && my <= y + altoFila - 6) {
                        barraSeleccionadaIndex = i;
                        setToolTipText(datos.get(i).etiqueta + ": " + datos.get(i).valorTexto);
                        break;
                    }
                }
            } else {
                int paddingLeft = 60;
                int paddingBottom = 70;
                int paddingTop = 60;
                int chartW = w - paddingLeft - 40;
                int chartH = h - paddingTop - paddingBottom;
                int n = datos.size();
                int barW = Math.min(100, Math.max(40, chartW / (n * 2)));
                int gap = (chartW - (n * barW)) / (n + 1);

                for (int i = 0; i < n; i++) {
                    int x = paddingLeft + gap + i * (barW + gap);
                    if (mx >= x && mx <= x + barW && my >= paddingTop && my <= paddingTop + chartH) {
                        barraSeleccionadaIndex = i;
                        setToolTipText(datos.get(i).etiqueta + ": " + datos.get(i).valorTexto);
                        break;
                    }
                }
            }

            if (barraSeleccionadaIndex != anterior) {
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Fondo del canvas gráfico
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g2.setColor(Tema.COLOR_BORDE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

            // Título y Subtítulo del gráfico
            g2.setColor(Tema.COLOR_TEXTO);
            g2.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 17));
            g2.drawString(tituloGrafico, 24, 28);

            g2.setColor(Tema.COLOR_TEXTO_SECUNDARIO);
            g2.setFont(Tema.FUENTE_PEQUENA);
            g2.drawString(subtituloGrafico, 24, 46);

            if (datos.isEmpty()) {
                g2.setColor(Tema.COLOR_TEXTO_SECUNDARIO);
                g2.setFont(Tema.FUENTE_NORMAL);
                String msg = "No hay datos para graficar. Presione 'Actualizar Datos'.";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
                g2.dispose();
                return;
            }

            if (esRankingHorizontal) {
                pintarRankingHorizontal(g2, w, h);
            } else {
                pintarBarrasVerticales(g2, w, h);
            }

            g2.dispose();
        }

        private void pintarBarrasVerticales(Graphics2D g2, int w, int h) {
            int paddingLeft = 70;
            int paddingRight = 40;
            int paddingTop = 75;
            int paddingBottom = 65;

            int chartW = w - paddingLeft - paddingRight;
            int chartH = h - paddingTop - paddingBottom;

            if (chartW <= 0 || chartH <= 0) return;

            // Encontrar valor máximo
            double maxVal = 1.0;
            for (ElementoDato d : datos) {
                if (d.valor > maxVal) maxVal = d.valor;
            }
            // Redondear maxVal hacia arriba para una escala limpia
            maxVal = Math.ceil(maxVal * 1.15);

            // Líneas de cuadrícula horizontales
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
            g2.setColor(new Color(230, 235, 242));
            int numLineas = 4;
            DecimalFormat dfEscala = new DecimalFormat("#,##0");

            for (int i = 0; i <= numLineas; i++) {
                int y = paddingTop + (int) (chartH * (1.0 - (double) i / numLineas));
                g2.drawLine(paddingLeft, y, paddingLeft + chartW, y);

                // Etiquetas del eje Y
                double valorGuia = (maxVal * i) / numLineas;
                g2.setColor(Tema.COLOR_TEXTO_SECUNDARIO);
                g2.setFont(Tema.FUENTE_PEQUENA);
                String txtGuia = dfEscala.format(valorGuia);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txtGuia, paddingLeft - fm.stringWidth(txtGuia) - 10, y + 4);
                g2.setColor(new Color(230, 235, 242));
            }
            g2.setStroke(new BasicStroke(1f));

            // Eje X base
            g2.setColor(Tema.COLOR_BORDE);
            g2.drawLine(paddingLeft, paddingTop + chartH, paddingLeft + chartW, paddingTop + chartH);

            int n = datos.size();
            int barW = Math.min(90, Math.max(38, chartW / (n * 2)));
            int gap = (chartW - (n * barW)) / (n + 1);

            for (int i = 0; i < n; i++) {
                ElementoDato d = datos.get(i);
                int x = paddingLeft + gap + i * (barW + gap);
                int barH = (int) ((d.valor / maxVal) * chartH);
                if (barH < 4 && d.valor > 0) barH = 4;
                int y = paddingTop + chartH - barH;

                boolean hover = (i == barraSeleccionadaIndex);

                // Pintar sombra de la barra
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(x + 2, y + 2, barW, barH, 8, 8);

                // Pintar gradiente de la barra
                Color cBase = d.color;
                Color cClaro = hover ? cBase.brighter() : new Color(
                        Math.min(255, cBase.getRed() + 25),
                        Math.min(255, cBase.getGreen() + 25),
                        Math.min(255, cBase.getBlue() + 25)
                );

                GradientPaint gp = new GradientPaint(x, y, cClaro, x, y + barH, cBase);
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                // Borde de barra si hover
                if (hover) {
                    g2.setColor(cBase.darker());
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(x, y, barW, barH, 8, 8);
                    g2.setStroke(new BasicStroke(1f));
                }

                // Badge / Píldora con valor sobre la barra
                g2.setFont(Tema.FUENTE_PEQUENA_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                String txtValor = d.valorTexto;
                int txtW = fm.stringWidth(txtValor);
                int badgeW = txtW + 12;
                int badgeH = 20;
                int badgeX = x + (barW - badgeW) / 2;
                int badgeY = Math.max(paddingTop - 15, y - badgeH - 6);

                g2.setColor(new Color(33, 33, 33, 210));
                g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 6, 6);
                g2.setColor(Color.WHITE);
                g2.drawString(txtValor, badgeX + 6, badgeY + 14);

                // Etiqueta al pie de la barra
                g2.setColor(Tema.COLOR_TEXTO);
                g2.setFont(Tema.FUENTE_PEQUENA_BOLD);
                FontMetrics fmEtiqueta = g2.getFontMetrics();
                int etiqW = fmEtiqueta.stringWidth(d.etiqueta);
                int etiqX = x + (barW - etiqW) / 2;
                g2.drawString(d.etiqueta, etiqX, paddingTop + chartH + 20);

                // Círculo de color indicativo al pie
                g2.setColor(d.color);
                g2.fillOval(etiqX - 12, paddingTop + chartH + 11, 8, 8);
            }
        }

        private void pintarRankingHorizontal(Graphics2D g2, int w, int h) {
            int paddingLeft = 210;
            int paddingRight = 80;
            int paddingTop = 75;
            int paddingBottom = 30;

            int chartW = w - paddingLeft - paddingRight;
            int n = datos.size();
            int altoFila = Math.max(26, (h - paddingTop - paddingBottom) / Math.max(1, n));
            int altoBarra = Math.min(22, altoFila - 8);

            double maxVal = 1.0;
            for (ElementoDato d : datos) {
                if (d.valor > maxVal) maxVal = d.valor;
            }

            for (int i = 0; i < n; i++) {
                ElementoDato d = datos.get(i);
                int y = paddingTop + i * altoFila;
                int barW = (int) ((d.valor / maxVal) * chartW);
                if (barW < 12) barW = 12;

                boolean hover = (i == barraSeleccionadaIndex);

                // Etiqueta del libro a la izquierda
                g2.setColor(Tema.COLOR_TEXTO);
                g2.setFont(Tema.FUENTE_PEQUENA_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                String etiquetaCorta = d.etiqueta;
                if (fm.stringWidth(etiquetaCorta) > paddingLeft - 25) {
                    while (etiquetaCorta.length() > 3 && fm.stringWidth(etiquetaCorta + "...") > paddingLeft - 25) {
                        etiquetaCorta = etiquetaCorta.substring(0, etiquetaCorta.length() - 1);
                    }
                    etiquetaCorta += "...";
                }
                g2.drawString(etiquetaCorta, paddingLeft - fm.stringWidth(etiquetaCorta) - 14, y + altoBarra - 4);

                // Barra horizontal de fondo suave
                g2.setColor(new Color(240, 243, 248));
                g2.fillRoundRect(paddingLeft, y, chartW, altoBarra, 6, 6);

                // Barra horizontal activa
                Color cBase = d.color;
                Color cClaro = hover ? cBase.brighter() : new Color(
                        Math.min(255, cBase.getRed() + 20),
                        Math.min(255, cBase.getGreen() + 20),
                        Math.min(255, cBase.getBlue() + 20)
                );

                GradientPaint gp = new GradientPaint(paddingLeft, y, cClaro, paddingLeft + barW, y, cBase);
                g2.setPaint(gp);
                g2.fillRoundRect(paddingLeft, y, barW, altoBarra, 6, 6);

                // Texto del detalle / ranking a la derecha
                g2.setColor(Tema.COLOR_TEXTO_SECUNDARIO);
                g2.setFont(Tema.FUENTE_PEQUENA);
                g2.drawString(d.valorTexto, paddingLeft + barW + 10, y + altoBarra - 5);
            }
        }
    }

    // Estructura de elemento de dato para gráficos
    private static class ElementoDato {
        String etiqueta;
        double valor;
        String valorTexto;
        Color color;

        ElementoDato(String etiqueta, double valor, String valorTexto, Color color) {
            this.etiqueta = etiqueta;
            this.valor = valor;
            this.valorTexto = valorTexto;
            this.color = color;
        }
    }
}
