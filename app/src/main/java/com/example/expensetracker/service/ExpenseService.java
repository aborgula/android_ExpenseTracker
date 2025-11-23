package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseService {

    public enum SortType {
        AMOUNT_ASC, AMOUNT_DESC,
        DATE_ASC, DATE_DESC,
        NAME_ASC, NAME_DESC
    }

    public List<Expense> sortExpenses(List<Expense> expenses, SortType sortType) {
        List<Expense> sorted = new ArrayList<>(expenses);

        switch (sortType) {
            case AMOUNT_ASC:
                sorted.sort((e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
                break;
            case AMOUNT_DESC:
                sorted.sort((e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
                break;
            case DATE_ASC:
                sorted.sort((e1, e2) -> parseDate(e1.getDate()).compareTo(parseDate(e2.getDate())));
                break;
            case DATE_DESC:
                sorted.sort((e1, e2) -> parseDate(e2.getDate()).compareTo(parseDate(e1.getDate())));
                break;
            case NAME_ASC:
                sorted.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
                break;
            case NAME_DESC:
                sorted.sort((e1, e2) -> e2.getName().compareToIgnoreCase(e1.getName()));
                break;
        }

        return sorted;
    }

    public List<Expense> filterExpenses(List<Expense> expenses,
                                        String minAmount,
                                        String maxAmount,
                                        List<String> selectedCategories) {
        double min = minAmount.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minAmount);
        double max = maxAmount.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxAmount);

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            boolean inCategory = selectedCategories.isEmpty() ||
                    selectedCategories.contains(e.getCategory());
            boolean inAmount = e.getAmount() >= min && e.getAmount() <= max;

            if (inCategory && inAmount) {
                filtered.add(e);
            }
        }

        return filtered;
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return new Date(0);
        }
    }
}
