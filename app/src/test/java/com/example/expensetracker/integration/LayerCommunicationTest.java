package com.example.expensetracker.integration;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.viewmodel.ExpensesViewModel;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test komunikacji między warstwami:
 * Fragment -> ViewModel -> Repository
 * Fragment -> ViewModel -> Service
 */
@RunWith(MockitoJUnitRunner.class)
public class LayerCommunicationTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    // Mockowane zależności
    @Mock
    private ExpenseRepository mockRepository;

    @Mock
    private Observer<List<Expense>> expensesObserver;

    @Mock
    private Observer<String> errorObserver;

    // Prawdziwy serwis
    private ExpenseService realService;

    // ViewModel łączy wszystkie warstwy
    private ExpensesViewModel viewModel;

    private List<Expense> testExpenses;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prawdziwy serwis
        realService = new ExpenseService();

        // Utwórz ViewModel z mockiem repository i prawdziwym serwisem
        viewModel = new ExpensesViewModel(mockRepository, realService);

        // Obserwuj LiveData
        viewModel.getExpenses().observeForever(expensesObserver);
        viewModel.getError().observeForever(errorObserver);

        // Testowe dane
        testExpenses = Arrays.asList(
                createExpense("1", "Groceries", 150.0, "Food", "20/11/2024"),
                createExpense("2", "Bus", 5.0, "Transport", "21/11/2024"),
                createExpense("3", "Cinema", 30.0, "Entertainment", "22/11/2024"),
                createExpense("4", "Restaurant", 80.0, "Food", "23/11/2024")
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
        expense.setUserId("testUser");
        return expense;
    }

    //  ---- TESTY KOMUNIKACJI: REPOSITORY -> VIEWMODEL  ----

    @Test
    public void repository_errorLoad_propagatesToViewModel() {
        // Arrange
        String errorMessage = "Database connection failed";
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onError(errorMessage);
            return null;
        }).when(mockRepository).loadExpenses(any(ExpenseRepository.ExpenseCallback.class));

        // Act
        viewModel.loadExpenses();

        // Assert - błąd powinien trafić do ViewModel
        verify(mockRepository, times(1)).loadExpenses(any());
        verify(errorObserver, times(1)).onChanged(errorMessage);
        verify(expensesObserver, never()).onChanged(any());
    }

    // ========== TESTY KOMUNIKACJI: SERVICE -> VIEWMODEL ==========

    @Test
    public void service_sortingLogic_appliedByViewModel() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Act - sortowanie przez vieModel z prawdziwym service
        viewModel.sortExpenses(ExpenseService.SortType.AMOUNT_ASC);

        // Assert
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(captor.capture());

        List<Expense> sortedList = captor.getValue();
        assertEquals(5.0, sortedList.get(0).getAmount(), 0.01);  // Bus - najtańszy
        assertEquals(150.0, sortedList.get(3).getAmount(), 0.01); // Groceries - najdroższy
    }

    @Test
    public void service_filteringLogic_appliedByViewModel() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Act
        viewModel.filterExpenses("50", "200", Collections.singletonList("Food"));

        // Assert
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(captor.capture());

        List<Expense> filteredList = captor.getValue();
        assertEquals(2, filteredList.size()); // Groceries (150) i Restaurant (80)
        assertTrue(filteredList.stream().allMatch(e -> e.getCategory().equals("Food")));
        assertTrue(filteredList.stream().allMatch(e -> e.getAmount() >= 50 && e.getAmount() <= 200));
    }

    // --- TESTY KOMUNIKACJI: VIEWMODEL -> REPOSITORY (DELETE) ---

    @Test
    public void viewModel_deleteExpense_callsRepository() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        doAnswer(invocation -> {
            ExpenseRepository.DeleteCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteExpense(any(), any());

        // Act
        Expense toDelete = testExpenses.get(0);
        viewModel.deleteExpense(toDelete);

        // Assert
        verify(mockRepository, times(1)).deleteExpense(eq(toDelete), any());
        verify(mockRepository, times(2)).loadExpenses(any());
    }



    // ========== TESTY PEŁNEGO FLOW ==========

    @Test
    public void fullFlow_loadSortFilterDelete_allLayersCommunicate() {
        // KROK 1: Załaduj dane (Repository -> ViewModel)
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();
        verify(expensesObserver, times(1)).onChanged(testExpenses);

        // KROK 2: Sortuj (ViewModel -> Service -> ViewModel)
        viewModel.sortExpenses(ExpenseService.SortType.AMOUNT_DESC);

        ArgumentCaptor<List<Expense>> sortCaptor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(sortCaptor.capture());
        List<Expense> sortedList = sortCaptor.getValue();
        assertEquals(150.0, sortedList.get(0).getAmount(), 0.01); // Najdroższy pierwszy

        // KROK 3: Filtruj (ViewModel -> Service -> ViewModel)
        viewModel.filterExpenses("", "", Collections.singletonList("Food"));

        ArgumentCaptor<List<Expense>> filterCaptor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(3)).onChanged(filterCaptor.capture());
        List<Expense> filteredList = filterCaptor.getValue();
        assertEquals(2, filteredList.size());

        // KROK 4: Usuń (ViewModel -> Repository -> ViewModel)
        doAnswer(invocation -> {
            ExpenseRepository.DeleteCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteExpense(any(), any());

        viewModel.deleteExpense(filteredList.get(0));

        verify(mockRepository, times(1)).deleteExpense(any(), any());
        verify(mockRepository, times(2)).loadExpenses(any()); // Po delete powinno się przeładować
    }

    @Test
    public void complexFiltering_serviceAndViewModelWork_together() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Act - zastosuj złożone filtry: kwota 20-100 + kategorie Food i Entertainment
        viewModel.filterExpenses("20", "100", Arrays.asList("Food", "Entertainment"));

        // Assert - powinien zwrócić Cinema (30, Entertainment) i Restaurant (80, Food)
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(captor.capture());

        List<Expense> result = captor.getValue();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Cinema")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Restaurant")));
    }

    @Test
    public void resetFilters_restoresOriginalData() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Zastosuj filtry
        viewModel.filterExpenses("50", "200", Collections.singletonList("Food"));

        // Act - resetuj
        viewModel.resetFilters();

        // Assert - powinna wrócić pełna lista
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(3)).onChanged(captor.capture());

        List<Expense> finalList = captor.getValue();
        assertEquals(4, finalList.size()); // Wszystkie 4 wydatki
    }

    @Test
    public void sortAfterFilter_maintainsFilters() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Filtruj tylko Food
        viewModel.filterExpenses("", "", Collections.singletonList("Food"));

        // Act - sortuj po filtrowaniu
        viewModel.sortExpenses(ExpenseService.SortType.AMOUNT_ASC);

        // Assert - powinno być nadal tylko 2 Food items, ale posortowane
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(3)).onChanged(captor.capture());

        List<Expense> result = captor.getValue();
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(e -> e.getCategory().equals("Food")));
        assertEquals(80.0, result.get(0).getAmount(), 0.01);  // Restaurant
        assertEquals(150.0, result.get(1).getAmount(), 0.01); // Groceries
    }
}