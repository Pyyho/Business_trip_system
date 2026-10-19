package ru.pkc.travel.fx;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import ru.pkc.travel.exception.BusinessException;
import ru.pkc.travel.exception.DataAccessException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FxTasks {
    private static final Logger LOG = Logger.getLogger(FxTasks.class.getName());
    private static final java.util.concurrent.ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "travel-database"); thread.setDaemon(true); return thread;
    });
    private FxTasks() { }
    public static <T> void run(Callable<T> work, Consumer<T> success, Consumer<Throwable> failure) {
        Task<T> task = new Task<>() { @Override protected T call() throws Exception { return work.call(); } };
        task.setOnSucceeded(e -> success.accept(task.getValue()));
        task.setOnFailed(e -> { LOG.log(Level.WARNING, "Операция не выполнена", task.getException()); failure.accept(task.getException()); });
        EXECUTOR.submit(task);
    }
    public static String message(Throwable e) {
        if (e instanceof BusinessException) return e.getMessage();
        if (e instanceof DataAccessException) return "Не удалось выполнить операцию с базой данных. Проверьте PostgreSQL и параметры DB_URL, DB_USER, DB_PASSWORD. Данные могли измениться — обновите таблицу.";
        return "Операция не выполнена. Попробуйте обновить данные. Подробности записаны в журнал.";
    }
    public static void error(Throwable e) {
        new Alert(Alert.AlertType.ERROR, message(e)).showAndWait();
    }
}
