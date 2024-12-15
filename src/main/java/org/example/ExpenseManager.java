package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Класс ExpenseManager управляет расходами, позволяя добавлять новые расходы,
 * получать отчеты о расходах и экспортировать данные в Excel.
 */
public class ExpenseManager {
    private Map<String, List<Expense>> expensesByCategory = new HashMap<>();

    /**
     * Добавляет новый расход в указанную категорию.
     *
     * @param category категория расхода.
     * @param amount   сумма расхода.
     * @param date     дата расхода.
     */
    public void addExpense(String category, double amount, LocalDate date) {
        expensesByCategory.computeIfAbsent(category, k -> new ArrayList<>())
                .add(new Expense(category, amount, date));
    }

    /**
     * Возвращает общую сумму всех расходов.
     *
     * @return общая сумма расходов.
     */
    public double getTotalExpenses() {
        return expensesByCategory.values().stream()
                .flatMap(List::stream)
                .mapToDouble(e -> e.getAmount())
                .sum();
    }


    /**
     * Экспортирует список расходов в указанный файл Excel.
     *
     * @param filePath путь к файлу, в который будут экспортированы данные.
     * @throws IOException если возникает ошибка при записи в файл.
     */
    public void exportToExcel(String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook(); // Создание книги
        Sheet sheet = workbook.createSheet("Расходы");

        // Добавление строки заголовка
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Дата");
        headerRow.createCell(1).setCellValue("Категория");
        headerRow.createCell(2).setCellValue("Сумма");

        int rowNum = 1;
        for (Map.Entry<String, List<Expense>> entry : expensesByCategory.entrySet()) {
            for (Expense expense : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getDate().toString());
                row.createCell(1).setCellValue(expense.getCategory());
                row.createCell(2).setCellValue(expense.getAmount());
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
    }


    /**
     * Возвращает карту, где ключ - категория, а значение - общая сумма расходов по этой категории.
     *
     * @return карта с общими расходами по категориям.
     */
    public Map<String, Double> getTotalByCategory() {
        return expensesByCategory.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .mapToDouble(e -> e.getAmount())
                        .sum()));
    }
}
