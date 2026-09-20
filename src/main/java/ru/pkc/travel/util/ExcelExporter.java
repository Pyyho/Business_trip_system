package ru.pkc.travel.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.pkc.travel.model.BusinessTrip;
import ru.pkc.travel.model.Employee;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExcelExporter implements DataExporter {
    @Override
    public Path export(List<Employee> employees, List<BusinessTrip> trips, Path target) throws IOException {
        Path absolute = target.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (Workbook workbook = new XSSFWorkbook()) {
            CellStyle header = headerStyle(workbook);
            writeEmployees(workbook, employees, header);
            writeTrips(workbook, trips, header);
            try (OutputStream output = Files.newOutputStream(absolute)) {
                workbook.write(output);
            }
        }
        return absolute;
    }

    private static void writeEmployees(Workbook workbook, List<Employee> employees, CellStyle header) {
        Sheet sheet = workbook.createSheet("Сотрудники");
        createHeader(sheet, header, "ID", "ФИО", "Email", "Отдел");
        int index = 1;
        for (Employee employee : employees) {
            Row row = sheet.createRow(index++);
            row.createCell(0).setCellValue(employee.getId());
            row.createCell(1).setCellValue(employee.getFullName());
            row.createCell(2).setCellValue(employee.getEmail());
            row.createCell(3).setCellValue(employee.getDepartment());
        }
        autoSize(sheet, 4);
    }

    private static void writeTrips(Workbook workbook, List<BusinessTrip> trips, CellStyle header) {
        Sheet sheet = workbook.createSheet("Служебные поездки");
        createHeader(sheet, header, "ID", "Сотрудник", "Место", "Цель", "Начало", "Окончание",
                "Статус", "Плановая стоимость", "Фактическая стоимость");
        int index = 1;
        for (BusinessTrip trip : trips) {
            Row row = sheet.createRow(index++);
            row.createCell(0).setCellValue(trip.getId());
            row.createCell(1).setCellValue(trip.getEmployee().getFullName());
            row.createCell(2).setCellValue(trip.getDestination());
            row.createCell(3).setCellValue(trip.getPurpose());
            row.createCell(4).setCellValue(trip.getStartDate().toString());
            row.createCell(5).setCellValue(trip.getEndDate().toString());
            row.createCell(6).setCellValue(trip.getStatus().getDisplayName());
            row.createCell(7).setCellValue(trip.getPlannedCost().doubleValue());
            if (trip.getActualCost() != null) row.createCell(8).setCellValue(trip.getActualCost().doubleValue());
        }
        autoSize(sheet, 9);
    }

    private static CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static void createHeader(Sheet sheet, CellStyle style, String... names) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < names.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(names[i]);
            cell.setCellStyle(style);
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, names.length - 1));
    }

    private static void autoSize(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) > 15000) sheet.setColumnWidth(i, 15000);
        }
    }
}
