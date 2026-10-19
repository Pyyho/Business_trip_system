package ru.pkc.travel.fx;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.junit.jupiter.api.*;
import ru.pkc.travel.model.Employee;
import ru.pkc.travel.exception.BusinessException;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.pkc.travel.fx.EntityScreen.column;

class FxViewsTest {
    @BeforeAll static void startToolkit() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(() -> { Platform.setImplicitExit(false); started.countDown(); });
        assertTrue(started.await(10, TimeUnit.SECONDS));
    }
    private static <T> T fx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work); Platform.runLater(task); return task.get(10, TimeUnit.SECONDS);
    }
    @Test void fxmlResourcesAndHandlersLoad() throws Exception {
        fx(() -> {
            for (String file : List.of("main", "entity", "form", "statistics")) {
                Parent view = FXMLLoader.load(getClass().getResource("/fx/" + file + ".fxml"));
                assertNotNull(view);
            }
            return null;
        });
    }
    @Test void tableCombinesSearchFiltersAndNumericSorting() throws Exception {
        CountDownLatch loaded = new CountDownLatch(1);
        FXMLLoader loader = fx(() -> {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/fx/entity.fxml"));
            Parent root = fxml.load();
            root.disableProperty().addListener((o,a,b) -> { if (!b) loaded.countDown(); });
            EntityScreen<Employee> screen = new EntityScreen<>("Сотрудники", List.of(
                column("ФИО", Employee::getFullName), column("Email", Employee::getEmail), column("Отдел", Employee::getDepartment), column("Число", Employee::getId)),
                () -> List.of(new Employee(10L,"Анна", "anna@test.ru","ИТ"), new Employee(2L,"Борис", "boris@test.ru","ИТ"), new Employee(3L,"Вера", "vera@test.ru","Кадры")),
                Employee::getId, id -> fail("Unexpected deletion"), (e,f) -> { }, 2, 0, null);
            fxml.<EntityController<Employee>>getController().configure(screen, null);
            return fxml;
        });
        assertTrue(loaded.await(10, TimeUnit.SECONDS));
        fx(() -> {
            TableView<Employee> table = (TableView<Employee>) loader.getNamespace().get("table");
            TextField search = (TextField) loader.getNamespace().get("search");
            ComboBox<String> department = (ComboBox<String>) loader.getNamespace().get("filterOne");
            assertEquals(3, table.getItems().size());
            search.setText("TEST.RU"); department.setValue("ИТ"); assertEquals(2, table.getItems().size());
            search.setText("борис"); assertEquals("Борис", table.getItems().getFirst().getFullName());
            search.clear(); department.setValue("Все");
            table.getSortOrder().setAll(table.getColumns().get(3)); table.sort();
            assertEquals(List.of(2L,3L,10L), table.getItems().stream().map(Employee::getId).toList());
            return null;
        });
    }
    @Test void formRejectsInvalidValuesBeforeCallingServices() throws Exception {
        fx(() -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/form.fxml")); loader.load();
            FormController form = loader.getController();
            assertThrows(BusinessException.class, () -> form.money("Цена", new java.math.BigDecimal("1.001")).get());
            assertThrows(BusinessException.class, () -> form.text("Название", " ", 10).get());
            assertThrows(BusinessException.class, () -> form.choice("Сотрудник", List.of(), null).get());
            assertThrows(BusinessException.class, () -> form.date("Дата", null).get());
            assertEquals(java.time.LocalDateTime.of(2026,1,2,9,0), form.time("Время", java.time.LocalDateTime.of(2026,1,2,9,0)).get());
            return null;
        });
    }
}
