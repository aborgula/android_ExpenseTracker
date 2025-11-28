package com.example.expensetracker.service;

import android.util.Log;
import com.example.expensetracker.model.Expense;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public class ExpenseService {

    private static final String TAG = "ExpenseService";
    private final SimpleDateFormat dateFormat;

    public ExpenseService() {
        this.dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
    }


    public enum SortType {
        AMOUNT_ASC, AMOUNT_DESC,
        DATE_ASC, DATE_DESC,
        NAME_ASC, NAME_DESC
    }

    public enum TimeFilter {
        TODAY, YESTERDAY, WEEK, MONTH, YEAR, ALL
    }


    public List<Expense> sortExpenses(List<Expense> expenses, SortType sortType) {
        if (expenses == null || expenses.isEmpty()) {
            return new ArrayList<>();
        }

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
        if (expenses == null || expenses.isEmpty()) {
            return new ArrayList<>();
        }

        double min = minAmount == null || minAmount.isEmpty() ? Double.MIN_VALUE : parseDouble(minAmount);
        double max = maxAmount == null || maxAmount.isEmpty() ? Double.MAX_VALUE : parseDouble(maxAmount);

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            boolean inCategory = selectedCategories == null || selectedCategories.isEmpty() ||
                    selectedCategories.contains(e.getCategory());
            boolean inAmount = e.getAmount() >= min && e.getAmount() <= max;

            if (inCategory && inAmount) {
                filtered.add(e);
            }
        }

        return filtered;
    }


    public List<Expense> filterByTime(List<Expense> expenses, TimeFilter timeFilter) {
        if (expenses == null || expenses.isEmpty()) {
            return new ArrayList<>();
        }

        List<Expense> filtered = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        normalizeDate(today);

        for (Expense expense : expenses) {
            try {
                Date parsedDate = dateFormat.parse(expense.getDate());
                if (parsedDate == null) continue;

                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTime(parsedDate);
                normalizeDate(expenseCal);

                long diffDays = calculateDaysDifference(today, expenseCal);

                if (matchesTimeFilter(diffDays, timeFilter)) {
                    filtered.add(expense);
                }

            } catch (ParseException ex) {
                Log.e(TAG, "Date parse error for " + expense.getDate(), ex);
            }
        }

        Log.d(TAG, "Time filter: " + timeFilter + " → " + filtered.size() + " results");
        return filtered;
    }


    private boolean matchesTimeFilter(long diffDays, TimeFilter timeFilter) {
        switch (timeFilter) {
            case TODAY:
                return diffDays == 0;
            case YESTERDAY:
                return diffDays == 1;
            case WEEK:
                return diffDays <= 7 && diffDays >= 0;
            case MONTH:
                return diffDays <= 30 && diffDays >= 0;
            case YEAR:
                return diffDays <= 365 && diffDays >= 0;
            case ALL:
                return true;
            default:
                return false;
        }
    }


    public Map<String, Float> groupByDay(List<Expense> expenses) {
        Map<String, Float> dailySums = new TreeMap<>();

        if (expenses == null || expenses.isEmpty()) {
            return dailySums;
        }

        for (Expense expense : expenses) {
            try {
                Date date = dateFormat.parse(expense.getDate());
                if (date == null) continue;

                String dayKey = dateFormat.format(date);
                float amount = (float) expense.getAmount();

                dailySums.put(dayKey, dailySums.getOrDefault(dayKey, 0f) + amount);
            } catch (ParseException ex) {
                Log.e(TAG, "Date parse error for " + expense.getDate(), ex);
            }
        }

        return dailySums;
    }


    public Map<String, Float> groupByCategory(List<Expense> expenses) {
        Map<String, Float> categorySums = new TreeMap<>();

        if (expenses == null || expenses.isEmpty()) {
            return categorySums;
        }

        for (Expense expense : expenses) {
            String category = expense.getCategory();
            if (category == null || category.isEmpty()) {
                category = "Uncategorized";
            }

            float amount = (float) expense.getAmount();
            categorySums.put(category, categorySums.getOrDefault(category, 0f) + amount);
        }

        return categorySums;
    }



    public float calculateTotal(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return 0f;
        }

        float total = 0f;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }


    private Date parseDate(String dateStr) {
        try {
            Date date = dateFormat.parse(dateStr);
            return date != null ? date : new Date(0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse date: " + dateStr, e);
            return new Date(0);
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse double: " + value, e);
            return 0.0;
        }
    }


    private long calculateDaysDifference(Calendar today, Calendar expenseDate) {
        long diffMillis = today.getTimeInMillis() - expenseDate.getTimeInMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }


    private void normalizeDate(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}