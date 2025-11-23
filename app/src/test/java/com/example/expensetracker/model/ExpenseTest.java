package com.example.expensetracker.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExpenseTest {

    // ===================== Test 1 =====================
    // Sprawdza konstruktor i getter-y
    @Test
    public void givenExpenseData_whenCreated_thenGettersReturnCorrectValues() {
        // Given
        String id = "exp1";
        String name = "Lunch";
        String date = "05/11/2025";
        double amount = 25.5;
        String category = "Food";
        int icon = 101;
        String userId = "user123";

        // When
        Expense expense = new Expense(id, name, date, amount, category, icon, userId);

        // Then
        assertEquals(id, expense.getId());
        assertEquals(name, expense.getName());
        assertEquals(date, expense.getDate());
        assertEquals(amount, expense.getAmount(), 0.0);
        assertEquals(category, expense.getCategory());
        assertEquals(userId, expense.getUserId());
    }

    // ===================== Test 2 =====================
    // Sprawdza setter id
    @Test
    public void givenNewId_whenSetId_thenIdIsUpdated() {
        // Given
        Expense expense = new Expense();
        String newId = "exp2";

        // When
        expense.setId(newId);

        // Then
        assertEquals(newId, expense.getId());
    }

    // ===================== Test 3 =====================
    // Sprawdza, czy kwota wydatku jest poprawnie ustawiana i zwracana
    @Test
    public void givenExpenseAmount_whenGetAmount_thenReturnCorrectValue() {
        // Given
        Expense expense = new Expense();
        double amount = 99.99;

        // When
        expense = new Expense("id", "Dinner", "05/11/2025", amount, "Food", 101, "user123");

        // Then
        assertEquals(amount, expense.getAmount(), 0.0);
    }

    // ===================== Test 4 =====================
    // Sprawdza zachowanie przy pustych polach
    @Test
    public void givenEmptyFields_whenCreated_thenGettersReturnExpectedDefaults() {
        // Given & When
        Expense expense = new Expense();

        // Then
        assertNull(expense.getId());
        assertNull(expense.getName());
        assertNull(expense.getDate());
        assertEquals(0.0, expense.getAmount(), 0.0);
        assertNull(expense.getCategory());
        assertNull(expense.getUserId());
    }
}
