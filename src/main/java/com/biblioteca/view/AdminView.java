package com.biblioteca.view;

import com.biblioteca.controller.AdminController;
import com.biblioteca.model.Configuracion;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal para el rol ADMINISTRADOR.
 * Dispone de pestañas para Gestión de Bibliotecarios, Catálogo, Configuración Global y Reportes.
 */
public class AdminView extends JFrame {

    private AdminController controller;

    // Pestaña Bibliotecarios
    private JTable tablaBibliotecarios;
    private DefaultTableModel modeloBibliotecarios;
    private JTextField txtBiblioUsername;
    private JTextField txtBiblioNombre;
    private JPasswordField txtBiblioPassword;
    private JButton btnBiblioGuardar;
    private JButton btnBiblioToggleActivo;
    private JButton btnBiblioLimpiar;

    // Pestaña Catálogo
    private JTable tablaLibros;
    private DefaultTableModel modeloLibros;
    private JTable tablaEjemplares;
    private DefaultTableModel modeloEjemplares;
    private JTextField txtLibroIsbn;
    private JTextField txtLibroTitulo;
    private JTextField txtLibroAutor;
    private JTextField txtLibroCategoria;
    private JTextField txtLibroEditorial;
    private JSpinner spinLibroAnio;
    private JSpinner spinEjemplaresIniciales;
    private JButton btnLibroGuardar;
    private JButton btnLibroEliminar;
    private JButton btnLibroLimpiar;
    private JButton btnEjemplarCambiarEstado;

    // Pestaña Configuración
    private JTextField txtConfMultaPorDia;
    private JTextField txtConfDiasPrestamo;
    private JTextField txtConfMaxPrestamos;
    private JTextField txtConfDiasRetencion;
    private JButton btnConfGuardar;

    // Pestaña Reportes
    private JComboBox<String> comboTipoReporte;
    private JTable tablaReporte;
    private DefaultTableModel modeloReporte;
    private JButton btnExportarExcel;

    public AdminView() {
        initComponents();
    }

    public void setController(AdminController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Panel de Administración");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
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

        tabbedPane.addTab("Bibliotecarios", crearPanelBibliotecarios());
        tabbedPane.addTab("Catálogo", crearPanelCatalogo());
        tabbedPane.addTab("Configuración", crearPanelConfiguracion());
        tabbedPane.addTab("Reportes", crearPanelReportes());

        panelContenedor.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(panelContenedor);
    }

