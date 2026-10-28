package com.biblioteca.view;

import com.biblioteca.controller.AdminController;
import com.biblioteca.model.Ejemplar;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Usuario;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal rediseñada para el rol ADMINISTRADOR.
 * Dispone de barra lateral de navegación con CardLayout, cabecera moderna,
 * dashboard con métricas clave y formularios estilizados en tarjetas.
 */
public class AdminView extends JFrame {

    private AdminController controller;

    // Componentes de Estructura
    private BarraNavegacionLateral barraLateral;
    private BarraSuperiorModerna barraSuperior;
    private JPanel panelContenedorTarjetas;
    private CardLayout cardLayout;

    // Métricas del Dashboard
    private TarjetaMetrica kpiLibros;
    private TarjetaMetrica kpiEjemplares;
    private TarjetaMetrica kpiBibliotecarios;
    private TarjetaMetrica kpiTarifaMulta;

    // Pestaña Bibliotecarios
    private Integer biblioSeleccionadoId = null;
    private JLabel lblBiblioModo;
    private TablaEstilizada tablaBibliotecarios;
    private DefaultTableModel modeloBibliotecarios;
    private CampoTexto txtBiblioUsername;
    private CampoTexto txtBiblioNombre;
    private CampoPassword txtBiblioPassword;
    private BotonPrimario btnBiblioRegistrar;
    private BotonAcento btnBiblioActualizar;
    private BotonPeligro btnBiblioEliminar;
    private BotonSecundario btnBiblioRestablecerClave;
    private BotonSecundario btnBiblioToggleActivo;
    private BotonSecundario btnBiblioLimpiar;

    // Pestaña Catálogo
    private Integer libroSeleccionadoId = null;
    private JLabel lblLibroModo;
    private JPanel panelCuerpoFormLibro;
    private BotonSecundario btnToggleFormLibro;
    private TablaEstilizada tablaLibros;
    private DefaultTableModel modeloLibros;
    private JLabel lblLibrosCount;
    private CampoTexto txtLibroBuscar;
    private BotonSecundario btnLibroBuscar;
    private BotonSecundario btnLibroMostrarTodos;

    private TablaEstilizada tablaEjemplares;
    private DefaultTableModel modeloEjemplares;
    private JLabel lblEjTit;
    private JLabel lblEjDetalle;
    private CampoTexto txtEjemplarBuscar;
    private BotonPrimario btnEjemplarAgregar;
    private List<Ejemplar> ejemplaresActuales = new ArrayList<>();

    private CampoTexto txtLibroIsbn;
    private CampoTexto txtLibroTitulo;
    private CampoTexto txtLibroAutor;
    private CampoTexto txtLibroCategoria;
    private CampoTexto txtLibroEditorial;
    private JSpinner spinLibroAnio;
    private JSpinner spinEjemplaresIniciales;
    private BotonPrimario btnLibroRegistrar;
    private BotonAcento btnLibroActualizar;
    private BotonPeligro btnLibroEliminar;
    private BotonSecundario btnLibroLimpiar;
    private BotonSecundario btnLibroAgregarEjemplar;
    private BotonSecundario btnEjemplarCambiarEstado;
    private BotonPeligro btnEjemplarEliminar;

    // Pestaña Configuración
    private CampoTexto txtConfMultaPorDia;
    private CampoTexto txtConfDiasPrestamo;
    private CampoTexto txtConfMaxPrestamos;
    private CampoTexto txtConfDiasRetencion;
    private BotonAcento btnConfGuardar;

    // Pestaña Reportes
    private JComboBox<String> comboTipoReporte;
    private TablaEstilizada tablaReporte;
    private DefaultTableModel modeloReporte;
    private BotonAcento btnExportarExcel;
    private PanelGraficaReporte panelGraficaReporte;
    private PanelGraficaReporte panelGraficaReporteSplit;
    private TablaEstilizada tablaReporteSplit;

    public AdminView() {
        Tema.aplicar();
        initComponents();
    }

    public void setController(AdminController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Panel de Administración");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(Iconos.crearAppLogoImage(64));

        JPanel panelRaiz = new JPanel(new BorderLayout());
        panelRaiz.setBackground(Tema.COLOR_FONDO);

        // Barra Lateral de Navegación
        barraLateral = new BarraNavegacionLateral("Administrador", clave -> {
            if (cardLayout != null) {
                cardLayout.show(panelContenedorTarjetas, clave);
                actualizarTitulosSeccion(clave);
            }
        });

        barraLateral.agregarItem("DASHBOARD", "Inicio", Iconos.crearDashboard(20, Color.WHITE));
        barraLateral.agregarItem("BIBLIOTECARIOS", "Bibliotecarios", Iconos.crearUsuario(20, Color.WHITE));
        barraLateral.agregarItem("CATALOGO", "Catálogo y Copias", Iconos.crearLibro(20, Color.WHITE));
        barraLateral.agregarItem("CONFIGURACION", "Configuración", Iconos.crearConfiguracion(20, Color.WHITE));
        barraLateral.agregarItem("REPORTES", "Reportes Excel", Iconos.crearReportes(20, Color.WHITE));

        panelRaiz.add(barraLateral, BorderLayout.WEST);

        // Panel Central (Cabecera Superior + Contenedor de Vistas)
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(Tema.COLOR_FONDO);

        barraSuperior = new BarraSuperiorModerna(this, () -> {
            if (controller != null) {
                controller.cerrarSesion();
            }
        });
        panelCentral.add(barraSuperior, BorderLayout.NORTH);

        // Vistas en CardLayout
        cardLayout = new CardLayout();
        panelContenedorTarjetas = new JPanel(cardLayout);
        panelContenedorTarjetas.setOpaque(false);
        panelContenedorTarjetas.setBorder(new EmptyBorder(16, 24, 20, 24));

        panelContenedorTarjetas.add(crearVistaDashboard(), "DASHBOARD");
        panelContenedorTarjetas.add(crearVistaBibliotecarios(), "BIBLIOTECARIOS");
        panelContenedorTarjetas.add(crearVistaCatalogo(), "CATALOGO");
        panelContenedorTarjetas.add(crearVistaConfiguracion(), "CONFIGURACION");
        panelContenedorTarjetas.add(crearVistaReportes(), "REPORTES");

        panelCentral.add(panelContenedorTarjetas, BorderLayout.CENTER);
        panelRaiz.add(panelCentral, BorderLayout.CENTER);

        setContentPane(panelRaiz);
    }

