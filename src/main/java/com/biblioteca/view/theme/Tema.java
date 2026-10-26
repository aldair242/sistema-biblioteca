package com.biblioteca.view.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Sistema de diseño global para la aplicación de Biblioteca.
 * Centraliza la paleta de colores, tipografías, dimensiones y configuración de FlatLaf.
 */
public final class Tema {

    private Tema() {}

    // Paleta de Colores
    public static final Color COLOR_PRIMARIO = Color.decode("#3F51B5");        // Azul Índigo
    public static final Color COLOR_PRIMARIO_OSCURO = Color.decode("#303F9F");
    public static final Color COLOR_PRIMARIO_CLARO = Color.decode("#E8EAF6");

    public static final Color COLOR_ACENTO = Color.decode("#2E7D32");          // Verde éxito / acciones positivas
    public static final Color COLOR_ACENTO_OSCURO = Color.decode("#1B5E20");
    public static final Color COLOR_ACENTO_CLARO = Color.decode("#E8F5E9");

    public static final Color COLOR_PELIGRO = Color.decode("#C62828");         // Rojo peligro / vencidos
    public static final Color COLOR_PELIGRO_OSCURO = Color.decode("#B71C1C");
    public static final Color COLOR_PELIGRO_CLARO = Color.decode("#FFEBEE");

    public static final Color COLOR_ADVERTENCIA = Color.decode("#F9A825");     // Ámbar / pendiente
    public static final Color COLOR_ADVERTENCIA_OSCURO = Color.decode("#F57F17");
    public static final Color COLOR_ADVERTENCIA_CLARO = Color.decode("#FFFDE7");

    public static final Color COLOR_INFO = Color.decode("#0288D1");            // Azul cielo / reservas
    public static final Color COLOR_INFO_CLARO = Color.decode("#E1F5FE");

    public static final Color COLOR_FONDO = Color.decode("#F5F7FA");           // Fondo general moderno
    public static final Color COLOR_TARJETA = Color.WHITE;                     // Superficie de tarjetas
    public static final Color COLOR_BORDE = Color.decode("#E2E8F0");           // Borde sutil
    public static final Color COLOR_TEXTO = Color.decode("#212121");           // Texto principal
    public static final Color COLOR_TEXTO_SECUNDARIO = Color.decode("#64748B"); // Texto secundario / gris

    // Barra lateral moderna
    public static final Color COLOR_SIDEBAR = Color.decode("#1E293B");         // Slate muy oscuro
    public static final Color COLOR_SIDEBAR_HOVER = Color.decode("#334155");
    public static final Color COLOR_SIDEBAR_ACTIVO = Color.decode("#3F51B5");

    // Tipografías
    public static final String NOMBRE_FUENTE = obtenerFuenteDisponible();
    public static final Font FUENTE_TITULO = new Font(NOMBRE_FUENTE, Font.BOLD, 22);
    public static final Font FUENTE_SUBTITULO = new Font(NOMBRE_FUENTE, Font.BOLD, 16);
    public static final Font FUENTE_SECCION = new Font(NOMBRE_FUENTE, Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font(NOMBRE_FUENTE, Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL_BOLD = new Font(NOMBRE_FUENTE, Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA = new Font(NOMBRE_FUENTE, Font.PLAIN, 12);
    public static final Font FUENTE_PEQUENA_BOLD = new Font(NOMBRE_FUENTE, Font.BOLD, 12);
    public static final Font FUENTE_METRICA = new Font(NOMBRE_FUENTE, Font.BOLD, 28);

    // Dimensiones
    public static final int ARCO_COMPONENTE = 12;
    public static final int ARCO_TARJETA = 14;
    public static final int ALTO_FILA_TABLA = 36;

    private static String obtenerFuenteDisponible() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fuentes = ge.getAvailableFontFamilyNames();
        for (String f : fuentes) {
            if ("Inter".equalsIgnoreCase(f)) return "Inter";
        }
        for (String f : fuentes) {
            if ("Segoe UI".equalsIgnoreCase(f)) return "Segoe UI";
        }
        return Font.SANS_SERIF;
    }

    /**
     * Aplica la configuración visual completa del sistema en FlatLaf y UIManager.
     */
    public static void aplicar() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            // Continuar con LookAndFeel activo
        }

        // Esquinas redondeadas
        UIManager.put("Component.arc", ARCO_COMPONENTE);
        UIManager.put("Button.arc", ARCO_COMPONENTE);
        UIManager.put("TextComponent.arc", ARCO_COMPONENTE);
        UIManager.put("ProgressBar.arc", ARCO_COMPONENTE);

        // Barra de desplazamiento delgada y moderna
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        // Tablas
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.rowHeight", ALTO_FILA_TABLA);
        UIManager.put("Table.gridColor", new Color(241, 245, 249));
        UIManager.put("Table.selectionBackground", COLOR_PRIMARIO_CLARO);
        UIManager.put("Table.selectionForeground", COLOR_TEXTO);
        UIManager.put("TableHeader.background", new Color(241, 245, 249));
        UIManager.put("TableHeader.foreground", COLOR_TEXTO);
        UIManager.put("TableHeader.separatorColor", new Color(226, 232, 240));
        UIManager.put("TableHeader.font", FUENTE_PEQUENA_BOLD);
        UIManager.put("TableHeader.cellBorder", BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Paneles y Tabs
        UIManager.put("Panel.background", COLOR_FONDO);
        UIManager.put("TabbedPane.tabHeight", 40);
        UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        UIManager.put("TabbedPane.showTabSeparators", false);
    }

    public static Border crearBordeVacio(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }
}

