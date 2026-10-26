package com.biblioteca.view;

import com.biblioteca.controller.ClienteController;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ventana principal para el rol CLIENTE (lector de la biblioteca).
 */
public class ClienteView extends JFrame {

    private ClienteController controller;

    // Catálogo
    private JTextField txtCatalogoBuscar;
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JButton btnReservarLibro;

    // Mis Préstamos
    private JTable tablaPrestamos;
    private DefaultTableModel modeloPrestamos;
    private JButton btnRenovarPrestamo;

    // Mis Reservas
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;
    private JButton btnCancelarReserva;

    // Mis Multas
    private JTable tablaMultas;
    private DefaultTableModel modeloMultas;
    private JLabel lblTotalMultas;

    // Cambiar Contraseña
    private JPasswordField txtClaveActual;
    private JPasswordField txtClaveNueva;
    private JPasswordField txtClaveConfirmar;
    private JButton btnGuardarClave;

    public ClienteView() {
        initComponents();
    }

    public void setController(ClienteController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Portal del Lector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        JPanel panelContenedor = new JPanel(new BorderLayout());

        // Barra Superior
        BarraSuperior barraSuperior = new BarraSuperior(this, () -> {
            if (controller != null) {
                controller.cerrarSesion();
            }
        });
        panelContenedor.add(barraSuperior, BorderLayout.NORTH);

        // Pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Catálogo", crearPanelCatalogo());
        tabbedPane.addTab("Mis Préstamos", crearPanelPrestamos());
        tabbedPane.addTab("Mis Reservas", crearPanelReservas());
        tabbedPane.addTab("Mis Multas", crearPanelMultas());
        tabbedPane.addTab("Cambiar Contraseña", crearPanelSeguridad());

        panelContenedor.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(panelContenedor);
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBuscar.add(new JLabel("Buscar por Título, Autor o ISBN:"));
        txtCatalogoBuscar = new JTextField(25);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Mostrar Todos");
        panelBuscar.add(txtCatalogoBuscar);
        panelBuscar.add(btnBuscar);
        panelBuscar.add(btnTodos);
        panel.add(panelBuscar, BorderLayout.NORTH);

        modeloCatalogo = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatalogo = new JTable(modeloCatalogo);
        panel.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnReservarLibro = new JButton("Reservar Libro (Si no hay ejemplares disponibles)");
        btnReservarLibro.setBackground(new Color(13, 110, 253));
        btnReservarLibro.setForeground(Color.WHITE);
        panelAcciones.add(btnReservarLibro);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> {
            if (controller != null) controller.buscarLibros(txtCatalogoBuscar.getText());
        });

        btnTodos.addActionListener(e -> {
            txtCatalogoBuscar.setText("");
            if (controller != null) controller.cargarCatalogo();
        });

        btnReservarLibro.addActionListener(e -> {
            int row = tablaCatalogo.getSelectedRow();
            if (row >= 0 && controller != null) {
                int libroId = (int) modeloCatalogo.getValueAt(row, 0);
                controller.solicitarReserva(libroId);
            } else {
                mostrarAdvertencia("Seleccione un libro del catálogo.");
            }
        });

