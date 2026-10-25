package ru.pkc.travel.fx;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import ru.pkc.travel.exception.BusinessException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

final class DateInput extends DatePicker {
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.uuuu").withResolverStyle(ResolverStyle.STRICT);

    private String invalidText;

    DateInput(LocalDate initial) {
        super(initial);
        setEditable(true);
        setPromptText("дд.мм.гггг");
        setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate value) {
                if (invalidText != null) {
                    return invalidText;
                }
                return value == null ? "" : FORMAT.format(value);
            }

            @Override
            public LocalDate fromString(String text) {
                try {
                    LocalDate value = parse(text);
                    invalidText = null;
                    return value;
                } catch (DateTimeParseException e) {
                    // DatePicker commits on focus loss and Enter. Keep invalid input visible
                    // until the form can show a validation message when saving.
                    invalidText = text;
                    return getValue();
                }
            }
        });
        valueProperty().addListener((observable, previous, value) -> invalidText = null);
        getEditor().setText(getConverter().toString(initial));
    }

    LocalDate readValue(String label) {
        String text = getEditor().getText();
        if (text == null || text.isBlank()) {
            throw new BusinessException(label + ": введите дату или выберите её в календаре.");
        }
        try {
            LocalDate value = parse(text);
            invalidText = null;
            setValue(value);
            getEditor().setText(FORMAT.format(value));
            return value;
        } catch (DateTimeParseException e) {
            throw new BusinessException(
                    label + ": введите существующую дату в формате дд.мм.гггг, например 25.10.2026.");
        }
    }

    private static LocalDate parse(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return LocalDate.parse(text.trim(), FORMAT);
    }
}
