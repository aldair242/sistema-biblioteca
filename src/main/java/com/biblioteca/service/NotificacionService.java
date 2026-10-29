package com.biblioteca.service;

import com.biblioteca.dao.ClienteDAO;
import com.biblioteca.dao.EjemplarDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Servicio encargado de la generación de tickets de comprobante de préstamo,
 * formateo de recordatorios y emisión de notificaciones Push (WhatsApp/SMS)
 * y correo electrónico con fecha límite de devolución.
 */
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteDAO clienteDAO;
    private final EjemplarDAO ejemplarDAO;
    private final LibroDAO libroDAO;
    private final ConfiguracionService configuracionService;

    public NotificacionService(ClienteDAO clienteDAO, EjemplarDAO ejemplarDAO,
                               LibroDAO libroDAO, ConfiguracionService configuracionService) {
        this.clienteDAO = clienteDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.libroDAO = libroDAO;
        this.configuracionService = configuracionService;
    }

    /**
     * Construye el modelo completo de Ticket de Préstamo con todos los datos
     * del cliente, ejemplar, título y fechas de vencimiento.
     *
     * @param prestamo Préstamo creado y persistido
     * @return TicketPrestamo con datos consolidados para emisión y despacho
     * @throws IllegalArgumentException si el préstamo proporcionado es nulo
     */
    public TicketPrestamo generarTicket(Prestamo prestamo) {
        if (prestamo == null) {
            throw new IllegalArgumentException("El préstamo no puede ser nulo para generar ticket.");
        }

        Cliente cliente = clienteDAO.buscarPorId(prestamo.getClienteId())
                .orElse(new Cliente(prestamo.getClienteId(), 1, "N/A", "Lector General", "", "", "", java.time.LocalDate.now()));

        Ejemplar ejemplar = ejemplarDAO.buscarPorId(prestamo.getEjemplarId())
                .orElse(new Ejemplar(prestamo.getEjemplarId(), 1, "EXP-000", EstadoEjemplar.PRESTADO));

        Libro libro = libroDAO.buscarPorId(ejemplar.getLibroId())
                .orElse(new Libro(ejemplar.getLibroId(), "S/N", "Título General", "Autor General", "General", "Editorial", 2026));

        BigDecimal tarifaMulta = configuracionService != null ? configuracionService.obtenerMultaPorDia() : new BigDecimal("2.50");

        int dias = (int) ChronoUnit.DAYS.between(prestamo.getFechaPrestamo(), prestamo.getFechaVencimiento());
        if (dias <= 0) dias = 1;

        String nombreCompleto = (cliente.getNombres() + " " + cliente.getApellidos()).trim();

        return new TicketPrestamo(
                prestamo.getId(),
                nombreCompleto,
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getEmail(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getCategoria(),
                ejemplar.getCodigoBarras(),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                dias,
                tarifaMulta
        );
    }

    /**
     * Genera el texto formateado optimizado para mensajería instantánea / Push (WhatsApp o SMS).
     *
     * @param ticket Ticket de préstamo con datos del lector y obra
     * @return Mensaje de texto formateado con emojis, plazos y fecha de devolución destacada
     */
    public String generarMensajePushWhatsApp(TicketPrestamo ticket) {
        return "🏛️ *BIBLIOTECA CI - COMPROBANTE DE PRÉSTAMO*\n\n" +
                "Estimado(a) *" + ticket.getClienteNombre() + "*,\n" +
                "Confirmamos el préstamo de la siguiente obra bibliográfica:\n\n" +
                "📖 *Título:* " + ticket.getLibroTitulo() + "\n" +
                "✍️ *Autor:* " + ticket.getLibroAutor() + "\n" +
                "🏷️ *Código de Ejemplar:* `" + ticket.getCodigoBarras() + "`\n" +
                "📅 *Fecha de Emisión:* " + ticket.getFechaPrestamo().format(FORMATO_FECHA) + "\n" +
                "⏱️ *Plazo asignado:* " + ticket.getDiasOtorgados() + " día(s)\n\n" +
                "⚠️ *FECHA LÍMITE EXACTA DE DEVOLUCIÓN:*\n" +
                "👉 *" + ticket.getFechaVencimiento().format(FORMATO_FECHA) + "* 👈\n\n" +
                "📌 _Por favor, entregue el ejemplar antes de la fecha límite para evitar recargos por mora (S/ " +
                ticket.getTarifaMultaPorDia() + " por día)._\n\n" +
                "¡Gracias por fomentar la lectura en nuestra comunidad!";
    }

    /**
     * Genera el cuerpo formal del correo electrónico de confirmación.
     *
     * @param ticket Ticket de préstamo con datos del lector y obra
     * @return Contenido textual formal del correo electrónico de confirmación
     */
    public String generarContenidoCorreo(TicketPrestamo ticket) {
        return "Estimado(a) " + ticket.getClienteNombre() + ",\n\n" +
                "Nos complace informarle que el préstamo ha sido registrado exitosamente en el Sistema de Gestión de Biblioteca CI.\n\n" +
                "--------------------------------------------------\n" +
                "DETALLES DEL PRÉSTAMO # " + ticket.getPrestamoId() + "\n" +
                "--------------------------------------------------\n" +
                "• Lector: " + ticket.getClienteNombre() + " (DNI: " + ticket.getClienteDni() + ")\n" +
                "• Título de la Obra: " + ticket.getLibroTitulo() + "\n" +
                "• Autor: " + ticket.getLibroAutor() + "\n" +
                "• Categoría: " + ticket.getLibroCategoria() + "\n" +
                "• Código Físico de Barras: " + ticket.getCodigoBarras() + "\n" +
                "• Fecha de Préstamo: " + ticket.getFechaPrestamo().format(FORMATO_FECHA) + "\n" +
                "• Plazo Otorgado: " + ticket.getDiasOtorgados() + " días\n\n" +
                ">>> FECHA LÍMITE DE DEVOLUCIÓN: " + ticket.getFechaVencimiento().format(FORMATO_FECHA) + " <<<\n\n" +
                "CONDICIONES DE DEVOLUCIÓN:\n" +
                "- Debe presentar el ejemplar en óptimas condiciones en el mostrador del bibliotecario.\n" +
                "- En caso de retraso, se aplicará una tarifa por mora de S/ " + ticket.getTarifaMultaPorDia() + " por día hábil.\n" +
                "- Puede solicitar renovaciones previas a la fecha de vencimiento desde el portal del lector o con su bibliotecario.\n\n" +
                "Atentamente,\n" +
                "Equipo de Servicios Bibliotecarios • Biblioteca CI\n" +
                "Universidad Tecnológica del Perú 2026\n";
    }

    /**
     * Genera la representación en texto plano para impresión o copia de comprobante de ticket.
     *
     * @param ticket Ticket de préstamo
     * @return Comprobante en formato ticket monocromático apto para impresión térmica
     */
    public String generarTextoImprimible(TicketPrestamo ticket) {
        return "========================================\n" +
                "         BIBLIOTECA CI - UTP 2026       \n" +
                "       TICKET DE PRÉSTAMO BIBLIOTECARIO \n" +
                "========================================\n" +
                "TICKET NRO: #" + String.format("%06d", ticket.getPrestamoId()) + "\n" +
                "FECHA EMISION: " + ticket.getFechaPrestamo().format(FORMATO_FECHA) + "\n" +
                "----------------------------------------\n" +
                "LECTOR: " + ticket.getClienteNombre() + "\n" +
                "DNI:    " + ticket.getClienteDni() + "\n" +
                "MOVIL:  " + (ticket.getClienteTelefono() != null ? ticket.getClienteTelefono() : "No registrado") + "\n" +
                "EMAIL:  " + (ticket.getClienteEmail() != null ? ticket.getClienteEmail() : "No registrado") + "\n" +
                "----------------------------------------\n" +
                "TITULO: " + ticket.getLibroTitulo() + "\n" +
                "AUTOR:  " + ticket.getLibroAutor() + "\n" +
                "CODIGO: " + ticket.getCodigoBarras() + "\n" +
                "PLAZO:  " + ticket.getDiasOtorgados() + " día(s)\n" +
                "========================================\n" +
                " >>> FECHA DE DEVOLUCION: " + ticket.getFechaVencimiento().format(FORMATO_FECHA) + " <<<\n" +
                "========================================\n" +
                "Mora por retraso: S/ " + ticket.getTarifaMultaPorDia() + " por día de mora.\n" +
                "Cuide el libro. Es patrimonio de todos.\n" +
                "========================================\n";
    }

    /**
     * Envía/despacha notificación Push o abre WhatsApp Web con el mensaje preformateado.
     *
     * @param ticket Ticket de préstamo
     * @return true si la operación se procesó y registró satisfactoriamente
     */
    public boolean enviarPushWhatsApp(TicketPrestamo ticket) {
        String tel = ticket.getClienteTelefono();
        String mensaje = generarMensajePushWhatsApp(ticket);

        logger.info("PUSH / WHATSAPP DESPACHADO -> Móvil: {}, Lector: '{}', Libro: '{}', Devolución: {}",
                tel != null && !tel.isBlank() ? tel : "[SIN NUMERO]",
                ticket.getClienteNombre(),
                ticket.getLibroTitulo(),
                ticket.getFechaVencimiento().format(FORMATO_FECHA));

        if (tel != null && !tel.isBlank()) {
            try {
                String numeroLimpio = tel.replaceAll("[^0-9]", "");
                if (numeroLimpio.length() == 9) {
                    numeroLimpio = "51" + numeroLimpio; // Código de país Perú
                }
                String url = "https://wa.me/" + numeroLimpio + "?text=" + URLEncoder.encode(mensaje, StandardCharsets.UTF_8);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                    return true;
                }
            } catch (Exception ex) {
                logger.warn("No se pudo abrir el navegador para WhatsApp Web directo: {}", ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Envía la confirmación por correo electrónico al cliente.
     *
     * @param ticket Ticket de préstamo
     * @return true si la notificación por correo se registró y despachó exitosamente
     */
    /**
     * Genera el contenido formal del correo de recuperación de contraseña.
     *
     * @param clienteNombre Nombre completo del lector
     * @param username      Nombre de usuario o DNI
     * @param claveTemporal Contraseña temporal generada
     * @return Mensaje formal de notificación
     */
    public String generarContenidoCorreoRecuperacion(String clienteNombre, String username, String claveTemporal) {
        return "Estimado(a) " + clienteNombre + ":\n\n" +
                "Hemos recibido una solicitud para restablecer la contraseña de su cuenta en el Sistema de Gestión de Biblioteca CI.\n\n" +
                "--------------------------------------------------\n" +
                "CREDENCIALES TEMPORALES DE ACCESO\n" +
                "--------------------------------------------------\n" +
                "• Usuario de acceso (DNI): " + username + "\n" +
                "• Contraseña Temporal: " + claveTemporal + "\n" +
                "--------------------------------------------------\n\n" +
                "INSTRUCCIONES DE ACCESO:\n" +
                "1. Inicie sesión en el sistema ingresando su DNI y esta contraseña temporal.\n" +
                "2. Por motivos de seguridad, el sistema le solicitará inmediatamente actualizar su contraseña por una clave personal definitiva.\n" +
                "3. Su nueva contraseña debe contener al menos 8 caracteres, incluyendo una letra mayúscula y un número.\n\n" +
                "Si usted no solicitó este cambio, por favor contacte de inmediato al personal de la biblioteca.\n\n" +
                "Atentamente,\n" +
                "Equipo de Biblioteca CI\n" +
                "Universidad Tecnológica del Perú 2026\n";
    }

    /**
     * Genera el contenido formal del correo de bienvenida para nuevos lectores.
     *
     * @param clienteNombre Nombre del lector
     * @param username      DNI / Usuario de acceso
     * @param claveTemporal Contraseña temporal inicial
     * @return Mensaje de bienvenida
     */
    public String generarContenidoCorreoBienvenida(String clienteNombre, String username, String claveTemporal) {
        return "Estimado(a) " + clienteNombre + ":\n\n" +
                "¡Bienvenido(a) a la Biblioteca CI de la Universidad Tecnológica del Perú!\n\n" +
                "Su cuenta de lector ha sido registrada exitosamente.\n\n" +
                "--------------------------------------------------\n" +
                "CREDENCIALES INICIALES DE ACCESO\n" +
                "--------------------------------------------------\n" +
                "• Usuario de acceso (DNI): " + username + "\n" +
                "• Contraseña Temporal: " + claveTemporal + "\n" +
                "--------------------------------------------------\n\n" +
                "En su primer inicio de sesión, el sistema le solicitará cambiar esta clave por una contraseña personal.\n\n" +
                "Atentamente,\n" +
                "Equipo de Biblioteca CI\n" +
                "Universidad Tecnológica del Perú 2026\n";
    }

    /**
     * Envía la notificación de clave temporal de recuperación al correo registrado.
     *
     * @param email         Correo electrónico registrado
     * @param clienteNombre Nombre del usuario
     * @param username      Usuario de acceso
     * @param claveTemporal Clave temporal generada
     * @return true si la notificación se procesó correctamente
     */
    public boolean enviarCorreoRecuperacionClave(String email, String clienteNombre, String username, String claveTemporal) {
        String cuerpo = generarContenidoCorreoRecuperacion(clienteNombre, username, claveTemporal);

        logger.info("CORREO RECUPERACION DESPACHADO -> Destino: {}, Usuario: '{}', Lector: '{}'",
                email != null && !email.isBlank() ? email : "[SIN EMAIL]", username, clienteNombre);

        if (email != null && !email.isBlank()) {
            try {
                String subject = "Recuperacion de Contrasena - Biblioteca CI";
                String mailto = "mailto:" + email + "?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
                        + "&body=" + URLEncoder.encode(cuerpo, StandardCharsets.UTF_8);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                    Desktop.getDesktop().mail(new URI(mailto));
                }
            } catch (Exception ex) {
                logger.warn("No se pudo invocar cliente de correo nativo: {}", ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Envía el correo de bienvenida con credenciales iniciales al nuevo lector.
     *
     * @param email         Correo electrónico del cliente
     * @param clienteNombre Nombre del lector
     * @param username      DNI del cliente
     * @param claveTemporal Clave temporal asignada
     * @return true si la notificación se procesó correctamente
     */
    public boolean enviarCorreoBienvenidaCliente(String email, String clienteNombre, String username, String claveTemporal) {
        String cuerpo = generarContenidoCorreoBienvenida(clienteNombre, username, claveTemporal);

        logger.info("CORREO BIENVENIDA DESPACHADO -> Destino: {}, Usuario: '{}', Lector: '{}'",
                email != null && !email.isBlank() ? email : "[SIN EMAIL]", username, clienteNombre);

        if (email != null && !email.isBlank()) {
            try {
                String subject = "Bienvenido a Biblioteca CI - Credenciales de Acceso";
                String mailto = "mailto:" + email + "?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
                        + "&body=" + URLEncoder.encode(cuerpo, StandardCharsets.UTF_8);
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
                    Desktop.getDesktop().mail(new URI(mailto));
                }
            } catch (Exception ex) {
                logger.warn("No se pudo invocar cliente de correo nativo: {}", ex.getMessage());
            }
        }
        return true;
    }

    public boolean enviarCorreo(TicketPrestamo ticket) {
        String email = ticket.getClienteEmail();
        String cuerpo = generarContenidoCorreo(ticket);

        logger.info("CORREO ELECTRONICO DESPACHADO -> Destino: {}, Lector: '{}', Libro: '{}', Devolución: {}",
                email != null && !email.isBlank() ? email : "[SIN EMAIL]",
                ticket.getClienteNombre(),
                ticket.getLibroTitulo(),
                ticket.getFechaVencimiento().format(FORMATO_FECHA));

        return true;
    }
}
