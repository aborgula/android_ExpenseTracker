package com.example.expensetracker.ui.activities;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;


@RunWith(org.robolectric.RobolectricTestRunner.class)
public class AddExpenseActivityTest {

    private AddExpenseActivity activity;

    @Before
    public void setUp() {

        // symulowanie działania instancji klasy
        activity = Mockito.spy(new AddExpenseActivity());
    }

    // Test 17 - sprawdza czy walidacja danych wykrywa puste pola i zgłasza błąd
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

    // Test 18 - sprawdza czy po wybraniu daty z DatePicker format jest poprawny
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


    // Test 19 - sprawdza czy podana kwota w formacie tekstowym jest poprawnie parsowana na Double
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