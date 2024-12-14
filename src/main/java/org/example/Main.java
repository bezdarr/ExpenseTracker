package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс приложения, отвечающий за отображение пользовательского интерфейса.
 */
public class Main extends Application {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private ExpenseManager expenseManager;
    private TextArea outputArea;

    private static final String[] VALID_CATEGORIES = {"Продукты", "Транспорт", "Развлечения", "Одежда", "Жильё",
            "Коммунальные услуги", "Связь", "Здоровье", "Образование", "Другое"};

    /**
     * Запускает приложение.
     *
     * @param args аргументы командной строки.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Инициализация основного окна приложения и создание интерфейса.
     *
     * @param primaryStage основная стадия приложения.
     */
    @Override
    public void start(Stage primaryStage) {
        expenseManager = new ExpenseManager();
        primaryStage.setTitle("Счётчик затрат");

        Label amountLabel = new Label("Цена:");
        TextField amountField = new TextField();

        Label categoryLabel = new Label("Категория:");
        ComboBox<String> categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(VALID_CATEGORIES));

        Label dateLabel = new Label("Дата (yyyy-MM-dd):");
        TextField dateField = new TextField();

        Button addButton = new Button("Добавить трату");
        Button exportButton = new Button("Экспорт в Excel");
        Button viewStatsButton = new Button("Посмотреть статистику по категориям");
        Button totalExpensesButton = new Button("Посмотреть общие затраты");

