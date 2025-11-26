package com.example.expensetracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ExpenseService.TimeFilter;
import java.util.List;
import java.util.Map;


public class StatsViewModel extends ViewModel {

    private final ExpenseRepository repository;
    private final ExpenseService expenseService;

    // Aktualny filtr czasowy
    private final MutableLiveData<TimeFilter> currentTimeFilter = new MutableLiveData<>(TimeFilter.TODAY);

    // Wszystkie wydatki z Firebase
    private final LiveData<List<Expense>> allExpenses;

    // Przefiltrowane wydatki (wynik)
    private final MediatorLiveData<List<Expense>> filteredExpenses = new MediatorLiveData<>();

    // Całkowita suma przefiltrowanych wydatków
    private final MediatorLiveData<Float> totalAmount = new MediatorLiveData<>();

    // Wydatki zgrupowane według dni (dla wykresu)
    private final MediatorLiveData<Map<String, Float>> dailyGroupedExpenses = new MediatorLiveData<>();

    // Wydatki zgrupowane według kategorii
    private final MediatorLiveData<Map<String, Float>> categoryGroupedExpenses = new MediatorLiveData<>();

    // Stan ładowania
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);


    /**
     * Konstruktor dla testów z dependency injection
     */
    public StatsViewModel(ExpenseRepository repository, ExpenseService expenseService) {
        this.repository = repository;
        this.expenseService = expenseService;
        this.allExpenses = repository.observeExpenses();

        setupObservers();
    }

    /**
     * Konfiguruje obserwatory - kiedy zmieni się filtr lub dane, przefiltruj
     */
    private void setupObservers() {
        // Obserwuj zmiany danych z Firebase
        filteredExpenses.addSource(allExpenses, expenses -> {
            if (expenses != null && currentTimeFilter.getValue() != null) {
                applyFilters();
            }
        });

        // Obserwuj zmiany filtru
        filteredExpenses.addSource(currentTimeFilter, filter -> {
            if (filter != null && allExpenses.getValue() != null) {
                applyFilters();
            }
        });

        // Obliczaj sumę przy każdej zmianie przefiltrowanych danych
        totalAmount.addSource(filteredExpenses, expenses -> {
            if (expenses != null) {
                float total = expenseService.calculateTotal(expenses);
                totalAmount.setValue(total);
            }
        });

        // Grupuj wydatki według dni przy każdej zmianie
        dailyGroupedExpenses.addSource(filteredExpenses, expenses -> {
            if (expenses != null) {
                Map<String, Float> grouped = expenseService.groupByDay(expenses);
                dailyGroupedExpenses.setValue(grouped);
            }
        });

        // Grupuj wydatki według kategorii przy każdej zmianie
        categoryGroupedExpenses.addSource(filteredExpenses, expenses -> {
            if (expenses != null) {
                Map<String, Float> grouped = expenseService.groupByCategory(expenses);
                categoryGroupedExpenses.setValue(grouped);
            }
        });
    }

    /**
     * Aplikuje wszystkie aktywne filtry
     */
    private void applyFilters() {
        List<Expense> expenses = allExpenses.getValue();
        TimeFilter timeFilter = currentTimeFilter.getValue();

        System.out.println("DEBUG: applyFilters called");
        System.out.println("DEBUG: expenses = " + (expenses != null ? expenses.size() : "null"));
        System.out.println("DEBUG: timeFilter = " + timeFilter);

        if (expenses != null && timeFilter != null) {
            isLoading.setValue(true);
            List<Expense> filtered = expenseService.filterByTime(expenses, timeFilter);
            System.out.println("DEBUG: filtered size = " + filtered.size());
            filteredExpenses.setValue(filtered);
            isLoading.setValue(false);
        }
    }

    // ==================== PUBLIC METHODS ====================

    /**
     * Zmienia typ filtru czasowego
     */
    public void setTimeFilter(TimeFilter timeFilter) {
        currentTimeFilter.setValue(timeFilter);
    }


    // ==================== GETTERS FOR LIVEDATA ====================

    public LiveData<List<Expense>> getFilteredExpenses() {
        return filteredExpenses;
    }

    public LiveData<Float> getTotalAmount() {
        return totalAmount;
    }


    public LiveData<Map<String, Float>> getDailyGroupedExpenses() {
        return dailyGroupedExpenses;
    }



    // ==================== LIFECYCLE ====================

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.removeExpensesListener();
    }
}