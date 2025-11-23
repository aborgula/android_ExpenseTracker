package com.example.expensetracker.ui.fragments;

import static org.junit.Assert.assertEquals;

import com.example.expensetracker.adapter.ExpenseAdapter;
import com.example.expensetracker.model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

public class ExpensesFragmentIntegrationTest {

    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseUser mockUser;
    @Mock
    private FirebaseDatabase mockDatabase;
    @Mock
    private DatabaseReference mockDatabaseReference;
    @Mock
    private ExpenseAdapter mockAdapter;

    private List<Expense> testExpenses;
    private String userId = "test_user_123";



}
