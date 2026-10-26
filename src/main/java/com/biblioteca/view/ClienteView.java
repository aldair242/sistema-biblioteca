package com.biblioteca.view;

import com.biblioteca.controller.ClienteController;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Multa;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;
import com.biblioteca.security.SesionActual;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal rediseñada para el rol CLIENTE (lector).
 * Cuenta con barra lateral, dashboard personal con tarjetas de métricas,
 * catálogo interactivo con visualización en cuadrícula de tarjetas o tabla,
 * y módulos para consultar préstamos, reservas, multas y seguridad.
 */
public class ClienteView extends JFrame {

    private ClienteController controller;

    // Componentes de Estructura
    private BarraNavegacionLateral barraLateral;
    private BarraSuperiorModerna barraSuperior;
    private JPanel panelContenedorTarjetas;
    private CardLayout cardLayout;

    // Dashboard del Lector
    private TarjetaMetrica kpiMisPrestamos;
    private TarjetaMetrica kpiProximoVencimiento;
    private TarjetaMetrica kpiMisReservas;
    private TarjetaMetrica kpiMisMultas;

    // Catálogo del Lector (Modos: Cuadrícula de Tarjetas y Tabla)
    private CampoTexto txtCatalogoBuscar;
    private JComboBox<String> comboCategorias;
    private JPanel panelGridLibros;
    private TablaEstilizada tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private BotonPrimario btnReservarLibro;
    private JPanel panelVistaCatalogoContenedor;
    private CardLayout cardCatalogoLayout;
    private boolean modoGrid = true;
    private List<Libro> listaLibrosActual = new ArrayList<>();
    private java.util.function.Function<Integer, Integer> contarDispActual;

    // Mis Préstamos
    private TablaEstilizada tablaPrestamos;
    private DefaultTableModel modeloPrestamos;
    private BotonAcento btnRenovarPrestamo;

    // Mis Reservas
    private TablaEstilizada tablaReservas;
    private DefaultTableModel modeloReservas;
    private BotonPeligro btnCancelarReserva;

    // Mis Multas
    private TablaEstilizada tablaMultas;
    private DefaultTableModel modeloMultas;
    private JLabel lblTotalMultas;

    // Cambiar Contraseña
    private CampoPassword txtClaveActual;
    private CampoPassword txtClaveNueva;
    private CampoPassword txtClaveConfirmar;
    private BotonAcento btnGuardarClave;

    public ClienteView() {
        Tema.aplicar();
        initComponents();
    }

    public void setController(ClienteController controller) {
        this.controller = controller;
    }

    private void initComponents() {
        setTitle("Sistema de Gestión de Biblioteca - Portal del Lector");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setIconImage(Iconos.crearAppLogoImage(64));

        JPanel panelRaiz = new JPanel(new BorderLayout());
        panelRaiz.setBackground(Tema.COLOR_FONDO);

        // Barra Lateral de Navegación
        barraLateral = new BarraNavegacionLateral("Lector", clave -> {
            if (cardLayout != null) {
                cardLayout.show(panelContenedorTarjetas, clave);
                actualizarTitulosSeccion(clave);
            }
        });

        barraLateral.agregarItem("DASHBOARD", "Mi Portal", Iconos.crearDashboard(20, Color.WHITE));
        barraLateral.agregarItem("CATALOGO", "Explorar Catálogo", Iconos.crearLibro(20, Color.WHITE));
        barraLateral.agregarItem("PRESTAMOS", "Mis Préstamos", Iconos.crearPrestamo(20, Color.WHITE));
        barraLateral.agregarItem("RESERVAS", "Mis Reservas", Iconos.crearReserva(20, Color.WHITE));
        barraLateral.agregarItem("MULTAS", "Mis Multas", Iconos.crearMulta(20, Color.WHITE));
        barraLateral.agregarItem("SEGURIDAD", "Mi Contraseña", Iconos.crearConfiguracion(20, Color.WHITE));

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
        panelContenedorTarjetas.add(crearVistaCatalogo(), "CATALOGO");
        panelContenedorTarjetas.add(crearVistaPrestamos(), "PRESTAMOS");
        panelContenedorTarjetas.add(crearVistaReservas(), "RESERVAS");
        panelContenedorTarjetas.add(crearVistaMultas(), "MULTAS");
        panelContenedorTarjetas.add(crearVistaSeguridad(), "SEGURIDAD");

        panelCentral.add(panelContenedorTarjetas, BorderLayout.CENTER);
        panelRaiz.add(panelCentral, BorderLayout.CENTER);

        setContentPane(panelRaiz);
    }

