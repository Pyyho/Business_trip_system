package ru.pkc.travel.fx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {
    @TempDir Path directory;
    @Test void exportsRussianTextQuotesMultilineAndFormulaText() throws Exception {
        Path file = directory.resolve("export.csv");
        CsvExporter.write(file, List.of("ФИО", "Описание"), List.of(List.of("Анна", "Отчёт; \"проверен\"\nСтрока"), List.of("=1+2", "Текст")));
        String content = Files.readString(file);
        assertTrue(content.startsWith("\ufeff\"ФИО\";\"Описание\"\r\n"));
        assertTrue(content.contains("\"Отчёт; \"\"проверен\"\"\nСтрока\""));
        assertTrue(content.contains("\"'=1+2\""));
    }
}
