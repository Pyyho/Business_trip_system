package ru.pkc.travel.fx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.model.*;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.Supplier;

public class FormController {
    @FXML private GridPane fields;
    @FXML private Label title;
    @FXML private Label error;
    @FXML private Button save;
    private Supplier<Runnable> operation;
    private Runnable saved;
    private int row;

    public void configure(String heading, Runnable saved) {
        title.setText(heading);
        this.saved = saved;
    }

    public void onSave(Supplier<Runnable> operation) {
        this.operation = operation;
    }

    private <T extends Control> T field(String label, T control) {
        control.setMaxWidth(Double.MAX_VALUE);
        fields.add(new Label(label), 0, row);
        fields.add(control, 1, row++);
        return control;
    }

    public Supplier<String> text(String label, String initial, int max) {
        TextField input = field(label, new TextField(initial == null ? "" : initial));
        return () -> {
            String value = input.getText().trim();
            if (value.isEmpty() || value.length() > max) {
                throw new BusinessException(
                        label + ": заполните поле (не более " + max + " символов).");
            }
            return value;
        };
    }

    public Supplier<BigDecimal> money(String label, BigDecimal initial) {
        TextField input =
                field(label, new TextField(initial == null ? "" : initial.toPlainString()));
        return () -> {
            try {
                BigDecimal value = new BigDecimal(input.getText().trim().replace(',', '.'));
                if (value.signum() < 0
                        || value.scale() > 2
                        || value.compareTo(new BigDecimal("999999999999.99")) > 0) {
                    throw new NumberFormatException();
                }
                return value;
            } catch (NumberFormatException e) {
                throw new BusinessException(
                        label
                                + ": введите сумму от 0 до 999999999999,99, максимум 2 знака после"
                                + " запятой.");
            }
        };
    }

    public Supplier<LocalDate> date(String label, LocalDate initial) {
        DatePicker picker = field(label, new DatePicker(initial));
        picker.setEditable(false);
        picker.setPromptText("Выберите дату");
        return () -> {
            if (picker.getValue() == null) {
                throw new BusinessException(label + ": выберите дату в календаре.");
            }
            return picker.getValue();
        };
    }

    public Supplier<LocalDateTime> time(String label, LocalDateTime initial) {
        Supplier<LocalDate> day =
                date(label + " • дата", initial == null ? null : initial.toLocalDate());
        Supplier<String> time =
                text(
                        label + " • время ЧЧ:мм",
                        initial == null
                                ? "09:00"
                                : initial.toLocalTime()
                                        .format(DateTimeFormatter.ofPattern("HH:mm")),
                        5);
        return () -> {
            LocalDate date = day.get();
            String value = time.get();
            try {
                return date.atTime(
                        LocalTime.parse(
                                value,
                                DateTimeFormatter.ofPattern("HH:mm")
                                        .withResolverStyle(ResolverStyle.STRICT)));
            } catch (java.time.format.DateTimeParseException e) {
                throw new BusinessException(
                        label + ": время должно быть в формате ЧЧ:мм (00:00–23:59).");
            }
        };
    }

    public <T> Supplier<T> choice(String label, List<T> choices, T initial) {
        ComboBox<T> combo =
                field(
                        label,
                        new ComboBox<>(
                                javafx.collections.FXCollections.observableArrayList(choices)));
        combo.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(T value) {
                        return display(value);
                    }

                    @Override
                    public T fromString(String value) {
                        throw new UnsupportedOperationException();
                    }
                });
        combo.setValue(initial);
        combo.setPromptText(
                choices.isEmpty() ? "Сначала создайте связанную запись" : "Выберите из списка");
        return () -> {
            if (combo.getValue() == null) {
                throw new BusinessException(label + ": выберите значение.");
            }
            return combo.getValue();
        };
    }

    public static String display(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Employee e) {
            return e.getFullName() + " • " + e.getDepartment();
        }
        if (value instanceof BusinessTrip t) {
            return t.getDestination()
                    + " • "
                    + t.getEmployee().getFullName()
                    + " • "
                    + t.getStartDate()
                    + " — "
                    + t.getEndDate();
        }
        return value.toString();
    }

    @FXML
    private void submit() {
        try {
            Runnable action = operation.get();
            save.setDisable(true);
            fields.setDisable(true);
            error.setText("Сохранение…");
            FxTasks.run(
                    () -> {
                        action.run();
                        return null;
                    },
                    ignored -> {
                        ((Stage) save.getScene().getWindow()).close();
                        saved.run();
                    },
                    e -> {
                        save.setDisable(false);
                        fields.setDisable(false);
                        error.setText(FxTasks.message(e));
                    });
        } catch (BusinessException e) {
            error.setText(e.getMessage());
        }
    }

    @FXML
    private void cancel() {
        if (!save.isDisabled()) {
            ((Stage) save.getScene().getWindow()).close();
        }
    }
}
