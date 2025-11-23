package com.example.expensetracker.ui.fragments;

import static org.junit.Assert.assertEquals;

import com.example.expensetracker.model.Expense;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragmentTest {
    private List<Expense> expenses;

    // ===================== Test 9 =====================
    // Sprawdza, czy lista wydatków jest poprawnie sortowana rosnąco po kwocie
    @Test
    public void givenExpenses_whenSortByAmountAscending_thenSortedCorrectly() {

        // Given
        // Expected order: Bus (2.00) -> Coffee (5.50) -> Restaurant (45.99)
        expenses = new ArrayList<>();
        expenses.add(new Expense("1", "Coffee", "15/11/2025", 5.50, "Food", 0, "user1"));
        expenses.add(new Expense("2", "Bus ticket", "10/11/2025", 2.00, "Transport", 0, "user1"));
        expenses.add(new Expense("3", "Restaurant", "20/11/2025", 45.99, "Food", 0, "user1"));

        // When
        sortExpensesByAmount(expenses, true); // ascending

        // Then
        assertEquals("First should be cheapest", 2.00, expenses.get(0).getAmount(), 0.01);
        assertEquals("Second should be middle price", 5.50, expenses.get(1).getAmount(), 0.01);
        assertEquals("Third should be most expensive", 45.99, expenses.get(2).getAmount(), 0.01);
    }


    // ===================== Test 10 =====================
    // Sprawdza, czy lista wydatków jest poprawnie filtrowana po zakresie kwot
    @Test
    public void givenExpenses_whenFilterByAmountRange_thenReturnsOnlyMatchingExpenses() {
        // Given
        expenses = new ArrayList<>();
        expenses.add(new Expense("1", "Coffee", "15/11/2025", 5.50, "Food", 0, "user1"));
        expenses.add(new Expense("2", "Bus ticket", "10/11/2025", 2.00, "Transport", 0, "user1"));
        expenses.add(new Expense("3", "Restaurant", "20/11/2025", 45.99, "Food", 0, "user1"));

        double minAmount = 3.00;
        double maxAmount = 10.00;
        // Expected: tylko Coffee (5.50) powinno zostać

        // When
        List<Expense> filtered = filterByAmount(expenses, minAmount, maxAmount);

        // Then
        assertEquals("Should return only 1 expense in range", 1 , filtered.size());
        assertEquals("Should be Coffee", "Coffee", filtered.get(0).getName());
        assertEquals("Should have amount 5.50", 5.50, filtered.get(0).getAmount(), 0.01);
    }

    // Uproszczona wersja sortExpenses() do testowania
    private void sortExpensesByAmount(List<Expense> list, boolean ascending) {
        if (ascending) {
            list.sort((e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
        } else {
            list.sort((e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
        }
    }

    // Filtrowanie po zakresie kwot - wycięta logika z showFilterDialog
    private List<Expense> filterByAmount(List<Expense> list, double min, double max) {
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : list) {
            if (e.getAmount() >= min && e.getAmount() <= max) {
                filtered.add(e);
            }
        }
        return filtered;
    }
}
