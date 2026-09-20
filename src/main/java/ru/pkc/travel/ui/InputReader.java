package ru.pkc.travel.ui;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.model.TripStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class InputReader {
    private final BufferedReader reader;

    public InputReader(InputStream input) {
        reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            String value = reader.readLine();
            if (value == null) throw new BusinessException("Ввод завершен.");
            return value.trim();
        } catch (IOException e) {
            throw new BusinessException("Ошибка чтения ввода: " + e.getMessage());
        }
    }

    public String readRequired(String prompt) {
        String value = readLine(prompt);
        if (value.isEmpty()) throw new BusinessException("Значение не может быть пустым.");
        return value;
    }

    public int readInt(String prompt) {
        String value = readRequired(prompt);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("Ожидалось целое число.");
        }
    }

    public long readId(String prompt) {
        String value = readRequired(prompt);
        try {
            long id = Long.parseLong(value);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new BusinessException("ID должен быть целым положительным числом.");
        }
    }

    public BigDecimal readMoney(String prompt) {
        String value = readRequired(prompt).replace(',', '.');
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("Стоимость должна быть числом.");
        }
    }

    public LocalDate readDate(String prompt) {
        String value = readRequired(prompt);
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BusinessException("Дата должна иметь формат ГГГГ-ММ-ДД, например 2026-09-19.");
        }
    }

    public TripStatus readStatus(String prompt) {
        TripStatus[] statuses = TripStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("%d. %s (%s)%n", i + 1, statuses[i].getDisplayName(), statuses[i].name());
        }
        int selected = readInt(prompt);
        if (selected < 1 || selected > statuses.length) {
            throw new BusinessException("Неизвестный статус.");
        }
        return statuses[selected - 1];
    }

    public boolean confirm(String prompt) {
        String value = readLine(prompt + " [да/нет]: ");
        return "да".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value);
    }
}
