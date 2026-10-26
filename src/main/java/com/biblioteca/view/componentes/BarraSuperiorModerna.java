package com.biblioteca.view.componentes;

import com.biblioteca.model.Usuario;
import com.biblioteca.security.SesionActual;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Barra superior moderna con título de sección, avatar de usuario con iniciales,
 * rol en chip estilizado y botón de cerrar sesión.
 */
public class BarraSuperiorModerna extends JPanel {

    private final JLabel lblTituloSeccion;
    private final JLabel lblSubtituloSeccion;

    public BarraSuperiorModerna(JFrame ventanaActual, Runnable accionCerrarSesion) {
        setLayout(new BorderLayout(15, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.COLOR_BORDE),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Título de la sección actual a la izquierda
        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);

        lblTituloSeccion = new JLabel("Panel Principal");
        lblTituloSeccion.setFont(Tema.FUENTE_TITULO);
        lblTituloSeccion.setForeground(Tema.COLOR_TEXTO);

        lblSubtituloSeccion = new JLabel("Bienvenido al sistema");
        lblSubtituloSeccion.setFont(Tema.FUENTE_PEQUENA);
        lblSubtituloSeccion.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        panelTitulos.add(lblTituloSeccion);
        panelTitulos.add(lblSubtituloSeccion);
        add(panelTitulos, BorderLayout.WEST);

        // Área de Usuario y Logout a la derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelDerecha.setOpaque(false);

        Usuario usuario = SesionActual.getUsuario();
        String nombre = usuario != null ? usuario.getNombre() : "Invitado";
        String rol = usuario != null ? usuario.getRol().name() : "INVITADO";
        String iniciales = obtenerIniciales(nombre);

        // Avatar circular con iniciales
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Tema.COLOR_PRIMARIO);
                g2.fill(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));

                g2.setFont(Tema.FUENTE_PEQUENA_BOLD);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(iniciales)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(iniciales, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(38, 38));
        panelDerecha.add(avatar);

        // Nombre y rol
        JPanel panelUsuario = new JPanel(new GridLayout(2, 1, 0, 2));
        panelUsuario.setOpaque(false);

        JLabel lblNombre = new JLabel(nombre);
        lblNombre.setFont(Tema.FUENTE_SECCION);
        lblNombre.setForeground(Tema.COLOR_TEXTO);

        JLabel lblRol = new JLabel(rol);
        lblRol.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblRol.setForeground(Tema.COLOR_PRIMARIO);

        panelUsuario.add(lblNombre);
        panelUsuario.add(lblRol);
        panelDerecha.add(panelUsuario);

        // Separador vertical sutil
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 28));
        sep.setForeground(Tema.COLOR_BORDE);
        panelDerecha.add(sep);

        // Botón Cerrar Sesión
        BotonSecundario btnLogout = new BotonSecundario("Salir", Iconos.crearCerrarSesion(16, Tema.COLOR_PELIGRO));
        btnLogout.setForeground(Tema.COLOR_PELIGRO);
        btnLogout.setBackground(Tema.COLOR_PELIGRO_CLARO);

        btnLogout.addActionListener(e -> {
            boolean confirm = DialogoModerno.mostrarConfirmacion(ventanaActual,
                    "¿Está seguro de que desea cerrar la sesión actual?",
                    "Cerrar Sesión");
            if (confirm && accionCerrarSesion != null) {
                accionCerrarSesion.run();
            }
        });

        panelDerecha.add(btnLogout);
        add(panelDerecha, BorderLayout.EAST);
    }

    public void setSeccion(String titulo, String subtitulo) {
        lblTituloSeccion.setText(titulo);
        lblSubtituloSeccion.setText(subtitulo);
    }

    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "US";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
        }
        return nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
    }
}

