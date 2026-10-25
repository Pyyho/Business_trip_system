package ru.pkc.travel.fx;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvExporter {
    private CsvExporter() {}

    public static void write(Path path, List<String> headers, List<List<String>> rows)
            throws IOException {
        try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write('\ufeff'); // Excel recognises Russian text as UTF-8.
            writer.write(line(headers));
            writer.write("\r\n");
            for (var row : rows) {
                writer.write(line(row));
                writer.write("\r\n");
            }
        }
    }

    private static String line(List<String> values) {
        return values.stream()
                .map(
                        value -> {
                            String safe = value == null ? "" : value;
                            // Prevent spreadsheet formulas from running when user-entered text is
                            // opened in Excel.
                            if (safe.stripLeading().matches("^[=+@-].*") || safe.startsWith("\t")) {
                                safe = "'" + safe;
                            }
                            return "\"" + safe.replace("\"", "\"\"") + "\"";
                        })
                .collect(Collectors.joining(";"));
    }
}
