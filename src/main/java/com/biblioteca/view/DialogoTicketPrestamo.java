package com.biblioteca.view;

import com.biblioteca.model.TicketPrestamo;
import com.biblioteca.service.NotificacionService;
import com.biblioteca.view.componentes.*;
import com.biblioteca.view.theme.Iconos;
import com.biblioteca.view.theme.Tema;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.format.DateTimeFormatter;

/**
 * Diálogo modal para la visualización, impresión y despacho de notificaciones
 * (Push WhatsApp / Correo electrónico) del ticket de comprobante de préstamo.
 */
public class DialogoTicketPrestamo extends JDialog {

    private final TicketPrestamo ticket;
    private final NotificacionService notificacionService;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblEstadoNotificacion;

    /**
     * Construye el diálogo modal de emisión de comprobante de préstamo.
     *
     * @param parent Ventana propietaria para centrado y modalidad
     * @param ticket Objeto modelo con los datos del préstamo y lector
     * @param notificacionService Servicio para despacho de notificaciones Push y correo
     */
    public DialogoTicketPrestamo(Window parent, TicketPrestamo ticket, NotificacionService notificacionService) {
        super(parent, "Ticket de Préstamo - Emisión Exitosa", ModalityType.APPLICATION_MODAL);
        this.ticket = ticket;
        this.notificacionService = notificacionService;

        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 720);
        setResizable(false);
        setLocationRelativeTo(getParent());

        JPanel panelRaiz = new JPanel(new BorderLayout(0, 16));
        panelRaiz.setBackground(Tema.COLOR_FONDO);
        panelRaiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Cabecera del diálogo con ícono de éxito
        JPanel panelHeader = new JPanel(new BorderLayout(12, 0));
        panelHeader.setOpaque(false);

        JLabel lblIcono = new JLabel(Iconos.crearCheck(32, Tema.COLOR_ACENTO));
        panelHeader.add(lblIcono, BorderLayout.WEST);

        JPanel panelHeaderTxt = new JPanel(new GridLayout(2, 1, 0, 2));
        panelHeaderTxt.setOpaque(false);
        JLabel lblTit = new JLabel("¡Préstamo Registrado con Éxito!");
        lblTit.setFont(Tema.FUENTE_TITULO);
        lblTit.setForeground(Tema.COLOR_TEXTO);

