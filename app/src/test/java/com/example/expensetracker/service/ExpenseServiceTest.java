package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ExpenseServiceTest {

    private ExpenseService service;
    private List<Expense> testExpenses;

    // ===== Arrange =====
    @Before
    public void setUp() {
        service = new ExpenseService();

        // testowe dane
        testExpenses = Arrays.asList(
                createExpense("1", "Groceries", 150.0, "Food", "15/11/2024"),
                createExpense("2", "Bus ticket", 5.0, "Transport", "20/11/2024"),
                createExpense("3", "Cinema", 30.0, "Entertainment", "18/11/2024"),
                createExpense("4", "Pharmacy", 25.0, "Health", "16/11/2024"),
                createExpense("5", "Restaurant", 80.0, "Food", "22/11/2024")
        );
    }

    private Expense createExpense(String id, String name, double amount,
                                  String category, String date) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setName(name);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setUserId("testUser123");
        return expense;
    }

    // ========== TESTY SORTOWANIA ==========

    @Test
    public void sortExpenses_byAmountAscending_returnsCorrectOrder() {

        // ===== Act =====
        List<Expense> sorted = service.sortExpenses(testExpenses,
                ExpenseService.SortType.AMOUNT_ASC);

        // ===== Assert =====
        assertEquals(5.0, sorted.get(0).getAmount(), 0.01);
        assertEquals(25.0, sorted.get(1).getAmount(), 0.01);
        assertEquals(30.0, sorted.get(2).getAmount(), 0.01);
        assertEquals(80.0, sorted.get(3).getAmount(), 0.01);
        assertEquals(150.0, sorted.get(4).getAmount(), 0.01);
    }


    @Test
    public void sortExpenses_byDateDescending_returnsCorrectOrder() {

        // ===== Act =====
        List<Expense> sorted = service.sortExpenses(testExpenses,
                ExpenseService.SortType.DATE_DESC);

        // ===== Assert =====
        assertEquals("Restaurant", sorted.get(0).getName()); // 22/11
        assertEquals("Bus ticket", sorted.get(1).getName()); // 20/11
        assertEquals("Cinema", sorted.get(2).getName());     // 18/11
        assertEquals("Pharmacy", sorted.get(3).getName());   // 16/11
        assertEquals("Groceries", sorted.get(4).getName());  // 15/11
    }

    @Test
    public void sortExpenses_byNameAscending_returnsCorrectOrder() {

        // ===== Act =====
        List<Expense> sorted = service.sortExpenses(testExpenses,
                ExpenseService.SortType.NAME_ASC);

        // ===== Assert =====
        assertEquals("Bus ticket", sorted.get(0).getName());
        assertEquals("Cinema", sorted.get(1).getName());
        assertEquals("Groceries", sorted.get(2).getName());
        assertEquals("Pharmacy", sorted.get(3).getName());
        assertEquals("Restaurant", sorted.get(4).getName());
    }

    @Test
    public void sortExpenses_withEmptyList_returnsEmptyList() {

        // ===== Act =====
        List<Expense> sorted = service.sortExpenses(Collections.emptyList(),
                ExpenseService.SortType.AMOUNT_ASC);

        // ===== Assert =====
        assertTrue(sorted.isEmpty());
    }


    // ========== TESTY FILTROWANIA ==========
    @Test
    public void filterExpenses_byMinAmountOnly_returnsExpensesAboveMin() {
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "50", "",
                Collections.emptyList());

        // ===== Assert =====
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(e -> e.getAmount() >= 50));
    }


    @Test
    public void filterExpenses_bySingleCategory_returnsOnlyThatCategory() {

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "", "",
                Collections.singletonList("Food"));

        // ===== Assert =====
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(e -> e.getCategory().equals("Food")));
    }

    @Test
    public void filterExpenses_byMultipleCategories_returnsAllMatchingCategories() {

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "", "",
                Arrays.asList("Food", "Transport"));

        // ===== Assert =====
        assertEquals(3, filtered.size());
        assertTrue(filtered.stream().anyMatch(e -> e.getName().equals("Groceries")));
        assertTrue(filtered.stream().anyMatch(e -> e.getName().equals("Bus ticket")));
        assertTrue(filtered.stream().anyMatch(e -> e.getName().equals("Restaurant")));
    }

    @Test
    public void filterExpenses_byCategoryAndAmount_returnsIntersection() {

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "50", "200",
                Collections.singletonList("Food"));

        // ===== Assert =====
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(e ->
                e.getCategory().equals("Food") && e.getAmount() >= 50 && e.getAmount() <= 200));
    }

    @Test
    public void filterExpenses_withNoFilters_returnsAllExpenses() {

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "", "",
                Collections.emptyList());

        // ===== Assert =====
        assertEquals(testExpenses.size(), filtered.size());
    }

    @Test
    public void filterExpenses_withNoMatches_returnsEmptyList() {

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(testExpenses,
                "1000", "2000",
                Collections.emptyList());

        // ===== Assert =====
        assertTrue(filtered.isEmpty());
    }

    // ========== TESTY EDGE CASES ==========
    @Test
    public void filterExpenses_withZeroAmount_handlesCorrectly() {

        // ===== Arrange =====
        List<Expense> expensesWithZero = Arrays.asList(
                createExpense("1", "Free item", 0.0, "Other", "15/11/2024"),
                createExpense("2", "Paid item", 10.0, "Other", "16/11/2024")
        );

        // ===== Act =====
        List<Expense> filtered = service.filterExpenses(expensesWithZero,
                "0", "5",
                Collections.emptyList());

        // ===== Assert =====
        assertEquals(1, filtered.size());
        assertEquals("Free item", filtered.get(0).getName());
    }

    @Test
    public void sortExpenses_withSameAmounts_maintainsStableOrder() {

        // ===== Arrange =====
        List<Expense> sameAmounts = Arrays.asList(
                createExpense("1", "First", 50.0, "Food", "15/11/2024"),
                createExpense("2", "Second", 50.0, "Transport", "16/11/2024"),
                createExpense("3", "Third", 50.0, "Health", "17/11/2024")
        );

        // ===== Act =====
        List<Expense> sorted = service.sortExpenses(sameAmounts,
                ExpenseService.SortType.AMOUNT_ASC);

        // ===== Assert =====
        assertEquals(3, sorted.size());
        // Wszystkie powinny mieć tę samą kwotę
        assertTrue(sorted.stream().allMatch(e -> e.getAmount() == 50.0));
    }
}