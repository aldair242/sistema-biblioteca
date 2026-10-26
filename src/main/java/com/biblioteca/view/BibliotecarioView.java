package com.biblioteca.view;

import com.biblioteca.controller.BibliotecarioController;
import com.biblioteca.model.*;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Ventana principal rediseñada para el rol BIBLIOTECARIO.
 * Dispone de barra lateral de navegación, dashboard operativo en tiempo real,
 * y módulos especializados para préstamos, devoluciones, clientes, catálogo, reservas y multas.
 */
public class BibliotecarioView extends JFrame {

    private BibliotecarioController controller;

    // Componentes de Estructura
    private BarraNavegacionLateral barraLateral;
    private BarraSuperiorModerna barraSuperior;
    private JPanel panelContenedorTarjetas;
    private CardLayout cardLayout;

    // KPIs del Dashboard Operativo
    private TarjetaMetrica kpiPrestamosActivos;
    private TarjetaMetrica kpiPrestamosVencidos;
    private TarjetaMetrica kpiMultasPendientes;
    private TarjetaMetrica kpiReservasEnEspera;

    // Pestaña Clientes
    private Integer clienteSeleccionadoId = null;
    private JLabel lblClienteModo;
    private CampoTexto txtClienteDni;
    private CampoTexto txtClienteNombres;
    private CampoTexto txtClienteApellidos;
    private CampoTexto txtClienteEmail;
    private CampoTexto txtClienteTelefono;
    private CampoTexto txtClienteBuscar;
    private TablaEstilizada tablaClientes;
    private DefaultTableModel modeloClientes;
    private BotonPrimario btnClienteRegistrar;
    private BotonAcento btnClienteActualizar;
    private BotonPeligro btnClienteEliminar;
    private BotonSecundario btnClienteRestablecerClave;
    private BotonSecundario btnClienteToggleActivo;
    private BotonSecundario btnClienteBuscar;
    private BotonSecundario btnClienteLimpiar;

    // Pestaña Catálogo
    private Integer catLibroSeleccionadoId = null;
    private CampoTexto txtCatalogoBuscar;
    private TablaEstilizada tablaCatLibros;
    private DefaultTableModel modeloCatLibros;
    private TablaEstilizada tablaCatEjemplares;
    private DefaultTableModel modeloCatEjemplares;
    private JLabel lblCatEjTit;
    private JLabel lblCatEjDetalle;
    private CampoTexto txtCatEjemplarBuscar;
    private java.util.List<Ejemplar> catEjemplaresActuales = new java.util.ArrayList<>();
    private BotonSecundario btnCatCambiarEstado;
    private BotonPrimario btnCatAgregarEjemplares;
    private BotonPeligro btnCatEliminarEjemplar;

    // Pestaña Préstamos
    private CampoTexto txtPrestamoDni;
    private CampoTexto txtPrestamoCodigoBarras;
    private JComboBox<String> comboDiasPrestamo;
    private BotonPrimario btnPrestamoRegistrar;
    private BotonAcento btnPrestamoRenovar;
    private TablaEstilizada tablaPrestamos;
    private DefaultTableModel modeloPrestamos;

    // Pestaña Devoluciones
    private CampoTexto txtDevolucionCodigoBarras;
    private BotonAcento btnDevolucionProcesar;
    private JLabel lblDevolucionResultado;
    private BotonPeligro btnDevolucionCobrarMulta;
    private Integer ultimaMultaIdGenerada;

    // Pestaña Reservas
    private CampoTexto txtReservaDni;
    private CampoTexto txtReservaLibroId;
    private BotonPrimario btnReservaCrear;
    private BotonPeligro btnReservaCancelar;
    private TablaEstilizada tablaReservas;
    private DefaultTableModel modeloReservas;

    // Pestaña Multas
    private TablaEstilizada tablaMultas;
    private DefaultTableModel modeloMultas;
    private BotonAcento btnMultaCobrar;
    private JCheckBox chkSoloPendientes;

    public BibliotecarioView() {
        Tema.aplicar();
        initComponents();
    }

    public void setController(BibliotecarioController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Mostrador del Bibliotecario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 800);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(Iconos.crearAppLogoImage(64));

        JPanel panelRaiz = new JPanel(new BorderLayout());
        panelRaiz.setBackground(Tema.COLOR_FONDO);

        // Barra Lateral de Navegación
        barraLateral = new BarraNavegacionLateral("Bibliotecario", clave -> {
            if (cardLayout != null) {
                cardLayout.show(panelContenedorTarjetas, clave);
                actualizarTitulosSeccion(clave);
            }
        });

        barraLateral.agregarItem("DASHBOARD", "Inicio", Iconos.crearDashboard(20, Color.WHITE));
        barraLateral.agregarItem("PRESTAMOS", "Préstamos", Iconos.crearPrestamo(20, Color.WHITE));
        barraLateral.agregarItem("DEVOLUCIONES", "Devoluciones", Iconos.crearDevolucion(20, Color.WHITE));
        barraLateral.agregarItem("CLIENTES", "Lectores / Clientes", Iconos.crearUsuario(20, Color.WHITE));
        barraLateral.agregarItem("CATALOGO", "Catálogo y Libros", Iconos.crearLibro(20, Color.WHITE));
        barraLateral.agregarItem("RESERVAS", "Reservas (FIFO)", Iconos.crearReserva(20, Color.WHITE));
        barraLateral.agregarItem("MULTAS", "Caja de Multas", Iconos.crearMulta(20, Color.WHITE));

        panelRaiz.add(barraLateral, BorderLayout.WEST);