    private void actualizarTitulosSeccion(String clave) {
        switch (clave) {
            case "DASHBOARD" -> barraSuperior.setSeccion("Mi Portal de Lectura", "Resumen de tus libros y estado actual");
            case "CATALOGO" -> barraSuperior.setSeccion("Catálogo Bibliográfico", "Descubre y reserva títulos para lectura");
            case "PRESTAMOS" -> barraSuperior.setSeccion("Mis Préstamos Activos", "Fechas de entrega y renovación de plazos");
            case "RESERVAS" -> barraSuperior.setSeccion("Mis Reservas en Cola", "Estado de turnos de recojo en biblioteca");
            case "MULTAS" -> barraSuperior.setSeccion("Mis Multas y Moras", "Detalle de importes pendientes o cancelados");
            case "SEGURIDAD" -> barraSuperior.setSeccion("Seguridad de la Cuenta", "Cambio de contraseña personal de acceso");
        }
    }

    // --- VISTA DASHBOARD LECTOR ---
    private JPanel crearVistaDashboard() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // KPIs del Lector
        JPanel panelKpis = new JPanel(new GridLayout(1, 4, 16, 0));
        panelKpis.setOpaque(false);

        kpiMisPrestamos = new TarjetaMetrica("Libros en Préstamo", "0", Iconos.crearPrestamo(26, Tema.COLOR_PRIMARIO), Tema.COLOR_PRIMARIO);
        kpiProximoVencimiento = new TarjetaMetrica("Próximo Vencimiento", "Al día", Iconos.crearDevolucion(26, Tema.COLOR_ACENTO), Tema.COLOR_ACENTO);
        kpiMisReservas = new TarjetaMetrica("Mis Reservas", "0", Iconos.crearReserva(26, Tema.COLOR_INFO), Tema.COLOR_INFO);
        kpiMisMultas = new TarjetaMetrica("Multas Pendientes", "S/ 0.00", Iconos.crearMulta(26, Tema.COLOR_ACENTO), Tema.COLOR_ACENTO);

        panelKpis.add(kpiMisPrestamos);
        panelKpis.add(kpiProximoVencimiento);
        panelKpis.add(kpiMisReservas);
        panelKpis.add(kpiMisMultas);

        panel.add(panelKpis, BorderLayout.NORTH);

        // Banner de bienvenida
        TarjetaPanel tarjetaBienvenida = new TarjetaPanel(new BorderLayout(15, 15));
        String nombreUsuario = SesionActual.estaAutenticado() ? SesionActual.getUsuario().getNombre() : "Lector";

        JLabel lblSaludo = new JLabel("¡Hola, " + nombreUsuario + "! Bienvenido a tu biblioteca");
        lblSaludo.setFont(Tema.FUENTE_SUBTITULO);
        lblSaludo.setForeground(Tema.COLOR_TEXTO);

        JLabel lblTexto = new JLabel("<html><body style='width: 650px; color: #64748B; font-size: 13px; line-height: 1.5;'>" +
                "Recuerda revisar la fecha de entrega de tus libros prestados para evitar cargos por mora (tarifa por día de retraso). " +
                "Puedes explorar el catálogo completo y solicitar reservas anticipadas en cualquier momento." +
                "</body></html>");

        tarjetaBienvenida.add(lblSaludo, BorderLayout.NORTH);
        tarjetaBienvenida.add(lblTexto, BorderLayout.CENTER);

        JPanel panelAccesos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panelAccesos.setOpaque(false);

        BotonPrimario btnExplorar = new BotonPrimario("Explorar Libros", Iconos.crearLibro(16, Color.WHITE));
        btnExplorar.addActionListener(e -> barraLateral.seleccionar("CATALOGO"));

        BotonSecundario btnVerPrestamos = new BotonSecundario("Ver Mis Préstamos", Iconos.crearPrestamo(16, Tema.COLOR_PRIMARIO));
        btnVerPrestamos.addActionListener(e -> barraLateral.seleccionar("PRESTAMOS"));

