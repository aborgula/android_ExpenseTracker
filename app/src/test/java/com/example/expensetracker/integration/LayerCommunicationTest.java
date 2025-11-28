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


// ==============  Testy komunikacji Fragment -> ViewModel -> Repository/Service   ===============

@RunWith(MockitoJUnitRunner.class)
public class LayerCommunicationTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ExpenseRepository mockRepository;

    @Mock
    private Observer<List<Expense>> expensesObserver;

    @Mock
    private Observer<String> errorObserver;

    private ExpenseService realService;

    private ExpensesViewModel viewModel;

    private List<Expense> testExpenses;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        realService = new ExpenseService();

        // mock repository i prawdziwy serwis
        viewModel = new ExpensesViewModel(mockRepository, realService);
        viewModel.getExpenses().observeForever(expensesObserver);
        viewModel.getError().observeForever(errorObserver);

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


    // Test 5 - sprawdza czy logika sortowania w ExpenseService działa poprawnie gdy jest używana przez ViewModel i jak dane przepływają
    // z Repozytorium przez Serwis do ViewModel
    @Test
    public void service_sortingLogic_appliedByViewModel() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Act
        viewModel.sortExpenses(ExpenseService.SortType.AMOUNT_ASC);

        // Assert
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(captor.capture());

        List<Expense> sortedList = captor.getValue();
        assertEquals(5.0, sortedList.get(0).getAmount(), 0.01);
        assertEquals(150.0, sortedList.get(3).getAmount(), 0.01);
    }

    // Test 6 - Repozytorium dostarcza dane do ViewModel, który używa serwisu do filtrowania danych, następnie
    // sprawdzane jest czy otrzymane wyniki spełniają kryteria
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
        assertEquals(2, filteredList.size());
        assertTrue(filteredList.stream().allMatch(e -> e.getCategory().equals("Food")));
        assertTrue(filteredList.stream().allMatch(e -> e.getAmount() >= 50 && e.getAmount() <= 200));
    }

    // Test 7 - sprawdza interakcję ViewModel z Repozytorium przy usuwaniu wydatku
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



    // Test 8 - sprawdza pełen cykl działania ViewModel w integracji z Repository i Service
    @Test
    public void fullFlow_loadSortFilterDelete_allLayersCommunicate() {
        // Ładowanie danych: Repository -> ViewModel
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();
        verify(expensesObserver, times(1)).onChanged(testExpenses);

        // Sortowanie: ViewModel -> Service -> ViewModel
        viewModel.sortExpenses(ExpenseService.SortType.AMOUNT_DESC);

        ArgumentCaptor<List<Expense>> sortCaptor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(sortCaptor.capture());
        List<Expense> sortedList = sortCaptor.getValue();
        // Sprawdzenie czy najdroższy wydatek jest pierwszy
        assertEquals(150.0, sortedList.get(0).getAmount(), 0.01);

        // Filtrowanie: ViewModel -> Service -> ViewModel
        viewModel.filterExpenses("", "", Collections.singletonList("Food"));

        ArgumentCaptor<List<Expense>> filterCaptor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(3)).onChanged(filterCaptor.capture());
        List<Expense> filteredList = filterCaptor.getValue();
        // Sprawdzenie czy są 2 wydatki z kategorii "Food"
        assertEquals(2, filteredList.size());

        // Usuwanie wydatku: ViewModel -> Repository -> ViewModel
        doAnswer(invocation -> {
            ExpenseRepository.DeleteCallback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(mockRepository).deleteExpense(any(), any());

        viewModel.deleteExpense(filteredList.get(0));
        // Sprawdzenie ile razy zostały wykonane poszczególne metody
        verify(mockRepository, times(1)).deleteExpense(any(), any());
        verify(mockRepository, times(2)).loadExpenses(any()); // Po delete powinno się przeładować
    }

    // Test 9 - sprawdza współpracę ViewModel i Service przy złożonych filtrach
    @Test
    public void complexFiltering_serviceAndViewModelWork_together() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();

        // Act - filtry: kwota 20-100 oraz kategorie Food i Entertainment
        viewModel.filterExpenses("20", "100", Arrays.asList("Food", "Entertainment"));

        // Assert - powinien zwrócić Cinema (30, Entertainment) i Restaurant (80, Food)
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(2)).onChanged(captor.capture());

        List<Expense> result = captor.getValue();
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Cinema")));
        assertTrue(result.stream().anyMatch(e -> e.getName().equals("Restaurant")));
    }

    // Test 10 - sprawdza, czy po usunięciu filtrów lista wydatków wraca do oryginalnej niefiltrowanej wersji
    @Test
    public void resetFilters_restoresOriginalData() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();
        viewModel.filterExpenses("50", "200", Collections.singletonList("Food"));

        // Act
        viewModel.resetFilters();

        // Assert
        ArgumentCaptor<List<Expense>> captor = ArgumentCaptor.forClass(List.class);
        verify(expensesObserver, atLeast(3)).onChanged(captor.capture());

        List<Expense> finalList = captor.getValue();
        assertEquals(4, finalList.size());
    }

    // Test 11 - sprawdza, czy po zastosowaniu filtrów można posortować wyniki i filtr nie znika: testowanie współpracy ViewModel i Service
    @Test
    public void sortAfterFilter_maintainsFilters() {
        // Arrange
        doAnswer(invocation -> {
            ExpenseRepository.ExpenseCallback callback = invocation.getArgument(0);
            callback.onSuccess(testExpenses);
            return null;
        }).when(mockRepository).loadExpenses(any());

        viewModel.loadExpenses();
        viewModel.filterExpenses("", "", Collections.singletonList("Food"));

        // Act
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