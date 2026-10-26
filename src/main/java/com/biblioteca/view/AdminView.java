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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private TablaEstilizada tablaLibros;
    private DefaultTableModel modeloLibros;
    private TablaEstilizada tablaEjemplares;
    private DefaultTableModel modeloEjemplares;
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
        setSize(1180, 760);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
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
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Formulario Libro en Tarjeta
        TarjetaPanel tarjetaForm = new TarjetaPanel(new BorderLayout(0, 10));
        lblLibroModo = new JLabel("Modo: Registro de Nuevo Título");
        lblLibroModo.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblLibroModo.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        tarjetaForm.add(lblLibroModo, BorderLayout.NORTH);

        JPanel panelCampos = new JPanel(new GridLayout(2, 4, 14, 8));
        panelCampos.setOpaque(false);

        panelCampos.add(crearCampo("ISBN:", txtLibroIsbn = new CampoTexto("978...")));
        panelCampos.add(crearCampo("Título:", txtLibroTitulo = new CampoTexto("Título del libro")));
        panelCampos.add(crearCampo("Autor:", txtLibroAutor = new CampoTexto("Nombre del autor")));
        panelCampos.add(crearCampo("Categoría:", txtLibroCategoria = new CampoTexto("Ej. Programación")));
        panelCampos.add(crearCampo("Editorial:", txtLibroEditorial = new CampoTexto("Ej. Pearson")));

        spinLibroAnio = new JSpinner(new SpinnerNumberModel(2024, 1500, 2100, 1));
        panelCampos.add(crearCampo("Año de publicación:", spinLibroAnio));

        spinEjemplaresIniciales = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        panelCampos.add(crearCampo("Copias iniciales:", spinEjemplaresIniciales));

        tarjetaForm.add(panelCampos, BorderLayout.CENTER);

        // Botones de acción Libro
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotones.setOpaque(false);

        btnLibroLimpiar = new BotonSecundario("Limpiar");
        btnLibroAgregarEjemplar = new BotonSecundario("+ Agregar Ejemplares", Iconos.crearMas(16, Tema.COLOR_PRIMARIO));
        btnLibroEliminar = new BotonPeligro("Eliminar Libro", Iconos.crearEliminar(16, Color.WHITE));
        btnLibroActualizar = new BotonAcento("Guardar Cambios", Iconos.crearCheck(16, Color.WHITE));
        btnLibroRegistrar = new BotonPrimario("Registrar Libro", Iconos.crearMas(16, Color.WHITE));

        panelBotones.add(btnLibroLimpiar);
        panelBotones.add(btnLibroAgregarEjemplar);
        panelBotones.add(btnLibroEliminar);
        panelBotones.add(btnLibroActualizar);
        panelBotones.add(btnLibroRegistrar);
        tarjetaForm.add(panelBotones, BorderLayout.SOUTH);

        panel.add(tarjetaForm, BorderLayout.NORTH);

        // Split de Tablas (Libros arriba, Ejemplares abajo)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Tarjeta Libros
        TarjetaPanel tarjetaLibros = new TarjetaPanel(new BorderLayout(0, 8));
        JLabel lblLibrosTit = new JLabel("Catálogo General de Libros");
        lblLibrosTit.setFont(Tema.FUENTE_SECCION);
        tarjetaLibros.add(lblLibrosTit, BorderLayout.NORTH);

        modeloLibros = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Año", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaLibros = new TablaEstilizada(modeloLibros);
        tablaLibros.setMensajeVacio("No hay libros registrados en el catálogo.");
        tarjetaLibros.add(new JScrollPane(tablaLibros), BorderLayout.CENTER);
        splitPane.setTopComponent(tarjetaLibros);

        // Tarjeta Ejemplares
        TarjetaPanel tarjetaEjemplares = new TarjetaPanel(new BorderLayout(0, 8));
        JPanel panelHeaderEj = new JPanel(new BorderLayout());
        panelHeaderEj.setOpaque(false);
        JLabel lblEjTit = new JLabel("Ejemplares Físicos del Libro Seleccionado");
        lblEjTit.setFont(Tema.FUENTE_SECCION);
        panelHeaderEj.add(lblEjTit, BorderLayout.WEST);

        JPanel panelAccionesEj = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelAccionesEj.setOpaque(false);
        btnEjemplarCambiarEstado = new BotonSecundario("Cambiar Estado");
        btnEjemplarEliminar = new BotonPeligro("Eliminar Ejemplar");
        panelAccionesEj.add(btnEjemplarCambiarEstado);
        panelAccionesEj.add(btnEjemplarEliminar);
        panelHeaderEj.add(panelAccionesEj, BorderLayout.EAST);
        tarjetaEjemplares.add(panelHeaderEj, BorderLayout.NORTH);

        modeloEjemplares = new DefaultTableModel(new String[]{"ID Ejemplar", "Código de Barras", "Estado Físico"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaEjemplares = new TablaEstilizada(modeloEjemplares);
        tablaEjemplares.setMensajeVacio("Seleccione un libro para consultar sus ejemplares.");
        tarjetaEjemplares.add(new JScrollPane(tablaEjemplares), BorderLayout.CENTER);
        splitPane.setBottomComponent(tarjetaEjemplares);

        panel.add(splitPane, BorderLayout.CENTER);

        // Eventos Catálogo
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

        btnLibroAgregarEjemplar.addActionListener(e -> {
            if (libroSeleccionadoId == null) {
                DialogoModerno.mostrarMensaje(this, "Seleccione primero un libro para agregarle nuevas copias.", "Validación", DialogoModerno.TipoDialogo.ADVERTENCIA);
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
                    try {
                        spinLibroAnio.setValue(Integer.parseInt(modeloLibros.getValueAt(row, 6).toString()));
                    } catch (Exception ignored) {}

                    lblLibroModo.setText("Modo: Editando Libro ID #" + libroSeleccionadoId + " (" + txtLibroTitulo.getText() + ")");
                    lblLibroModo.setForeground(Tema.COLOR_PRIMARIO);

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

    // --- VISTA CONFIGURACIÓN ---
    private JPanel crearVistaConfiguracion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        TarjetaPanel tarjeta = new TarjetaPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setPreferredSize(new Dimension(540, 480));
        tarjeta.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel lblTit = new JLabel("Parámetros Operativos del Sistema");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Ajuste los valores de negocio vigentes para préstamos, moras y reservas.");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        tarjeta.add(lblTit);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblSub);
        tarjeta.add(Box.createVerticalStrut(22));

        txtConfMultaPorDia = new CampoTexto();
        tarjeta.add(crearCampoConfig("Multa diaria por retraso (S/.):", txtConfMultaPorDia, "Monto aplicado por cada día de mora"));
        tarjeta.add(Box.createVerticalStrut(14));

        txtConfDiasPrestamo = new CampoTexto();
        tarjeta.add(crearCampoConfig("Días permitidos de préstamo:", txtConfDiasPrestamo, "Plazo regular de devolución para los lectores"));
        tarjeta.add(Box.createVerticalStrut(14));

        txtConfMaxPrestamos = new CampoTexto();
        tarjeta.add(crearCampoConfig("Límite de préstamos simultáneos por cliente:", txtConfMaxPrestamos, "Máxima cantidad de libros activos a la vez"));
        tarjeta.add(Box.createVerticalStrut(14));

        txtConfDiasRetencion = new CampoTexto();
        tarjeta.add(crearCampoConfig("Días de retención para reserva:", txtConfDiasRetencion, "Tiempo límite para recoger un ejemplar reservado"));
        tarjeta.add(Box.createVerticalStrut(24));

        btnConfGuardar = new BotonAcento("Guardar Parámetros de Configuración", Iconos.crearCheck(18, Color.WHITE));
        btnConfGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnConfGuardar.setAlignmentX(Component.LEFT_ALIGNMENT);

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

        tarjeta.add(btnConfGuardar);
        panel.add(tarjeta);
        return panel;
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

        // Tabla de Reportes
        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        modeloReporte = new DefaultTableModel();
        tablaReporte = new TablaEstilizada(modeloReporte);
        tablaReporte.setMensajeVacio("Presione 'Actualizar Datos' para generar la consulta de reporte.");
        tarjetaTabla.add(new JScrollPane(tablaReporte), BorderLayout.CENTER);
        panel.add(tarjetaTabla, BorderLayout.CENTER);

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

        JLabel a = new JLabel(ayuda);
        a.setFont(Tema.FUENTE_PEQUENA);
        a.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

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
    }

    public void cargarTablaLibros(List<Libro> lista, java.util.function.Function<Integer, Integer> contarDisp) {
        modeloLibros.setRowCount(0);
        int totalDisponibles = 0;
        for (Libro l : lista) {
            int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
            totalDisponibles += disp;
            modeloLibros.addRow(new Object[]{
                    l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), l.getAnio(), disp
            });
        }
        if (kpiLibros != null) {
            kpiLibros.setValor(String.valueOf(lista.size()));
        }
    }

    public void cargarTablaEjemplares(List<Ejemplar> lista) {
        modeloEjemplares.setRowCount(0);
        for (Ejemplar ej : lista) {
            modeloEjemplares.addRow(new Object[]{
                    ej.getId(), ej.getCodigoBarras(), ej.getEstado()
            });
        }
        if (kpiEjemplares != null && (kpiEjemplares.getComponents().length > 0)) {
            // Actualizar si hay contexto general
        }
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
        tablaReporte.setModel(model);
        this.modeloReporte = model;
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
