package com.biblioteca.view;

import com.biblioteca.controller.BibliotecarioController;
import com.biblioteca.model.*;
import com.biblioteca.service.PrestamoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal para el rol BIBLIOTECARIO.
 * Permite operar todo el flujo de clientes, catálogo, préstamos, devoluciones, reservas y multas.
 */
public class BibliotecarioView extends JFrame {

    private BibliotecarioController controller;

    // Pestaña Clientes
    private Integer clienteSeleccionadoId = null;
    private JLabel lblClienteModo;
    private JTextField txtClienteDni;
    private JTextField txtClienteNombres;
    private JTextField txtClienteApellidos;
    private JTextField txtClienteEmail;
    private JTextField txtClienteTelefono;
    private JTextField txtClienteBuscar;
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;
    private JButton btnClienteRegistrar;
    private JButton btnClienteActualizar;
    private JButton btnClienteEliminar;
    private JButton btnClienteRestablecerClave;
    private JButton btnClienteToggleActivo;
    private JButton btnClienteBuscar;
    private JButton btnClienteLimpiar;

    // Pestaña Catálogo
    private Integer catLibroSeleccionadoId = null;
    private JTextField txtCatalogoBuscar;
    private JTable tablaCatLibros;
    private DefaultTableModel modeloCatLibros;
    private JTable tablaCatEjemplares;
    private DefaultTableModel modeloCatEjemplares;
    private JButton btnCatCambiarEstado;
    private JButton btnCatAgregarEjemplares;
    private JButton btnCatEliminarEjemplar;

    // Pestaña Préstamos
    private JTextField txtPrestamoDni;
    private JTextField txtPrestamoCodigoBarras;
    private JButton btnPrestamoRegistrar;
    private JButton btnPrestamoRenovar;
    private JTable tablaPrestamos;
    private DefaultTableModel modeloPrestamos;

    // Pestaña Devoluciones
    private JTextField txtDevolucionCodigoBarras;
    private JButton btnDevolucionProcesar;
    private JLabel lblDevolucionResultado;
    private JButton btnDevolucionCobrarMulta;
    private Integer ultimaMultaIdGenerada;

    // Pestaña Reservas
    private JTextField txtReservaDni;
    private JTextField txtReservaLibroId;
    private JButton btnReservaCrear;
    private JButton btnReservaCancelar;
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;

    // Pestaña Multas
    private JTable tablaMultas;
    private DefaultTableModel modeloMultas;
    private JButton btnMultaCobrar;
    private JCheckBox chkSoloPendientes;

    public BibliotecarioView() {
        initComponents();
    }

    public void setController(BibliotecarioController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Panel del Bibliotecario");
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

        tabbedPane.addTab("Clientes", crearPanelClientes());
        tabbedPane.addTab("Catálogo", crearPanelCatalogo());
        tabbedPane.addTab("Préstamos", crearPanelPrestamos());
        tabbedPane.addTab("Devoluciones", crearPanelDevoluciones());
        tabbedPane.addTab("Reservas", crearPanelReservas());
        tabbedPane.addTab("Multas", crearPanelMultas());

        panelContenedor.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(panelContenedor);
    }

    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        // Formulario Superior
        JPanel panelFormContenedor = new JPanel(new BorderLayout(6, 6));
        panelFormContenedor.setBorder(new TitledBorder("Gestión y Registro de Clientes"));

        lblClienteModo = new JLabel("Modo: Nuevo Cliente (Seleccione una fila de la tabla para editar o eliminar)");
        lblClienteModo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClienteModo.setForeground(new Color(108, 117, 125));
        lblClienteModo.setBorder(new EmptyBorder(2, 6, 4, 6));
        panelFormContenedor.add(lblClienteModo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(2, 6, 8, 8));
        panelCampos.setBorder(new EmptyBorder(4, 6, 6, 6));

        panelCampos.add(new JLabel("DNI (8 dígitos):"));
        txtClienteDni = new JTextField();
        panelCampos.add(txtClienteDni);