        return panel;
    }

    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloPrestamos = new DefaultTableModel(new String[]{"ID Préstamo", "Ejemplar ID", "Fecha Préstamo", "Fecha Vencimiento", "Fecha Devolución", "Estado", "Renovaciones"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPrestamos = new JTable(modeloPrestamos);

        // Renderer para resaltar vencidos en ROJO
        tablaPrestamos.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String estado = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "";
                Object vencObj = table.getValueAt(row, 3);

                boolean vencido = "VENCIDO".equalsIgnoreCase(estado);
                if (vencObj instanceof LocalDate fv && "ACTIVO".equalsIgnoreCase(estado)) {
                    vencido = fv.isBefore(LocalDate.now());
                }

                if (vencido) {
                    c.setForeground(new Color(220, 53, 69));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });

        panel.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRenovarPrestamo = new JButton("Renovar Préstamo");
        btnRenovarPrestamo.setBackground(new Color(25, 135, 84));
        btnRenovarPrestamo.setForeground(Color.WHITE);
        panelAcciones.add(btnRenovarPrestamo);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        btnRenovarPrestamo.addActionListener(e -> {
            int row = tablaPrestamos.getSelectedRow();
            if (row >= 0 && controller != null) {
                int prestamoId = (int) modeloPrestamos.getValueAt(row, 0);
                controller.renovarPrestamo(prestamoId);
            } else {
                mostrarAdvertencia("Seleccione un préstamo de la tabla.");
            }
        });

        return panel;
    }

    private JPanel crearPanelReservas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloReservas = new DefaultTableModel(new String[]{"ID Reserva", "ID Libro", "Ejemplar Asignado", "Fecha Reserva", "Fecha Expiración", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelarReserva = new JButton("Cancelar Reserva");
        btnCancelarReserva.setBackground(new Color(220, 53, 69));
        btnCancelarReserva.setForeground(Color.WHITE);
        panelAcciones.add(btnCancelarReserva);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        btnCancelarReserva.addActionListener(e -> {
            int row = tablaReservas.getSelectedRow();
            if (row >= 0 && controller != null) {
                int id = (int) modeloReservas.getValueAt(row, 0);
                controller.cancelarReserva(id);
            } else {
                mostrarAdvertencia("Seleccione una reserva de la tabla.");
            }
        });

        return panel;
    }

    private JPanel crearPanelMultas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        modeloMultas = new DefaultTableModel(new String[]{"ID Multa", "ID Préstamo", "Días Retraso", "Tarifa", "Monto", "Estado", "Fecha Pago"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMultas = new JTable(modeloMultas);
        panel.add(new JScrollPane(tablaMultas), BorderLayout.CENTER);

        JPanel panelResumen = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        lblTotalMultas = new JLabel("Total Pendiente de Pago: S/ 0.00");
        lblTotalMultas.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalMultas.setForeground(new Color(220, 53, 69));
        panelResumen.add(lblTotalMultas);
        panel.add(panelResumen, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelSeguridad() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Contraseña Actual:"), gbc);
        gbc.gridx = 1;
        txtClaveActual = new JPasswordField(18);
        panel.add(txtClaveActual, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nueva Contraseña (mínimo 8 caracteres):"), gbc);
        gbc.gridx = 1;
        txtClaveNueva = new JPasswordField(18);
        panel.add(txtClaveNueva, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirmar Nueva Contraseña:"), gbc);
        gbc.gridx = 1;
        txtClaveConfirmar = new JPasswordField(18);
        panel.add(txtClaveConfirmar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        btnGuardarClave = new JButton("Actualizar Mi Contraseña");
        btnGuardarClave.setBackground(new Color(13, 110, 253));
        btnGuardarClave.setForeground(Color.WHITE);
        panel.add(btnGuardarClave, gbc);

        btnGuardarClave.addActionListener(e -> {
            if (controller != null) {
                controller.cambiarContrasena(
                        new String(txtClaveActual.getPassword()),
                        new String(txtClaveNueva.getPassword()),
                        new String(txtClaveConfirmar.getPassword())
                );
            }
        });

        return panel;
    }

    // Métodos para actualizar la vista
    public void cargarTablaCatalogo(List<Libro> libros, java.util.function.Function<Integer, Integer> contarDisp) {
        modeloCatalogo.setRowCount(0);
        for (Libro l : libros) {
            int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
            modeloCatalogo.addRow(new Object[]{
                    l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), disp
            });
        }
    }

    public void cargarTablaPrestamos(List<Prestamo> prestamos) {
        modeloPrestamos.setRowCount(0);
        for (Prestamo p : prestamos) {
            modeloPrestamos.addRow(new Object[]{
                    p.getId(), p.getEjemplarId(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                    p.getFechaDevolucion() != null ? p.getFechaDevolucion() : "-",
                    p.getEstado(), p.getRenovaciones()
            });
        }
    }

    public void cargarTablaReservas(List<Reserva> reservas) {
        modeloReservas.setRowCount(0);
        for (Reserva r : reservas) {
            modeloReservas.addRow(new Object[]{
                    r.getId(), r.getLibroId(), r.getEjemplarId() != null ? r.getEjemplarId() : "-",
                    r.getFechaReserva(), r.getFechaExpiracion() != null ? r.getFechaExpiracion() : "-", r.getEstado()
            });
        }
    }

    public void cargarTablaMultas(List<Multa> multas) {
        modeloMultas.setRowCount(0);
        BigDecimal totalPendiente = BigDecimal.ZERO;

        for (Multa m : multas) {
            modeloMultas.addRow(new Object[]{
                    m.getId(), m.getPrestamoId(), m.getDiasRetraso(), "S/ " + m.getTarifaAplicada(),
                    "S/ " + m.getMonto(), m.getEstado(), m.getFechaPago() != null ? m.getFechaPago() : "-"
            });
            if ("PENDIENTE".equalsIgnoreCase(m.getEstado().name())) {
                totalPendiente = totalPendiente.add(m.getMonto());
            }
        }
        lblTotalMultas.setText("Total Pendiente de Pago: S/ " + totalPendiente.toPlainString());
    }

    public void limpiarFormSeguridad() {
        txtClaveActual.setText("");
        txtClaveNueva.setText("");
        txtClaveConfirmar.setText("");
    }

    public void mostrarMensaje(String msj) {
        JOptionPane.showMessageDialog(this, msj, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarAdvertencia(String adv) {
        JOptionPane.showMessageDialog(this, adv, "Atención", JOptionPane.WARNING_MESSAGE);
    }
}

