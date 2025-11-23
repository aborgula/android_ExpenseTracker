package com.example.expensetracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ExpenseService.SortType;

import java.util.ArrayList;
import java.util.List;

public class ExpensesViewModel extends ViewModel {
    private final ExpenseRepository repository;
    private final ExpenseService service;

    private final MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> currentFilteredExpenses = new ArrayList<>();

    // Parametry aktualnych filtrów
    private String currentMinAmount = "";
    private String currentMaxAmount = "";
    private List<String> currentCategories = new ArrayList<>();

    public ExpensesViewModel(ExpenseRepository repository, ExpenseService service) {
        this.repository = repository;
        this.service = service;
    }

    public LiveData<List<Expense>> getExpenses() {
        return expensesLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadExpenses() {
        repository.loadExpenses(new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                allExpenses = new ArrayList<>(expenses);
                currentFilteredExpenses = new ArrayList<>(expenses);

                // Jeśli są aktywne filtry, zastosuj je ponownie
                if (!currentMinAmount.isEmpty() || !currentMaxAmount.isEmpty() || !currentCategories.isEmpty()) {
                    applyCurrentFilters();
                } else {
                    expensesLiveData.setValue(expenses);
                }
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue(error);
            }
        });
    }

    public void deleteExpense(Expense expense) {
        repository.deleteExpense(expense, new ExpenseRepository.DeleteCallback() {
            @Override
            public void onSuccess() {
                // Po udanym usunięciu, przeładuj listę
                loadExpenses();
            }

            @Override
            public void onError(String error) {
                errorLiveData.setValue("Failed to delete: " + error);
            }
        });
    }

    public void sortExpenses(SortType sortType) {
        List<Expense> sorted = service.sortExpenses(currentFilteredExpenses, sortType);
        currentFilteredExpenses = sorted;
        expensesLiveData.setValue(sorted);
    }

    public void filterExpenses(String minAmount, String maxAmount, List<String> categories) {
        // Zapisz parametry filtrów
        currentMinAmount = minAmount;
        currentMaxAmount = maxAmount;
        currentCategories = new ArrayList<>(categories);

        applyCurrentFilters();
    }

    public void resetFilters() {
        currentMinAmount = "";
        currentMaxAmount = "";
        currentCategories.clear();
        currentFilteredExpenses = new ArrayList<>(allExpenses);
        expensesLiveData.setValue(allExpenses);
    }

    private void applyCurrentFilters() {
        List<Expense> filtered = service.filterExpenses(
                allExpenses,
                currentMinAmount,
                currentMaxAmount,
                currentCategories
        );
        currentFilteredExpenses = filtered;
        expensesLiveData.setValue(filtered);
    }
}