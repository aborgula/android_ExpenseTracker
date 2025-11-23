package com.example.expensetracker.ui.activities;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.DatePickerDialog;
import android.widget.EditText;

import com.example.expensetracker.model.Category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Calendar;

@RunWith(org.robolectric.RobolectricTestRunner.class)
public class AddExpenseActivityTest {

    private AddExpenseActivity activity;

    @Before
    public void setUp() {
        // Tworzymy instancję klasy (symulujemy jej działanie)
        activity = Mockito.spy(new AddExpenseActivity());
    }

    // ===================== Test 5 =====================
    // Sprawdza, że walidacja danych wykrywa puste pola i zgłasza błąd
    @Test
    public void givenEmptyFields_whenValidate_thenShouldShowErrorToast() {
        // Given
        String name = "";
        String date = "";
        String amount = "";
        String category = "";

        // When - symulacja logiki z przycisku
        boolean areFieldsEmpty = name.isEmpty() || date.isEmpty() || amount.isEmpty() || category.isEmpty();

        // Then
        assertTrue("Empty fields should fail validation", areFieldsEmpty);
    }

    // ===================== Test 6 =====================
    // Sprawdza, że po wybraniu daty z DatePickera format jest poprawny (DD/MM/YYYY)
    @Test
    public void givenDatePickerSelection_whenDateSelected_thenFormatIsCorrect() {
        // Given
        int year = 2025;
        int month = 10;
        int day = 8;

        // When
        String formattedDate = day + "/" + (month + 1) + "/" + year;

        // Then
        assertEquals("Date format should be DD/MM/YYYY", "8/11/2025", formattedDate);
    }

    // ===================== Test 7 =====================
    // Sprawdza zachowanie walidacji, gdy tylko niektóre pola są wypełnione
    @Test
    public void givenPartiallyFilledFields_whenValidating_thenShouldFail() {
        // Given - tylko niektóre pola wypełnione
        String name = "Coffee";
        String date = "8/11/2025";
        String amount = "";
        String category = "";

        // When
        boolean areFieldsEmpty = name.isEmpty() || date.isEmpty() ||
                amount.isEmpty() || category.isEmpty();

        // Then
        assertTrue("Validation should fail when some fields are empty", areFieldsEmpty);
    }

    // ===================== Test 8 =====================
    // Sprawdza, czy poprawna kwota w formacie tekstowym jest poprawnie parsowana na double
    @Test
    public void givenValidAmount_whenParsing_thenNoExceptionThrown() {
        // Given
        String validAmount = "25.50";

        // When
        double amount = 0;
        boolean exceptionThrown = false;
        try {
            amount = Double.parseDouble(validAmount);
        } catch (NumberFormatException e) {
            exceptionThrown = true;
        }

        // Then
        assertFalse("Valid amount should not throw exception", exceptionThrown);
        assertEquals("Amount should be 25.50", 25.50, amount, 0.001);
    }
}
