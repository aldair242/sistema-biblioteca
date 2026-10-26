package com.biblioteca.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas Unitarias - ExcelExporter (Apache POI)")
class ExcelExporterTest {

    @Test
    @DisplayName("normalizarRutaArchivo agrega extensión .xlsx si no está presente")
    void normalizarRutaArchivo_AgregaExtension() {
        File f1 = new File("reporte_mensual");
        File normalizado1 = ExcelExporter.normalizarRutaArchivo(f1);
        assertEquals("reporte_mensual.xlsx", normalizado1.getName());

        File f2 = new File("reporte_mensual.xlsx");
        File normalizado2 = ExcelExporter.normalizarRutaArchivo(f2);
        assertEquals("reporte_mensual.xlsx", normalizado2.getName());
    }

    @Test
    @DisplayName("exportar genera un archivo .xlsx estilizado con totales, monedas y fechas verificables")
    void exportar_GeneraArchivoValido_YVerificaFormato(@TempDir Path tempDir) throws Exception {
        File destino = tempDir.resolve("test_exportacion.xlsx").toFile();

        List<String> encabezados = List.of("ID", "Concepto", "Monto", "Fecha Registro", "Activo");

        List<List<Object>> datos = List.of(
                List.of(1, "Libro A", new BigDecimal("45.50"), LocalDate.of(2026, 1, 15), true),
                List.of(2, "Libro B", new BigDecimal("120.00"), LocalDate.of(2026, 2, 20), false),
                List.of(3, "Multa C", new BigDecimal("15.75"), LocalDate.of(2026, 3, 5), true)
        );

        // Exportar con moneda en columna 2 (Monto), fecha en columna 3 (Fecha Registro), y total en columna 2
        ExcelExporter.exportar(
                "Finanzas",
                encabezados,
                datos,
                destino,
                Set.of(2),
                Set.of(3),
                true,
                2
        );

        assertTrue(destino.exists(), "El archivo Excel generado debe existir en el disco");
        assertTrue(destino.length() > 0, "El archivo Excel no debe estar vacío");

        // Leer el archivo con Apache POI para verificar su integridad y estructura
        try (FileInputStream fis = new FileInputStream(destino);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Finanzas");
            assertNotNull(sheet, "Debe existir la hoja 'Finanzas'");

            // 1. Verificar encabezados
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            assertEquals("ID", headerRow.getCell(0).getStringCellValue());
            assertEquals("Concepto", headerRow.getCell(1).getStringCellValue());
            assertEquals("Monto", headerRow.getCell(2).getStringCellValue());
            assertEquals("Fecha Registro", headerRow.getCell(3).getStringCellValue());
            assertEquals("Activo", headerRow.getCell(4).getStringCellValue());

            CellStyle headerStyle = headerRow.getCell(0).getCellStyle();
            assertEquals(IndexedColors.ROYAL_BLUE.getIndex(), headerStyle.getFillForegroundColor());

            // 2. Verificar datos de filas
            Row row1 = sheet.getRow(1);
            assertNotNull(row1);
            assertEquals(1.0, row1.getCell(0).getNumericCellValue(), 0.001);
            assertEquals("Libro A", row1.getCell(1).getStringCellValue());
            assertEquals(45.50, row1.getCell(2).getNumericCellValue(), 0.001);
            assertEquals("SÍ", row1.getCell(4).getStringCellValue());

            // 3. Verificar fila de total general
            Row totalRow = sheet.getRow(4);
            assertNotNull(totalRow, "Debe existir la fila 4 con los totales generales");
            assertEquals("TOTAL GENERAL:", totalRow.getCell(1).getStringCellValue());

            double sumaEsperada = 45.50 + 120.00 + 15.75;
            assertEquals(sumaEsperada, totalRow.getCell(2).getNumericCellValue(), 0.01);
        }
    }
}
