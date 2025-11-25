package com.example.expensetracker.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.expensetracker.model.Expense;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseRepository {
    private final DatabaseReference databaseRef;
    private final FirebaseAuth auth;

    // Mapa do przechowywania aktywnych listenerów
    private final Map<String, ValueEventListener> activeListeners = new HashMap<>();

    public ExpenseRepository() {
        this.databaseRef = FirebaseDatabase.getInstance().getReference("expenses");
        this.auth = FirebaseAuth.getInstance();
    }

    // Konstruktor do testów
    public ExpenseRepository(DatabaseReference databaseRef, FirebaseAuth auth) {
        this.databaseRef = databaseRef;
        this.auth = auth;
    }

    /**
     * Istniejąca metoda z callback - zachowana dla kompatybilności wstecznej
     */
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

    /**
     * NOWA METODA: Pobiera wydatki jako LiveData z zarządzaniem listenerem
     * Idealna do użycia z ViewModel
     */
    public LiveData<List<Expense>> observeExpenses() {
        MutableLiveData<List<Expense>> expensesLiveData = new MutableLiveData<>();
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userExpensesRef = databaseRef.child(userId);

        ValueEventListener listener = new ValueEventListener() {
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
                expensesLiveData.setValue(expenses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Możesz zwrócić null lub pustą listę w przypadku błędu
                expensesLiveData.setValue(new ArrayList<>());
            }
        };

        userExpensesRef.addValueEventListener(listener);

        // Zapisz listener, żeby móc go później usunąć
        activeListeners.put(userId, listener);

        return expensesLiveData;
    }

    /**
     * NOWA METODA: Usuwa aktywny listener dla danego użytkownika
     * Wywoływać w onCleared() ViewModel lub onDestroyView() Fragment
     */
    public void removeExpensesListener() {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            ValueEventListener listener = activeListeners.get(userId);

            if (listener != null) {
                databaseRef.child(userId).removeEventListener(listener);
                activeListeners.remove(userId);
            }
        }
    }


    /**
     * Istniejąca metoda usuwania - zachowana
     */
    public void deleteExpense(Expense expense, DeleteCallback callback) {
        databaseRef.child(expense.getUserId())
                .child(expense.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }


    // ==================== CALLBACKS ====================

    public interface ExpenseCallback {
        void onSuccess(List<Expense> expenses);
        void onError(String error);
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}