package com.biblioteca.service;

import com.biblioteca.dao.*;
import com.biblioteca.model.*;
import com.biblioteca.security.Autorizador;
import com.biblioteca.util.ExcelExporter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Servicio encargado de la generación y exportación de reportes institucionales a Excel.
 * Consulta los DAO correspondientes y utiliza Apache POI (a través de {@link ExcelExporter}),
 * Google Guava (Stopwatch, Multimap) y Apache Commons IO.
 */
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);

    private final PrestamoDAO prestamoDAO;
    private final ClienteDAO clienteDAO;
    private final LibroDAO libroDAO;
    private final EjemplarDAO ejemplarDAO;
    private final MultaDAO multaDAO;

    public ReporteService(PrestamoDAO prestamoDAO,
                          ClienteDAO clienteDAO,
                          LibroDAO libroDAO,
                          EjemplarDAO ejemplarDAO,
                          MultaDAO multaDAO) {
        this.prestamoDAO = prestamoDAO;
        this.clienteDAO = clienteDAO;
        this.libroDAO = libroDAO;
        this.ejemplarDAO = ejemplarDAO;
        this.multaDAO = multaDAO;
    }

    /**
     * Exporta el reporte solicitado según su tipo numérico.
     *
     * @param tipoReporte 0=Préstamos, 1=Multas, 2=Top libros, 3=Inventario
     * @param destino     Archivo destino
     * @throws IOException Si ocurre un fallo en la escritura
     */
    public void exportarReporte(int tipoReporte, File destino) throws IOException {
        switch (tipoReporte) {
            case 0 -> generarReportePrestamos(destino);
            case 1 -> generarReporteMultas(destino);
            case 2 -> generarReporteLibrosMasPrestados(destino);
            case 3 -> generarReporteInventarioEjemplares(destino);
            default -> throw new IllegalArgumentException("Tipo de reporte no reconocido: " + tipoReporte);
        }
    }

    /**
     * Genera el reporte de préstamos activos y vencidos.
     *
     * @param destino Archivo destino .xlsx
     * @throws IOException Si ocurre un fallo de I/O
     */
    public void generarReportePrestamos(File destino) throws IOException {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Stopwatch cronometro = Stopwatch.createStarted();
        logger.info("Iniciando generación de reporte de préstamos activos y vencidos...");

        List<Prestamo> prestamos = prestamoDAO.listar().stream()
                .filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO || p.getEstado() == EstadoPrestamo.VENCIDO)
                .toList();

        List<String> encabezados = List.of(
                "ID Préstamo",
                "DNI Cliente",
                "Nombre Cliente",
                "Código de Barras",
                "Título del Libro",
                "Fecha Préstamo",
                "Fecha Vencimiento",
                "Estado",
                "Renovaciones"
        );

        Map<Integer, Cliente> cacheClientes = new HashMap<>();
        Map<Integer, Ejemplar> cacheEjemplares = new HashMap<>();
        Map<Integer, Libro> cacheLibros = new HashMap<>();

        List<List<Object>> filas = new ArrayList<>();
        for (Prestamo p : prestamos) {
            Cliente cliente = cacheClientes.computeIfAbsent(p.getClienteId(),
                    id -> clienteDAO.buscarPorId(id).orElse(null));
            Ejemplar ejemplar = cacheEjemplares.computeIfAbsent(p.getEjemplarId(),
                    id -> ejemplarDAO.buscarPorId(id).orElse(null));
            Libro libro = (ejemplar != null)
                    ? cacheLibros.computeIfAbsent(ejemplar.getLibroId(), id -> libroDAO.buscarPorId(id).orElse(null))
                    : null;

            filas.add(List.of(
                    p.getId(),
                    cliente != null ? cliente.getDni() : "N/A",
                    cliente != null ? cliente.getNombreCompleto() : "N/A",
                    ejemplar != null ? ejemplar.getCodigoBarras() : "N/A",
                    libro != null ? libro.getTitulo() : "N/A",
                    p.getFechaPrestamo(),
                    p.getFechaVencimiento(),
                    p.getEstado().name(),
                    p.getRenovaciones()
            ));
        }

        ExcelExporter.exportar(
                "Préstamos Activos y Vencidos",
                encabezados,
                filas,
                destino,
                Set.of(),
                Set.of(5, 6),
                false,
                -1
        );

        logger.info("Reporte de préstamos generado en: {} ({})",
                FilenameUtils.getName(destino.getName()), cronometro.stop());
    }

    /**
     * Genera el reporte de multas pendientes y pagadas con cálculo de total general.
     *
     * @param destino Archivo destino .xlsx
     * @throws IOException Si ocurre un fallo de I/O
     */
    public void generarReporteMultas(File destino) throws IOException {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Stopwatch cronometro = Stopwatch.createStarted();
        logger.info("Iniciando generación de reporte de multas...");

        List<Multa> multas = multaDAO.listar();

        List<String> encabezados = List.of(
                "ID Multa",
                "ID Préstamo",
                "DNI Cliente",
                "Cliente",
                "Días Retraso",
                "Tarifa Diaria",
                "Monto Total",
                "Estado",
                "Fecha Pago"
        );

        Map<Integer, Prestamo> cachePrestamos = new HashMap<>();
        Map<Integer, Cliente> cacheClientes = new HashMap<>();

        List<List<Object>> filas = new ArrayList<>();
        for (Multa m : multas) {
            Prestamo prestamo = cachePrestamos.computeIfAbsent(m.getPrestamoId(),
                    id -> prestamoDAO.buscarPorId(id).orElse(null));
            Cliente cliente = null;
            if (prestamo != null) {
                cliente = cacheClientes.computeIfAbsent(prestamo.getClienteId(),
                        id -> clienteDAO.buscarPorId(id).orElse(null));
            }

            filas.add(List.of(
                    m.getId(),
                    m.getPrestamoId(),
                    cliente != null ? cliente.getDni() : "N/A",
                    cliente != null ? cliente.getNombreCompleto() : "N/A",
                    m.getDiasRetraso(),
                    m.getTarifaAplicada(),
                    m.getMonto(),
                    m.getEstado().name(),
                    m.getFechaPago() != null ? m.getFechaPago() : "-"
            ));
        }

        // Columna 5 = Tarifa Diaria, Columna 6 = Monto Total (ambas moneda). Columna totalizar: 6
        ExcelExporter.exportar(
                "Multas",
                encabezados,
                filas,
                destino,
                Set.of(5, 6),
                Set.of(8),
                true,
                6
        );

        logger.info("Reporte de multas generado en: {} ({})",
                FilenameUtils.getName(destino.getName()), cronometro.stop());
    }

    /**
     * Genera el reporte de los 10 libros más prestados de la biblioteca.
     *
     * @param destino Archivo destino .xlsx
     * @throws IOException Si ocurre un fallo de I/O
     */
    public void generarReporteLibrosMasPrestados(File destino) throws IOException {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Stopwatch cronometro = Stopwatch.createStarted();
        logger.info("Iniciando generación de reporte de libros más prestados...");

        List<Libro> libros = libroDAO.listarMasPrestados(10);

        List<String> encabezados = List.of(
                "Puesto",
                "ID Libro",
                "ISBN",
                "Título",
                "Autor",
                "Categoría",
                "Editorial",
                "Año de Publicación"
        );

        List<List<Object>> filas = new ArrayList<>();
        int ranking = 1;
        for (Libro l : libros) {
            filas.add(List.of(
                    ranking++,
                    l.getId(),
                    l.getIsbn(),
                    l.getTitulo(),
                    l.getAutor(),
                    l.getCategoria(),
                    l.getEditorial(),
                    l.getAnio()
            ));
        }

        ExcelExporter.exportar(
                "Top 10 Libros Más Prestados",
                encabezados,
                filas,
                destino,
                Set.of(),
                Set.of(),
                false,
                -1
        );

        logger.info("Reporte de top libros generado en: {} ({})",
                FilenameUtils.getName(destino.getName()), cronometro.stop());
    }

    /**
     * Genera el inventario de ejemplares agrupados por estado operativo.
     * Utiliza Guava Multimap para organizar la agrupación.
     *
     * @param destino Archivo destino .xlsx
     * @throws IOException Si ocurre un fallo de I/O
     */
    public void generarReporteInventarioEjemplares(File destino) throws IOException {
        Autorizador.requerir(Rol.ADMIN, Rol.BIBLIOTECARIO);
        Stopwatch cronometro = Stopwatch.createStarted();
        logger.info("Iniciando generación de reporte de inventario de ejemplares...");

        List<Ejemplar> todosLosEjemplares = ejemplarDAO.listar();

        // Agrupación utilizando Google Guava Multimap
        Multimap<EstadoEjemplar, Ejemplar> ejemplaresPorEstado = ArrayListMultimap.create();
        for (Ejemplar ej : todosLosEjemplares) {
            ejemplaresPorEstado.put(ej.getEstado(), ej);
        }

        List<String> encabezados = List.of(
                "Estado del Ejemplar",
                "ID Ejemplar",
                "Código de Barras",
                "ID Libro",
                "Título del Libro",
                "Autor",
                "Categoría"
        );

        Map<Integer, Libro> cacheLibros = new HashMap<>();
        List<List<Object>> filas = new ArrayList<>();

        for (EstadoEjemplar estado : EstadoEjemplar.values()) {
            Collection<Ejemplar> listaEstado = ejemplaresPorEstado.get(estado);
            for (Ejemplar ej : listaEstado) {
                Libro libro = cacheLibros.computeIfAbsent(ej.getLibroId(),
                        id -> libroDAO.buscarPorId(id).orElse(null));

                filas.add(List.of(
                        estado.name(),
                        ej.getId(),
                        ej.getCodigoBarras(),
                        ej.getLibroId(),
                        libro != null ? libro.getTitulo() : "N/A",
                        libro != null ? libro.getAutor() : "N/A",
                        libro != null ? libro.getCategoria() : "N/A"
                ));
            }
        }

        ExcelExporter.exportar(
                "Inventario de Ejemplares",
                encabezados,
                filas,
                destino,
                Set.of(),
                Set.of(),
                false,
                -1
        );

        logger.info("Reporte de inventario generado en: {} ({})",
                FilenameUtils.getName(destino.getName()), cronometro.stop());
    }
}