        JLabel lblSub = new JLabel("Comprobante de circulación y notificación al lector");
        lblSub.setFont(Tema.FUENTE_PEQUENA);
        lblSub.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);

        panelHeaderTxt.add(lblTit);
        panelHeaderTxt.add(lblSub);
        panelHeader.add(panelHeaderTxt, BorderLayout.CENTER);
        panelRaiz.add(panelHeader, BorderLayout.NORTH);

        // Tarjeta central estilo comprobante/ticket
        TarjetaPanel tarjetaTicket = new TarjetaPanel(new BorderLayout(0, 12));
        tarjetaTicket.setBackground(Color.WHITE);
        tarjetaTicket.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.COLOR_BORDE, 1, true),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));

        // Encabezado del ticket
        JPanel headerTicket = new JPanel(new BorderLayout());
        headerTicket.setOpaque(false);

        JLabel lblInstitucion = new JLabel("BIBLIOTECA CI • COMPROBANTE");
        lblInstitucion.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 14));
        lblInstitucion.setForeground(Tema.COLOR_PRIMARIO);

        JLabel lblNro = new JLabel("Ticket #" + String.format("%06d", ticket.getPrestamoId()));
        lblNro.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 14));
        lblNro.setForeground(Tema.COLOR_TEXTO);

        headerTicket.add(lblInstitucion, BorderLayout.WEST);
        headerTicket.add(lblNro, BorderLayout.EAST);

        // Cuerpo con detalles en GridBagLayout
        JPanel panelDetalles = new JPanel(new GridBagLayout());
        panelDetalles.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        int fila = 0;
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Lector / Solicitante:", ticket.getClienteNombre());
        agregarFilaDetalle(panelDetalles, gbc, fila++, "DNI:", ticket.getClienteDni());
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Móvil / WhatsApp:",
                ticket.getClienteTelefono() != null && !ticket.getClienteTelefono().isBlank() ? ticket.getClienteTelefono() : "No registrado");
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Correo Electrónico:",
                ticket.getClienteEmail() != null && !ticket.getClienteEmail().isBlank() ? ticket.getClienteEmail() : "No registrado");
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Obra Bibliográfica:", ticket.getLibroTitulo());
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Autor:", ticket.getLibroAutor());
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Código Físico:", ticket.getCodigoBarras());
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Fecha de Préstamo:", ticket.getFechaPrestamo().format(FORMATO));
        agregarFilaDetalle(panelDetalles, gbc, fila++, "Plazo Concedido:", ticket.getDiasOtorgados() + " día(s)");

        // Tarjeta destacada de Fecha Límite de Devolución
        JPanel panelVencimiento = new JPanel(new BorderLayout(10, 4));
        panelVencimiento.setBackground(new Color(254, 242, 242)); // Rojo suave
        panelVencimiento.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(248, 113, 113), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitVence = new JLabel("FECHA LÍMITE DE DEVOLUCIÓN REQUERIDA:");
        lblTitVence.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblTitVence.setForeground(Tema.COLOR_PELIGRO);

        JLabel lblFechaVence = new JLabel(ticket.getFechaVencimiento().format(FORMATO));
        lblFechaVence.setFont(new Font(Tema.NOMBRE_FUENTE, Font.BOLD, 22));
        lblFechaVence.setForeground(Tema.COLOR_PELIGRO);

        JLabel lblAvisoMora = new JLabel("Tarifa por día de mora: S/ " + ticket.getTarifaMultaPorDia() + " tras el vencimiento.");
        lblAvisoMora.setFont(Tema.FUENTE_PEQUENA);
        lblAvisoMora.setForeground(new Color(153, 27, 27));

        panelVencimiento.add(lblTitVence, BorderLayout.NORTH);
        panelVencimiento.add(lblFechaVence, BorderLayout.CENTER);
        panelVencimiento.add(lblAvisoMora, BorderLayout.SOUTH);

        // Ensamblar Tarjeta del Ticket
        JPanel panelCentro = new JPanel(new BorderLayout(0, 10));
        panelCentro.setOpaque(false);
        panelCentro.add(headerTicket, BorderLayout.NORTH);
        panelCentro.add(panelDetalles, BorderLayout.CENTER);
        panelCentro.add(panelVencimiento, BorderLayout.SOUTH);

        tarjetaTicket.add(panelCentro, BorderLayout.CENTER);
        panelRaiz.add(tarjetaTicket, BorderLayout.CENTER);

        // Pie: Estado de notificación y botones de acción
        JPanel panelInferior = new JPanel(new BorderLayout(0, 10));
        panelInferior.setOpaque(false);

        lblEstadoNotificacion = new JLabel("Listo para despachar comprobante digital o físico.", SwingConstants.CENTER);
        lblEstadoNotificacion.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblEstadoNotificacion.setForeground(Tema.COLOR_PRIMARIO);
        panelInferior.add(lblEstadoNotificacion, BorderLayout.NORTH);

        // Botones de acción rápida
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));
        panelBotones.setOpaque(false);

        BotonPrimario btnPush = new BotonPrimario("Enviar WhatsApp / SMS", Iconos.crearCheck(16, Color.WHITE));
        BotonAcento btnCorreo = new BotonAcento("Enviar Correo Confirmación", Iconos.crearCheck(16, Color.WHITE));
        BotonSecundario btnCopiar = new BotonSecundario("Copiar Texto del Ticket", Iconos.crearReportes(16, Tema.COLOR_PRIMARIO));
        BotonSecundario btnCerrar = new BotonSecundario("Cerrar", null);

        panelBotones.add(btnPush);
        panelBotones.add(btnCorreo);
        panelBotones.add(btnCopiar);
        panelBotones.add(btnCerrar);

        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        panelRaiz.add(panelInferior, BorderLayout.SOUTH);

        setContentPane(panelRaiz);

        // Eventos de botones
        btnPush.addActionListener(e -> {
            boolean ok = notificacionService.enviarPushWhatsApp(ticket);
            if (ok) {
                lblEstadoNotificacion.setText("Push / WhatsApp despachado al móvil: " +
                        (ticket.getClienteTelefono() != null ? ticket.getClienteTelefono() : "cliente"));
                lblEstadoNotificacion.setForeground(Tema.COLOR_ACENTO);
                DialogoModerno.mostrarMensaje(this,
                        "Notificación Push / WhatsApp preparada exitosamente.\n" +
                                "Destinatario: " + ticket.getClienteNombre() + " (" + ticket.getClienteTelefono() + ")\n" +
                                "Devolución pactada: " + ticket.getFechaVencimiento().format(FORMATO),
                        "Push Enviado", DialogoModerno.TipoDialogo.EXITO);
            }
        });

        btnCorreo.addActionListener(e -> {
            boolean ok = notificacionService.enviarCorreo(ticket);
            if (ok) {
                lblEstadoNotificacion.setText("Correo de confirmación enviado a: " +
                        (ticket.getClienteEmail() != null ? ticket.getClienteEmail() : "cliente"));
                lblEstadoNotificacion.setForeground(Tema.COLOR_ACENTO);
                DialogoModerno.mostrarMensaje(this,
                        "Correo de confirmación enviado con éxito.\n" +
                                "Destinatario: " + ticket.getClienteEmail() + "\n" +
                                "Fecha límite de entrega: " + ticket.getFechaVencimiento().format(FORMATO),
                        "Correo Despachado", DialogoModerno.TipoDialogo.EXITO);
            }
        });

        btnCopiar.addActionListener(e -> {
            String texto = notificacionService.generarTextoImprimible(ticket);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
            lblEstadoNotificacion.setText("¡Comprobante copiado al portapapeles con éxito!");
            lblEstadoNotificacion.setForeground(Tema.COLOR_PRIMARIO);
        });

        btnCerrar.addActionListener(e -> dispose());
    }

    private void agregarFilaDetalle(JPanel p, GridBagConstraints gbc, int fila, String etiqueta, String valor) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.38;
        JLabel lblE = new JLabel(etiqueta);
        lblE.setFont(Tema.FUENTE_PEQUENA_BOLD);
        lblE.setForeground(Tema.COLOR_TEXTO_SECUNDARIO);
        p.add(lblE, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.62;
        JLabel lblV = new JLabel(valor);
        lblV.setFont(Tema.FUENTE_NORMAL);
        lblV.setForeground(Tema.COLOR_TEXTO);
        p.add(lblV, gbc);
    }
}