        outputArea = new TextArea();
        outputArea.setEditable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8);
        grid.setHgap(10);

        grid.add(amountLabel, 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(categoryLabel, 0, 1);
        grid.add(categoryComboBox, 1, 1);
        grid.add(dateLabel, 0, 2);
        grid.add(dateField, 1, 2);
        grid.add(addButton, 0, 3);
        grid.add(exportButton, 1, 3);
        grid.add(viewStatsButton, 2, 3);
        grid.add(totalExpensesButton, 0, 4);
        grid.add(outputArea, 0, 5, 3, 1);

        addButton.setOnAction(e -> {
            String amountText = amountField.getText();
            String category = categoryComboBox.getValue(); // Получить выбранную категорию
            String dateText = dateField.getText();

            try {
                double amount = Double.parseDouble(amountText);
                LocalDate date = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (category == null) {
                    showAlert("Ошибка", "Пожалуйста, выберите предложенную категорию!", Alert.AlertType.ERROR);
                    logger.error("Пользователь не выбрал категорию!");
                    return;
                }
                if (amount<0) {
                    showAlert("Ошибка", "Неверный формат цены!", Alert.AlertType.ERROR);
                    logger.error("Ошибка при парсинге суммы!");
                }
                else {
                    expenseManager.addExpense(category, amount, date);
                    showAlert("Успешно", "Трата успешно добавлена!", Alert.AlertType.INFORMATION);
                    logger.info("Трата добавлена: категория={}, сумма={}, дата={}", category, amount, date);
                }
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат цены!", Alert.AlertType.ERROR);
                logger.error("Ошибка при парсинге суммы: {}", ex.getMessage());
            } catch (java.time.format.DateTimeParseException ex) {
                showAlert("Ошибка", "Неверный формат даты! Используйте yyyy-MM-dd.", Alert.AlertType.ERROR);
                logger.error("Ошибка при парсинге даты: {}", ex.getMessage());
            } catch (Exception ex) {
                showAlert("Ошибка", "Непредвиденная ошибка: " + ex.getMessage(), Alert.AlertType.ERROR);
                logger.fatal("Неизвестная ошибка: {}", ex.getMessage(), ex);
            } finally {
                amountField.clear();
                categoryComboBox.getSelectionModel().clearSelection(); // Очистка выбора категории
                dateField.clear();
            }
        });

        exportButton.setOnAction(e -> {
            try {
                expenseManager.exportToExcel("expenses.xlsx");
                showAlert("Успешно", "Траты успешно перенесены в expenses.xlsx!", Alert.AlertType.INFORMATION);
                logger.info("Траты успешно экспортированы в файл expenses.xlsx");
            } catch (IOException ex) {
                showAlert("Ошибка", "Не удалось экспортировать в Excel: " + ex.getMessage(), Alert.AlertType.ERROR);
                logger.error("Ошибка экспорта в Excel: {}", ex.getMessage());
            } catch (Exception ex) {
                showAlert("Ошибка", "Непредвиденная ошибка: " + ex.getMessage(), Alert.AlertType.ERROR);
                logger.fatal("Неизвестная ошибка при экспорте в Excel: {}", ex.getMessage(), ex);
            }
        });


        viewStatsButton.setOnAction(e -> {
            try {
                Map<String, Double> categoryStats = expenseManager.getTotalByCategory();
                if (!categoryStats.isEmpty()) {
                    displayCategoryStats(primaryStage, categoryStats);
                    logger.info("Статистика по категориям отображена");
                } else {
                    showAlert("Инфо", "Траты не были добавлены!", Alert.AlertType.INFORMATION);
                    logger.warn("Нет данных для статистики по категориям");
                }
            } catch (Exception ex) {
                showAlert("Ошибка", "Непредвиденная ошибка: " + ex.getMessage(), Alert.AlertType.ERROR);
                logger.fatal("Неизвестная ошибка при получении статистики по категориям: {}", ex.getMessage(), ex);
            }
        });
        
        totalExpensesButton.setOnAction(e -> {
            try {
                double totalExpenses = expenseManager.getTotalExpenses();
                outputArea.setText("Общие траты: " + totalExpenses);
                logger.info("Отображены общие затраты: {}", totalExpenses);
            } catch (Exception ex) {
                showAlert("Ошибка", "Непредвиденная ошибка: " + ex.getMessage(), Alert.AlertType.ERROR);
                logger.fatal("Неизвестная ошибка при получении общих затрат: {}", ex.getMessage(), ex);
            }
        });

        Scene scene = new Scene(grid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        logger.info("Интерфейс приложения запущен.");
    }

    /**
     * Отображает статистику трат по категориям в виде круговой диаграммы.
     *
     * @param primaryStage основная стадия приложения.
     * @param categoryStats карта с категориями и соответствующими тратами.
     */
    private void displayCategoryStats(Stage primaryStage, Map<String, Double> categoryStats) {
        try {
            double total = categoryStats.values().stream().mapToDouble(Double::doubleValue).sum();
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

            for (Map.Entry<String, Double> entry : categoryStats.entrySet()) {
                double percentage = total == 0 ? 0 : (entry.getValue() / total) * 100;
                pieChartData.add(new PieChart.Data(entry.getKey() + " (" + String.format("%.2f", percentage) + "%)", entry.getValue()));
            }

            PieChart pieChart = new PieChart(pieChartData);
            pieChart.setTitle("Категории трат");

            VBox vbox = new VBox(pieChart);
            vbox.setPadding(new Insets(10));

            for (String category : categoryStats.keySet()) {
                double amount = categoryStats.get(category);
                double percentage = total == 0 ? 0 : (amount / total) * 100;
                vbox.getChildren().add(new Label(category + ": " + String.format("%.2f", percentage) + "%"));
            }

            Scene statsScene = new Scene(vbox, 600, 400);
            Stage statsStage = new Stage();
            statsStage.setTitle("Статистика по категориям");
            statsStage.setScene(statsScene);
            statsStage.show();
            logger.info("Диаграмма категорий затрат успешно создана и показана");
        } catch (Exception ex) {
            showAlert("Ошибка", "Непредвиденная ошибка при отображении трат: " + ex.getMessage(), Alert.AlertType.ERROR);
            logger.error("Ошибка при создании диаграммы категорий затрат: {}", ex.getMessage());
        }
    }

    /**
     * Показывает всплывающее сообщение для пользователя.
     *
     * @param title заголовок сообщения.
     * @param message текст сообщения.
     * @param alertType тип предупреждения.
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        logger.info("Отображено сообщение: {}", message);
    }
}