package com.biblioteca.controller;

import com.biblioteca.exception.ValidacionException;
import com.biblioteca.model.*;
import com.biblioteca.security.PasswordHasher;
import com.biblioteca.security.SesionActual;
import com.biblioteca.service.*;
import com.biblioteca.util.ExcelExporter;
import com.biblioteca.util.ManejadorExcepciones;
import com.biblioteca.view.AdminView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para la interfaz del ADMINISTRADOR.
 */
public class AdminController {

    private final AdminView view;
    private final AuthController authController;
    private final ServiceFactory serviceFactory;

    public AdminController(AdminView view, AuthController authController, ServiceFactory serviceFactory) {
        this.view = view;
        this.authController = authController;
        this.serviceFactory = serviceFactory;
        this.view.setController(this);
    }

    public void iniciar() {
        cargarBibliotecarios();
        cargarCatalogo();
        cargarConfiguracion();
        cargarReporte(0);
        view.setVisible(true);
    }

    // --- BIBLIOTECARIOS ---
    public void cargarBibliotecarios() {
        List<Usuario> biblios = serviceFactory.getUsuarioDAO().listarPorRol(Rol.BIBLIOTECARIO);
        view.cargarTablaBibliotecarios(biblios);
    }

    public void guardarBibliotecario(String username, String nombre, String password) {
        if (username.trim().isEmpty() || nombre.trim().isEmpty()) {
            view.mostrarAdvertencia("El nombre de usuario y el nombre completo son obligatorios.");
            return;
        }

        try {
            if (serviceFactory.getUsuarioDAO().buscarPorUsername(username.trim()).isPresent()) {
                view.mostrarAdvertencia("Ya existe un usuario con el nombre de cuenta '" + username + "'.");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                password = "Biblio" + (int)(Math.random() * 9000 + 1000) + "!"; // Contraseña inicial generada
            }

            String hash = PasswordHasher.hashear(password.trim());
            Usuario u = new Usuario(null, username.trim(), hash, Rol.BIBLIOTECARIO, nombre.trim(), true, true, 0, null);
            serviceFactory.getUsuarioDAO().guardar(u);

            view.mostrarModalClaveTemporal(username.trim(), nombre.trim(), password.trim());
            view.limpiarFormBibliotecario();
            cargarBibliotecarios();

        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "registro de bibliotecario", ex);
        }
    }

    public void actualizarBibliotecario(int id, String username, String nombre, String passwordOpcional) {
        if (username.trim().isEmpty() || nombre.trim().isEmpty()) {
            view.mostrarAdvertencia("El nombre de usuario y el nombre completo son obligatorios.");
            return;
        }

        try {
            Usuario u = serviceFactory.getUsuarioDAO().buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bibliotecario no encontrado"));

            var existente = serviceFactory.getUsuarioDAO().buscarPorUsername(username.trim());
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                view.mostrarAdvertencia("El nombre de cuenta '" + username + "' ya pertenece a otro usuario.");
                return;
            }

            u.setUsername(username.trim());
            u.setNombre(nombre.trim());

            if (passwordOpcional != null && !passwordOpcional.trim().isEmpty()) {
                u.setPasswordHash(PasswordHasher.hashear(passwordOpcional.trim()));
                u.setDebeCambiarClave(true);
            }

            serviceFactory.getUsuarioDAO().actualizar(u);
            view.mostrarMensaje("Bibliotecario actualizado exitosamente.");
            view.limpiarFormBibliotecario();
            cargarBibliotecarios();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "actualización de bibliotecario", ex);
        }
    }

    public void eliminarBibliotecario(int id) {
        if (SesionActual.estaAutenticado() && SesionActual.getUsuario().getId() == id) {
            view.mostrarAdvertencia("No puede eliminar la cuenta con la que actualmente ha iniciado sesión.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de eliminar permanentemente a este bibliotecario?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                serviceFactory.getUsuarioDAO().eliminar(id);
                view.mostrarMensaje("Bibliotecario eliminado exitosamente.");
                view.limpiarFormBibliotecario();
                cargarBibliotecarios();
            } catch (Exception ex) {
                view.mostrarAdvertencia("No se puede eliminar físicamente al bibliotecario porque tiene historial de préstamos atendidos.\n" +
                        "Utilice 'Activar / Desactivar' para suspender su cuenta.");
            }
        }
    }

    public void restablecerClaveBibliotecario(int id) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Desea restablecer la contraseña de este bibliotecario con una nueva clave temporal?",
                "Restablecer Contraseña", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Usuario u = serviceFactory.getUsuarioDAO().buscarPorId(id)
                        .orElseThrow(() -> new IllegalArgumentException("Bibliotecario no encontrado"));
                String claveTemp = "Biblio" + (int)(Math.random() * 9000 + 1000) + "!";
                String hash = PasswordHasher.hashear(claveTemp);
                serviceFactory.getUsuarioDAO().cambiarClave(id, hash, true);
                view.mostrarModalClaveTemporal(u.getUsername(), u.getNombre(), claveTemp);
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "restablecimiento de contraseña", ex);
            }
        }
    }

    public void toggleActivoBibliotecario(int id) {
        serviceFactory.getUsuarioDAO().buscarPorId(id).ifPresent(u -> {
            boolean nuevoEstado = !u.isActivo();
            serviceFactory.getUsuarioDAO().cambiarEstadoActivo(id, nuevoEstado);
            view.mostrarMensaje("Estado de '" + u.getUsername() + "' cambiado a: " + (nuevoEstado ? "ACTIVO" : "INACTIVO"));
            cargarBibliotecarios();
        });
    }

    // --- CATÁLOGO ---
    public void cargarCatalogo() {
        List<Libro> libros = serviceFactory.getCatalogoService().listarTodosLibros();
        view.cargarTablaLibros(libros, id -> serviceFactory.getCatalogoService().contarDisponibles(id));
    }

    public void cargarEjemplaresDeLibro(int libroId) {
        List<Ejemplar> ejemplares = serviceFactory.getCatalogoService().listarEjemplaresPorLibro(libroId);
        view.cargarTablaEjemplares(ejemplares);
    }

    public void guardarLibro(String isbn, String titulo, String autor, String categoria, String editorial, int anio, int ejemplares) {
        try {
            Libro l = new Libro(null, isbn.trim(), titulo.trim(), autor.trim(), categoria.trim(), editorial.trim(), anio);
            serviceFactory.getCatalogoService().registrarLibro(l, ejemplares);
            view.mostrarMensaje("Libro y " + ejemplares + " ejemplar(es) registrados exitosamente.");
            view.limpiarFormLibro();
            cargarCatalogo();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "registro de libro", ex);
        }
    }

    public void actualizarLibro(int id, String isbn, String titulo, String autor, String categoria, String editorial, int anio) {
        try {
            Libro l = new Libro(id, isbn.trim(), titulo.trim(), autor.trim(), categoria.trim(), editorial.trim(), anio);
            serviceFactory.getCatalogoService().actualizarLibro(l);
            view.mostrarMensaje("Libro actualizado exitosamente.");
            view.limpiarFormLibro();
            cargarCatalogo();
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "actualización de libro", ex);
        }
    }

    public void eliminarLibro(int id) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de eliminar este libro y todos sus ejemplares no prestados?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                serviceFactory.getCatalogoService().eliminarLibro(id);
                view.mostrarMensaje("Libro eliminado del catálogo.");
                view.limpiarFormLibro();
                cargarCatalogo();
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "eliminación de libro", ex);
            }
        }
    }

    public void agregarEjemplares(int libroId, int cantidad) {
        try {
            List<Ejemplar> nuevos = serviceFactory.getCatalogoService().agregarMultiplesEjemplares(libroId, cantidad);
            view.mostrarMensaje("Se agregaron exitosamente " + nuevos.size() + " nuevo(s) ejemplar(es).");
            cargarCatalogo();
            cargarEjemplaresDeLibro(libroId);
        } catch (Exception ex) {
            ManejadorExcepciones.manejar(view, "adición de ejemplares", ex);
        }
    }

    public void eliminarEjemplar(int ejemplarId, int libroId) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de eliminar físicamente este ejemplar?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                serviceFactory.getCatalogoService().eliminarEjemplar(ejemplarId);
                view.mostrarMensaje("Ejemplar eliminado correctamente.");
                cargarCatalogo();
                cargarEjemplaresDeLibro(libroId);
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "eliminación de ejemplar", ex);
            }
        }
    }

    public void cambiarEstadoEjemplarDialog(int ejemplarId, int libroId) {
        EstadoEjemplar[] estados = {EstadoEjemplar.DISPONIBLE, EstadoEjemplar.EN_MANTENIMIENTO, EstadoEjemplar.BAJA};
        EstadoEjemplar seleccionado = (EstadoEjemplar) JOptionPane.showInputDialog(
                view,
                "Seleccione el nuevo estado para el ejemplar:",
                "Modificar Estado de Ejemplar",
                JOptionPane.QUESTION_MESSAGE,
                null,
                estados,
                estados[0]);

        if (seleccionado != null) {
            try {
                serviceFactory.getCatalogoService().cambiarEstadoEjemplar(ejemplarId, seleccionado);
                view.mostrarMensaje("Estado del ejemplar actualizado exitosamente.");
                cargarCatalogo();
                cargarEjemplaresDeLibro(libroId);
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "modificación de estado de ejemplar", ex);
            }
        }
    }

    // --- CONFIGURACIÓN ---
    public void cargarConfiguracion() {
        ConfiguracionService cs = serviceFactory.getConfiguracionService();
        view.setValoresConfiguracion(
                cs.obtenerMultaPorDia().toPlainString(),
                String.valueOf(cs.obtenerDiasPrestamo()),
                String.valueOf(cs.obtenerMaxPrestamos()),
                String.valueOf(cs.obtenerDiasRetencionReserva())
        );
    }

    public void guardarConfiguracion(String multa, String dias, String max, String retencion) {
        int confirm = JOptionPane.showConfirmDialog(view,
                "¿Desea actualizar los parámetros globales de la biblioteca?",
                "Confirmar Configuración", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                ConfiguracionService cs = serviceFactory.getConfiguracionService();
                cs.actualizar(ConfiguracionService.CLAVE_MULTA_POR_DIA, multa);
                cs.actualizar(ConfiguracionService.CLAVE_DIAS_PRESTAMO, dias);
                cs.actualizar(ConfiguracionService.CLAVE_MAX_PRESTAMOS, max);
                cs.actualizar(ConfiguracionService.CLAVE_DIAS_RETENCION_RESERVA, retencion);

                view.mostrarMensaje("Parámetros de configuración actualizados exitosamente.");
                cargarConfiguracion();
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "actualización de parámetros de configuración", ex);
            }
        }
    }

    // --- REPORTES ---
    public void cargarReporte(int tipo) {
        DefaultTableModel model = new DefaultTableModel();

        switch (tipo) {
            case 0 -> { // Préstamos activos y vencidos
                model.setColumnIdentifiers(new String[]{"ID", "Cliente ID", "Ejemplar ID", "Fecha Préstamo", "Fecha Vencimiento", "Estado", "Renovaciones"});
                List<Prestamo> prestamos = serviceFactory.getPrestamoService().listarActivosYVencidos();
                for (Prestamo p : prestamos) {
                    model.addRow(new Object[]{p.getId(), p.getClienteId(), p.getEjemplarId(), p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado(), p.getRenovaciones()});
                }
            }
            case 1 -> { // Multas pendientes y pagadas
                model.setColumnIdentifiers(new String[]{"ID", "Préstamo ID", "Días Retraso", "Tarifa Aplicada", "Monto", "Estado", "Fecha Pago"});
                List<Multa> multas = serviceFactory.getMultaService().listarTodas();
                for (Multa m : multas) {
                    model.addRow(new Object[]{m.getId(), m.getPrestamoId(), m.getDiasRetraso(), "S/ " + m.getTarifaAplicada(), "S/ " + m.getMonto(), m.getEstado(), m.getFechaPago() != null ? m.getFechaPago() : "-"});
                }
            }
            case 2 -> { // Libros más prestados (Top 10)
                model.setColumnIdentifiers(new String[]{"ID", "ISBN", "Título", "Autor", "Categoría", "Editorial", "Año"});
                List<Libro> top = serviceFactory.getLibroDAO().listarMasPrestados(10);
                for (Libro l : top) {
                    model.addRow(new Object[]{l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getCategoria(), l.getEditorial(), l.getAnio()});
                }
            }
            case 3 -> { // Inventario de ejemplares por estado
                model.setColumnIdentifiers(new String[]{"ID Ejemplar", "ID Libro", "Código de Barras", "Estado Actual"});
                List<Ejemplar> ejemplares = serviceFactory.getEjemplarDAO().listar();
                for (Ejemplar ej : ejemplares) {
                    model.addRow(new Object[]{ej.getId(), ej.getLibroId(), ej.getCodigoBarras(), ej.getEstado()});
                }
            }
        }
        view.setReporteModel(model);
    }

    public void exportarExcelReporte(int tipo, JTable tabla) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Reporte en Excel");

        String nombreSugerido = switch (tipo) {
            case 0 -> "Reporte_Prestamos_Activos_Vencidos.xlsx";
            case 1 -> "Reporte_Multas.xlsx";
            case 2 -> "Reporte_Top_Libros_Mas_Prestados.xlsx";
            case 3 -> "Reporte_Inventario_Ejemplares.xlsx";
            default -> "Reporte_Biblioteca.xlsx";
        };
        chooser.setSelectedFile(new java.io.File(nombreSugerido));

        int userSelection = chooser.showSaveDialog(view);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File archivoSeleccionado = chooser.getSelectedFile();
            java.io.File archivoNormalizado = ExcelExporter.normalizarRutaArchivo(archivoSeleccionado);

            if (archivoNormalizado.exists()) {
                int confirm = JOptionPane.showConfirmDialog(
                        view,
                        "El archivo '" + archivoNormalizado.getName() + "' ya existe.\n¿Desea sobrescribirlo?",
                        "Confirmar Sobrescritura",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                serviceFactory.getReporteService().exportarReporte(tipo, archivoNormalizado);
                view.mostrarMensaje("Reporte exportado exitosamente a:\n" + archivoNormalizado.getAbsolutePath());
            } catch (Exception ex) {
                ManejadorExcepciones.manejar(view, "exportación de reporte a Excel", ex);
            }
        }
    }

    public void cerrarSesion() {
        authController.cerrarSesion(view);
    }
}
