package com.example.expensetracker.integration;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ExpenseService.TimeFilter;
import com.example.expensetracker.viewmodel.StatsViewModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static org.mockito.Mockito.*;

/**
 * Testy integracyjne dla StatsViewModel
 * Sprawdzają komunikację: Repository -> ViewModel -> Service -> ViewModel
 */
@RunWith(MockitoJUnitRunner.class)
public class StatsViewModelIntegrationTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    // Mockowane zależności
    @Mock
    private ExpenseRepository mockRepository;

    @Mock
    private Observer<List<Expense>> filteredExpensesObserver;

    @Mock
    private Observer<Float> totalAmountObserver;

    @Mock
    private Observer<Map<String, Float>> dailyGroupedObserver;


    // Prawdziwy serwis
    private ExpenseService realService;

    // ViewModel łączy wszystkie warstwy
    private StatsViewModel viewModel;

    // LiveData symulujące Firebase
    private MutableLiveData<List<Expense>> firebaseExpenses;

    private List<Expense> testExpenses;
    private SimpleDateFormat dateFormat;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Format daty używany w aplikacji
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Prawdziwy serwis
        realService = new ExpenseService();

        // Symulacja LiveData z Firebase
        firebaseExpenses = new MutableLiveData<>();
        when(mockRepository.observeExpenses()).thenReturn(firebaseExpenses);

        // Utwórz ViewModel
        viewModel = new StatsViewModel(mockRepository, realService);

        // Obserwuj wszystkie LiveData
        viewModel.getFilteredExpenses().observeForever(filteredExpensesObserver);
        viewModel.getTotalAmount().observeForever(totalAmountObserver);
        viewModel.getDailyGroupedExpenses().observeForever(dailyGroupedObserver);

        // Przygotuj testowe dane
        testExpenses = createTestExpenses();
    }

    /**
     * Tworzy wydatki z różnych przedziałów czasowych
     */
    private List<Expense> createTestExpenses() {
        Calendar cal = Calendar.getInstance();

        // Dzisiaj
        Expense today1 = createExpense("1", "Today Groceries", 50.0,
                "Food", formatDate(cal.getTime()));
        Expense today2 = createExpense("2", "Today Transport", 10.0,
                "Transport", formatDate(cal.getTime()));

        // Wczoraj
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Expense yesterday1 = createExpense("3", "Yesterday Cinema", 30.0,
                "Entertainment", formatDate(cal.getTime()));
        Expense yesterday2 = createExpense("4", "Yesterday Food", 40.0,
                "Food", formatDate(cal.getTime()));

        // 3 dni temu (w tym tygodniu)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -3);
        Expense thisWeek = createExpense("5", "Week Restaurant", 80.0,
                "Food", formatDate(cal.getTime()));

        // 10 dni temu (w tym miesiącu)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Expense thisMonth = createExpense("6", "Month Shopping", 150.0,
                "Shopping", formatDate(cal.getTime()));

        // 2 miesiące temu (w tym roku)
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -2);
        Expense thisYear = createExpense("7", "Year Electronics", 500.0,
                "Electronics", formatDate(cal.getTime()));

        return Arrays.asList(today1, today2, yesterday1, yesterday2,
                thisWeek, thisMonth, thisYear);
    }

    private String formatDate(Date date) {
        return dateFormat.format(date);
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

    // ========== TESTY TIME FILTER ==========

    @Test
    public void timeFilter_TODAY_filtersCorrectly() {
        // Arrange - symuluj dane z Firebase
        firebaseExpenses.setValue(testExpenses);

        // Act - ustaw filtr TODAY (domyślny)
        viewModel.setTimeFilter(TimeFilter.TODAY);

        // Assert - powinny być tylko 2 wydatki z dzisiaj
        verify(filteredExpensesObserver, atLeastOnce()).onChanged(argThat(expenses -> {
            if (expenses == null) return false;
            return expenses.size() == 2
                    && expenses.stream().allMatch(e ->
                    e.getName().startsWith("Today"));
        }));

        // Assert - suma powinna być 60.0 (50 + 10)
        verify(totalAmountObserver, atLeastOnce()).onChanged(60.0f);
    }


    @Test
    public void timeFilter_MONTH_filtersCorrectly() {
        // Arrange
        firebaseExpenses.setValue(testExpenses);

        // Act
        viewModel.setTimeFilter(TimeFilter.MONTH);

        // Assert - wydatki z ostatnich 30 dni
        // Today (2) + Yesterday (2) + ThisWeek (1) + ThisMonth (1) = 6
        verify(filteredExpensesObserver, atLeastOnce()).onChanged(argThat(expenses -> {
            if (expenses == null) return false;
            return expenses.size() == 6;
        }));

        // Assert - suma: 50+10+30+40+80+150 = 360.0
        verify(totalAmountObserver, atLeastOnce()).onChanged(360.0f);
    }


    // ========== TESTY TOTAL AMOUNT ==========

    @Test
    public void totalAmount_calculatesCorrectly_afterFilterChange() {
        // Arrange
        firebaseExpenses.setValue(testExpenses);
        viewModel.setTimeFilter(TimeFilter.TODAY);

        // Assert - początkowa suma dla TODAY
        verify(totalAmountObserver, atLeastOnce()).onChanged(60.0f);

        // Act - zmień na WEEK
        viewModel.setTimeFilter(TimeFilter.WEEK);

        // Assert - nowa suma dla WEEK
        verify(totalAmountObserver, atLeastOnce()).onChanged(210.0f);
    }

    @Test
    public void totalAmount_updatesWhen_newDataArrives() {
        // Arrange - początkowe dane
        firebaseExpenses.setValue(testExpenses.subList(0, 2)); // Tylko 2 z dzisiaj
        viewModel.setTimeFilter(TimeFilter.TODAY);

        // Assert - początkowa suma
        verify(totalAmountObserver, atLeastOnce()).onChanged(60.0f);

        // Act - nowe dane z Firebase (dodaj trzeci wydatek)
        Expense newToday = createExpense("8", "Today Extra", 25.0,
                "Food", formatDate(new Date()));
        List<Expense> updatedExpenses = Arrays.asList(
                testExpenses.get(0), testExpenses.get(1), newToday
        );
        firebaseExpenses.setValue(updatedExpenses);

        // Assert - zaktualizowana suma: 60 + 25 = 85
        verify(totalAmountObserver, atLeastOnce()).onChanged(85.0f);
    }

}