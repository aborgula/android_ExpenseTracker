package com.example.expensetracker.repository;

import androidx.annotation.NonNull;
import com.example.expensetracker.model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    private final DatabaseReference databaseRef;
    private final FirebaseAuth auth;

    public ExpenseRepository() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference("expenses");
        this.auth = FirebaseAuth.getInstance();
    }

    // Konstruktor do testów
    public ExpenseRepository(DatabaseReference databaseRef, FirebaseAuth auth) {
        this.databaseRef = databaseRef;
        this.auth = auth;
    }

    public void loadExpenses(ExpenseCallback callback) {
        String userId = auth.getCurrentUser().getUid();

        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Expense> expenses = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense e = dataSnapshot.getValue(Expense.class);
                    if (e != null) {
                        e.setId(dataSnapshot.getKey());
                        expenses.add(e);
                    }
                }
                callback.onSuccess(expenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void deleteExpense(Expense expense, DeleteCallback callback) {
        databaseRef.child(expense.getUserId())
                .child(expense.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public interface ExpenseCallback {
        void onSuccess(List<Expense> expenses);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}