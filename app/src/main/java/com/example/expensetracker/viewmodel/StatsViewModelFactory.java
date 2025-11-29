package com.example.expensetracker.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.service.ExpenseService;

public class StatsViewModelFactory implements ViewModelProvider.Factory {

    private final ExpenseRepository repository;
    private final ExpenseService expenseService;

    public StatsViewModelFactory(ExpenseRepository repository, ExpenseService expenseService) {
        this.repository = repository;
        this.expenseService = expenseService;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(StatsViewModel.class)) {
            return (T) new StatsViewModel(repository, expenseService);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}