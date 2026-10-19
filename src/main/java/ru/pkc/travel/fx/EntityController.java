package ru.pkc.travel.fx;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.*;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import ru.pkc.travel.model.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

public class EntityController<T> {
    @FXML private VBox root;
    @FXML private Label title, count, dateError, filterOneLabel, filterTwoLabel;
    @FXML private TextField search;
    @FXML private ComboBox<String> filterOne, filterTwo;
    @FXML private DatePicker from, to;
    @FXML private HBox dates;
    @FXML private TableView<T> table;
    @FXML private Button edit, delete, status;
    private final ObservableList<T> data = FXCollections.observableArrayList();
    private FilteredList<T> filtered;
    private EntityScreen<T> screen;
    private MainController main;
    private static final String ALL = "Все";

    public void configure(EntityScreen<T> screen, MainController main) {
        this.screen = screen; this.main = main; title.setText(screen.title());
        filterOneLabel.setText(screen.columns().get(screen.firstFilter()).title());
        filterTwoLabel.setText(screen.columns().get(screen.secondFilter()).title());
        for (var definition : screen.columns()) {
            TableColumn<T, Object> column = new TableColumn<>(definition.title());
            column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(definition.value().apply(cell.getValue())));
            column.setPrefWidth(definition.title().equals("Поездка") ? 320 : 170);
            column.setComparator(EntityController::compare);
            table.getColumns().add(column);
        }
        filtered = new FilteredList<>(data);
        SortedList<T> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty()); table.setItems(sorted);
        edit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        delete.disableProperty().bind(edit.disableProperty()); status.disableProperty().bind(edit.disableProperty());
        status.setVisible(screen.title().equals("Поездки")); status.managedProperty().bind(status.visibleProperty());
        dates.setVisible(screen.date() != null); dates.managedProperty().bind(dates.visibleProperty());
        search.textProperty().addListener((o,a,b) -> applyFilters());
        filterOne.valueProperty().addListener((o,a,b) -> applyFilters());
        filterTwo.valueProperty().addListener((o,a,b) -> applyFilters());
        from.valueProperty().addListener((o,a,b) -> applyFilters()); to.valueProperty().addListener((o,a,b) -> applyFilters());
        table.setRowFactory(view -> { TableRow<T> row = new TableRow<>(); row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !row.isEmpty()) showForm(row.getItem());
        }); return row; });
        reload();
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compare(Object a, Object b) {
        if (a == b) return 0; if (a == null) return -1; if (b == null) return 1;
        if (a.getClass().equals(b.getClass()) && a instanceof Comparable c) return c.compareTo(b);
        return a.toString().compareToIgnoreCase(b.toString());
    }
    private void fillFilter(ComboBox<String> combo, int index) {
        String previous = combo.getValue();
        var values = new TreeSet<String>();
        for (T item : data) values.add(value(item, index));
        combo.getItems().setAll(ALL); combo.getItems().addAll(values);
        combo.setValue(previous != null && values.contains(previous) ? previous : ALL);
        combo.setPromptText(screen.columns().get(index).title());
        combo.setTooltip(new Tooltip(screen.columns().get(index).title()));
    }
    private String value(T item, int index) { return FormController.display(screen.columns().get(index).value().apply(item)); }
    private void applyFilters() {
        if (filtered == null) return;
        boolean invalid = from.getValue() != null && to.getValue() != null && to.getValue().isBefore(from.getValue());
        dateError.setText(invalid ? "Конец периода должен быть не раньше начала." : "");
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(item -> {
            if (invalid) return false;
            if (!query.isEmpty() && screen.columns().stream().noneMatch(c -> FormController.display(c.value().apply(item)).toLowerCase(Locale.ROOT).contains(query))) return false;
            if (!matches(filterOne, value(item, screen.firstFilter())) || !matches(filterTwo, value(item, screen.secondFilter()))) return false;
            LocalDate day = screen.date() == null ? null : screen.date().apply(item);
            return day == null || ((from.getValue() == null || !day.isBefore(from.getValue())) && (to.getValue() == null || !day.isAfter(to.getValue())));
        });
        count.setText("Показано: " + filtered.size() + " из " + data.size() + "  •  Сортировка: нажмите заголовок столбца; Shift — несколько столбцов");
    }
    private boolean matches(ComboBox<String> filter, String value) { return filter.getValue() == null || ALL.equals(filter.getValue()) || filter.getValue().equals(value); }
    public void reload() {
        root.setDisable(true);
        FxTasks.run(screen.load()::get, result -> {
            data.setAll(result); fillFilter(filterOne, screen.firstFilter()); fillFilter(filterTwo, screen.secondFilter());
            applyFilters(); root.setDisable(false);
        }, e -> { root.setDisable(false); FxTasks.error(e); });
    }
    @FXML private void reset() { search.clear(); filterOne.setValue(ALL); filterTwo.setValue(ALL); from.setValue(null); to.setValue(null); }
    @FXML private void refresh() { reload(); }
    @FXML private void add() { showForm(null); }
    @FXML private void edit() { T selected = table.getSelectionModel().getSelectedItem(); if (selected != null) showForm(selected); }
    private void showForm(T selected) {
        main.loadReferences(() -> openDialog(selected == null ? "Добавить • " + screen.title() : "Редактировать • " + screen.title(),
                form -> screen.form().accept(selected, form)));
    }
    private void openDialog(String heading, Consumer<FormController> setup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/form.fxml")); Parent content = loader.load();
            FormController controller = loader.getController(); controller.configure(heading, main::refreshAll); setup.accept(controller);
            Stage dialog = new Stage(); dialog.initOwner(root.getScene().getWindow()); dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(heading); Scene scene = new Scene(content, 720, 650);
            scene.getStylesheets().setAll(root.getScene().getStylesheets()); dialog.setScene(scene); dialog.setMinWidth(620); dialog.setMinHeight(480);
            dialog.setOnCloseRequest(event -> { if (content.lookup("#save").isDisabled()) event.consume(); }); dialog.showAndWait();
        } catch (IOException e) { FxTasks.error(e); }
    }
    @FXML private void changeStatus() {
        T selected = table.getSelectionModel().getSelectedItem();
        if (!(selected instanceof BusinessTrip trip)) return;
        openDialog("Статус поездки • " + trip.getDestination(), form -> {
            var target = form.choice("Новый статус", Arrays.stream(TripStatus.values()).filter(trip.getStatus()::canTransitionTo).toList(), null);
            var actual = form.money("Факт, ₽ (только для завершения)", java.math.BigDecimal.ZERO);
            form.onSave(() -> { var state = target.get(); var sum = state == TripStatus.COMPLETED ? actual.get() : null;
                return () -> main.context().trips.changeStatus(trip.getId(), state, sum); });
        });
    }
    @FXML private void delete() {
        T selected = table.getSelectionModel().getSelectedItem(); if (selected == null) return;
        String detail = screen.columns().stream().limit(3).map(c -> c.title() + ": " + FormController.display(c.value().apply(selected))).collect(java.util.stream.Collectors.joining("\n"));
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, detail + (selected instanceof BusinessTrip ? "\nТакже будут удалены билеты, проживание и расходы этой поездки." : ""), ButtonType.YES, ButtonType.CANCEL);
        confirm.initOwner(root.getScene().getWindow()); confirm.setHeaderText("Удалить запись? • " + screen.title());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.YES) return;
        root.setDisable(true);
        FxTasks.run(() -> { screen.delete().accept(screen.id().applyAsLong(selected)); return null; }, v -> {
            root.setDisable(false); main.refreshAll();
        }, e -> { root.setDisable(false); FxTasks.error(e); });
    }
    @FXML private void export() {
        FileChooser chooser = new FileChooser(); chooser.setTitle("Экспорт • " + screen.title()); chooser.setInitialFileName(screen.title() + ".csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (UTF-8)", "*.csv"));
        var file = chooser.showSaveDialog(root.getScene().getWindow()); if (file == null) return;
        var headers = screen.columns().stream().map(EntityScreen.Column::title).toList();
        var rows = table.getItems().stream().map(item -> screen.columns().stream().map(c -> FormController.display(c.value().apply(item))).toList()).toList();
        FxTasks.run(() -> { CsvExporter.write(file.toPath(), headers, rows); return rows.size(); }, n -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Сохранено записей: " + n + "\n" + file.getAbsolutePath());
            alert.setHeaderText("Экспорт завершён"); alert.showAndWait();
        }, e -> { Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось сохранить файл. Проверьте права доступа и не открыт ли файл в другой программе."); alert.showAndWait(); });
    }
}