    private void actualizarTitulosSeccion(String clave) {
        switch (clave) {
            case "DASHBOARD" -> barraSuperior.setSeccion("Panel General", "Resumen de métricas y estado del sistema");
            case "BIBLIOTECARIOS" -> barraSuperior.setSeccion("Gestión de Personal", "Administración de cuentas para bibliotecarios");
            case "CATALOGO" -> barraSuperior.setSeccion("Catálogo Bibliográfico", "Administración de títulos, autores y ejemplares físicos");
            case "CONFIGURACION" -> barraSuperior.setSeccion("Configuración Global", "Parámetros operativos de préstamos, moras y reservas");
            case "REPORTES" -> barraSuperior.setSeccion("Reportes y Auditoría", "Generación de datos y exportación a formato Excel (.xlsx)");
        }
    }

    // --- VISTA DASHBOARD ---
    private JPanel crearVistaDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Fila de Tarjetas Métricas (KPIs)
        JPanel panelKpis = new JPanel(new GridLayout(1, 4, 16, 0));
        panelKpis.setOpaque(false);

        kpiLibros = new TarjetaMetrica("Títulos en Catálogo", "0", Iconos.crearLibro(26, Tema.COLOR_PRIMARIO), Tema.COLOR_PRIMARIO);
        kpiEjemplares = new TarjetaMetrica("Ejemplares Físicos", "0", Iconos.crearPrestamo(26, Tema.COLOR_ACENTO), Tema.COLOR_ACENTO);
        kpiBibliotecarios = new TarjetaMetrica("Bibliotecarios", "0", Iconos.crearUsuario(26, Tema.COLOR_INFO), Tema.COLOR_INFO);
        kpiTarifaMulta = new TarjetaMetrica("Tarifa Mora / Día", "S/ 1.00", Iconos.crearMulta(26, Tema.COLOR_ADVERTENCIA_OSCURO), Tema.COLOR_ADVERTENCIA_OSCURO);

        panelKpis.add(kpiLibros);
        panelKpis.add(kpiEjemplares);
        panelKpis.add(kpiBibliotecarios);
        panelKpis.add(kpiTarifaMulta);

        panel.add(panelKpis, BorderLayout.NORTH);

        // Tarjeta Central Informativa / Acciones Rápidas
        TarjetaPanel panelBienvenida = new TarjetaPanel(new BorderLayout(15, 15));
        JLabel lblTituloDash = new JLabel("Bienvenido al Panel de Control de Administración");
        lblTituloDash.setFont(Tema.FUENTE_SUBTITULO);
        lblTituloDash.setForeground(Tema.COLOR_TEXTO);

        JLabel lblDescDash = new JLabel("<html><body style='width: 600px; color: #64748B; font-size: 13px; line-height: 1.5;'>" +
                "Desde esta consola centralizada puede supervisar el inventario bibliográfico completo, dar de alta nuevos bibliotecarios, " +
                "modificar los parámetros globales de moras y retención de reservas, y generar reportes ejecutivos en formato Excel." +
                "</body></html>");

        panelBienvenida.add(lblTituloDash, BorderLayout.NORTH);
        panelBienvenida.add(lblDescDash, BorderLayout.CENTER);

        // Accesos rápidos
        JPanel panelAccesos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelAccesos.setOpaque(false);

        BotonPrimario btnIrLibros = new BotonPrimario("Gestionar Libros", Iconos.crearLibro(16, Color.WHITE));
        btnIrLibros.addActionListener(e -> barraLateral.seleccionar("CATALOGO"));

        BotonSecundario btnIrBiblios = new BotonSecundario("Gestionar Bibliotecarios", Iconos.crearUsuario(16, Tema.COLOR_PRIMARIO));
        btnIrBiblios.addActionListener(e -> barraLateral.seleccionar("BIBLIOTECARIOS"));

        BotonSecundario btnIrReportes = new BotonSecundario("Ver Reportes", Iconos.crearReportes(16, Tema.COLOR_PRIMARIO));
        btnIrReportes.addActionListener(e -> barraLateral.seleccionar("REPORTES"));

        panelAccesos.add(btnIrLibros);
        panelAccesos.add(btnIrBiblios);
        panelAccesos.add(btnIrReportes);
        panelBienvenida.add(panelAccesos, BorderLayout.SOUTH);