        panelCampos.add(new JLabel("Nombres:"));
        txtClienteNombres = new JTextField();
        panelCampos.add(txtClienteNombres);

        panelCampos.add(new JLabel("Apellidos:"));
        txtClienteApellidos = new JTextField();
        panelCampos.add(txtClienteApellidos);

        panelCampos.add(new JLabel("Correo Electrónico:"));
        txtClienteEmail = new JTextField();
        panelCampos.add(txtClienteEmail);

        panelCampos.add(new JLabel("Teléfono:"));
        txtClienteTelefono = new JTextField();
        panelCampos.add(txtClienteTelefono);

        // Espaciador en cuadrícula
        panelCampos.add(new JLabel(""));
        panelCampos.add(new JLabel(""));

        panelFormContenedor.add(panelCampos, BorderLayout.CENTER);

        // Barra de Botones del formulario
        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        btnClienteRegistrar = new JButton("Registrar Nuevo");
        btnClienteRegistrar.setBackground(new Color(13, 110, 253));
        btnClienteRegistrar.setForeground(Color.WHITE);
        btnClienteRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnClienteActualizar = new JButton("Guardar Cambios");
        btnClienteActualizar.setBackground(new Color(25, 135, 84));
        btnClienteActualizar.setForeground(Color.WHITE);
        btnClienteActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnClienteEliminar = new JButton("Eliminar Cliente");
        btnClienteEliminar.setBackground(new Color(220, 53, 69));
        btnClienteEliminar.setForeground(Color.WHITE);
        btnClienteEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnClienteRestablecerClave = new JButton("Restablecer Clave");
        btnClienteToggleActivo = new JButton("Activar / Desactivar");
        btnClienteLimpiar = new JButton("Limpiar");

        panelBotonesForm.add(btnClienteRegistrar);
        panelBotonesForm.add(btnClienteActualizar);
        panelBotonesForm.add(btnClienteEliminar);
        panelBotonesForm.add(btnClienteRestablecerClave);
        panelBotonesForm.add(btnClienteToggleActivo);
        panelBotonesForm.add(btnClienteLimpiar);
        panelFormContenedor.add(panelBotonesForm, BorderLayout.SOUTH);

        panel.add(panelFormContenedor, BorderLayout.NORTH);

        // Barra de Búsqueda y Tabla
        JPanel panelCentro = new JPanel(new BorderLayout(6, 6));
        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        panelBuscar.add(new JLabel("Buscar por DNI o Apellidos:"));
        txtClienteBuscar = new JTextField(20);
        btnClienteBuscar = new JButton("Buscar");
        JButton btnVerTodos = new JButton("Mostrar Todos");
        panelBuscar.add(txtClienteBuscar);
        panelBuscar.add(btnClienteBuscar);
        panelBuscar.add(btnVerTodos);
        panelCentro.add(panelBuscar, BorderLayout.NORTH);

