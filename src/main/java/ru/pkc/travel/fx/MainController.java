package ru.pkc.travel.fx;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import ru.pkc.travel.model.TripStatistics;
import java.io.IOException;
import java.util.*;

public class MainController {
    @FXML private TabPane tabs;
    @FXML private Label connection;
    @FXML private Button reconnect;
    private TravelContext context;
    private EntityReferences references;
    private final List<EntityController<?>> controllers = new ArrayList<>();
    private FlowPane statistics;
    private boolean connecting;
    public TravelContext context() { return context; }
    public EntityReferences references() { return references; }
    @FXML public void connect() {
        if (connecting) return;
        connecting = true; reconnect.setDisable(true); connection.setText("Подключение к PostgreSQL…");
        FxTasks.run(TravelContext::new, result -> {
            context = result; connecting = false; reconnect.setVisible(false); reconnect.setManaged(false);
            connection.setText(""); buildScreens();
        }, e -> {
            connecting = false; reconnect.setDisable(false);
            connection.setText("Нет подключения. Запустите PostgreSQL и проверьте DB_URL, DB_USER, DB_PASSWORD."); FxTasks.error(e);
        });
    }
    private void buildScreens() {
        try {
            addScreen("employee/employee.fxml", "Сотрудники");
            addScreen("businesstrip/businesstrip.fxml", "Поездки");
            addScreen("transportticket/transportticket.fxml", "Билеты");
            addScreen("accommodation/accommodation.fxml", "Проживание");
            addScreen("tripexpense/tripexpense.fxml", "Расходы");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/statistics.fxml"));
            VBox content = loader.load(); statistics = (FlowPane) loader.getNamespace().get("metrics");
            Button refresh = (Button) loader.getNamespace().get("refreshStatistics"); refresh.setOnAction(e -> refreshStatistics());
            Tab tab = new Tab("Статистика", content); tabs.getTabs().add(tab);
            tab.setOnSelectionChanged(e -> { if (tab.isSelected()) refreshStatistics(); }); refreshStatistics();
        } catch (IOException e) { FxTasks.error(e); }
    }
    private void addScreen(String resource, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/" + resource));
        var content = loader.<VBox>load(); EntityController<?> controller = loader.getController();
        controllers.add(controller); Tab tab = new Tab(title, content); tabs.getTabs().add(tab);
        controller.configure(this);
        tab.setOnSelectionChanged(e -> { if (tab.isSelected()) controller.reload(); });
    }
    public void loadReferences(Runnable ready) {
        tabs.setDisable(true);
        FxTasks.run(() -> new EntityReferences(context.employees.findAll(), context.trips.findAll()), result -> {
            references = result; tabs.setDisable(false); ready.run();
        }, e -> { tabs.setDisable(false); FxTasks.error(e); });
    }
    public void refreshAll() { controllers.forEach(EntityController::reload); refreshStatistics(); }
    private void refreshStatistics() {
        if (statistics == null) return;
        statistics.setDisable(true);
        FxTasks.run(context.trips::statistics, this::showStatistics, e -> { statistics.setDisable(false); FxTasks.error(e); });
    }
    private void showStatistics(TripStatistics s) {
        statistics.getChildren().clear();
        metric("Всего поездок", s.getTotal()); metric("Запланированы", s.getPlanned());
        metric("Согласованы / в процессе", s.getActive()); metric("Завершены", s.getCompleted()); metric("Отменены", s.getCancelled());
        metric("Плановый бюджет, ₽", s.getTotalPlannedCost()); metric("Фактические затраты, ₽", s.getTotalActualCost());
        metric("Средняя длительность, дней", String.format(Locale.forLanguageTag("ru-RU"), "%.1f", s.getAverageDurationDays()));
        statistics.setDisable(false);
    }
    private void metric(String name, Object value) {
        Label heading = new Label(name); heading.getStyleClass().add("muted");
        Label number = new Label(value.toString()); number.getStyleClass().add("metric-value");
        VBox card = new VBox(12, heading, number); card.getStyleClass().add("metric-card"); statistics.getChildren().add(card);
    }
}
