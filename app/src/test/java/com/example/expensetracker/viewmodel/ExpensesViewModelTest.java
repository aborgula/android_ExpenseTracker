package com.example.expensetracker.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExpensesViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ExpenseRepository mockRepository;

    @Mock
    private ExpenseService mockService;

    @Mock
    private Observer<List<Expense>> expensesObserver;

    @Mock
    private Observer<String> errorObserver;

    private ExpensesViewModel viewModel;
    private List<Expense> testExpenses;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new ExpensesViewModel(mockRepository, mockService);

        // Obserwuj LiveData
        viewModel.getExpenses().observeForever(expensesObserver);
        viewModel.getError().observeForever(errorObserver);

        // Przygotuj testowe dane
        testExpenses = Arrays.asList(
                createExpense("1", "Groceries", 150.0, "Food"),
                createExpense("2", "Bus ticket", 5.0, "Transport"),
                createExpense("3", "Cinema", 30.0, "Entertainment")
        );
    }

    private Expense createExpense(String id, String name, double amount, String category) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setName(name);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setUserId("testUser123");
        return expense;
    }

    // ========== TESTY ŁADOWANIA DANYCH ==========
    @Test
    public void loadExpenses_onSuccess_updatesLiveData() {
        // Arrange - przygotuj mock repository
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any(ExpenseRepository.ExpenseCallback.class));

        // Act - załaduj dane
        viewModel.loadExpenses();

        // Assert - sprawdź czy LiveData zostało zaktualizowane
        verify(expensesObserver).onChanged(testExpenses);
        verify(mockRepository, times(1)).loadExpenses(any());
    }



    // ========== TESTY USUWANIA ==========
    @Test
    public void deleteExpense_onSuccess_reloadsExpenses() {
        // Arrange - najpierw załaduj dane
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any(ExpenseRepository.ExpenseCallback.class));

        viewModel.loadExpenses();

        // Przygotuj mock dla usuwania
        doAnswer(invocation -> {
            ExpenseRepository.DeleteCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteExpense(any(Expense.class),
                any(ExpenseRepository.DeleteCallback.class));

        // Act - usuń wydatek
        Expense expenseToDelete = testExpenses.get(0);
        viewModel.deleteExpense(expenseToDelete);

        // Assert - sprawdź czy loadExpenses zostało wywołane ponownie
        verify(mockRepository, times(2)).loadExpenses(any());
    }


    // ========== TESTY FILTROWANIA ==========
    @Test
    public void filterExpenses_callsServiceAndUpdatesLiveData() {
        // Arrange - załaduj dane
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any(ExpenseRepository.ExpenseCallback.class));

        viewModel.loadExpenses();

        // Przygotuj mockowany serwis - zwróć tylko jeden element
        List<Expense> filteredExpenses = Collections.singletonList(testExpenses.get(0));
        when(mockService.filterExpenses(any(), any(), any(), any())).thenReturn(filteredExpenses);

        // Act
        viewModel.filterExpenses("50", "200", Collections.singletonList("Food"));

        // Assert
        verify(mockService).filterExpenses(any(), eq("50"), eq("200"),
                eq(Collections.singletonList("Food")));
        verify(expensesObserver, atLeastOnce()).onChanged(filteredExpenses);
    }

    @Test
    public void resetFilters_restoresOriginalList() {
        // Arrange - załaduj dane
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any(ExpenseRepository.ExpenseCallback.class));

        viewModel.loadExpenses();

        // Zastosuj filtry
        List<Expense> filteredExpenses = Collections.singletonList(testExpenses.get(0));
        when(mockService.filterExpenses(any(), any(), any(), any())).thenReturn(filteredExpenses);
        viewModel.filterExpenses("50", "200", Collections.singletonList("Food"));

        // Act - zresetuj filtry
        viewModel.resetFilters();

        // Assert - sprawdź czy przywrócono pełną listę
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeastOnce()).onChanged(captor.capture());

        List<Expense> lastValue = captor.getValue();
        assertEquals(testExpenses.size(), lastValue.size());
    }
}