        panelAccesos.add(btnExplorar);
        panelAccesos.add(btnVerPrestamos);
        tarjetaBienvenida.add(panelAccesos, BorderLayout.SOUTH);

        panel.add(tarjetaBienvenida, BorderLayout.CENTER);
        return panel;
    }

    // --- VISTA CATÁLOGO DEL CLIENTE (GRID DE TARJETAS Y TABLA) ---
    private JPanel crearVistaCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Barra Superior de Búsqueda y Filtros
        TarjetaPanel tarjetaBarra = new TarjetaPanel(new BorderLayout(15, 0));
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        panelIzquierda.setOpaque(false);

        panelIzquierda.add(new JLabel("Buscar:"));
        txtCatalogoBuscar = new CampoTexto("Título, Autor o ISBN...", 22);
        BotonSecundario btnBuscar = new BotonSecundario("Buscar", Iconos.crearBuscar(16, Tema.COLOR_PRIMARIO));
        BotonSecundario btnTodos = new BotonSecundario("Todos");

        panelIzquierda.add(txtCatalogoBuscar);
        panelIzquierda.add(btnBuscar);
        panelIzquierda.add(btnTodos);

        panelIzquierda.add(new JLabel("Categoría:"));
        comboCategorias = new JComboBox<>(new String[]{"Todas las Categorías"});
        comboCategorias.setFont(Tema.FUENTE_NORMAL);
        panelIzquierda.add(comboCategorias);

        tarjetaBarra.add(panelIzquierda, BorderLayout.WEST);

        // Botón para alternar Vista de Tarjetas / Tabla
        BotonSecundario btnAlternarVista = new BotonSecundario("Cambiar a Tabla / Tarjetas", Iconos.crearDashboard(16, Tema.COLOR_PRIMARIO));
        btnAlternarVista.addActionListener(e -> alternarVistaCatalogo());
        tarjetaBarra.add(btnAlternarVista, BorderLayout.EAST);

        panel.add(tarjetaBarra, BorderLayout.NORTH);

        // Contenedor dinámico (CardLayout entre Grid de Libros y Tabla)
        cardCatalogoLayout = new CardLayout();
        panelVistaCatalogoContenedor = new JPanel(cardCatalogoLayout);
        panelVistaCatalogoContenedor.setOpaque(false);

        // 1. Vista en Cuadrícula de Tarjetas (Cards)
        panelGridLibros = new JPanel(new GridLayout(0, 3, 16, 16));
        panelGridLibros.setOpaque(false);
        JScrollPane scrollGrid = new JScrollPane(panelGridLibros);
        scrollGrid.setOpaque(false);
        scrollGrid.getViewport().setOpaque(false);
        scrollGrid.setBorder(null);
        panelVistaCatalogoContenedor.add(scrollGrid, "GRID");

        // 2. Vista en Tabla Tradicional
        TarjetaPanel tarjetaTabla = new TarjetaPanel(new BorderLayout(0, 10));
        modeloCatalogo = new DefaultTableModel(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Disponibles"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCatalogo = new TablaEstilizada(modeloCatalogo);
        tablaCatalogo.setMensajeVacio("No hay libros disponibles en el catálogo.");
        tarjetaTabla.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        JPanel panelAccionTabla = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelAccionTabla.setOpaque(false);
        btnReservarLibro = new BotonPrimario("Reservar Libro Seleccionado", Iconos.crearReserva(16, Color.WHITE));
        panelAccionTabla.add(btnReservarLibro);
        tarjetaTabla.add(panelAccionTabla, BorderLayout.SOUTH);

        panelVistaCatalogoContenedor.add(tarjetaTabla, "TABLA");

        panel.add(panelVistaCatalogoContenedor, BorderLayout.CENTER);

        // Eventos
        Runnable runBuscarLib = () -> {
            if (controller != null) controller.buscarLibros(txtCatalogoBuscar.getText());
        };
        btnBuscar.addActionListener(e -> runBuscarLib.run());
        txtCatalogoBuscar.addActionListener(e -> runBuscarLib.run());

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

        btnTodos.addActionListener(e -> {
            txtCatalogoBuscar.setText("");
            comboCategorias.setSelectedIndex(0);
            if (controller != null) controller.cargarCatalogo();
        });

        comboCategorias.addActionListener(e -> filtrarPorCategoria());

        btnReservarLibro.addActionListener(e -> {
            int row = tablaCatalogo.getSelectedRow();
            if (row >= 0 && controller != null) {
                int libroId = (int) modeloCatalogo.getValueAt(row, 0);
                controller.solicitarReserva(libroId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un libro de la tabla para solicitar su reserva.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    private void alternarVistaCatalogo() {
        modoGrid = !modoGrid;
        cardCatalogoLayout.show(panelVistaCatalogoContenedor, modoGrid ? "GRID" : "TABLA");
    }

    private void renderizarGridTarjetas(List<Libro> libros, java.util.function.Function<Integer, Integer> contarDisp) {
        panelGridLibros.removeAll();

        if (libros.isEmpty()) {
            JPanel pVacio = new JPanel(new GridBagLayout());
            pVacio.setOpaque(false);
            JLabel lbl = new JLabel("No se encontraron libros con el filtro actual.");
            lbl.setFont(Tema.FUENTE_SUBTITULO);
            lbl.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
            pVacio.add(lbl);
            panelGridLibros.add(pVacio);
        } else {
            for (Libro l : libros) {
                int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
                panelGridLibros.add(crearTarjetaLibro(l, disp));
            }
        }

        panelGridLibros.revalidate();
        panelGridLibros.repaint();
    }

    private JPanel crearTarjetaLibro(Libro l, int disponibles) {
        TarjetaPanel card = new TarjetaPanel(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(280, 210));

        // Cabecera de la tarjeta: Categoría y Disponibilidad
        JPanel pTop = new JPanel(new BorderLayout());
        pTop.setOpaque(false);

        JLabel lblCat = new JLabel(l.getCategoria() != null ? l.getCategoria().toUpperCase() : "GENERAL");
        lblCat.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblCat.setForeground(Tema.COLOR_PRIMARIO);

        String estadoTexto = disponibles > 0 ? disponibles + " disp." : "Agotado";
        JLabel lblDisp = new JLabel(" " + estadoTexto + " ");
        lblDisp.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblDisp.setOpaque(true);
        if (disponibles > 0) {
            lblDisp.setBackground(Tema.COLOR_ACENTO_CLARO);
            lblDisp.setForeground(Tema.COLOR_ACENTO);
        } else {
            lblDisp.setBackground(Tema.COLOR_ADVERTENCIA_CLARO);
            lblDisp.setForeground(Tema.COLOR_ADVERTENCIA_OSCURO);
        }
        lblDisp.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        pTop.add(lblCat, BorderLayout.WEST);
        pTop.add(lblDisp, BorderLayout.EAST);
        card.add(pTop, BorderLayout.NORTH);

        // Cuerpo: Título y Autor
        JPanel pMid = new JPanel();
        pMid.setLayout(new BoxLayout(pMid, BoxLayout.Y_AXIS));
        pMid.setOpaque(false);

        JLabel lblTit = new JLabel("<html><b>" + l.getTitulo() + "</b></html>");
        lblTit.setFont(Tema.FUENTE_SECCION);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblAut = new JLabel("Por " + l.getAutor());
        lblAut.setFont(Tema.FUENTE_PEQUENA);
        lblAut.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        JLabel lblIsbn = new JLabel("ISBN: " + l.getIsbn() + (l.getAnio() != null ? " • " + l.getAnio() : ""));
        lblIsbn.setFont(Tema.FUENTE_PEQUENA);
        lblIsbn.setForeground(new Color(148, 163, 184));

        pMid.add(lblTit);
        pMid.add(Box.createVerticalStrut(4));
        pMid.add(lblAut);
        pMid.add(Box.createVerticalStrut(4));
        pMid.add(lblIsbn);
        card.add(pMid, BorderLayout.CENTER);

        // Pie: Botón Reservar
        JPanel pBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pBot.setOpaque(false);

        if (disponibles == 0) {
            BotonPrimario btnRes = new BotonPrimario("Reservar (Turno FIFO)", Iconos.crearReserva(14, Color.WHITE));
            btnRes.setFont(Tema.FUENTE_PEQUENA_BOLD);
            btnRes.addActionListener(e -> {
                if (controller != null) controller.solicitarReserva(l.getId());
            });
            pBot.add(btnRes);
        } else {
            BotonSecundario btnDisponible = new BotonSecundario("Disponible en Sala", Iconos.crearCheck(14, Tema.COLOR_ACENTO));
            btnDisponible.setFont(Tema.FUENTE_PEQUENA_BOLD);
            btnDisponible.setEnabled(false);
            pBot.add(btnDisponible);
        }

        card.add(pBot, BorderLayout.SOUTH);
        return card;
    }

    private void filtrarPorCategoria() {
        String seleccionada = (String) comboCategorias.getSelectedItem();
        if (seleccionada == null || seleccionada.equals("Todas las Categorías")) {
            renderizarGridTarjetas(listaLibrosActual, contarDispActual);
        } else {
            List<Libro> filtrada = listaLibrosActual.stream()
                    .filter(l -> seleccionada.equalsIgnoreCase(l.getCategoria()))
                    .toList();
            renderizarGridTarjetas(filtrada, contarDispActual);
        }
    }

    // --- VISTA MIS PRÉSTAMOS ---
    private JPanel crearVistaPrestamos() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjeta = new TarjetaPanel(new BorderLayout(0, 10));
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblTit = new JLabel("Historial y Préstamos Activos");
        lblTit.setFont(Tema.FUENTE_SECCION);
        panelHeader.add(lblTit, BorderLayout.WEST);

        btnRenovarPrestamo = new BotonAcento("Renovar Plazo de Préstamo", Iconos.crearActualizar(16, Color.WHITE));
        panelHeader.add(btnRenovarPrestamo, BorderLayout.EAST);
        tarjeta.add(panelHeader, BorderLayout.NORTH);

        modeloPrestamos = new DefaultTableModel(new String[]{"ID Préstamo", "Ejemplar ID", "Fecha Préstamo", "Vencimiento", "Fecha Devolución", "Estado", "Renovaciones"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPrestamos = new TablaEstilizada(modeloPrestamos);
        tablaPrestamos.setMensajeVacio("No cuentas con préstamos activos ni vencidos en este momento.");
        tarjeta.add(new JScrollPane(tablaPrestamos), BorderLayout.CENTER);

        panel.add(tarjeta, BorderLayout.CENTER);

        btnRenovarPrestamo.addActionListener(e -> {
            int row = tablaPrestamos.getSelectedRow();
            if (row >= 0 && controller != null) {
                int prestamoId = (int) modeloPrestamos.getValueAt(row, 0);
                controller.renovarPrestamo(prestamoId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione un préstamo de la tabla para solicitar su renovación.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    // --- VISTA MIS RESERVAS ---
    private JPanel crearVistaReservas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjeta = new TarjetaPanel(new BorderLayout(0, 10));
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblTit = new JLabel("Mis Reservas y Posición en Cola");
        lblTit.setFont(Tema.FUENTE_SECCION);
        panelHeader.add(lblTit, BorderLayout.WEST);

        btnCancelarReserva = new BotonPeligro("Cancelar Mi Reserva", Iconos.crearEliminar(16, Color.WHITE));
        panelHeader.add(btnCancelarReserva, BorderLayout.EAST);
        tarjeta.add(panelHeader, BorderLayout.NORTH);

        modeloReservas = new DefaultTableModel(new String[]{"ID Reserva", "Libro ID", "Ejemplar Asignado", "Fecha Registro", "Fecha Límite Recojo", "Estado"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaReservas = new TablaEstilizada(modeloReservas);
        tablaReservas.setMensajeVacio("No tienes reservas registradas en este momento.");
        tarjeta.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);

        panel.add(tarjeta, BorderLayout.CENTER);

        btnCancelarReserva.addActionListener(e -> {
            int row = tablaReservas.getSelectedRow();
            if (row >= 0 && controller != null) {
                int reservaId = (int) modeloReservas.getValueAt(row, 0);
                controller.cancelarReserva(reservaId);
            } else {
                DialogoModerno.mostrarMensaje(this, "Seleccione una reserva de la tabla para cancelarla.", "Atención", DialogoModerno.TipoDialogo.ADVERTENCIA);
            }
        });

        return panel;
    }

    // --- VISTA MIS MULTAS ---
    private JPanel crearVistaMultas() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        TarjetaPanel tarjeta = new TarjetaPanel(new BorderLayout(0, 10));
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setOpaque(false);

        JLabel lblTit = new JLabel("Mis Multas y Sanciones por Retraso");
        lblTit.setFont(Tema.FUENTE_SECCION);
        panelHeader.add(lblTit, BorderLayout.WEST);

        lblTotalMultas = new JLabel("Total Pendiente: S/ 0.00");
        lblTotalMultas.setFont(Tema.FUENTE_SECCION);
        lblTotalMultas.setForeground(Tema.COLOR_PELIGRO);
        panelHeader.add(lblTotalMultas, BorderLayout.EAST);
        tarjeta.add(panelHeader, BorderLayout.NORTH);

        modeloMultas = new DefaultTableModel(new String[]{"ID Multa", "ID Préstamo", "Días Retraso", "Tarifa / Día", "Monto Total", "Estado", "Fecha Pago"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMultas = new TablaEstilizada(modeloMultas);
        tablaMultas.setMensajeVacio("¡Felicitaciones! No tienes multas pendientes de pago.");
        tarjeta.add(new JScrollPane(tablaMultas), BorderLayout.CENTER);

        panel.add(tarjeta, BorderLayout.CENTER);
        return panel;
    }

    // --- VISTA CAMBIAR CONTRASEÑA ---
    private JPanel crearVistaSeguridad() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        TarjetaPanel tarjeta = new TarjetaPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setPreferredSize(new Dimension(480, 430));
        tarjeta.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel lblTit = new JLabel("Actualizar Contraseña de Acceso");
        lblTit.setFont(Tema.FUENTE_SUBTITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Asegúrese de incluir mayúsculas, minúsculas y números (mínimo 8 caracteres).");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        tarjeta.add(lblTit);
        tarjeta.add(Box.createVerticalStrut(4));
        tarjeta.add(lblSub);
        tarjeta.add(Box.createVerticalStrut(20));

        txtClaveActual = new CampoPassword("Contraseña actual");
        tarjeta.add(crearCampoSeguridad("Contraseña actual:", txtClaveActual));
        tarjeta.add(Box.createVerticalStrut(14));

        txtClaveNueva = new CampoPassword("Nueva contraseña segura");
        tarjeta.add(crearCampoSeguridad("Nueva contraseña:", txtClaveNueva));
        tarjeta.add(Box.createVerticalStrut(14));

        txtClaveConfirmar = new CampoPassword("Repita la nueva contraseña");
        tarjeta.add(crearCampoSeguridad("Confirmar nueva contraseña:", txtClaveConfirmar));
        tarjeta.add(Box.createVerticalStrut(24));

        btnGuardarClave = new BotonAcento("Actualizar Mi Contraseña", Iconos.crearCheck(18, Color.WHITE));
        btnGuardarClave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnGuardarClave.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGuardarClave.addActionListener(e -> {
            if (controller != null) {
                controller.cambiarContrasena(
                        txtClaveActual.getPasswordString(),
                        txtClaveNueva.getPasswordString(),
                        txtClaveConfirmar.getPasswordString()
                );
            }
        });

        tarjeta.add(btnGuardarClave);
        panel.add(tarjeta);
        return panel;
    }

    private JPanel crearCampoSeguridad(String etiqueta, Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(etiqueta);
        l.setFont(Tema.FUENTE_SECCION);
        l.setForeground(Tema.COLOR_TEXTO);

        p.add(l, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    // --- MÉTODOS PÚBLICOS DE ACTUALIZACIÓN (COMPATIBILIDAD CON CONTROLLER) ---
    public void cargarTablaCatalogo(List<Libro> libros, java.util.function.Function<Integer, Integer> contarDisp) {
        this.listaLibrosActual = libros;
        this.contarDispActual = contarDisp;

        // Actualizar opciones de categoría en el dropdown
        comboCategorias.removeAllItems();
        comboCategorias.addItem("Todas las Categorías");
        libros.stream().map(Libro::getCategoria)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct().sorted()
                .forEach(comboCategorias::addItem);

        // Actualizar modelo de tabla
        modeloCatalogo.setRowCount(0);
        for (Libro l : libros) {
            int disp = contarDisp != null ? contarDisp.apply(l.getId()) : 0;
            modeloCatalogo.addRow(new Object[]{
                    l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), disp
            });
        }

        // Actualizar cuadrícula de tarjetas
        renderizarGridTarjetas(libros, contarDisp);

        if (tablaCatalogo.getColumnCount() >= 7) {
            tablaCatalogo.fijarAnchoColumna(0, 45, 50, 60);    // ID
            tablaCatalogo.fijarAnchoColumna(1, 120, 135, 150); // ISBN
            tablaCatalogo.fijarAnchoColumna(2, 200, 270, 0);   // Título
            tablaCatalogo.fijarAnchoColumna(3, 140, 170, 220); // Autor
            tablaCatalogo.fijarAnchoColumna(4, 100, 120, 150); // Categoría
            tablaCatalogo.fijarAnchoColumna(5, 110, 130, 160); // Editorial
            tablaCatalogo.fijarAnchoColumna(6, 75, 85, 105);   // Disponibles
        }
    }

    public void cargarTablaPrestamos(List<Prestamo> prestamos) {
        modeloPrestamos.setRowCount(0);
        int activos = 0;
        LocalDate proximoVenc = null;

        for (Prestamo p : prestamos) {
            modeloPrestamos.addRow(new Object[]{
                    p.getId(), p.getEjemplarId(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                    p.getFechaDevolucion() != null ? p.getFechaDevolucion() : "-",
                    p.getEstado(), p.getRenovaciones()
            });

            if ("ACTIVO".equalsIgnoreCase(p.getEstado().name())) {
                activos++;
                if (proximoVenc == null || p.getFechaVencimiento().isBefore(proximoVenc)) {
                    proximoVenc = p.getFechaVencimiento();
                }
            }
        }

        if (kpiMisPrestamos != null) kpiMisPrestamos.setValor(String.valueOf(activos));
        if (kpiProximoVencimiento != null) {
            kpiProximoVencimiento.setValor(proximoVenc != null ? proximoVenc.toString() : "Al día");
        }
        if (tablaPrestamos.getColumnCount() >= 7) {
            tablaPrestamos.fijarAnchoColumna(0, 45, 50, 60);   // ID
            tablaPrestamos.fijarAnchoColumna(1, 80, 95, 110);  // Ejemplar ID
            tablaPrestamos.fijarAnchoColumna(2, 100, 120, 135);// Fecha Préstamo
            tablaPrestamos.fijarAnchoColumna(3, 100, 120, 135);// Fecha Vencimiento
            tablaPrestamos.fijarAnchoColumna(4, 100, 120, 135);// Fecha Devolución
            tablaPrestamos.fijarAnchoColumna(5, 100, 120, 140);// Estado
            tablaPrestamos.fijarAnchoColumna(6, 80, 95, 110);  // Renovaciones
        }
    }

    public void cargarTablaReservas(List<Reserva> reservas) {
        modeloReservas.setRowCount(0);
        int activas = 0;

        for (Reserva r : reservas) {
            modeloReservas.addRow(new Object[]{
                    r.getId(), r.getLibroId(), r.getEjemplarId() != null ? r.getEjemplarId() : "-",
                    r.getFechaReserva(), r.getFechaExpiracion() != null ? r.getFechaExpiracion() : "-", r.getEstado()
            });
            if ("PENDIENTE".equalsIgnoreCase(r.getEstado().name()) || "LISTA".equalsIgnoreCase(r.getEstado().name())) {
                activas++;
            }
        }

        if (kpiMisReservas != null) kpiMisReservas.setValor(String.valueOf(activas));
        if (tablaReservas.getColumnCount() >= 6) {
            tablaReservas.fijarAnchoColumna(0, 45, 50, 60);   // ID
            tablaReservas.fijarAnchoColumna(1, 75, 85, 100);  // Libro ID
            tablaReservas.fijarAnchoColumna(2, 75, 85, 100);  // Ejemplar ID
            tablaReservas.fijarAnchoColumna(3, 100, 120, 135);// Fecha Reserva
            tablaReservas.fijarAnchoColumna(4, 100, 120, 135);// Fecha Expiración
            tablaReservas.fijarAnchoColumna(5, 100, 120, 140);// Estado
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
        if (kpiMisMultas != null) kpiMisMultas.setValor("S/ " + totalPendiente.toPlainString());
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

    public void limpiarFormSeguridad() {
        txtClaveActual.setText("");
        txtClaveNueva.setText("");
        txtClaveConfirmar.setText("");
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
