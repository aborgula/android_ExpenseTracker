package com.example.expensetracker;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> expenses = new ArrayList<>();

    private enum SortType {
        AMOUNT_ASC, AMOUNT_DESC,
        DATE_ASC, DATE_DESC,
        NAME_ASC, NAME_DESC
    }

    private SortType currentSort = SortType.DATE_DESC; // domyślnie od najnowszego


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        recyclerView = view.findViewById(R.id.recycler_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.btn_sort).setOnClickListener(v -> showSortOptions());
        adapter = new ExpenseAdapter(getContext(), expenses);
        recyclerView.setAdapter(adapter);

        loadExpenses();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // nie obsługujemy drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expense = expenses.get(position);

                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Usuń lokalnie
                            expenses.remove(position);
                            adapter.notifyItemRemoved(position);

                            // Usuń z Firebase
                            DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                                    .getReference("expenses")
                                    .child(expense.getUserId())
                                    .child(expense.getId()); // musisz mieć unikalny klucz w Expense

                            databaseRef.removeValue().addOnFailureListener(e -> {
                                // Jeśli nie udało się usunąć z Firebase, przywróć element
                                expenses.add(position, expense);
                                adapter.notifyItemInserted(position);
                                Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // przywróć element
                        })
                        .setCancelable(false)
                        .show();
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }

    private void loadExpenses() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("expenses")
                .child(userId);  // ✅ Dodaj userId jako child!

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenses.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Expense e = dataSnapshot.getValue(Expense.class);
                    if (e != null) {
                        e.setId(dataSnapshot.getKey());
                        expenses.add(e);
                    }
                }
                adapter.notifyDataSetChanged();

                // Debug - sprawdź ile wydatków znaleziono
                Log.d("HomeFragment", "Loaded " + expenses.size() + " expenses");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load expenses: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void sortExpenses(SortType sortType) {
        currentSort = sortType;

        switch (sortType) {
            case AMOUNT_ASC:
                expenses.sort((e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
                break;
            case AMOUNT_DESC:
                expenses.sort((e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
                break;
            case DATE_ASC:
                expenses.sort((e1, e2) -> parseDate(e1.getDate()).compareTo(parseDate(e2.getDate())));
                break;
            case DATE_DESC:
                expenses.sort((e1, e2) -> parseDate(e2.getDate()).compareTo(parseDate(e1.getDate())));
                break;
            case NAME_ASC:
                expenses.sort((e1, e2) -> e1.getName().compareToIgnoreCase(e2.getName()));
                break;
            case NAME_DESC:
                expenses.sort((e1, e2) -> e2.getName().compareToIgnoreCase(e1.getName()));
                break;
        }

        adapter.notifyDataSetChanged();
    }


    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return new Date(0); // jeśli nie uda się sparsować, traktuj jako najstarsza data
        }
    }

    private void showSortOptions() {
        String[] options = {
                "Amount ↑", "Amount ↓",
                "Date ↑", "Date ↓",
                "Name A-Z", "Name Z-A"
        };

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Sort by")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: sortExpenses(SortType.AMOUNT_ASC); break;
                        case 1: sortExpenses(SortType.AMOUNT_DESC); break;
                        case 2: sortExpenses(SortType.DATE_ASC); break;
                        case 3: sortExpenses(SortType.DATE_DESC); break;
                        case 4: sortExpenses(SortType.NAME_ASC); break;
                        case 5: sortExpenses(SortType.NAME_DESC); break;
                    }
                })
                .show();
    }


}
