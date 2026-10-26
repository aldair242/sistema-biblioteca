package com.biblioteca.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Utilidad técnica para exportación de datos a hojas de cálculo Microsoft Excel (.xlsx)
 * utilizando Apache POI y utilidades de Apache Commons IO.
 */
public final class ExcelExporter {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);

    private ExcelExporter() {
    }

    /**
     * Valida y ajusta la ruta del archivo destino asegurando la extensión .xlsx.
     * Utiliza FilenameUtils de Apache Commons IO.
     *
     * @param archivoOriginal Archivo seleccionado por el usuario
     * @return Archivo con extensión .xlsx normalizada
     */
    public static File normalizarRutaArchivo(File archivoOriginal) {
        if (archivoOriginal == null) {
            throw new IllegalArgumentException("El archivo destino no puede ser nulo.");
        }

        String extension = FilenameUtils.getExtension(archivoOriginal.getName());
        if (!"xlsx".equalsIgnoreCase(extension)) {
            return new File(archivoOriginal.getParentFile(), archivoOriginal.getName() + ".xlsx");
        }
        return archivoOriginal;
    }

    /**
     * Genera un libro de Excel estilizado y lo escribe en el archivo destino.
     *
     * @param nombreHoja        Nombre de la pestaña
     * @param encabezados       Lista de textos de encabezado
     * @param datos             Filas de datos con tipos arbitrarios (String, Number, LocalDate, etc.)
     * @param archivoDestino    Archivo de salida
     * @param indicesMoneda     Conjunto de índices de columnas que representan valores monetarios
     * @param indicesFecha      Conjunto de índices de columnas que representan fechas
     * @param incluirFilaTotal  Indica si se debe calcular una fila de total al pie de tabla
     * @param columnaTotalIndex Índice de la columna numérica a totalizar (si incluirFilaTotal es true)
     * @throws IOException Si ocurre un error de escritura
     */
    public static void exportar(String nombreHoja,
                                List<String> encabezados,
                                List<List<Object>> datos,
                                File archivoDestino,
                                Set<Integer> indicesMoneda,
                                Set<Integer> indicesFecha,
                                boolean incluirFilaTotal,
                                int columnaTotalIndex) throws IOException {

        File archivoFinal = normalizarRutaArchivo(archivoDestino);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(nombreHoja != null ? nombreHoja : "Reporte");
            CreationHelper createHelper = workbook.getCreationHelper();

            // 1. Estilo para Encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 2. Estilo para Formato de Moneda (S/. #,##0.00)
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("\"S/ \"#,##0.00"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);

            // 3. Estilo para Formato de Fecha (yyyy-MM-dd)
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);

            // 4. Estilo General para Celdas
            CellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setBorderBottom(BorderStyle.THIN);
            defaultStyle.setBorderTop(BorderStyle.THIN);
            defaultStyle.setBorderLeft(BorderStyle.THIN);
            defaultStyle.setBorderRight(BorderStyle.THIN);

            // 5. Estilo para Fila de Totales
            CellStyle totalLabelStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setBorderTop(BorderStyle.DOUBLE);
            totalLabelStyle.setBorderBottom(BorderStyle.DOUBLE);

            CellStyle totalCurrencyStyle = workbook.createCellStyle();
            totalCurrencyStyle.setFont(totalFont);
            totalCurrencyStyle.setDataFormat(createHelper.createDataFormat().getFormat("\"S/ \"#,##0.00"));
            totalCurrencyStyle.setAlignment(HorizontalAlignment.RIGHT);
            totalCurrencyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            totalCurrencyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalCurrencyStyle.setBorderTop(BorderStyle.DOUBLE);
            totalCurrencyStyle.setBorderBottom(BorderStyle.DOUBLE);

            // Crear Fila de Encabezados
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int col = 0; col < encabezados.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(encabezados.get(col));
                cell.setCellStyle(headerStyle);
            }

            // Escribir Filas de Datos
            int rowIndex = 1;
            BigDecimal sumaTotal = BigDecimal.ZERO;

            for (List<Object> fila : datos) {
                Row row = sheet.createRow(rowIndex++);
                for (int col = 0; col < fila.size(); col++) {
                    Cell cell = row.createCell(col);
                    Object valor = fila.get(col);

                    if (valor == null) {
                        cell.setCellValue("-");
                        cell.setCellStyle(defaultStyle);
                    } else if (indicesMoneda != null && indicesMoneda.contains(col)) {
                        double num = 0.0;
                        if (valor instanceof BigDecimal bd) {
                            num = bd.doubleValue();
                            if (col == columnaTotalIndex) sumaTotal = sumaTotal.add(bd);
                        } else if (valor instanceof Number n) {
                            num = n.doubleValue();
                            if (col == columnaTotalIndex) sumaTotal = sumaTotal.add(BigDecimal.valueOf(num));
                        } else {
                            try {
                                BigDecimal parsed = new BigDecimal(valor.toString().replaceAll("[^0-9.]", ""));
                                num = parsed.doubleValue();
                                if (col == columnaTotalIndex) sumaTotal = sumaTotal.add(parsed);
                            } catch (Exception ignored) {
                            }
                        }
                        cell.setCellValue(num);
                        cell.setCellStyle(currencyStyle);

                    } else if (indicesFecha != null && indicesFecha.contains(col)) {
                        if (valor instanceof LocalDate ld) {
                            cell.setCellValue(java.sql.Date.valueOf(ld));
                        } else if (valor instanceof LocalDateTime ldt) {
                            cell.setCellValue(java.sql.Timestamp.valueOf(ldt));
                        } else if (valor instanceof Date d) {
                            cell.setCellValue(d);
                        } else {
                            cell.setCellValue(valor.toString());
                        }
                        cell.setCellStyle(dateStyle);

                    } else if (valor instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                        cell.setCellStyle(defaultStyle);
                    } else if (valor instanceof Boolean b) {
                        cell.setCellValue(b ? "SÍ" : "NO");
                        cell.setCellStyle(defaultStyle);
                    } else {
                        cell.setCellValue(valor.toString());
                        cell.setCellStyle(defaultStyle);
                    }
                }
            }

            // Fila de Total opcional
            if (incluirFilaTotal && columnaTotalIndex >= 0 && columnaTotalIndex < encabezados.size()) {
                Row totalRow = sheet.createRow(rowIndex);
                totalRow.setHeightInPoints(20);

                for (int col = 0; col < encabezados.size(); col++) {
                    Cell cell = totalRow.createCell(col);
                    if (col == columnaTotalIndex - 1) {
                        cell.setCellValue("TOTAL GENERAL:");
                        cell.setCellStyle(totalLabelStyle);
                    } else if (col == columnaTotalIndex) {
                        cell.setCellValue(sumaTotal.doubleValue());
                        cell.setCellStyle(totalCurrencyStyle);
                    } else {
                        cell.setCellValue("");
                        cell.setCellStyle(totalLabelStyle);
                    }
                }
            }

            // Ajustar ancho automático para todas las columnas
            for (int col = 0; col < encabezados.size(); col++) {
                sheet.autoSizeColumn(col);
                // Darle un margen adicional de legibilidad
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(currentWidth + 1200, 256 * 60));
            }

            // Escribir archivo al disco
            try (FileOutputStream fos = new FileOutputStream(archivoFinal)) {
                workbook.write(fos);
            }
            logger.info("Reporte Excel generado exitosamente en: {}", archivoFinal.getAbsolutePath());
        }
    }
}
