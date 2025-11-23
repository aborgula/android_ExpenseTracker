package com.example.expensetracker.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;

public class ExpensesViewModelFactory implements ViewModelProvider.Factory {
    private final ExpenseRepository repository;
    private final ExpenseService service;

    public ExpensesViewModelFactory(ExpenseRepository repository, ExpenseService service) {
        this.repository = repository;
        this.service = service;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ExpensesViewModel.class)) {
            return (T) new ExpensesViewModel(repository, service);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}