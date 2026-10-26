package com.biblioteca.service;

import com.biblioteca.dao.*;
import com.biblioteca.model.*;
import com.biblioteca.security.SesionActual;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - ReporteService")
class ReporteServiceTest {

    @Mock
    private PrestamoDAO prestamoDAO;
    @Mock
    private ClienteDAO clienteDAO;
    @Mock
    private LibroDAO libroDAO;
    @Mock
    private EjemplarDAO ejemplarDAO;
    @Mock
    private MultaDAO multaDAO;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        Usuario admin = new Usuario(1, "admin", "hash", Rol.ADMIN, "Admin General", true, false, 0, null);
        SesionActual.iniciarSesion(admin);

        reporteService = new ReporteService(prestamoDAO, clienteDAO, libroDAO, ejemplarDAO, multaDAO);
    }

    @AfterEach
    void tearDown() {
        SesionActual.cerrarSesion();
    }

    @Test
    @DisplayName("generarReportePrestamos genera archivo con datos correctos")
    void generarReportePrestamos_Exito(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("reporte_prestamos.xlsx").toFile();

        Prestamo p = new Prestamo(1, 10, 20, 2, LocalDate.now(), LocalDate.now().plusDays(7), null, EstadoPrestamo.ACTIVO, 0);
        when(prestamoDAO.listar()).thenReturn(List.of(p));

        Cliente c = new Cliente(10, 5, "12345678", "Juan", "Pérez", "juan@test.com", "987654321", LocalDate.now());
        when(clienteDAO.buscarPorId(10)).thenReturn(Optional.of(c));

        Ejemplar ej = new Ejemplar(20, 30, "LIB-0001", EstadoEjemplar.PRESTADO);
        when(ejemplarDAO.buscarPorId(20)).thenReturn(Optional.of(ej));

        Libro l = new Libro(30, "978-0134685991", "Effective Java", "Joshua Bloch", "Programación", "Addison-Wesley", 2018);
        when(libroDAO.buscarPorId(30)).thenReturn(Optional.of(l));

        reporteService.generarReportePrestamos(destino);

        assertTrue(destino.exists());
        try (FileInputStream fis = new FileInputStream(destino);
             Workbook wb = new XSSFWorkbook(fis)) {
            assertNotNull(wb.getSheet("Préstamos Activos y Vencidos"));
            assertEquals("ID Préstamo", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("generarReporteMultas genera archivo con fila de total")
    void generarReporteMultas_Exito(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("reporte_multas.xlsx").toFile();

        Multa m = new Multa(1, 5, 3, new BigDecimal("2.50"), new BigDecimal("7.50"), EstadoMulta.PENDIENTE, null);
        when(multaDAO.listar()).thenReturn(List.of(m));

        reporteService.generarReporteMultas(destino);

        assertTrue(destino.exists());
        try (FileInputStream fis = new FileInputStream(destino);
             Workbook wb = new XSSFWorkbook(fis)) {
            assertNotNull(wb.getSheet("Multas"));
        }
    }

    @Test
    @DisplayName("generarReporteLibrosMasPrestados exporta ranking top 10")
    void generarReporteLibrosMasPrestados_Exito(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("reporte_top.xlsx").toFile();

        Libro l = new Libro(1, "1234567890", "Clean Code", "Robert C. Martin", "Ingeniería", "Prentice Hall", 2008);
        when(libroDAO.listarMasPrestados(10)).thenReturn(List.of(l));

        reporteService.generarReporteLibrosMasPrestados(destino);

        assertTrue(destino.exists());
        try (FileInputStream fis = new FileInputStream(destino);
             Workbook wb = new XSSFWorkbook(fis)) {
            assertNotNull(wb.getSheet("Top 10 Libros Más Prestados"));
        }
    }

    @Test
    @DisplayName("generarReporteInventarioEjemplares agrupa por estado con Guava Multimap")
    void generarReporteInventarioEjemplares_Exito(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("reporte_inventario.xlsx").toFile();

        Ejemplar e1 = new Ejemplar(1, 100, "BC-001", EstadoEjemplar.DISPONIBLE);
        Ejemplar e2 = new Ejemplar(2, 100, "BC-002", EstadoEjemplar.PRESTADO);
        when(ejemplarDAO.listar()).thenReturn(List.of(e1, e2));

        reporteService.generarReporteInventarioEjemplares(destino);

        assertTrue(destino.exists());
        try (FileInputStream fis = new FileInputStream(destino);
             Workbook wb = new XSSFWorkbook(fis)) {
            assertNotNull(wb.getSheet("Inventario de Ejemplares"));
        }
    }
}
