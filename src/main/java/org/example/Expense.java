package org.example;

import java.time.LocalDate;

/**
 * Этот класс представляет одну трату, включая категорию, сумму и дату.
 */
public class Expense {
    private final String category;
    private final double amount;
    private final LocalDate date;

    /**
     * Создает новую трату.
     *
     * @param category категория траты.
     * @param amount сумма траты.
     * @param date дата траты.
     */
    public Expense(String category, double amount, LocalDate date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    /**
     * Возвращает категорию траты.
     *
     * @return категория.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Возвращает сумму траты.
     *
     * @return сумма.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Возвращает дату траты.
     *
     * @return дата.
     */
    public LocalDate getDate() {
        return date;
    }
}