    private JPanel crearPanelBibliotecarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Formulario
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 8, 8));
        panelForm.setBorder(new TitledBorder("Datos del Bibliotecario"));

        panelForm.add(new JLabel("Nombre de Usuario:"));
        txtBiblioUsername = new JTextField();
        panelForm.add(txtBiblioUsername);

        panelForm.add(new JLabel("Nombre Completo:"));
        txtBiblioNombre = new JTextField();
        panelForm.add(txtBiblioNombre);

        panelForm.add(new JLabel("Contraseña inicial:"));
        txtBiblioPassword = new JPasswordField();
        panelForm.add(txtBiblioPassword);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnBiblioGuardar = new JButton("Registrar / Guardar");
        btnBiblioGuardar.setBackground(new Color(13, 110, 253));
        btnBiblioGuardar.setForeground(Color.WHITE);

        btnBiblioToggleActivo = new JButton("Activar / Desactivar");
        btnBiblioLimpiar = new JButton("Limpiar");

        panelAcciones.add(btnBiblioGuardar);
        panelAcciones.add(btnBiblioToggleActivo);
        panelAcciones.add(btnBiblioLimpiar);
        panelForm.add(new JLabel("Acciones:"));
        panelForm.add(panelAcciones);

        panel.add(panelForm, BorderLayout.NORTH);

        // Tabla
        modeloBibliotecarios = new DefaultTableModel(new String[]{"ID", "Usuario", "Nombre Completo", "Estado", "Intentos Fallidos"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaBibliotecarios = new JTable(modeloBibliotecarios);
        panel.add(new JScrollPane(tablaBibliotecarios), BorderLayout.CENTER);

        // Eventos
        btnBiblioGuardar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarBibliotecario(txtBiblioUsername.getText(), txtBiblioNombre.getText(), new String(txtBiblioPassword.getPassword()));
            }
        });

        btnBiblioToggleActivo.addActionListener(e -> {
            int row = tablaBibliotecarios.getSelectedRow();
            if (row >= 0 && controller != null) {
                int id = (int) modeloBibliotecarios.getValueAt(row, 0);
                controller.toggleActivoBibliotecario(id);
            } else {
                mostrarAdvertencia("Seleccione un bibliotecario de la tabla.");
            }
        });

        btnBiblioLimpiar.addActionListener(e -> limpiarFormBibliotecario());

        return panel;
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Formulario Libro
        JPanel panelForm = new JPanel(new GridLayout(4, 4, 8, 8));
        panelForm.setBorder(new TitledBorder("Registrar / Editar Libro"));

        panelForm.add(new JLabel("ISBN:"));
        txtLibroIsbn = new JTextField();
        panelForm.add(txtLibroIsbn);

        panelForm.add(new JLabel("Título:"));
        txtLibroTitulo = new JTextField();
        panelForm.add(txtLibroTitulo);

        panelForm.add(new JLabel("Autor:"));
        txtLibroAutor = new JTextField();
        panelForm.add(txtLibroAutor);

        panelForm.add(new JLabel("Categoría:"));
        txtLibroCategoria = new JTextField();
        panelForm.add(txtLibroCategoria);

        panelForm.add(new JLabel("Editorial:"));
        txtLibroEditorial = new JTextField();
        panelForm.add(txtLibroEditorial);

        panelForm.add(new JLabel("Año:"));
        spinLibroAnio = new JSpinner(new SpinnerNumberModel(2024, 1500, 2100, 1));
        panelForm.add(spinLibroAnio);

        panelForm.add(new JLabel("Ejemplares Iniciales:"));
        spinEjemplaresIniciales = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        panelForm.add(spinEjemplaresIniciales);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        btnLibroGuardar = new JButton("Guardar Libro");
        btnLibroGuardar.setBackground(new Color(13, 110, 253));
        btnLibroGuardar.setForeground(Color.WHITE);

        btnLibroEliminar = new JButton("Eliminar Libro");
        btnLibroEliminar.setBackground(new Color(220, 53, 69));
        btnLibroEliminar.setForeground(Color.WHITE);

        btnLibroLimpiar = new JButton("Limpiar");

        panelBtns.add(btnLibroGuardar);
        panelBtns.add(btnLibroEliminar);
        panelBtns.add(btnLibroLimpiar);
        panelForm.add(panelBtns);

        panel.add(panelForm, BorderLayout.NORTH);

        // Tablas Split (Libros arriba, Ejemplares abajo)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);

        modeloLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Año", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaLibros = new JTable(modeloLibros);
        splitPane.setTopComponent(new JScrollPane(tablaLibros));

        JPanel panelEjemplares = new JPanel(new BorderLayout(5, 5));
        panelEjemplares.setBorder(new TitledBorder("Ejemplares del Libro Seleccionado"));

        modeloEjemplares = new DefaultTableModel(new String[]{"ID", "Código de Barras", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEjemplares = new JTable(modeloEjemplares);
        panelEjemplares.add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);

        JPanel panelAccionesEjemplar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnEjemplarCambiarEstado = new JButton("Cambiar Estado (Disponible / Mantenimiento / Baja)");
        panelAccionesEjemplar.add(btnEjemplarCambiarEstado);
        panelEjemplares.add(panelAccionesEjemplar, BorderLayout.SOUTH);

        splitPane.setBottomComponent(panelEjemplares);
        panel.add(splitPane, BorderLayout.CENTER);

        // Eventos
        btnLibroGuardar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarLibro(txtLibroIsbn.getText(), txtLibroTitulo.getText(), txtLibroAutor.getText(),
                        txtLibroCategoria.getText(), txtLibroEditorial.getText(),
                        (int) spinLibroAnio.getValue(), (int) spinEjemplaresIniciales.getValue());
            }
        });

        btnLibroEliminar.addActionListener(e -> {
            int row = tablaLibros.getSelectedRow();
            if (row >= 0 && controller != null) {
                int id = (int) modeloLibros.getValueAt(row, 0);
                controller.eliminarLibro(id);
            } else {
                mostrarAdvertencia("Seleccione un libro para eliminar.");
            }
        });

        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaLibros.getSelectedRow();
                if (row >= 0 && controller != null) {
                    int id = (int) modeloLibros.getValueAt(row, 0);
                    controller.cargarEjemplaresDeLibro(id);
                }
            }
        });

        btnEjemplarCambiarEstado.addActionListener(e -> {
            int row = tablaEjemplares.getSelectedRow();
            if (row >= 0 && controller != null) {
                int ejemplarId = (int) modeloEjemplares.getValueAt(row, 0);
                controller.cambiarEstadoEjemplarDialog(ejemplarId);
            } else {
                mostrarAdvertencia("Seleccione un ejemplar de la tabla inferior.");
            }
        });

        btnLibroLimpiar.addActionListener(e -> limpiarFormLibro());

        return panel;
    }

    private JPanel crearPanelConfiguracion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Multa por día de retraso (S/.):"), gbc);
        gbc.gridx = 1;
        txtConfMultaPorDia = new JTextField(15);
        panel.add(txtConfMultaPorDia, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Días permitidos de préstamo:"), gbc);
        gbc.gridx = 1;
        txtConfDiasPrestamo = new JTextField(15);
        panel.add(txtConfDiasPrestamo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Máximo de préstamos simultáneos por cliente:"), gbc);
        gbc.gridx = 1;
        txtConfMaxPrestamos = new JTextField(15);
        panel.add(txtConfMaxPrestamos, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Días de retención para reserva de libro:"), gbc);
        gbc.gridx = 1;
        txtConfDiasRetencion = new JTextField(15);
        panel.add(txtConfDiasRetencion, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        btnConfGuardar = new JButton("Guardar Parámetros de Configuración");
        btnConfGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfGuardar.setBackground(new Color(13, 110, 253));
        btnConfGuardar.setForeground(Color.WHITE);
        panel.add(btnConfGuardar, gbc);

        btnConfGuardar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarConfiguracion(
                        txtConfMultaPorDia.getText(),
                        txtConfDiasPrestamo.getText(),
                        txtConfMaxPrestamos.getText(),
                        txtConfDiasRetencion.getText()
                );
            }
        });

        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelSuperior.add(new JLabel("Tipo de Reporte:"));

        comboTipoReporte = new JComboBox<>(new String[]{
                "Préstamos Activos y Vencidos",
                "Multas Pendientes y Pagadas",
                "Libros Más Prestados (Top 10)",
                "Inventario de Ejemplares por Estado"
        });
        panelSuperior.add(comboTipoReporte);

        JButton btnCargarReporte = new JButton("Cargar Datos");
        panelSuperior.add(btnCargarReporte);

        btnExportarExcel = new JButton("Exportar a Excel (.xlsx)");
        btnExportarExcel.setBackground(new Color(25, 135, 84));
        btnExportarExcel.setForeground(Color.WHITE);
        panelSuperior.add(btnExportarExcel);

        panel.add(panelSuperior, BorderLayout.NORTH);

        modeloReporte = new DefaultTableModel();
        tablaReporte = new JTable(modeloReporte);
        panel.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);

        btnCargarReporte.addActionListener(e -> {
            if (controller != null) {
                controller.cargarReporte(comboTipoReporte.getSelectedIndex());
            }
        });

        comboTipoReporte.addActionListener(e -> {
            if (controller != null) {
                controller.cargarReporte(comboTipoReporte.getSelectedIndex());
            }
        });

        btnExportarExcel.addActionListener(e -> {
            if (controller != null) {
                controller.exportarExcelReporte(comboTipoReporte.getSelectedIndex(), tablaReporte);
            }
        });

        return panel;
    }

    // Métodos para actualizar vistas desde el Controller
    public void cargarTablaBibliotecarios(List<Usuario> lista) {
        modeloBibliotecarios.setRowCount(0);
        for (Usuario u : lista) {
            modeloBibliotecarios.addRow(new Object[]{
                    u.getId(),
                    u.getUsername(),
                    u.getNombre(),
                    u.isActivo() ? "ACTIVO" : "INACTIVO",
                    u.getIntentosFallidos()
            });
        }
    }

    public void cargarTablaLibros(List<Libro> lista, java.util.function.Function<Integer, Integer> contarDisp) {
        modeloLibros.setRowCount(0);
        for (Libro l : lista) {
            int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
            modeloLibros.addRow(new Object[]{
                    l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), l.getAnio(), disp
            });
        }
    }

    public void cargarTablaEjemplares(List<Ejemplar> lista) {
        modeloEjemplares.setRowCount(0);
        for (Ejemplar ej : lista) {
            modeloEjemplares.addRow(new Object[]{
                    ej.getId(), ej.getCodigoBarras(), ej.getEstado()
            });
        }
    }

    public void setValoresConfiguracion(String multa, String diasP, String maxP, String retencion) {
        txtConfMultaPorDia.setText(multa);
        txtConfDiasPrestamo.setText(diasP);
        txtConfMaxPrestamos.setText(maxP);
        txtConfDiasRetencion.setText(retencion);
    }

    public void setReporteModel(DefaultTableModel model) {
        tablaReporte.setModel(model);
        this.modeloReporte = model;
    }

    public void limpiarFormBibliotecario() {
        txtBiblioUsername.setText("");
        txtBiblioNombre.setText("");
        txtBiblioPassword.setText("");
    }

    public void limpiarFormLibro() {
        txtLibroIsbn.setText("");
        txtLibroTitulo.setText("");
        txtLibroAutor.setText("");
        txtLibroCategoria.setText("");
        txtLibroEditorial.setText("");
        spinLibroAnio.setValue(2024);
        spinEjemplaresIniciales.setValue(3);
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