        panel.add(panelBienvenida, BorderLayout.CENTER);
        return panel;
    }

    // --- VISTA BIBLIOTECARIOS ---
    private JPanel crearVistaBibliotecarios() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Formulario en Tarjeta
        TarjetaPanel tarjetaForm = new TarjetaPanel(new BorderLayout(0, 12));
        lblBiblioModo = new JLabel("Modo: Registro de Nuevo Bibliotecario");
        lblBiblioModo.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblBiblioModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        tarjetaForm.add(lblBiblioModo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(1, 3, 16, 0));
        panelCampos.setOpaque(false);

        JPanel pUser = new JPanel(new BorderLayout(0, 5));
        pUser.setOpaque(false);
        pUser.add(new JLabel("Usuario de acceso:"), BorderLayout.NORTH);
        txtBiblioUsername = new CampoTexto("Ej. m_sanchez");
        pUser.add(txtBiblioUsername, BorderLayout.CENTER);

        JPanel pNom = new JPanel(new BorderLayout(0, 5));
        pNom.setOpaque(false);
        pNom.add(new JLabel("Nombre completo:"), BorderLayout.NORTH);
        txtBiblioNombre = new CampoTexto("Ej. Manuel Sánchez Ramos");
        pNom.add(txtBiblioNombre, BorderLayout.CENTER);

        JPanel pPass = new JPanel(new BorderLayout(0, 5));
        pPass.setOpaque(false);
        pPass.add(new JLabel("Contraseña inicial (opcional al editar):"), BorderLayout.NORTH);
        txtBiblioPassword = new CampoPassword("Dejar en blanco para autogenerar");
        pPass.add(txtBiblioPassword, BorderLayout.CENTER);

        panelCampos.add(pUser);
        panelCampos.add(pNom);
        panelCampos.add(pPass);
        tarjetaForm.add(panelCampos, BorderLayout.CENTER);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotones.setOpaque(false);

        btnBiblioLimpiar = new BotonSecundario("Limpiar");
        btnBiblioToggleActivo = new BotonSecundario("Activar / Desactivar");
        btnBiblioRestablecerClave = new BotonSecundario("Restablecer Clave");
        btnBiblioEliminar = new BotonPeligro("Eliminar", Iconos.crearEliminar(16, Color.WHITE));
        btnBiblioActualizar = new BotonAcento("Guardar Cambios", Iconos.crearCheck(16, Color.WHITE));
        btnBiblioRegistrar = new BotonPrimario("Registrar Nuevo", Iconos.crearMas(16, Color.WHITE));

        panelBotones.add(btnBiblioLimpiar);
        panelBotones.add(btnBiblioToggleActivo);
        panelBotones.add(btnBiblioRestablecerClave);
        panelBotones.add(btnBiblioEliminar);
        panelBotones.add(btnBiblioActualizar);
        panelBotones.add(btnBiblioRegistrar);
        tarjetaForm.add(panelBotones, BorderLayout.SOUTH);

        panel.add(tarjetaForm, BorderLayout.NORTH);

        // Tabla en Tarjeta
        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        JLabel lblTablaTit = new JLabel("Personal Bibliotecario Registrado");
        lblTablaTit.setFont(Tema.FUENTE_SECCION);
        lblTablaTit.setForeground(Tema.COLOR_TEXTO);
        tarjetaTabla.add(lblTablaTit, BorderLayout.NORTH);

        modeloBibliotecarios = new DefaultTableModel(new String[]{"ID", "Usuario", "Nombre Completo", "Estado", "Intentos Fallidos"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaBibliotecarios = new TablaEstilizada(modeloBibliotecarios);
        tablaBibliotecarios.setMensajeVacio("No hay bibliotecarios registrados en el sistema.");
        tarjetaTabla.add(new JScrollPane(tablaBibliotecarios), BorderLayout.CENTER);

        panel.add(tarjetaTabla, BorderLayout.CENTER);

        // Eventos
        btnBiblioRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarBibliotecario(txtBiblioUsername.getText(), txtBiblioNombre.getText(), txtBiblioPassword.getPasswordString());
            }
        });

        btnBiblioActualizar.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione primero un bibliotecario de la tabla para editar sus datos.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.actualizarBibliotecario(biblioSeleccionadoId, txtBiblioUsername.getText(), txtBiblioNombre.getText(), txtBiblioPassword.getPasswordString());
            }
        });

        btnBiblioEliminar.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un bibliotecario de la tabla para eliminar.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.eliminarBibliotecario(biblioSeleccionadoId);
            }
        });

        btnBiblioRestablecerClave.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un bibliotecario de la tabla para restablecer su clave.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.restablecerClaveBibliotecario(biblioSeleccionadoId);
            }
        });

        btnBiblioToggleActivo.addActionListener(e -> {
            if (biblioSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione un bibliotecario de la tabla.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
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
                    lblBiblioModo.setForeground(Tema.COLOR_PRIMARIO);
                }
            }
        });

        return panel;
    }

    // --- VISTA CATÁLOGO ---
    private JPanel crearVistaCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        // 1. Formulario Libro en Tarjeta (Con interruptor para Ocultar/Mostrar)
        TarjetaPanel tarjetaForm = new TarjetaPanel(new BorderLayout(0, 8));

        JPanel panelHeaderForm = new JPanel(new BorderLayout());
        panelHeaderForm.setOpaque(false);
        lblLibroModo = new JLabel("Modo: Registro de Nuevo Título");
        lblLibroModo.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblLibroModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        panelHeaderForm.add(lblLibroModo, BorderLayout.WEST);

        btnToggleFormLibro = new BotonSecundario("Ocultar Formulario");
        btnToggleFormLibro.setFont(Tema.FUENTE_PEQUENA);
        panelHeaderForm.add(btnToggleFormLibro, BorderLayout.EAST);
        tarjetaForm.add(panelHeaderForm, BorderLayout.NORTH);

        panelCuerpoFormLibro = new JPanel(new BorderLayout(0, 10));
        panelCuerpoFormLibro.setOpaque(false);

        // Campos con GridBagLayout para proporciones óptimas y no apretadas
        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 6, 4, 6);

        // Fila 0: ISBN (18%), Título (42%), Autor (24%), Categoría (16%)
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.18;
        panelCampos.add(crearCampo("ISBN:", txtLibroIsbn = new CampoTexto("978...")), gbc);

        gbc.gridx = 1; gbc.weightx = 0.42;
        panelCampos.add(crearCampo("Título del Libro:", txtLibroTitulo = new CampoTexto("Título completo de la obra")), gbc);

        gbc.gridx = 2; gbc.weightx = 0.24;
        panelCampos.add(crearCampo("Autor:", txtLibroAutor = new CampoTexto("Nombre del autor")), gbc);

        gbc.gridx = 3; gbc.weightx = 0.16;
        panelCampos.add(crearCampo("Categoría:", txtLibroCategoria = new CampoTexto("Ej. Programación")), gbc);

        // Fila 1: Editorial (28%), Año (16%), Copias (16%), Espacio libre
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.weightx = 0.28;
        panelCampos.add(crearCampo("Editorial:", txtLibroEditorial = new CampoTexto("Ej. Pearson / Planeta")), gbc);

        gbc.gridx = 1; gbc.weightx = 0.18;
        spinLibroAnio = new JSpinner(new SpinnerNumberModel(2024, 1500, 2100, 1));
        panelCampos.add(crearCampo("Año de publicación:", spinLibroAnio), gbc);

        gbc.gridx = 2; gbc.weightx = 0.18;
        spinEjemplaresIniciales = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        panelCampos.add(crearCampo("Copias iniciales:", spinEjemplaresIniciales), gbc);

        gbc.gridx = 3; gbc.weightx = 0.36;
        panelCampos.add(Box.createGlue(), gbc);

        panelCuerpoFormLibro.add(panelCampos, BorderLayout.CENTER);

        // Botones de acción del Libro
        JPanel panelBotones = new JPanel(new BorderLayout(10, 0));
        panelBotones.setOpaque(false);

        JPanel panelBotonesIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelBotonesIzq.setOpaque(false);
        btnLibroLimpiar = new BotonSecundario("Limpiar Campos");
        panelBotonesIzq.add(btnLibroLimpiar);

        JPanel panelBotonesDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBotonesDer.setOpaque(false);
        btnLibroEliminar = new BotonPeligro("Eliminar Libro", Iconos.crearEliminar(16, Color.WHITE));
        btnLibroActualizar = new BotonAcento("Guardar Cambios", Iconos.crearCheck(16, Color.WHITE));
        btnLibroRegistrar = new BotonPrimario("Registrar Libro", Iconos.crearMas(16, Color.WHITE));
        btnLibroAgregarEjemplar = new BotonSecundario("Agregar Copias", Iconos.crearMas(16, Tema.COLOR_PRIMARIO));

        panelBotonesDer.add(btnLibroEliminar);
        panelBotonesDer.add(btnLibroActualizar);
        panelBotonesDer.add(btnLibroRegistrar);

        panelBotones.add(panelBotonesIzq, BorderLayout.WEST);
        panelBotones.add(panelBotonesDer, BorderLayout.EAST);
        panelCuerpoFormLibro.add(panelBotones, BorderLayout.SOUTH);

        tarjetaForm.add(panelCuerpoFormLibro, BorderLayout.CENTER);
        panel.add(tarjetaForm, BorderLayout.NORTH);

        // 2. Split de Tablas (Libros arriba con búsqueda, Ejemplares abajo con filtro)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerSize(7);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // --- Tarjeta Libros con Barra de Búsqueda Integrada ---
        TarjetaPanel tarjetaLibros = new TarjetaPanel(new BorderLayout(0, 8));

        JPanel panelHeaderLibros = new JPanel(new BorderLayout(10, 0));
        panelHeaderLibros.setOpaque(false);

        JPanel panelTitLibros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTitLibros.setOpaque(false);
        JLabel lblLibrosTit = new JLabel("Catálogo General de Libros");
        lblLibrosTit.setFont(Tema.FUENTE_SECCION);
        lblLibrosTit.setForeground(Tema.COLOR_TEXTO);
        lblLibrosCount = new JLabel("(0 títulos)");
        lblLibrosCount.setFont(Tema.FUENTE_PEQUENA);
        lblLibrosCount.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        panelTitLibros.add(lblLibrosTit);
        panelTitLibros.add(lblLibrosCount);
        panelHeaderLibros.add(panelTitLibros, BorderLayout.WEST);

        // Barra de Búsqueda de Libros
        JPanel panelBuscarLibros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBuscarLibros.setOpaque(false);
        txtLibroBuscar = new CampoTexto("Buscar por título, autor, ISBN o categoría...", 25);
        btnLibroBuscar = new BotonSecundario("Buscar", Iconos.crearBuscar(16, Tema.COLOR_PRIMARIO));
        btnLibroMostrarTodos = new BotonSecundario("Mostrar Todos");

        panelBuscarLibros.add(txtLibroBuscar);
        panelBuscarLibros.add(btnLibroBuscar);
        panelBuscarLibros.add(btnLibroMostrarTodos);
        panelHeaderLibros.add(panelBuscarLibros, BorderLayout.EAST);
        tarjetaLibros.add(panelHeaderLibros, BorderLayout.NORTH);

        modeloLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Año", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaLibros = new TablaEstilizada(modeloLibros);
        tablaLibros.setMensajeVacio("No se encontraron libros que coincidan con la búsqueda.");
        configurarColumnasLibros();

        tarjetaLibros.add(new JScrollPane(tablaLibros), BorderLayout.CENTER);
        splitPane.setTopComponent(tarjetaLibros);

        // --- Tarjeta Ejemplares con Filtro y Acciones ---
        TarjetaPanel tarjetaEjemplares = new TarjetaPanel(new BorderLayout(0, 8));

        JPanel panelHeaderEj = new JPanel(new BorderLayout(10, 0));
        panelHeaderEj.setOpaque(false);

        JPanel panelTitEj = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelTitEj.setOpaque(false);
        lblEjTit = new JLabel("Ejemplares Físicos");
        lblEjTit.setFont(Tema.FUENTE_SECCION);
        lblEjTit.setForeground(Tema.COLOR_TEXTO);
        lblEjDetalle = new JLabel("(Seleccione un libro en la tabla superior para ver sus copias)");
        lblEjDetalle.setFont(Tema.FUENTE_PEQUENA);
        lblEjDetalle.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        panelTitEj.add(lblEjTit);
        panelTitEj.add(lblEjDetalle);
        panelHeaderEj.add(panelTitEj, BorderLayout.WEST);

        // Acciones y Filtro de Ejemplares
        JPanel panelAccionesEj = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAccionesEj.setOpaque(false);
        txtEjemplarBuscar = new CampoTexto("Filtrar copias...", 16);
        btnEjemplarAgregar = new BotonPrimario("Agregar Copias", Iconos.crearMas(16, Color.WHITE));
        btnEjemplarCambiarEstado = new BotonSecundario("Cambiar Estado");
        btnEjemplarEliminar = new BotonPeligro("Eliminar Ejemplar");

        panelAccionesEj.add(txtEjemplarBuscar);
        panelAccionesEj.add(btnEjemplarAgregar);
        panelAccionesEj.add(btnEjemplarCambiarEstado);
        panelAccionesEj.add(btnEjemplarEliminar);
        panelHeaderEj.add(panelAccionesEj, BorderLayout.EAST);
        tarjetaEjemplares.add(panelHeaderEj, BorderLayout.NORTH);

        modeloEjemplares = new DefaultTableModel(new String[]{"ID Ejemplar", "Código de Barras", "Estado Físico"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEjemplares = new TablaEstilizada(modeloEjemplares);
        tablaEjemplares.setMensajeVacio("Seleccione un libro para consultar sus ejemplares físicos.");
        configurarColumnasEjemplares();

        tarjetaEjemplares.add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);
        splitPane.setBottomComponent(tarjetaEjemplares);

        panel.add(splitPane, BorderLayout.CENTER);

        // --- Eventos del Formulario y Toggle ---
        btnToggleFormLibro.addActionListener(e -> {
            boolean visible = !panelCuerpoFormLibro.isVisible();
            panelCuerpoFormLibro.setVisible(visible);
            btnToggleFormLibro.setText(visible ? "Ocultar Formulario" : "Mostrar Formulario (+)");
            tarjetaForm.revalidate();
            tarjetaForm.repaint();
        });

        btnLibroRegistrar.addActionListener(e -> {
            if (controller != null) {
                controller.guardarLibro(txtLibroIsbn.getText(), txtLibroTitulo.getText(), txtLibroAutor.getText(),
                        txtLibroCategoria.getText(), txtLibroEditorial.getText(),
                        (int) spinLibroAnio.getValue(), (int) spinEjemplaresIniciales.getValue());
            }
        });

        btnLibroActualizar.addActionListener(e -> {
            if (libroSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione primero un libro de la tabla para editar.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
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
                DialogoModerno.mostrarMensaje(this, "Seleccione un libro para eliminar.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            if (controller != null) {
                controller.eliminarLibro(libroSeleccionadoId);
            }
        });

        // Evento compartido para agregar copias (desde el form o desde la tarjeta de ejemplares)
        java.awt.event.ActionListener accionAgregarCopias = e -> {
            if (libroSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione primero un libro de la tabla para incorporarle nuevas copias.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
                return;
            }
            String cantStr = JOptionPane.showInputDialog(this, "¿Cuántos ejemplares desea incorporar a este libro?", "1");
            if (cantStr != null && !cantStr.trim().isEmpty() && controller != null) {
                try {
                    int cant = Integer.parseInt(cantStr.trim());
                    if (cant <= 0) throw new NumberFormatException();
                    controller.agregarEjemplares(libroSeleccionadoId, cant);
                } catch (NumberFormatException ex) {
                    DialogoModerno.mostrarMensaje(this, "Debe ingresar una cantidad entera válida mayor a 0.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
                }
            }
        };
        btnLibroAgregarEjemplar.addActionListener(accionAgregarCopias);
        btnEjemplarAgregar.addActionListener(accionAgregarCopias);

        btnLibroLimpiar.addActionListener(e -> limpiarFormLibro());

        // --- Eventos de Búsqueda de Libros ---
        Runnable ejecutarBusquedaLibros = () -> {
            if (controller != null) {
                controller.buscarCatalogo(txtLibroBuscar.getText());
            }
        };
        btnLibroBuscar.addActionListener(e -> ejecutarBusquedaLibros.run());
        txtLibroBuscar.addActionListener(e -> ejecutarBusquedaLibros.run());

        btnLibroMostrarTodos.addActionListener(e -> {
            txtLibroBuscar.setText("");
            if (controller != null) {
                controller.cargarCatalogo();
            }
        });

        txtLibroBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { verificarVacio(); }
            public void removeUpdate(DocumentEvent e) { verificarVacio(); }
            public void changedUpdate(DocumentEvent e) { verificarVacio(); }
            private void verificarVacio() {
                if (txtLibroBuscar.getText().trim().isEmpty() && controller != null) {
                    controller.cargarCatalogo();
                }
            }
        });

        // --- Eventos de Filtro de Ejemplares en Tiempo Real ---
        txtEjemplarBuscar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarEjemplares(); }
            public void removeUpdate(DocumentEvent e) { filtrarEjemplares(); }
            public void changedUpdate(DocumentEvent e) { filtrarEjemplares(); }
        });

        // --- Selección en Tabla de Libros ---
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
                    try {
                        spinLibroAnio.setValue(Integer.parseInt(modeloLibros.getValueAt(row, 6).toString()));
                    } catch (Exception ignored) {}

                    String titulo = txtLibroTitulo.getText();
                    lblLibroModo.setText("Modo: Editando Libro ID #" + libroSeleccionadoId + " («" + (titulo.length() > 35 ? titulo.substring(0, 32) + "..." : titulo) + "»)");
                    lblLibroModo.setForeground(Tema.COLOR_PRIMARIO);

                    lblEjTit.setText("Ejemplares de: " + (titulo.length() > 30 ? titulo.substring(0, 27) + "..." : titulo));

                    // Si el formulario estaba oculto, expandirlo al seleccionar
                    if (!panelCuerpoFormLibro.isVisible()) {
                        panelCuerpoFormLibro.setVisible(true);
                        btnToggleFormLibro.setText("Ocultar Formulario");
                        tarjetaForm.revalidate();
                    }

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
                DialogoModerno.mostrarMensaje(this, "Seleccione un ejemplar de la tabla inferior.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        btnEjemplarEliminar.addActionListener(e -> {
            int row = tablaEjemplares.getSelectedRow();
            if (row >= 0 && controller != null && libroSeleccionadoId != null) {
                int ejemplarId = (int) modeloEjemplares.getValueAt(row, 0);
                controller.eliminarEjemplar(ejemplarId, libroSeleccionadoId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un ejemplar de la tabla inferior.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    private void configurarColumnasLibros() {
        if (tablaLibros.getColumnCount() >= 8) {
            tablaLibros.fijarAnchoColumna(0, 45, 50, 60);    // ID
            tablaLibros.fijarAnchoColumna(1, 120, 135, 150); // ISBN
            tablaLibros.fijarAnchoColumna(2, 200, 270, 0);   // Título (expansible)
            tablaLibros.fijarAnchoColumna(3, 140, 170, 220); // Autor
            tablaLibros.fijarAnchoColumna(4, 100, 120, 150); // Categoría
            tablaLibros.fijarAnchoColumna(5, 110, 130, 160); // Editorial
            tablaLibros.fijarAnchoColumna(6, 50, 60, 70);    // Año
            tablaLibros.fijarAnchoColumna(7, 75, 85, 105);   // Disponibles
        }
    }

    private void configurarColumnasEjemplares() {
        if (tablaEjemplares.getColumnCount() >= 3) {
            tablaEjemplares.fijarAnchoColumna(0, 60, 80, 100);  // ID Ejemplar
            tablaEjemplares.fijarAnchoColumna(1, 150, 220, 0);  // Código de Barras
            tablaEjemplares.fijarAnchoColumna(2, 120, 150, 180);// Estado Físico
        }
    }

    private void filtrarEjemplares() {
        String f = txtEjemplarBuscar != null ? txtEjemplarBuscar.getText().trim().toLowerCase() : "";
        modeloEjemplares.setRowCount(0);
        int disp = 0;
        for (Ejemplar ej : ejemplaresActuales) {
            if (ej.getEstado() == com.biblioteca.model.EstadoEjemplar.DISPONIBLE) {
                disp++;
            }
            if (f.isEmpty() ||
                ej.getCodigoBarras().toLowerCase().contains(f) ||
                ej.getEstado().name().toLowerCase().contains(f) ||
                String.valueOf(ej.getId()).contains(f)) {
                modeloEjemplares.addRow(new Object[]{ej.getId(), ej.getCodigoBarras(), ej.getEstado()});
            }
        }
        if (lblEjDetalle != null && libroSeleccionadoId != null) {
            lblEjDetalle.setText("(" + ejemplaresActuales.size() + " copias físicas | " + disp + " disponibles)");
        }
        configurarColumnasEjemplares();
    }

    // --- VISTA CONFIGURACIÓN ---
    private JComponent crearVistaConfiguracion() {
        JPanel panelContenedor = new JPanel(new GridBagLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(16, 16, 24, 16));

        TarjetaPanel tarjeta = new TarjetaPanel(new BorderLayout(0, 16));
        tarjeta.setPreferredSize(new Dimension(600, 610));
        tarjeta.setMinimumSize(new Dimension(500, 560));
        tarjeta.setBorder(new EmptyBorder(28, 36, 28, 36));

        // 1. Encabezado con ícono descriptivo
        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.setOpaque(false);

        JPanel panelTituloIcono = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelTituloIcono.setOpaque(false);
        panelTituloIcono.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblIcono = new JLabel(Iconos.crearConfiguracion(22, Tema.COLOR_PRIMARIO));
        JLabel lblTit = new JLabel("Parámetros Operativos del Sistema");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        panelTituloIcono.add(lblIcono);
        panelTituloIcono.add(lblTit);

        JLabel lblSub = new JLabel("Ajuste los valores de negocio vigentes para préstamos, moras y reservas.");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblSub.setBorder(new EmptyBorder(4, 32, 0, 0));

        panelEncabezado.add(panelTituloIcono);
        panelEncabezado.add(Box.createVerticalStrut(4));
        panelEncabezado.add(lblSub);

        tarjeta.add(panelEncabezado, BorderLayout.NORTH);

        // 2. Formulario de Parámetros
        JPanel panelForm = new JPanel();
        panelForm.setLayout(new BoxLayout(panelForm, BoxLayout.Y_AXIS));
        panelForm.setOpaque(false);

        txtConfMultaPorDia = new CampoTexto();
        txtConfMultaPorDia.setPlaceholder("Ej. 0.50");
        panelForm.add(crearCampoConfig("Multa diaria por retraso (S/.):", txtConfMultaPorDia, "Monto aplicado en soles por cada día de mora acumulado"));
        panelForm.add(Box.createVerticalStrut(12));

        txtConfDiasPrestamo = new CampoTexto();
        txtConfDiasPrestamo.setPlaceholder("Ej. 7");
        panelForm.add(crearCampoConfig("Días permitidos de préstamo:", txtConfDiasPrestamo, "Plazo regular de devolución asignado a los lectores"));
        panelForm.add(Box.createVerticalStrut(12));

        txtConfMaxPrestamos = new CampoTexto();
        txtConfMaxPrestamos.setPlaceholder("Ej. 3");
        panelForm.add(crearCampoConfig("Límite de préstamos simultáneos por cliente:", txtConfMaxPrestamos, "Cantidad máxima de libros prestados activos a la vez"));
        panelForm.add(Box.createVerticalStrut(12));

        txtConfDiasRetencion = new CampoTexto();
        txtConfDiasRetencion.setPlaceholder("Ej. 3");
        panelForm.add(crearCampoConfig("Días de retención para reserva:", txtConfDiasRetencion, "Tiempo límite para recoger un ejemplar físico reservado"));

        tarjeta.add(panelForm, BorderLayout.CENTER);

        // 3. Footer con nota informativa y botones
        JPanel panelFooter = new JPanel(new BorderLayout(0, 10));
        panelFooter.setOpaque(false);

        JLabel lblNota = new JLabel(" Los cambios guardados se aplicarán de inmediato en todas las operaciones del sistema.",
                Iconos.crearAlerta(14, Tema.COLOR_TEXTO_SECUNDARIO), SwingConstants.LEFT);
        lblNota.setFont(Tema.FUENTE_PEQUENA);
        lblNota.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        panelFooter.add(lblNota, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);

        BotonSecundario btnRestablecer = new BotonSecundario("Restablecer", Iconos.crearActualizar(16, Tema.COLOR_PRIMARIO));
        btnRestablecer.setPreferredSize(new Dimension(150, 42));
        btnRestablecer.setToolTipText("Recargar los valores actuales guardados en la base de datos");
        btnRestablecer.addActionListener(e -> {
            if (controller != null) {
                controller.cargarConfiguracion();
            }
        });

        btnConfGuardar = new BotonAcento("Guardar Cambios", Iconos.crearCheck(18, Color.WHITE));
        btnConfGuardar.setFont(Tema.FUENTE_SECCION);
        btnConfGuardar.setPreferredSize(new Dimension(180, 42));
        btnConfGuardar.setToolTipText("Guardar la nueva configuración en la base de datos");

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

        panelBotones.add(btnRestablecer);
        panelBotones.add(btnConfGuardar);
        panelFooter.add(panelBotones, BorderLayout.CENTER);

        tarjeta.add(panelFooter, BorderLayout.SOUTH);
        panelContenedor.add(tarjeta);

        JScrollPane scroll = new JScrollPane(panelContenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // --- VISTA REPORTES ---
    private JPanel crearVistaReportes() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // Barra superior de filtros
        TarjetaPanel tarjetaFiltro = new TarjetaPanel(new BorderLayout(15, 0));
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelIzquierda.setOpaque(false);

        JLabel lblSel = new JLabel("Tipo de Reporte:");
        lblSel.setFont(Tema.FUENTE_SECCION);
        panelIzquierda.add(lblSel);

        comboTipoReporte = new JComboBox<>(new String[]{
                "Préstamos Activos y Vencidos",
                "Multas Pendientes y Pagadas",
                "Libros Más Prestados (Top 10)",
                "Inventario de Ejemplares por Estado"
        });
        comboTipoReporte.setFont(Tema.FUENTE_NORMAL);
        panelIzquierda.add(comboTipoReporte);

        BotonSecundario btnCargar = new BotonSecundario("Actualizar Datos", Iconos.crearActualizar(16, Tema.COLOR_PRIMARIO));
        panelIzquierda.add(btnCargar);
        tarjetaFiltro.add(panelIzquierda, BorderLayout.WEST);

        btnExportarExcel = new BotonAcento("Exportar a Excel (.xlsx)", Iconos.crearReportes(16, Color.WHITE));
        tarjetaFiltro.add(btnExportarExcel, BorderLayout.EAST);
        panel.add(tarjetaFiltro, BorderLayout.NORTH);

        // Contenedor principal con pestañas: Gráficas Estadísticas / Tabla Detallada / Vista Dividida
        TarjetaPanel tarjetaContenido = new TarjetaPanel(new BorderLayout(0, 10));

        JTabbedPane tabsReportes = new JTabbedPane();
        tabsReportes.setFont(Tema.FUENTE_SECCION);

        // Tab 0: Gráfica Estadística interactiva
        panelGraficaReporte = new PanelGraficaReporte();
        tabsReportes.addTab("📊 Visualización Gráfica", panelGraficaReporte);

        // Tab 1: Tabla de Datos Tabulares
        modeloReporte = new DefaultTableModel();
        tablaReporte = new TablaEstilizada(modeloReporte);
        tablaReporte.setMensajeVacio("Presione 'Actualizar Datos' para generar la consulta de reporte.");
        JPanel panelTablaContenedor = new JPanel(new BorderLayout());
        panelTablaContenedor.setOpaque(false);
        panelTablaContenedor.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        tabsReportes.addTab("📋 Tabla Detallada", panelTablaContenedor);

        // Tab 2: Vista Dividida (Gráfica + Tabla)
        JSplitPane splitReportes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitReportes.setOpaque(false);
        splitReportes.setBorder(null);
        splitReportes.setResizeWeight(0.5);
        splitReportes.setDividerSize(6);

        panelGraficaReporteSplit = new PanelGraficaReporte();
        splitReportes.setLeftComponent(panelGraficaReporteSplit);

        tablaReporteSplit = new TablaEstilizada(modeloReporte);
        splitReportes.setRightComponent(new JScrollPane(tablaReporteSplit));

        tabsReportes.addTab("📑 Vista Dividida (Ambas)", splitReportes);

        tarjetaContenido.add(tabsReportes, BorderLayout.CENTER);
        panel.add(tarjetaContenido, BorderLayout.CENTER);

        btnCargar.addActionListener(e -> {
            if (controller != null) controller.cargarReporte(comboTipoReporte.getSelectedIndex());
        });

        comboTipoReporte.addActionListener(e -> {
            if (controller != null) controller.cargarReporte(comboTipoReporte.getSelectedIndex());
        });

        btnExportarExcel.addActionListener(e -> {
            if (controller != null) controller.exportarExcelReporte(comboTipoReporte.getSelectedIndex(), tablaReporte);
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

    private JPanel crearCampoConfig(String etiqueta, JTextField campo, String ayuda) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(etiqueta);
        l.setFont(Tema.FUENTE_SECCION);
        l.setForeground(Tema.COLOR_TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel a = new JLabel(ayuda);
        a.setFont(Tema.FUENTE_PEQUENA);
        a.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        a.setAlignmentX(Component.LEFT_ALIGNMENT);

        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        campo.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(a);
        p.add(Box.createVerticalStrut(6));
        p.add(campo);
        return p;
    }

    // --- MÉTODOS PÚBLICOS DE ACTUALIZACIÓN (COMPATIBILIDAD CON CONTROLLER) ---
    public void cargarTablaBibliotecarios(List<Usuario> lista) {
        modeloBibliotecarios.setRowCount(0);
        for (Usuario u : lista) {
            modeloBibliotecarios.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getNombre(), u.isActivo() ? "ACTIVO" : "INACTIVO", u.getIntentosFallidos()
            });
        }
        if (kpiBibliotecarios != null) {
            kpiBibliotecarios.setValor(String.valueOf(lista.size()));
        }
        configurarColumnasBibliotecarios();
    }

    private void configurarColumnasBibliotecarios() {
        if (tablaBibliotecarios != null && tablaBibliotecarios.getColumnCount() >= 5) {
            tablaBibliotecarios.fijarAnchoColumna(0, 45, 55, 70);
            tablaBibliotecarios.fijarAnchoColumna(1, 110, 140, 180);
            tablaBibliotecarios.fijarAnchoColumna(2, 180, 260, 0);
            tablaBibliotecarios.fijarAnchoColumna(3, 100, 120, 140);
            tablaBibliotecarios.fijarAnchoColumna(4, 90, 110, 130);
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
        if (kpiLibros != null) {
            kpiLibros.setValor(String.valueOf(lista.size()));
        }
        if (lblLibrosCount != null) {
            lblLibrosCount.setText("(" + lista.size() + " títulos)");
        }
        configurarColumnasLibros();
    }

    public void cargarTablaEjemplares(List<Ejemplar> lista) {
        this.ejemplaresActuales = lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        filtrarEjemplares();
        configurarColumnasEjemplares();
    }

    public void setValoresConfiguracion(String multa, String diasP, String maxP, String retencion) {
        txtConfMultaPorDia.setText(multa);
        txtConfDiasPrestamo.setText(diasP);
        txtConfMaxPrestamos.setText(maxP);
        txtConfDiasRetencion.setText(retencion);
        if (kpiTarifaMulta != null) {
            kpiTarifaMulta.setValor("S/ " + multa);
        }
    }

    public void setReporteModel(DefaultTableModel model) {
        this.modeloReporte = model;
        if (tablaReporte != null) {
            tablaReporte.setModel(model);
        }
        if (tablaReporteSplit != null) {
            tablaReporteSplit.setModel(model);
        }
        int tipo = comboTipoReporte != null ? comboTipoReporte.getSelectedIndex() : 0;
        if (panelGraficaReporte != null) {
            panelGraficaReporte.actualizarDesdeModelo(tipo, model);
        }
        if (panelGraficaReporteSplit != null) {
            panelGraficaReporteSplit.actualizarDesdeModelo(tipo, model);
        }
    }

    public void limpiarFormBibliotecario() {
        biblioSeleccionadoId = null;
        txtBiblioUsername.setText("");
        txtBiblioNombre.setText("");
        txtBiblioPassword.setText("");
        tablaBibliotecarios.clearSelection();
        lblBiblioModo.setText("Modo: Registro de Nuevo Bibliotecario");
        lblBiblioModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
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
        ejemplaresActuales.clear();
        if (lblEjTit != null) lblEjTit.setText("Ejemplares Físicos");
        if (lblEjDetalle != null) lblEjDetalle.setText("(Seleccione un libro en la tabla superior para ver sus copias)");
        if (txtEjemplarBuscar != null) txtEjemplarBuscar.setText("");
        lblLibroModo.setText("Modo: Registro de Nuevo Título");
        lblLibroModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
    }

    public void mostrarModalClaveTemporal(String username, String nombre, String claveTemporal) {
        String mensaje = "Se han generado las credenciales de acceso para el bibliotecario:<br/><br/>" +
                "<b>Nombre:</b> " + nombre + "<br/>" +
                "<b>Usuario:</b> " + username + "<br/>" +
                "<b>Contraseña Asignada:</b> <span style='color:#3F51B5; font-size:15px; font-weight:bold;'>" + claveTemporal + "</span><br/><br/>" +
                "<i>El usuario deberá cambiar su clave obligatoriamente al iniciar sesión.</i>";

        DialogoModerno.mostrarMensaje(this, mensaje, "Credenciales de Bibliotecario", DialogoModerno.TipoDialogo.EXITO);
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