        modeloClientes = new DefaultTableModel(new String[]{"ID", "DNI", "Nombres", "Apellidos", "Email", "Teléfono", "Fecha Registro"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaClientes = new JTable(modeloClientes);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelCentro.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        panel.add(panelCentro, BorderLayout.CENTER);

        // Eventos
        btnClienteRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.registrarCliente(txtClienteDni.getText(), txtClienteNombres.getText(),
                        txtClienteApellidos.getText(), txtClienteEmail.getText(), txtClienteTelefono.getText());
            }
        });

        btnClienteActualizar.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un cliente de la tabla para editar sus datos.");
                return;
            }
            if (controller != null) {
                controller.editarCliente(clienteSeleccionadoId, txtClienteDni.getText(), txtClienteNombres.getText(),
                        txtClienteApellidos.getText(), txtClienteEmail.getText(), txtClienteTelefono.getText());
            }
        });

        btnClienteEliminar.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un cliente de la tabla para eliminar.");
                return;
            }
            if (controller != null) {
                controller.eliminarCliente(clienteSeleccionadoId);
            }
        });

        btnClienteRestablecerClave.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un cliente de la tabla para restablecer su clave.");
                return;
            }
            if (controller != null) {
                controller.restablecerClaveCliente(clienteSeleccionadoId);
            }
        });

        btnClienteToggleActivo.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                mostrarAdvertencia("Por favor seleccione un cliente de la tabla.");
                return;
            }
            if (controller != null) {
                controller.toggleActivoCliente(clienteSeleccionadoId);
            }
        });

        btnClienteBuscar.addActionListener(e -> {
            if (controller != null) controller.buscarClientes(txtClienteBuscar.getText());
        });

        btnVerTodos.addActionListener(e -> {
            txtClienteBuscar.setText("");
            if (controller != null) controller.cargarClientes();
        });

        btnClienteLimpiar.addActionListener(e -> limpiarFormCliente());

        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaClientes.getSelectedRow();
                if (row >= 0) {
                    clienteSeleccionadoId = (int) modeloClientes.getValueAt(row, 0);
                    txtClienteDni.setText(modeloClientes.getValueAt(row, 1).toString());
                    txtClienteNombres.setText(modeloClientes.getValueAt(row, 2).toString());
                    txtClienteApellidos.setText(modeloClientes.getValueAt(row, 3).toString());
                    txtClienteEmail.setText(modeloClientes.getValueAt(row, 4) != null ? modeloClientes.getValueAt(row, 4).toString() : "");
                    txtClienteTelefono.setText(modeloClientes.getValueAt(row, 5) != null ? modeloClientes.getValueAt(row, 5).toString() : "");

                    lblClienteModo.setText("Modo: Editando Cliente ID #" + clienteSeleccionadoId + " (" + txtClienteNombres.getText() + " " + txtClienteApellidos.getText() + ")");
                    lblClienteModo.setForeground(new Color(13, 110, 253));
                }
            }
        });

        return panel;
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBuscar.add(new JLabel("Buscar Libro:"));
        txtCatalogoBuscar = new JTextField(25);
        JButton btnBuscarLibro = new JButton("Buscar");
        JButton btnTodosLibros = new JButton("Todos");
        panelBuscar.add(txtCatalogoBuscar);
        panelBuscar.add(btnBuscarLibro);
        panelBuscar.add(btnTodosLibros);
        panel.add(panelBuscar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);

        modeloCatLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatLibros = new JTable(modeloCatLibros);
        tablaCatLibros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        splitPane.setTopComponent(new JScrollPane(tablaCatLibros));

        JPanel panelEjemplares = new JPanel(new BorderLayout(5, 5));
        panelEjemplares.setBorder(new TitledBorder("Ejemplares y Estado Físico del Libro Seleccionado"));

        modeloCatEjemplares = new DefaultTableModel(new String[]{"ID Ejemplar", "Código de Barras", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatEjemplares = new JTable(modeloCatEjemplares);
        tablaCatEjemplares.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelEjemplares.add(new JScrollPane(tablaCatEjemplares), BorderLayout.CENTER);

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnCatAgregarEjemplares = new JButton("+ Agregar Ejemplares");
        btnCatAgregarEjemplares.setBackground(new Color(13, 110, 253));
        btnCatAgregarEjemplares.setForeground(Color.WHITE);

        btnCatCambiarEstado = new JButton("Cambiar Estado (Disponible / Mantenimiento / Baja)");

        btnCatEliminarEjemplar = new JButton("Eliminar Ejemplar");
        btnCatEliminarEjemplar.setBackground(new Color(220, 53, 69));
        btnCatEliminarEjemplar.setForeground(Color.WHITE);

        panelBtns.add(btnCatAgregarEjemplares);
        panelBtns.add(btnCatCambiarEstado);
        panelBtns.add(btnCatEliminarEjemplar);
        panelEjemplares.add(panelBtns, BorderLayout.SOUTH);

        splitPane.setBottomComponent(panelEjemplares);
        panel.add(splitPane, BorderLayout.CENTER);

        btnBuscarLibro.addActionListener(e -> {
            if (controller != null) controller.buscarCatalogo(txtCatalogoBuscar.getText());
        });

        btnTodosLibros.addActionListener(e -> {
            txtCatalogoBuscar.setText("");
            if (controller != null) controller.cargarCatalogo();
        });

        tablaCatLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaCatLibros.getSelectedRow();
                if (row >= 0 && controller != null) {
                    catLibroSeleccionadoId = (int) modeloCatLibros.getValueAt(row, 0);
                    controller.cargarEjemplaresDeLibro(catLibroSeleccionadoId);
                }
            }
        });

        btnCatAgregarEjemplares.addActionListener(e -> {
            if (catLibroSeleccionadoId == null) {
                mostrarAdvertencia("Seleccione primero un libro de la tabla superior para agregarle ejemplares.");
                return;
            }
            String cantStr = JOptionPane.showInputDialog(this, "¿Cuántos ejemplares desea agregar al libro seleccionado?", "1");
            if (cantStr != null && !cantStr.trim().isEmpty() && controller != null) {
                try {
                    int cant = Integer.parseInt(cantStr.trim());
                    if (cant <= 0) throw new NumberFormatException();
                    controller.agregarEjemplares(catLibroSeleccionadoId, cant);
                } catch (NumberFormatException ex) {
                    mostrarAdvertencia("Debe ingresar un número entero mayor a 0.");
                }
            }
        });

        btnCatCambiarEstado.addActionListener(e -> {
            int row = tablaCatEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && catLibroSeleccionadoId != null) {
                int ejemplarId = (int) modeloCatEjemplares.getValueAt(row, 0);
                controller.cambiarEstadoEjemplarDialog(ejemplarId, catLibroSeleccionadoId);
            } else {
                mostrarAdvertencia("Seleccione un ejemplar de la tabla inferior.");
            }
        });

        btnCatEliminarEjemplar.addActionListener(e -> {
            int row = tablaCatEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && catLibroSeleccionadoId != null) {
                int ejemplarId = (int) modeloCatEjemplares.getValueAt(row, 0);
                controller.eliminarEjemplar(ejemplarId, catLibroSeleccionadoId);
            } else {
                mostrarAdvertencia("Seleccione un ejemplar de la tabla inferior.");
            }
        });

        return panel;
    }

    private JPanel crearPanelPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JPanel panelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelForm.setBorder(new TitledBorder("Registrar Nuevo Préstamo"));

        panelForm.add(new JLabel("DNI del Cliente:"));
        txtPrestamoDni = new JTextField(12);
        panelForm.add(txtPrestamoDni);

        panelForm.add(new JLabel("Código de Barras del Ejemplar:"));
        txtPrestamoCodigoBarras = new JTextField(15);
        panelForm.add(txtPrestamoCodigoBarras);

        btnPrestamoRegistrar = new JButton("Registrar Préstamo");
        btnPrestamoRegistrar.setBackground(new Color(13, 110, 253));
        btnPrestamoRegistrar.setForeground(Color.WHITE);
        btnPrestamoRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelForm.add(btnPrestamoRegistrar);

        panel.add(panelForm, BorderLayout.NORTH);

        JPanel panelTabla = new JPanel(new BorderLayout(5, 5));
        panelTabla.setBorder(new TitledBorder("Préstamos Activos y Vencidos"));

        modeloPrestamos = new DefaultTableModel(new String[]{"ID", "Cliente ID", "Ejemplar ID", "Fecha Préstamo", "Fecha Vencimiento", "Estado", "Renovaciones"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPrestamos = new JTable(modeloPrestamos);
        tablaPrestamos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelTabla.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        JPanel panelAccionesPrestamo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnPrestamoRenovar = new JButton("Renovar Préstamo Seleccionado");
        btnPrestamoRenovar.setBackground(new Color(25, 135, 84));
        btnPrestamoRenovar.setForeground(Color.WHITE);
        btnPrestamoRenovar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelAccionesPrestamo.add(btnPrestamoRenovar);
        panelTabla.add(panelAccionesPrestamo, BorderLayout.SOUTH);

        panel.add(panelTabla, BorderLayout.CENTER);

        btnPrestamoRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.registrarPrestamo(txtPrestamoDni.getText(), txtPrestamoCodigoBarras.getText());
            }
        });

        btnPrestamoRenovar.addActionListener(e -> {
            int row = tablaPrestamos.getSelectedRow();
            if (row >= 0 && controller != null) {
                int prestamoId = (int) modeloPrestamos.getValueAt(row, 0);
                controller.renovarPrestamo(prestamoId);
            } else {
                mostrarAdvertencia("Seleccione un préstamo de la tabla para renovarlo.");
            }
        });

        return panel;
    }

    private JPanel crearPanelDevoluciones() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel panelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelForm.setBorder(new TitledBorder("Recepción de Ejemplares (Devolución)"));

        panelForm.add(new JLabel("Código de Barras del Ejemplar:"));
        txtDevolucionCodigoBarras = new JTextField(18);
        btnDevolucionProcesar = new JButton("Registrar Devolución");
        btnDevolucionProcesar.setBackground(new Color(25, 135, 84));
        btnDevolucionProcesar.setForeground(Color.WHITE);
        btnDevolucionProcesar.setFont(new Font("Segoe UI", Font.BOLD, 12));

        panelForm.add(txtDevolucionCodigoBarras);
        panelForm.add(btnDevolucionProcesar);
        panel.add(panelForm, BorderLayout.NORTH);

        JPanel panelResultado = new JPanel(new BorderLayout(15, 15));
        panelResultado.setBorder(new TitledBorder("Comprobante de Devolución"));

        lblDevolucionResultado = new JLabel("<html><i>Ingrese un código de barras para procesar la devolución.</i></html>");
        lblDevolucionResultado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelResultado.add(lblDevolucionResultado, BorderLayout.CENTER);

        JPanel panelCobro = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnDevolucionCobrarMulta = new JButton("Cobrar Multa de Inmediato");
        btnDevolucionCobrarMulta.setBackground(new Color(220, 53, 69));
        btnDevolucionCobrarMulta.setForeground(Color.WHITE);
        btnDevolucionCobrarMulta.setVisible(false);
        panelCobro.add(btnDevolucionCobrarMulta);
        panelResultado.add(panelCobro, BorderLayout.SOUTH);

        panel.add(panelResultado, BorderLayout.CENTER);

        btnDevolucionProcesar.addActionListener(e -> {
            if (controller != null) {
                controller.registrarDevolucion(txtDevolucionCodigoBarras.getText());
            }
        });

        btnDevolucionCobrarMulta.addActionListener(e -> {
            if (controller != null && ultimaMultaIdGenerada != null) {
                controller.cobrarMulta(ultimaMultaIdGenerada);
                btnDevolucionCobrarMulta.setVisible(false);
            }
        });

        return panel;
    }

    private JPanel crearPanelReservas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JPanel panelForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelForm.setBorder(new TitledBorder("Registrar Reserva para Lector"));

        panelForm.add(new JLabel("DNI Cliente:"));
        txtReservaDni = new JTextField(10);
        panelForm.add(txtReservaDni);

        panelForm.add(new JLabel("ID Libro:"));
        txtReservaLibroId = new JTextField(6);
        panelForm.add(txtReservaLibroId);

        btnReservaCrear = new JButton("Crear Reserva (Cola FIFO)");
        btnReservaCrear.setBackground(new Color(13, 110, 253));
        btnReservaCrear.setForeground(Color.WHITE);
        panelForm.add(btnReservaCrear);

        JButton btnRefrescarReservas = new JButton("Actualizar");
        panelForm.add(btnRefrescarReservas);

        panel.add(panelForm, BorderLayout.NORTH);

        modeloReservas = new DefaultTableModel(new String[]{"ID", "Cliente ID", "Libro ID", "Ejemplar Asignado", "Fecha Reserva", "Fecha Expiración", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnReservaCancelar = new JButton("Cancelar Reserva Seleccionada");
        btnReservaCancelar.setBackground(new Color(220, 53, 69));
        btnReservaCancelar.setForeground(Color.WHITE);
        panelAcciones.add(btnReservaCancelar);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        btnReservaCrear.addActionListener(e -> {
            if (controller != null) controller.crearReserva(txtReservaDni.getText(), txtReservaLibroId.getText());
        });

        btnReservaCancelar.addActionListener(e -> {
            int row = tablaReservas.getSelectedRow();
            if (row >= 0 && controller != null) {
                int reservaId = (int) modeloReservas.getValueAt(row, 0);
                controller.cancelarReserva(reservaId);
            } else {
                mostrarAdvertencia("Seleccione una reserva de la tabla.");
            }
        });

        btnRefrescarReservas.addActionListener(e -> {
            if (controller != null) controller.cargarReservas();
        });

        return panel;
    }

    private JPanel crearPanelMultas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panelFiltro.setBorder(new TitledBorder("Filtro y Gestión de Multas"));

        chkSoloPendientes = new JCheckBox("Mostrar únicamente multas pendientes de pago", true);
        panelFiltro.add(chkSoloPendientes);

        JButton btnRefrescar = new JButton("Actualizar");
        panelFiltro.add(btnRefrescar);

        panel.add(panelFiltro, BorderLayout.NORTH);

        modeloMultas = new DefaultTableModel(new String[]{"ID Multa", "ID Préstamo", "Días Retraso", "Tarifa/Día", "Total", "Estado", "Fecha Pago"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMultas = new JTable(modeloMultas);
        tablaMultas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaMultas), BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnMultaCobrar = new JButton("Cobrar Multa Seleccionada");
        btnMultaCobrar.setBackground(new Color(25, 135, 84));
        btnMultaCobrar.setForeground(Color.WHITE);
        btnMultaCobrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelAcciones.add(btnMultaCobrar);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        chkSoloPendientes.addActionListener(e -> {
            if (controller != null) controller.cargarMultas(chkSoloPendientes.isSelected());
        });

        btnRefrescar.addActionListener(e -> {
            if (controller != null) controller.cargarMultas(chkSoloPendientes.isSelected());
        });

        btnMultaCobrar.addActionListener(e -> {
            int row = tablaMultas.getSelectedRow();
            if (row >= 0 && controller != null) {
                int multaId = (int) modeloMultas.getValueAt(row, 0);
                controller.cobrarMulta(multaId);
            } else {
                mostrarAdvertencia("Seleccione una multa de la tabla para registrar su pago.");
            }
        });

        return panel;
    }

    // Métodos para actualizar la vista
    public void cargarTablaClientes(List<Cliente> clientes) {
        modeloClientes.setRowCount(0);
        for (Cliente c : clientes) {
            modeloClientes.addRow(new Object[]{
                    c.getId(), c.getDni(), c.getNombres(), c.getApellidos(), c.getEmail(), c.getTelefono(), c.getFechaRegistro()
            });
        }
    }

    public void cargarTablaCatLibros(List<Libro> libros, java.util.function.Function<Integer, Integer> contarDisp) {
        modeloCatLibros.setRowCount(0);
        for (Libro l : libros) {
            int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
            modeloCatLibros.addRow(new Object[]{
                    l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), disp
            });
        }
    }

    public void cargarTablaCatEjemplares(List<Ejemplar> ejemplares) {
        modeloCatEjemplares.setRowCount(0);
        for (Ejemplar ej : ejemplares) {
            modeloCatEjemplares.addRow(new Object[]{
                    ej.getId(), ej.getCodigoBarras(), ej.getEstado()
            });
        }
    }

    public void cargarTablaPrestamos(List<Prestamo> prestamos) {
        modeloPrestamos.setRowCount(0);
        for (Prestamo p : prestamos) {
            modeloPrestamos.addRow(new Object[]{
                    p.getId(), p.getClienteId(), p.getEjemplarId(), p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado(), p.getRenovaciones()
            });
        }
    }

    public void cargarTablaReservas(List<Reserva> reservas) {
        modeloReservas.setRowCount(0);
        for (Reserva r : reservas) {
            modeloReservas.addRow(new Object[]{
                    r.getId(), r.getClienteId(), r.getLibroId(), r.getEjemplarId() != null ? r.getEjemplarId() : "-",
                    r.getFechaReserva(), r.getFechaExpiracion() != null ? r.getFechaExpiracion() : "-", r.getEstado()
            });
        }
    }

    public void cargarTablaMultas(List<Multa> multas) {
        modeloMultas.setRowCount(0);
        for (Multa m : multas) {
            modeloMultas.addRow(new Object[]{
                    m.getId(), m.getPrestamoId(), m.getDiasRetraso(), "S/ " + m.getTarifaAplicada(),
                    "S/ " + m.getMonto(), m.getEstado(), m.getFechaPago() != null ? m.getFechaPago() : "-"
            });
        }
    }

    public void mostrarResultadoDevolucion(PrestamoService.ResultadoDevolucion resultado) {
        StringBuilder sb = new StringBuilder("<html><div style='padding:5px;'>");
        sb.append("<b>Devolución Exitosa:</b><br/>");
        sb.append("Préstamo ID: ").append(resultado.getPrestamo().getId()).append("<br/>");
        sb.append("Fecha Devolución: ").append(resultado.getPrestamo().getFechaDevolucion()).append("<br/>");

        if (resultado.tieneMulta()) {
            sb.append("<span style='color:red;'><b>Multa Generada por retraso:</b> S/ ").append(resultado.getMulta().getMonto()).append("</span><br/>");
            sb.append("Días de retraso: ").append(resultado.getMulta().getDiasRetraso()).append("<br/>");
            ultimaMultaIdGenerada = resultado.getMulta().getId();
            btnDevolucionCobrarMulta.setVisible(true);
        } else {
            sb.append("<span style='color:green;'><b>Devolución a tiempo - Sin multas aplicadas.</b></span><br/>");
            btnDevolucionCobrarMulta.setVisible(false);
            ultimaMultaIdGenerada = null;
        }

        if (resultado.isAsignadoAReserva()) {
            sb.append("<span style='color:blue;'><i>El ejemplar devuelto fue asignado a la primera reserva en cola de espera.</i></span>");
        } else {
            sb.append("<i>El ejemplar quedó disponible para préstamos.</i>");
        }

        sb.append("</div></html>");
        lblDevolucionResultado.setText(sb.toString());
        txtDevolucionCodigoBarras.setText("");
    }

    public void mostrarModalClaveTemporal(String dni, String nombre, String claveTemporal) {
        String mensaje = "<html><body style='width: 320px; font-family: Segoe UI;'>" +
                "<h3 style='color: #0d6efd;'>Credenciales del Lector</h3>" +
                "<p>Se ha configurado la cuenta de usuario para el cliente:</p>" +
                "<p><b>Nombre:</b> " + nombre + "<br/>" +
                "<b>Usuario (DNI):</b> " + dni + "<br/>" +
                "<b>Contraseña Temporal:</b> <span style='background:#ffc107; font-size:14px; padding:2px 6px; font-weight:bold;'>" + claveTemporal + "</span></p>" +
                "<p style='color: #dc3545;'><i>El lector estará obligado a cambiar esta contraseña en su primer inicio de sesión.</i></p>" +
                "</body></html>";

        JOptionPane.showMessageDialog(this, mensaje, "Contraseña Temporal de Cliente", JOptionPane.INFORMATION_MESSAGE);
    }

    public void limpiarFormCliente() {
        clienteSeleccionadoId = null;
        txtClienteDni.setText("");
        txtClienteNombres.setText("");
        txtClienteApellidos.setText("");
        txtClienteEmail.setText("");
        txtClienteTelefono.setText("");
        tablaClientes.clearSelection();
        lblClienteModo.setText("Modo: Nuevo Cliente (Seleccione una fila de la tabla para editar o eliminar)");
        lblClienteModo.setForeground(new Color(108, 117, 125));
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
