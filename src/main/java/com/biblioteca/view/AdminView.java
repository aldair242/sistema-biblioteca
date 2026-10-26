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
    private Integer biblioSeleccionadoId = null;
    private JLabel lblBiblioModo;
    private JTable tablaBibliotecarios;
    private DefaultTableModel modeloBibliotecarios;
    private JTextField txtBiblioUsername;
    private JTextField txtBiblioNombre;
    private JPasswordField txtBiblioPassword;
    private JButton btnBiblioRegistrar;
    private JButton btnBiblioActualizar;
    private JButton btnBiblioEliminar;
    private JButton btnBiblioRestablecerClave;
    private JButton btnBiblioToggleActivo;
    private JButton btnBiblioLimpiar;

    // Pestaña Catálogo
    private Integer libroSeleccionadoId = null;
    private JLabel lblLibroModo;
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
    private JButton btnLibroRegistrar;
    private JButton btnLibroActualizar;
    private JButton btnLibroEliminar;
    private JButton btnLibroLimpiar;
    private JButton btnLibroAgregarEjemplar;
    private JButton btnEjemplarCambiarEstado;
    private JButton btnEjemplarEliminar;

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
        setSize(1080, 740);
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
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Formulario
        JPanel panelFormContenedor = new JPanel(new BorderLayout(6, 6));
        panelFormContenedor.setBorder(new TitledBorder("Gestión del Personal Bibliotecario"));

        lblBiblioModo = new JLabel("Modo: Nuevo Bibliotecario (Seleccione una fila de la tabla para editar o eliminar)");
        lblBiblioModo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBiblioModo.setForeground(new Color(108, 117, 125));
        lblBiblioModo.setBorder(new EmptyBorder(2, 6, 4, 6));
        panelFormContenedor.add(lblBiblioModo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 10, 8));
        panelCampos.setBorder(new EmptyBorder(4, 6, 6, 6));

        panelCampos.add(new JLabel("Nombre de Usuario:"));
        txtBiblioUsername = new JTextField();
        panelCampos.add(txtBiblioUsername);

        panelCampos.add(new JLabel("Nombre Completo:"));
        txtBiblioNombre = new JTextField();
        panelCampos.add(txtBiblioNombre);

        panelCampos.add(new JLabel("Contraseña (opcional al editar):"));
        txtBiblioPassword = new JPasswordField();
        panelCampos.add(txtBiblioPassword);

        panelCampos.add(new JLabel(""));
        panelCampos.add(new JLabel(""));

        panelFormContenedor.add(panelCampos, BorderLayout.CENTER);

        // Botones de acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btnBiblioRegistrar = new JButton("Registrar Nuevo");
        btnBiblioRegistrar.setBackground(new Color(13, 110, 253));
        btnBiblioRegistrar.setForeground(Color.WHITE);
        btnBiblioRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnBiblioActualizar = new JButton("Guardar Cambios");
        btnBiblioActualizar.setBackground(new Color(25, 135, 84));
        btnBiblioActualizar.setForeground(Color.WHITE);
        btnBiblioActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnBiblioEliminar = new JButton("Eliminar Bibliotecario");
        btnBiblioEliminar.setBackground(new Color(220, 53, 69));
        btnBiblioEliminar.setForeground(Color.WHITE);
        btnBiblioEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnBiblioRestablecerClave = new JButton("Restablecer Clave");
        btnBiblioToggleActivo = new JButton("Activar / Desactivar");
        btnBiblioLimpiar = new JButton("Limpiar");

        panelAcciones.add(btnBiblioRegistrar);
        panelAcciones.add(btnBiblioActualizar);
        panelAcciones.add(btnBiblioEliminar);
        panelAcciones.add(btnBiblioRestablecerClave);
        panelAcciones.add(btnBiblioToggleActivo);
        panelAcciones.add(btnBiblioLimpiar);

        panelFormContenedor.add(panelAcciones, BorderLayout.SOUTH);
        panel.add(panelFormContenedor, BorderLayout.NORTH);

        // Tabla
        modeloBibliotecarios = new DefaultTableModel(new String[]{"ID", "Usuario", "Nombre Completo", "Estado", "Intentos Fallidos"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaBibliotecarios = new JTable(modeloBibliotecarios);
        tablaBibliotecarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaBibliotecarios), BorderLayout.CENTER);

        // Eventos
        btnBiblioRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarBibliotecario(txtBiblioUsername.getText(), txtBiblioNombre.getText(), new String(txtBiblioPassword.getPassword()));
            }
        });

        btnBiblioActualizar.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un bibliotecario de la tabla para editar.");
                return;
            }
            if (controller != null) {
                controller.actualizarBibliotecario(biblioSeleccionadoId, txtBiblioUsername.getText(), txtBiblioNombre.getText(), new String(txtBiblioPassword.getPassword()));
            }
        });

        btnBiblioEliminar.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un bibliotecario de la tabla para eliminar.");
                return;
            }
            if (controller != null) {
                controller.eliminarBibliotecario(biblioSeleccionadoId);
            }
        });

        btnBiblioRestablecerClave.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un bibliotecario de la tabla para restablecer su clave.");
                return;
            }
            if (controller != null) {
                controller.restablecerClaveBibliotecario(biblioSeleccionadoId);
            }
        });

        btnBiblioToggleActivo.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                mostrarAdvertencia("Seleccione un bibliotecario de la tabla.");
                return;
            }
            if (controller != null) {
                controller.toggleActivoBibliotecario(biblioSeleccionadoId);
            }
        });

        btnBiblioLimpiar.addActionListener(e -> limpiarFormBibliotecario());

        tablaBibliotecarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaBibliotecarios.getSelectedRow();
                if (row >= 0) {
                    biblioSeleccionadoId = (int) modeloBibliotecarios.getValueAt(row, 0);
                    txtBiblioUsername.setText(modeloBibliotecarios.getValueAt(row, 1).toString());
                    txtBiblioNombre.setText(modeloBibliotecarios.getValueAt(row, 2).toString());
                    txtBiblioPassword.setText("");

                    lblBiblioModo.setText("Modo: Editando Bibliotecario ID #" + biblioSeleccionadoId + " (" + txtBiblioNombre.getText() + ")");
                    lblBiblioModo.setForeground(new Color(13, 110, 253));
                }
            }
        });

        return panel;
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Formulario Libro
        JPanel panelFormContenedor = new JPanel(new BorderLayout(6, 6));
        panelFormContenedor.setBorder(new TitledBorder("Gestión de Libros y Ejemplares"));

        lblLibroModo = new JLabel("Modo: Nuevo Libro (Seleccione uno de la tabla para editar, eliminar o gestionar ejemplares)");
        lblLibroModo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLibroModo.setForeground(new Color(108, 117, 125));
        lblLibroModo.setBorder(new EmptyBorder(2, 6, 4, 6));
        panelFormContenedor.add(lblLibroModo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(2, 7, 8, 8));
        panelForm.setBorder(new EmptyBorder(4, 6, 6, 6));

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

        panelFormContenedor.add(panelForm, BorderLayout.CENTER);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btnLibroRegistrar = new JButton("Registrar Nuevo Libro");
        btnLibroRegistrar.setBackground(new Color(13, 110, 253));
        btnLibroRegistrar.setForeground(Color.WHITE);
        btnLibroRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnLibroActualizar = new JButton("Guardar Cambios");
        btnLibroActualizar.setBackground(new Color(25, 135, 84));
        btnLibroActualizar.setForeground(Color.WHITE);
        btnLibroActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnLibroEliminar = new JButton("Eliminar Libro");
        btnLibroEliminar.setBackground(new Color(220, 53, 69));
        btnLibroEliminar.setForeground(Color.WHITE);
        btnLibroEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnLibroAgregarEjemplar = new JButton("+ Agregar Ejemplares");
        btnLibroLimpiar = new JButton("Limpiar");

        panelBtns.add(btnLibroRegistrar);
        panelBtns.add(btnLibroActualizar);
        panelBtns.add(btnLibroEliminar);
        panelBtns.add(btnLibroAgregarEjemplar);
        panelBtns.add(btnLibroLimpiar);

        panelFormContenedor.add(panelBtns, BorderLayout.SOUTH);
        panel.add(panelFormContenedor, BorderLayout.NORTH);

        // Tablas Split (Libros arriba, Ejemplares abajo)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);

        modeloLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Año", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaLibros = new JTable(modeloLibros);
        tablaLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        splitPane.setTopComponent(new JScrollPane(tablaLibros));

        JPanel panelEjemplares = new JPanel(new BorderLayout(5, 5));
        panelEjemplares.setBorder(new TitledBorder("Ejemplares Físicos del Libro Seleccionado"));

        modeloEjemplares = new DefaultTableModel(new String[]{"ID", "Código de Barras", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEjemplares = new JTable(modeloEjemplares);
        tablaEjemplares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelEjemplares.add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);

        JPanel panelAccionesEjemplar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnEjemplarCambiarEstado = new JButton("Cambiar Estado (Disponible / Mantenimiento / Baja)");
        btnEjemplarEliminar = new JButton("Eliminar Ejemplar");
        btnEjemplarEliminar.setBackground(new Color(220, 53, 69));
        btnEjemplarEliminar.setForeground(Color.WHITE);

        panelAccionesEjemplar.add(btnEjemplarCambiarEstado);
        panelAccionesEjemplar.add(btnEjemplarEliminar);
        panelEjemplares.add(panelAccionesEjemplar, BorderLayout.SOUTH);

        splitPane.setBottomComponent(panelEjemplares);
        panel.add(splitPane, BorderLayout.CENTER);

        // Eventos
        btnLibroRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarLibro(txtLibroIsbn.getText(), txtLibroTitulo.getText(), txtLibroAutor.getText(),
                        txtLibroCategoria.getText(), txtLibroEditorial.getText(),
                        (int) spinLibroAnio.getValue(), (int) spinEjemplaresIniciales.getValue());
            }
        });

        btnLibroActualizar.addActionListener(e -> {
            if (libroSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un libro de la tabla para editar sus datos.");
                return;
            }
            if (controller != null) {
                controller.actualizarLibro(libroSeleccionadoId, txtLibroIsbn.getText(), txtLibroTitulo.getText(),
                        txtLibroAutor.getText(), txtLibroCategoria.getText(), txtLibroEditorial.getText(),
                        (int) spinLibroAnio.getValue());
            }
        });

        btnLibroEliminar.addActionListener(e -> {
            if (libroSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un libro de la tabla para eliminar.");
                return;
            }
            if (controller != null) {
                controller.eliminarLibro(libroSeleccionadoId);
            }
        });

        btnLibroAgregarEjemplar.addActionListener(e -> {
            if (libroSeleccionadoId == null) {
                mostrarAdvertencia("Seleccione primero un libro de la tabla para agregarle ejemplares.");
                return;
            }
            String cantStr = JOptionPane.showInputDialog(this, "¿Cuántos ejemplares desea agregar al libro seleccionado?", "1");
            if (cantStr != null && !cantStr.trim().isEmpty() && controller != null) {
                try {
                    int cant = Integer.parseInt(cantStr.trim());
                    if (cant <= 0) throw new NumberFormatException();
                    controller.agregarEjemplares(libroSeleccionadoId, cant);
                } catch (NumberFormatException ex) {
                    mostrarAdvertencia("Debe ingresar un número entero mayor a 0.");
                }
            }
        });

        btnLibroLimpiar.addActionListener(e -> limpiarFormLibro());

        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaLibros.getSelectedRow();
                if (row >= 0) {
                    libroSeleccionadoId = (int) modeloLibros.getValueAt(row, 0);
                    txtLibroIsbn.setText(modeloLibros.getValueAt(row, 1).toString());
                    txtLibroTitulo.setText(modeloLibros.getValueAt(row, 2).toString());
                    txtLibroAutor.setText(modeloLibros.getValueAt(row, 3).toString());
                    txtLibroCategoria.setText(modeloLibros.getValueAt(row, 4) != null ? modeloLibros.getValueAt(row, 4).toString() : "");
                    txtLibroEditorial.setText(modeloLibros.getValueAt(row, 5) != null ? modeloLibros.getValueAt(row, 5).toString() : "");
                    if (modeloLibros.getValueAt(row, 6) != null) {
                        try {
                            spinLibroAnio.setValue(Integer.parseInt(modeloLibros.getValueAt(row, 6).toString()));
                        } catch (Exception ignored) {}
                    }
                    lblLibroModo.setText("Modo: Editando Libro ID #" + libroSeleccionadoId + " (" + txtLibroTitulo.getText() + ")");
                    lblLibroModo.setForeground(new Color(13, 110, 253));

                    if (controller != null) {
                        controller.cargarEjemplaresDeLibro(libroSeleccionadoId);
                    }
                }
            }
        });

        btnEjemplarCambiarEstado.addActionListener(e -> {
            int row = tablaEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && libroSeleccionadoId != null) {
                int ejemplarId = (int) modeloEjemplares.getValueAt(row, 0);
                controller.cambiarEstadoEjemplarDialog(ejemplarId, libroSeleccionadoId);
            } else {
                mostrarAdvertencia("Seleccione un ejemplar de la tabla inferior.");
            }
        });

        btnEjemplarEliminar.addActionListener(e -> {
            int row = tablaEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && libroSeleccionadoId != null) {
                int ejemplarId = (int) modeloEjemplares.getValueAt(row, 0);
                controller.eliminarEjemplar(ejemplarId, libroSeleccionadoId);
            } else {
                mostrarAdvertencia("Seleccione un ejemplar de la tabla inferior.");
            }
        });

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
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

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
        biblioSeleccionadoId = null;
        txtBiblioUsername.setText("");
        txtBiblioNombre.setText("");
        txtBiblioPassword.setText("");
        tablaBibliotecarios.clearSelection();
        lblBiblioModo.setText("Modo: Nuevo Bibliotecario (Seleccione una fila de la tabla para editar o eliminar)");
        lblBiblioModo.setForeground(new Color(108, 117, 125));
    }

    public void limpiarFormLibro() {
        libroSeleccionadoId = null;
        txtLibroIsbn.setText("");
        txtLibroTitulo.setText("");
        txtLibroAutor.setText("");
        txtLibroCategoria.setText("");
        txtLibroEditorial.setText("");
        spinLibroAnio.setValue(2024);
        spinEjemplaresIniciales.setValue(3);
        tablaLibros.clearSelection();
        modeloEjemplares.setRowCount(0);
        lblLibroModo.setText("Modo: Nuevo Libro (Seleccione uno de la tabla para editar, eliminar o gestionar ejemplares)");
        lblLibroModo.setForeground(new Color(108, 117, 125));
    }

    public void mostrarModalClaveTemporal(String username, String nombre, String claveTemporal) {
        String mensaje = "<html><body style='width: 320px; font-family: Segoe UI;'>" +
                "<h3 style='color: #0d6efd;'>Credenciales de Bibliotecario</h3>" +
                "<p>Se han generado las credenciales de acceso:</p>" +
                "<p><b>Nombre:</b> " + nombre + "<br/>" +
                "<b>Usuario:</b> " + username + "<br/>" +
                "<b>Contraseña Asignada:</b> <span style='background:#ffc107; font-size:14px; padding:2px 6px; font-weight:bold;'>" + claveTemporal + "</span></p>" +
                "<p style='color: #dc3545;'><i>El usuario deberá cambiar su contraseña obligatoriamente al iniciar sesión.</i></p>" +
                "</body></html>";

        JOptionPane.showMessageDialog(this, mensaje, "Contraseña Temporal de Bibliotecario", JOptionPane.INFORMATION_MESSAGE);
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