        // Panel Central (Cabecera Superior + Vistas)
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Tema.COLOR_FONDO);

        barraSuperior = new BarraSuperiorModerna(this, () -> {
            if (controller != null) {
                controller.cerrarSesion();
            }
        });
        panelCentral.add(barraSuperior, BorderLayout.NORTH);

        // Contenedor de Tarjetas con CardLayout
        cardLayout = new CardLayout();
        panelContenedorTarjetas = new JPanel(cardLayout);
        panelContenedorTarjetas.setOpaque(false);
        panelContenedorTarjetas.setBorder(new EmptyBorder(16, 24, 20, 24));

        panelContenedorTarjetas.add(crearVistaDashboard(), "DASHBOARD");
        panelContenedorTarjetas.add(crearVistaPrestamos(), "PRESTAMOS");
        panelContenedorTarjetas.add(crearVistaDevoluciones(), "DEVOLUCIONES");
        panelContenedorTarjetas.add(crearVistaClientes(), "CLIENTES");
        panelContenedorTarjetas.add(crearVistaCatalogo(), "CATALOGO");
        panelContenedorTarjetas.add(crearVistaReservas(), "RESERVAS");
        panelContenedorTarjetas.add(crearVistaMultas(), "MULTAS");

        panelCentral.add(panelContenedorTarjetas, BorderLayout.CENTER);
        panelRaiz.add(panelCentral, BorderLayout.CENTER);

        setContentPane(panelRaiz);
    }

    private void actualizarTitulosSeccion(String clave) {
        switch (clave) {
            case "DASHBOARD" -> barraSuperior.setSeccion("Mostrador Principal", "Resumen operativo de atención al lector");
            case "PRESTAMOS" -> barraSuperior.setSeccion("Registro de Préstamos", "Emisión de préstamos y renovación de plazos");
            case "DEVOLUCIONES" -> barraSuperior.setSeccion("Recepción y Devoluciones", "Procesamiento de ejemplares y cálculo automático de moras");
            case "CLIENTES" -> barraSuperior.setSeccion("Gestión de Lectores", "Registro, edición y credenciales temporales de clientes");
            case "CATALOGO" -> barraSuperior.setSeccion("Catálogo Bibliográfico", "Consulta de disponibilidad e incorporación de ejemplares");
            case "RESERVAS" -> barraSuperior.setSeccion("Cola de Reservas", "Control de turnos FIFO y reservas asignadas");
            case "MULTAS" -> barraSuperior.setSeccion("Control de Multas", "Historial de penalidades y registro de cobranzas");
        }
    }

    // --- VISTA DASHBOARD OPERATIVO ---
    private JPanel crearVistaDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // KPIs en fila superior
        JPanel panelKpis = new JPanel(new GridLayout(1, 4, 16, 0));
        panelKpis.setOpaque(false);

        kpiPrestamosActivos = new TarjetaMetrica("Préstamos Activos", "0", Iconos.crearPrestamo(26, Tema.COLOR_PRIMARIO), Tema.COLOR_PRIMARIO);
        kpiPrestamosVencidos = new TarjetaMetrica("Préstamos Vencidos", "0", Iconos.crearEliminar(26, Tema.COLOR_PELIGRO), Tema.COLOR_PELIGRO);
        kpiMultasPendientes = new TarjetaMetrica("Multas Pendientes", "S/ 0.00", Iconos.crearMulta(26, Tema.COLOR_ADVERTENCIA_OSCURO), Tema.COLOR_ADVERTENCIA_OSCURO);
        kpiReservasEnEspera = new TarjetaMetrica("Reservas en Cola", "0", Iconos.crearReserva(26, Tema.COLOR_INFO), Tema.COLOR_INFO);

        panelKpis.add(kpiPrestamosActivos);
        panelKpis.add(kpiPrestamosVencidos);
        panelKpis.add(kpiMultasPendientes);
        panelKpis.add(kpiReservasEnEspera);

        panel.add(panelKpis, BorderLayout.NORTH);

        // Acciones Rápidas en Tarjeta
        TarjetaPanel tarjetaAcciones = new TarjetaPanel(new BorderLayout(15, 15));
        JLabel lblTit = new JLabel("Acciones Rápidas del Mostrador");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblDesc = new JLabel("<html><body style='width: 650px; color: #64748B; font-size: 13px; line-height: 1.5;'>" +
                "Seleccione la operación que desea realizar a continuación para acceder directamente al módulo correspondiente." +
                "</body></html>");

        tarjetaAcciones.add(lblTit, BorderLayout.NORTH);
        tarjetaAcciones.add(lblDesc, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        panelBotones.setOpaque(false);

        BotonPrimario btnIrPrestar = new BotonPrimario("Nuevo Préstamo", Iconos.crearPrestamo(16, Color.WHITE));
        btnIrPrestar.addActionListener(e -> barraLateral.seleccionar("PRESTAMOS"));

        BotonAcento btnIrDevolver = new BotonAcento("Recepción de Ejemplar", Iconos.crearDevolucion(16, Color.WHITE));
        btnIrDevolver.addActionListener(e -> barraLateral.seleccionar("DEVOLUCIONES"));

        BotonSecundario btnIrCliente = new BotonSecundario("Registrar Nuevo Lector", Iconos.crearUsuario(16, Tema.COLOR_PRIMARIO));
        btnIrCliente.addActionListener(e -> barraLateral.seleccionar("CLIENTES"));

        BotonSecundario btnIrMultas = new BotonSecundario("Cobrar Multas", Iconos.crearMulta(16, Tema.COLOR_PRIMARIO));
        btnIrMultas.addActionListener(e -> barraLateral.seleccionar("MULTAS"));

        panelBotones.add(btnIrPrestar);
        panelBotones.add(btnIrDevolver);
        panelBotones.add(btnIrCliente);
        panelBotones.add(btnIrMultas);
        tarjetaAcciones.add(panelBotones, BorderLayout.SOUTH);

        panel.add(tarjetaAcciones, BorderLayout.CENTER);
        return panel;
    }

    // --- VISTA CLIENTES ---
    private JPanel crearVistaClientes() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Formulario Clientes en Tarjeta
        TarjetaPanel tarjetaForm = new TarjetaPanel(new BorderLayout(0, 10));
        lblClienteModo = new JLabel("Modo: Nuevo Lector (Seleccione una fila para editar o eliminar)");
        lblClienteModo.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblClienteModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        tarjetaForm.add(lblClienteModo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(1, 5, 12, 0));
        panelCampos.setOpaque(false);

        panelCampos.add(crearCampo("DNI (8 dígitos):", txtClienteDni = new CampoTexto("8 dígitos")));
        panelCampos.add(crearCampo("Nombres:", txtClienteNombres = new CampoTexto("Nombres del lector")));
        panelCampos.add(crearCampo("Apellidos:", txtClienteApellidos = new CampoTexto("Apellidos")));
        panelCampos.add(crearCampo("Correo Electrónico:", txtClienteEmail = new CampoTexto("correo@ejemplo.com")));
        panelCampos.add(crearCampo("Teléfono:", txtClienteTelefono = new CampoTexto("9 dígitos")));

        tarjetaForm.add(panelCampos, BorderLayout.CENTER);

        // Botones de acción del formulario
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        panelBotones.setOpaque(false);

        btnClienteLimpiar = new BotonSecundario("Limpiar");
        btnClienteToggleActivo = new BotonSecundario("Activar / Desactivar");
        btnClienteRestablecerClave = new BotonSecundario("Restablecer Clave");
        btnClienteEliminar = new BotonPeligro("Eliminar Lector", Iconos.crearEliminar(16, Color.WHITE));
        btnClienteActualizar = new BotonAcento("Guardar Cambios", Iconos.crearCheck(16, Color.WHITE));
        btnClienteRegistrar = new BotonPrimario("Registrar Lector", Iconos.crearMas(16, Color.WHITE));

        panelBotones.add(btnClienteLimpiar);
        panelBotones.add(btnClienteToggleActivo);
        panelBotones.add(btnClienteRestablecerClave);
        panelBotones.add(btnClienteEliminar);
        panelBotones.add(btnClienteActualizar);
        panelBotones.add(btnClienteRegistrar);
        tarjetaForm.add(panelBotones, BorderLayout.SOUTH);

        panel.add(tarjetaForm, BorderLayout.NORTH);

        // Tabla de Clientes en Tarjeta con Barra de Búsqueda
        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));

        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelBuscar.setOpaque(false);
        panelBuscar.add(new JLabel("Buscar Lector:"));
        txtClienteBuscar = new CampoTexto("DNI, Nombres o Apellidos", 22);
        btnClienteBuscar = new BotonSecundario("Buscar", Iconos.crearBuscar(16, Tema.COLOR_PRIMARIO));
        BotonSecundario btnVerTodos = new BotonSecundario("Mostrar Todos");

        panelBuscar.add(txtClienteBuscar);
        panelBuscar.add(btnClienteBuscar);
        panelBuscar.add(btnVerTodos);
        tarjetaTabla.add(panelBuscar, BorderLayout.NORTH);

        modeloClientes = new DefaultTableModel(new String[]{"ID", "DNI", "Nombres", "Apellidos", "Email", "Teléfono", "Fecha Registro"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaClientes = new TablaEstilizada(modeloClientes);
        tablaClientes.setMensajeVacio("No se encontraron lectores registrados.");
        tarjetaTabla.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        panel.add(tarjetaTabla, BorderLayout.CENTER);

        // Eventos
        btnClienteRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.registrarCliente(txtClienteDni.getText(), txtClienteNombres.getText(),
                        txtClienteApellidos.getText(), txtClienteEmail.getText(), txtClienteTelefono.getText());
            }
        });

        btnClienteActualizar.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un lector de la tabla para editar sus datos.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.editarCliente(clienteSeleccionadoId, txtClienteDni.getText(), txtClienteNombres.getText(),
                        txtClienteApellidos.getText(), txtClienteEmail.getText(), txtClienteTelefono.getText());
            }
        });

        btnClienteEliminar.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un lector de la tabla para eliminar.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.eliminarCliente(clienteSeleccionadoId);
            }
        });

        btnClienteRestablecerClave.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un lector de la tabla para restablecer su clave.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.restablecerClaveCliente(clienteSeleccionadoId);
            }
        });

        btnClienteToggleActivo.addActionListener(e -> {
            if (clienteSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un lector de la tabla.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
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

                    lblClienteModo.setText("Modo: Editando Lector ID #" + clienteSeleccionadoId + " (" + txtClienteNombres.getText() + " " + txtClienteApellidos.getText() + ")");
                    lblClienteModo.setForeground(Tema.COLOR_PRIMARIO);
                }
            }
        });

        return panel;
    }

    // --- VISTA PRÉSTAMOS ---
    private JPanel crearVistaPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Formulario Nuevo Préstamo
        TarjetaPanel tarjetaForm = new TarjetaPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        tarjetaForm.add(new JLabel("DNI del Lector:"));
        txtPrestamoDni = new CampoTexto("8 dígitos", 12);
        tarjetaForm.add(txtPrestamoDni);

        tarjetaForm.add(new JLabel("Código de Barras del Ejemplar:"));
        txtPrestamoCodigoBarras = new CampoTexto("Ej. L0001-001", 15);
        tarjetaForm.add(txtPrestamoCodigoBarras);

        tarjetaForm.add(new JLabel("Plazo de Devolución:"));
        comboDiasPrestamo = new JComboBox<>(new String[]{
                "1 día (Alta demanda / Express)",
                "3 días (Fin de semana / Reserva)",
                "7 días (Estándar recomendado)",
                "14 días (Investigación)",
                "Por defecto del sistema"
        });
        comboDiasPrestamo.setFont(Tema.FUENTE_NORMAL);
        comboDiasPrestamo.setSelectedIndex(2); // 7 días por defecto
        tarjetaForm.add(comboDiasPrestamo);

        btnPrestamoRegistrar = new BotonPrimario("Emitir Préstamo", Iconos.crearPrestamo(16, Color.WHITE));
        tarjetaForm.add(btnPrestamoRegistrar);
        panel.add(tarjetaForm, BorderLayout.NORTH);

        // Tabla de Préstamos Activos y Vencidos
        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        JPanel panelHeaderP = new JPanel(new BorderLayout());
        panelHeaderP.setOpaque(false);

        JLabel lblTitP = new JLabel("Préstamos en Curso y Vencimientos");
        lblTitP.setFont(Tema.FUENTE_SECCION);
        panelHeaderP.add(lblTitP, BorderLayout.WEST);

        btnPrestamoRenovar = new BotonAcento("Renovar Préstamo Seleccionado", Iconos.crearActualizar(16, Color.WHITE));
        panelHeaderP.add(btnPrestamoRenovar, BorderLayout.EAST);
        tarjetaTabla.add(panelHeaderP, BorderLayout.NORTH);

        modeloPrestamos = new DefaultTableModel(new String[]{"ID", "Cliente ID", "Ejemplar ID", "Fecha Préstamo", "Fecha Vencimiento", "Estado", "Renovaciones"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPrestamos = new TablaEstilizada(modeloPrestamos);
        tablaPrestamos.setMensajeVacio("No hay préstamos activos ni vencidos en este momento.");
        tarjetaTabla.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        panel.add(tarjetaTabla, BorderLayout.CENTER);

        btnPrestamoRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.registrarPrestamo(
                        txtPrestamoDni.getText(),
                        txtPrestamoCodigoBarras.getText(),
                        getDiasPrestamoSeleccionados()
                );
            }
        });

        btnPrestamoRenovar.addActionListener(e -> {
            int row = tablaPrestamos.getSelectedRow();
            if (row >= 0 && controller != null) {
                int prestamoId = (int) modeloPrestamos.getValueAt(row, 0);
                controller.renovarPrestamo(prestamoId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un préstamo de la tabla para renovarlo.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    // --- VISTA DEVOLUCIONES ---
    private JPanel crearVistaDevoluciones() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        TarjetaPanel tarjetaRecep = new TarjetaPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        tarjetaRecep.add(new JLabel("Código de Barras del Ejemplar devuelto:"));
        txtDevolucionCodigoBarras = new CampoTexto("Ej. L0001-001", 18);
        btnDevolucionProcesar = new BotonAcento("Registrar Devolución", Iconos.crearDevolucion(16, Color.WHITE));

        tarjetaRecep.add(txtDevolucionCodigoBarras);
        tarjetaRecep.add(btnDevolucionProcesar);
        panel.add(tarjetaRecep, BorderLayout.NORTH);

        // Tarjeta con comprobante de recepción
        TarjetaPanel tarjetaResultado = new TarjetaPanel(new BorderLayout(15, 15));
        JLabel lblTitComp = new JLabel("Comprobante de Devolución e Inspección");
        lblTitComp.setFont(Tema.FUENTE_SUBTITULO);
        tarjetaResultado.add(lblTitComp, BorderLayout.NORTH);

        lblDevolucionResultado = new JLabel("<html><body style='color:#64748B; font-size:13px;'>" +
                "<i>Escanee o ingrese el código de barras del libro para validar la fecha de vencimiento y registrar la devolución en el sistema.</i>" +
                "</body></html>");
        tarjetaResultado.add(lblDevolucionResultado, BorderLayout.CENTER);

        JPanel panelBotonCobro = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotonCobro.setOpaque(false);
        btnDevolucionCobrarMulta = new BotonPeligro("Cobrar Multa de Inmediato", Iconos.crearMulta(16, Color.WHITE));
        btnDevolucionCobrarMulta.setVisible(false);
        panelBotonCobro.add(btnDevolucionCobrarMulta);
        tarjetaResultado.add(panelBotonCobro, BorderLayout.SOUTH);

        panel.add(tarjetaResultado, BorderLayout.CENTER);

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

    // --- VISTA CATÁLOGO ---
    private JPanel crearVistaCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjetaBuscar = new TarjetaPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        tarjetaBuscar.add(new JLabel("Buscar Libro:"));
        txtCatalogoBuscar = new CampoTexto("Título, Autor o ISBN", 25);
        BotonSecundario btnBuscar = new BotonSecundario("Buscar", Iconos.crearBuscar(16, Tema.COLOR_PRIMARIO));
        BotonSecundario btnTodos = new BotonSecundario("Mostrar Todos");

        tarjetaBuscar.add(txtCatalogoBuscar);
        tarjetaBuscar.add(btnBuscar);
        tarjetaBuscar.add(btnTodos);
        panel.add(tarjetaBuscar, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        TarjetaPanel tarjetaLibros = new TarjetaPanel(new BorderLayout(0, 8));
        JLabel lblL = new JLabel("Títulos Disponibles en Biblioteca");
        lblL.setFont(Tema.FUENTE_SECCION);
        tarjetaLibros.add(lblL, BorderLayout.NORTH);

        modeloCatLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatLibros = new TablaEstilizada(modeloCatLibros);
        tablaCatLibros.setMensajeVacio("No se encontraron libros con el criterio especificado.");
        tarjetaLibros.add(new JScrollPane(tablaCatLibros), BorderLayout.CENTER);
        splitPane.setTopComponent(tarjetaLibros);

        TarjetaPanel tarjetaEj = new TarjetaPanel(new BorderLayout(0, 8));
        JPanel panelHeaderEj = new JPanel(new BorderLayout(10, 0));
        panelHeaderEj.setOpaque(false);

        JPanel panelTitEj = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTitEj.setOpaque(false);
        lblCatEjTit = new JLabel("Copias Físicas");
        lblCatEjTit.setFont(Tema.FUENTE_SECCION);
        lblCatEjTit.setForeground(Tema.COLOR_TEXTO);
        lblCatEjDetalle = new JLabel("(Seleccione un libro para ver sus ejemplares)");
        lblCatEjDetalle.setFont(Tema.FUENTE_PEQUENA);
        lblCatEjDetalle.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        panelTitEj.add(lblCatEjTit);
        panelTitEj.add(lblCatEjDetalle);
        panelHeaderEj.add(panelTitEj, BorderLayout.WEST);

        JPanel panelAccionesEj = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAccionesEj.setOpaque(false);
        txtCatEjemplarBuscar = new CampoTexto("Filtrar copias...", 15);
        btnCatAgregarEjemplares = new BotonPrimario("Agregar Copias", Iconos.crearMas(16, Color.WHITE));
        btnCatCambiarEstado = new BotonSecundario("Cambiar Estado");
        btnCatEliminarEjemplar = new BotonPeligro("Eliminar Ejemplar");

        panelAccionesEj.add(txtCatEjemplarBuscar);
        panelAccionesEj.add(btnCatAgregarEjemplares);
        panelAccionesEj.add(btnCatCambiarEstado);
        panelAccionesEj.add(btnCatEliminarEjemplar);
        panelHeaderEj.add(panelAccionesEj, BorderLayout.EAST);
        tarjetaEj.add(panelHeaderEj, BorderLayout.NORTH);

        modeloCatEjemplares = new DefaultTableModel(new String[]{"ID Ejemplar", "Código de Barras", "Estado Físico"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatEjemplares = new TablaEstilizada(modeloCatEjemplares);
        tablaCatEjemplares.setMensajeVacio("Seleccione un libro para listar sus ejemplares.");
        tarjetaEj.add(new JScrollPane(tablaCatEjemplares), BorderLayout.CENTER);
        splitPane.setBottomComponent(tarjetaEj);

        panel.add(splitPane, BorderLayout.CENTER);

        Runnable runBuscarCat = () -> {
            if (controller != null) controller.buscarCatalogo(txtCatalogoBuscar.getText());
        };
        btnBuscar.addActionListener(e -> runBuscarCat.run());
        txtCatalogoBuscar.addActionListener(e -> runBuscarCat.run());

        btnTodos.addActionListener(e -> {
            txtCatalogoBuscar.setText("");
            if (controller != null) controller.cargarCatalogo();
        });

        txtCatalogoBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkVacio(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkVacio(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkVacio(); }
            private void checkVacio() {
                if (txtCatalogoBuscar.getText().trim().isEmpty() && controller != null) {
                    controller.cargarCatalogo();
                }
            }
        });

        txtCatEjemplarBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarCatEjemplares(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarCatEjemplares(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarCatEjemplares(); }
        });

        tablaCatLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tablaCatLibros.getSelectedRow();
                if (row >= 0 && controller != null) {
                    catLibroSeleccionadoId = (int) modeloCatLibros.getValueAt(row, 0);
                    String tit = modeloCatLibros.getValueAt(row, 2).toString();
                    if (lblCatEjTit != null) {
                        lblCatEjTit.setText("Ejemplares: " + (tit.length() > 25 ? tit.substring(0, 22) + "..." : tit));
                    }
                    controller.cargarEjemplaresDeLibro(catLibroSeleccionadoId);
                }
            }
        });

        btnCatAgregarEjemplares.addActionListener(e -> {
            if (catLibroSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione primero un libro de la tabla superior.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            String cantStr = JOptionPane.showInputDialog(this, "¿Cuántos ejemplares desea agregar al libro seleccionado?", "1");
            if (cantStr != null && !cantStr.trim().isEmpty() && controller != null) {
                try {
                    int cant = Integer.parseInt(cantStr.trim());
                    if (cant <= 0) throw new NumberFormatException();
                    controller.agregarEjemplares(catLibroSeleccionadoId, cant);
                } catch (NumberFormatException ex) {
                    DialogoModerno.mostrarMensaje(this, "Debe ingresar un número entero positivo mayor a 0.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
                }
            }
        });

        btnCatCambiarEstado.addActionListener(e -> {
            int row = tablaCatEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && catLibroSeleccionadoId != null) {
                int ejemplarId = (int) modeloCatEjemplares.getValueAt(row, 0);
                controller.cambiarEstadoEjemplarDialog(ejemplarId, catLibroSeleccionadoId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un ejemplar de la tabla inferior.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        btnCatEliminarEjemplar.addActionListener(e -> {
            int row = tablaCatEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && catLibroSeleccionadoId != null) {
                int ejemplarId = (int) modeloCatEjemplares.getValueAt(row, 0);
                controller.eliminarEjemplar(ejemplarId, catLibroSeleccionadoId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un ejemplar de la tabla inferior.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    // --- VISTA RESERVAS ---
    private JPanel crearVistaReservas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjetaForm = new TarjetaPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        tarjetaForm.add(new JLabel("DNI del Lector:"));
        txtReservaDni = new CampoTexto("8 dígitos", 10);
        tarjetaForm.add(txtReservaDni);

        tarjetaForm.add(new JLabel("ID del Libro:"));
        txtReservaLibroId = new CampoTexto("ID", 6);
        tarjetaForm.add(txtReservaLibroId);

        btnReservaCrear = new BotonPrimario("Asignar Turno en Cola (FIFO)", Iconos.crearReserva(16, Color.WHITE));
        BotonSecundario btnRefrescar = new BotonSecundario("Actualizar");

        tarjetaForm.add(btnReservaCrear);
        tarjetaForm.add(btnRefrescar);
        panel.add(tarjetaForm, BorderLayout.NORTH);

        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        JPanel panelHeaderRes = new JPanel(new BorderLayout());
        panelHeaderRes.setOpaque(false);

        JLabel lblTitR = new JLabel("Cola de Espera y Reservas Asignadas");
        lblTitR.setFont(Tema.FUENTE_SECCION);
        panelHeaderRes.add(lblTitR, BorderLayout.WEST);

        btnReservaCancelar = new BotonPeligro("Cancelar Reserva Seleccionada", Iconos.crearEliminar(16, Color.WHITE));
        panelHeaderRes.add(btnReservaCancelar, BorderLayout.EAST);
        tarjetaTabla.add(panelHeaderRes, BorderLayout.NORTH);

        modeloReservas = new DefaultTableModel(new String[]{"ID", "Cliente ID", "Libro ID", "Ejemplar Asignado", "Fecha Reserva", "Fecha Expiración", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new TablaEstilizada(modeloReservas);
        tablaReservas.setMensajeVacio("No hay reservas activas en este momento.");
        tarjetaTabla.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        panel.add(tarjetaTabla, BorderLayout.CENTER);

        btnReservaCrear.addActionListener(e -> {
            if (controller != null) controller.crearReserva(txtReservaDni.getText(), txtReservaLibroId.getText());
        });

        btnReservaCancelar.addActionListener(e -> {
            int row = tablaReservas.getSelectedRow();
            if (row >= 0 && controller != null) {
                int reservaId = (int) modeloReservas.getValueAt(row, 0);
                controller.cancelarReserva(reservaId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione una reserva de la tabla para cancelarla.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        btnRefrescar.addActionListener(e -> {
            if (controller != null) controller.cargarReservas();
        });

        return panel;
    }

    // --- VISTA MULTAS ---
    private JPanel crearVistaMultas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjetaFiltro = new TarjetaPanel(new BorderLayout(15, 0));
        JPanel pFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        pFiltro.setOpaque(false);
        chkSoloPendientes = new JCheckBox("Mostrar únicamente multas pendientes de pago", true);
        chkSoloPendientes.setFont(Tema.FUENTE_NORMAL);
        BotonSecundario btnRefrescar = new BotonSecundario("Actualizar");

        pFiltro.add(chkSoloPendientes);
        pFiltro.add(btnRefrescar);
        tarjetaFiltro.add(pFiltro, BorderLayout.WEST);

        btnMultaCobrar = new BotonAcento("Cobrar Multa Seleccionada", Iconos.crearMulta(16, Color.WHITE));
        tarjetaFiltro.add(btnMultaCobrar, BorderLayout.EAST);
        panel.add(tarjetaFiltro, BorderLayout.NORTH);

        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        modeloMultas = new DefaultTableModel(new String[]{"ID Multa", "ID Préstamo", "Días Retraso", "Tarifa/Día", "Total", "Estado", "Fecha Pago"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMultas = new TablaEstilizada(modeloMultas);
        tablaMultas.setMensajeVacio("No hay multas registradas con el filtro actual.");
        tarjetaTabla.add(new JScrollPane(tablaMultas), BorderLayout.CENTER);
        panel.add(tarjetaTabla, BorderLayout.CENTER);

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
                DialogoModerno.mostrarMensaje(this, "Seleccione una multa de la tabla para registrar su cobro.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    private JPanel crearCampo(String etiqueta, Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setFont(Tema.FUENTE_PEQUENA_BOLD);
        l.setForeground(Tema.COLOR_TEXTO);
        p.add(l, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    // --- MÉTODOS PÚBLICOS DE ACTUALIZACIÓN (COMPATIBILIDAD CON CONTROLLER) ---
    public void cargarTablaClientes(List<Cliente> clientes) {
        modeloClientes.setRowCount(0);
        for (Cliente c : clientes) {
            modeloClientes.addRow(new Object[]{
                    c.getId(), c.getDni(), c.getNombres(), c.getApellidos(), c.getEmail(), c.getTelefono(), c.getFechaRegistro()
            });
        }
        if (tablaClientes.getColumnCount() >= 7) {
            tablaClientes.fijarAnchoColumna(0, 45, 50, 60);    // ID
            tablaClientes.fijarAnchoColumna(1, 80, 95, 110);   // DNI
            tablaClientes.fijarAnchoColumna(2, 130, 160, 200); // Nombres
            tablaClientes.fijarAnchoColumna(3, 130, 160, 200); // Apellidos
            tablaClientes.fijarAnchoColumna(4, 160, 200, 0);   // Email
            tablaClientes.fijarAnchoColumna(5, 90, 105, 120);  // Teléfono
            tablaClientes.fijarAnchoColumna(6, 95, 110, 125);  // Fecha Registro
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
        configurarColumnasCatLibros();
    }

    private void configurarColumnasCatLibros() {
        if (tablaCatLibros.getColumnCount() >= 7) {
            tablaCatLibros.fijarAnchoColumna(0, 45, 50, 60);    // ID
            tablaCatLibros.fijarAnchoColumna(1, 120, 135, 150); // ISBN
            tablaCatLibros.fijarAnchoColumna(2, 200, 270, 0);   // Título
            tablaCatLibros.fijarAnchoColumna(3, 140, 170, 220); // Autor
            tablaCatLibros.fijarAnchoColumna(4, 100, 120, 150); // Categoría
            tablaCatLibros.fijarAnchoColumna(5, 110, 130, 160); // Editorial
            tablaCatLibros.fijarAnchoColumna(6, 75, 85, 105);   // Disponibles
        }
    }

    public void cargarTablaCatEjemplares(List<Ejemplar> ejemplares) {
        this.catEjemplaresActuales = ejemplares != null ? new java.util.ArrayList<>(ejemplares) : new java.util.ArrayList<>();
        filtrarCatEjemplares();
    }

    private void filtrarCatEjemplares() {
        String f = txtCatEjemplarBuscar != null ? txtCatEjemplarBuscar.getText().trim().toLowerCase() : "";
        modeloCatEjemplares.setRowCount(0);
        int disp = 0;
        for (Ejemplar ej : catEjemplaresActuales) {
            if (ej.getEstado() == EstadoEjemplar.DISPONIBLE) disp++;
            if (f.isEmpty() ||
                ej.getCodigoBarras().toLowerCase().contains(f) ||
                ej.getEstado().name().toLowerCase().contains(f) ||
                String.valueOf(ej.getId()).contains(f)) {
                modeloCatEjemplares.addRow(new Object[]{
                        ej.getId(), ej.getCodigoBarras(), ej.getEstado()
                });
            }
        }
        if (lblCatEjDetalle != null && catLibroSeleccionadoId != null) {
            lblCatEjDetalle.setText("(" + catEjemplaresActuales.size() + " copias | " + disp + " disponibles)");
        }
        configurarColumnasCatEjemplares();
    }

    private void configurarColumnasCatEjemplares() {
        if (tablaCatEjemplares.getColumnCount() >= 3) {
            tablaCatEjemplares.fijarAnchoColumna(0, 60, 80, 100);  // ID Ejemplar
            tablaCatEjemplares.fijarAnchoColumna(1, 150, 220, 0);  // Código de Barras
            tablaCatEjemplares.fijarAnchoColumna(2, 120, 150, 180);// Estado Físico
        }
    }

    public void cargarTablaPrestamos(List<Prestamo> prestamos) {
        modeloPrestamos.setRowCount(0);
        int activos = 0;
        int vencidos = 0;
        for (Prestamo p : prestamos) {
            modeloPrestamos.addRow(new Object[]{
                    p.getId(), p.getClienteId(), p.getEjemplarId(), p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado(), p.getRenovaciones()
            });
            if (p.getEstado() == EstadoPrestamo.ACTIVO) activos++;
            if (p.getEstado() == EstadoPrestamo.VENCIDO) vencidos++;
        }
        if (kpiPrestamosActivos != null) kpiPrestamosActivos.setValor(String.valueOf(activos));
        if (kpiPrestamosVencidos != null) kpiPrestamosVencidos.setValor(String.valueOf(vencidos));
        if (tablaPrestamos.getColumnCount() >= 7) {
            tablaPrestamos.fijarAnchoColumna(0, 45, 50, 60);   // ID
            tablaPrestamos.fijarAnchoColumna(1, 75, 85, 100);  // Cliente ID
            tablaPrestamos.fijarAnchoColumna(2, 75, 85, 100);  // Ejemplar ID
            tablaPrestamos.fijarAnchoColumna(3, 100, 120, 135);// Fecha Préstamo
            tablaPrestamos.fijarAnchoColumna(4, 100, 120, 135);// Fecha Vencimiento
            tablaPrestamos.fijarAnchoColumna(5, 100, 120, 140);// Estado
            tablaPrestamos.fijarAnchoColumna(6, 80, 95, 110);  // Renovaciones
        }
    }

    public void cargarTablaReservas(List<Reserva> reservas) {
        modeloReservas.setRowCount(0);
        int pendientes = 0;
        for (Reserva r : reservas) {
            modeloReservas.addRow(new Object[]{
                    r.getId(), r.getClienteId(), r.getLibroId(), r.getEjemplarId() != null ? r.getEjemplarId() : "-",
                    r.getFechaReserva(), r.getFechaExpiracion() != null ? r.getFechaExpiracion() : "-", r.getEstado()
            });
            if (r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.LISTA) pendientes++;
        }
        if (kpiReservasEnEspera != null) kpiReservasEnEspera.setValor(String.valueOf(pendientes));
        if (tablaReservas.getColumnCount() >= 7) {
            tablaReservas.fijarAnchoColumna(0, 45, 50, 60);   // ID
            tablaReservas.fijarAnchoColumna(1, 75, 85, 100);  // Cliente ID
            tablaReservas.fijarAnchoColumna(2, 75, 85, 100);  // Libro ID
            tablaReservas.fijarAnchoColumna(3, 75, 85, 100);  // Ejemplar ID
            tablaReservas.fijarAnchoColumna(4, 100, 120, 135);// Fecha Reserva
            tablaReservas.fijarAnchoColumna(5, 100, 120, 135);// Fecha Expiración
            tablaReservas.fijarAnchoColumna(6, 100, 120, 140);// Estado
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
            if (m.getEstado() == EstadoMulta.PENDIENTE) {
                totalPendiente = totalPendiente.add(m.getMonto());
            }
        }
        if (kpiMultasPendientes != null) kpiMultasPendientes.setValor("S/ " + totalPendiente.toPlainString());
        if (tablaMultas.getColumnCount() >= 7) {
            tablaMultas.fijarAnchoColumna(0, 45, 50, 60);   // ID
            tablaMultas.fijarAnchoColumna(1, 75, 85, 100);  // Préstamo ID
            tablaMultas.fijarAnchoColumna(2, 80, 95, 110);  // Días Retraso
            tablaMultas.fijarAnchoColumna(3, 90, 110, 130); // Tarifa
            tablaMultas.fijarAnchoColumna(4, 90, 110, 130); // Monto
            tablaMultas.fijarAnchoColumna(5, 100, 120, 140);// Estado
            tablaMultas.fijarAnchoColumna(6, 100, 120, 140);// Fecha Pago
        }
    }

    public void mostrarResultadoDevolucion(PrestamoService.ResultadoDevolucion resultado) {
        StringBuilder sb = new StringBuilder("<html><div style='padding:5px; font-family: Segoe UI, sans-serif;'>");
        sb.append("<h3 style='color:#2E7D32; margin-bottom:5px;'>Devolución Procesada con Éxito</h3>");
        sb.append("<b>ID Préstamo:</b> ").append(resultado.getPrestamo().getId()).append("<br/>");
        sb.append("<b>Fecha de Devolución:</b> ").append(resultado.getPrestamo().getFechaDevolucion()).append("<br/><br/>");

        if (resultado.tieneMulta()) {
            sb.append("<div style='background:#FFEBEE; border:1px solid #EF9A9A; padding:8px; border-radius:6px;'>");
            sb.append("<b style='color:#C62828;'>Mora Generada por retraso:</b> S/ ").append(resultado.getMulta().getMonto()).append("<br/>");
            sb.append("<b>Días de retraso:</b> ").append(resultado.getMulta().getDiasRetraso()).append(" día(s)<br/>");
            sb.append("</div>");
            ultimaMultaIdGenerada = resultado.getMulta().getId();
            btnDevolucionCobrarMulta.setVisible(true);
        } else {
            sb.append("<div style='background:#E8F5E9; border:1px solid #A5D6A7; padding:8px; border-radius:6px;'>");
            sb.append("<b style='color:#2E7D32;'>Devolución a tiempo - Sin multas ni penalidades aplicadas.</b>");
            sb.append("</div>");
            btnDevolucionCobrarMulta.setVisible(false);
            ultimaMultaIdGenerada = null;
        }

        sb.append("<br/>");
        if (resultado.isAsignadoAReserva()) {
            sb.append("<b style='color:#0288D1;'>El ejemplar devuelto fue asignado automáticamente a la primera reserva en cola FIFO.</b>");
        } else {
            sb.append("<span style='color:#64748B;'>El ejemplar quedó disponible de inmediato en el catálogo para nuevos préstamos.</span>");
        }

        sb.append("</div></html>");
        lblDevolucionResultado.setText(sb.toString());
        txtDevolucionCodigoBarras.setText("");
    }

    public void mostrarModalClaveTemporal(String dni, String nombre, String claveTemporal) {
        String mensaje = "Se ha configurado la cuenta de acceso para el lector:<br/><br/>" +
                "<b>Nombre:</b> " + nombre + "<br/>" +
                "<b>Usuario (DNI):</b> " + dni + "<br/>" +
                "<b>Contraseña Temporal:</b> <span style='color:#3F51B5; font-size:15px; font-weight:bold;'>" + claveTemporal + "</span><br/><br/>" +
                "<i>El lector estará obligado a cambiar esta contraseña en su primer inicio de sesión.</i>";

        DialogoModerno.mostrarMensaje(this, mensaje, "Credenciales de Lector", DialogoModerno.TipoDialogo.EXITO);
    }

    public void limpiarFormCliente() {
        clienteSeleccionadoId = null;
        txtClienteDni.setText("");
        txtClienteNombres.setText("");
        txtClienteApellidos.setText("");
        txtClienteEmail.setText("");
        txtClienteTelefono.setText("");
        tablaClientes.clearSelection();
        lblClienteModo.setText("Modo: Nuevo Lector (Seleccione una fila para editar o eliminar)");
        lblClienteModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
    }

    public Integer getDiasPrestamoSeleccionados() {
        if (comboDiasPrestamo == null) return null;
        int idx = comboDiasPrestamo.getSelectedIndex();
        return switch (idx) {
            case 0 -> 1;
            case 1 -> 3;
            case 2 -> 7;
            case 3 -> 14;
            default -> null;
        };
    }

    public void limpiarCamposPrestamo() {
        if (txtPrestamoDni != null) txtPrestamoDni.setText("");
        if (txtPrestamoCodigoBarras != null) txtPrestamoCodigoBarras.setText("");
        if (comboDiasPrestamo != null) comboDiasPrestamo.setSelectedIndex(2);
    }

    public void mostrarMensaje(String msj) {
        DialogoModerno.mostrarMensaje(this, msj, "Éxito", DialogoModerno.TipoDialogo.EXITO);
    }

    public void mostrarError(String error) {
        DialogoModerno.mostrarMensaje(this, error, "Error", DialogoModerno.TipoDialogo.ERROR);
    }

    public void mostrarAdvertencia(String adv) {
        DialogoModerno.mostrarMensaje(this, adv, "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
    }
}
