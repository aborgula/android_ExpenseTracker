package com.example.expensetracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private String filterMinAmount = "";
    private String filterMaxAmount = "";

    // Kategorie
    private List<String> selectedCategories = new ArrayList<>();

    private enum SortType {
        AMOUNT_ASC, AMOUNT_DESC,
        DATE_ASC, DATE_DESC,
        NAME_ASC, NAME_DESC
    }

    private SortType currentSort = SortType.DATE_DESC;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);
        recyclerView = view.findViewById(R.id.recycler_expenses);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.btn_sort).setOnClickListener(v -> showSortOptions());
        view.findViewById(R.id.btn_filter).setOnClickListener(v -> showFilterDialog());
        adapter = new ExpenseAdapter(getContext(), expenses);
        recyclerView.setAdapter(adapter);

        loadExpenses();

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expense = expenses.get(position);

                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            expenses.remove(position);
                            adapter.notifyItemRemoved(position);

                            DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                                    .getReference("expenses")
                                    .child(expense.getUserId())
                                    .child(expense.getId());

                            databaseRef.removeValue().addOnFailureListener(e -> {
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
                .child(userId);

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

    private void showFilterDialog() {
        // Inflate custom layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter, null);

        EditText minAmount = dialogView.findViewById(R.id.minAmount);
        EditText maxAmount = dialogView.findViewById(R.id.maxAmount);
        Button btnResetAmount = dialogView.findViewById(R.id.btnResetAmount);
        LinearLayout categoriesContainer = dialogView.findViewById(R.id.categoriesContainer);
        Button btnSaveFilters = dialogView.findViewById(R.id.btnSaveFilters);
        Button btnResetFilters = dialogView.findViewById(R.id.btnResetFilters);

        // Lista kategorii
        String[] categories = {"Food", "Transport", "Shopping", "Entertainment", "Health", "Bills", "Other"};
        List<CheckBox> categoryCheckBoxes = new ArrayList<>();

        // Dodaj checkboxy dynamicznie
        for (String category : categories) {
            CheckBox cb = new CheckBox(getContext());
            cb.setText(category);
            cb.setTextColor(Color.parseColor("#333333"));
            // Przywróć zaznaczenie jeśli wcześniej wybrano
            if (selectedCategories.contains(category)) cb.setChecked(true);
            categoriesContainer.addView(cb);
            categoryCheckBoxes.add(cb);
        }

        // Przywróć wartości kwot
        minAmount.setText(filterMinAmount);
        maxAmount.setText(filterMaxAmount);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        dialog.show();

        // Metoda pomocnicza do stosowania wszystkich filtrów
        Runnable applyFilters = () -> {
            String minStr = minAmount.getText().toString().trim();
            String maxStr = maxAmount.getText().toString().trim();
            double min = minStr.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minStr);
            double max = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);

            List<String> currentSelectedCategories = new ArrayList<>();
            for (CheckBox cb : categoryCheckBoxes) {
                if (cb.isChecked()) currentSelectedCategories.add(cb.getText().toString());
            }

            List<Expense> filtered = new ArrayList<>();
            for (Expense e : expenses) {
                boolean inCategory = currentSelectedCategories.isEmpty() || currentSelectedCategories.contains(e.getCategory());
                boolean inAmount = e.getAmount() >= min && e.getAmount() <= max;
                if (inCategory && inAmount) {
                    filtered.add(e);
                }
            }
            adapter.updateList(filtered);
        };

        btnResetAmount.setOnClickListener(v -> {
            minAmount.setText("");
            maxAmount.setText("");
        });


        // Obsługa Save (zachowuje wszystkie filtry)
        btnSaveFilters.setOnClickListener(v -> {
            // Zapisz aktualne wartości filtrów do zmiennych fragmentu
            filterMinAmount = minAmount.getText().toString().trim();
            filterMaxAmount = maxAmount.getText().toString().trim();

            selectedCategories.clear();
            for (CheckBox cb : categoryCheckBoxes) {
                if (cb.isChecked()) selectedCategories.add(cb.getText().toString());
            }

            applyFilters.run();
            Toast.makeText(getContext(), "Filters saved", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Obsługa Reset
        btnResetFilters.setOnClickListener(v -> {
            minAmount.setText("");
            maxAmount.setText("");
            for (CheckBox cb : categoryCheckBoxes) cb.setChecked(false);

            // Wyczyść zapisane filtry
            filterMinAmount = "";
            filterMaxAmount = "";
            selectedCategories.clear();

            adapter.updateList(expenses); // przywraca pełną listę
            Toast.makeText(getContext(), "Filters reset", Toast.LENGTH_SHORT).show();
        });
    }






    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return new Date(0);
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

    private void applyFilters(EditText minAmount, EditText maxAmount, List<CheckBox> categoryCheckBoxes) {
        String minStr = minAmount.getText().toString().trim();
        String maxStr = maxAmount.getText().toString().trim();
        double min = minStr.isEmpty() ? Double.MIN_VALUE : Double.parseDouble(minStr);
        double max = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);

        List<String> selectedCategories = new ArrayList<>();
        for (CheckBox cb : categoryCheckBoxes) {
            if (cb.isChecked()) selectedCategories.add(cb.getText().toString());
        }

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : expenses) {
            boolean inCategory = selectedCategories.isEmpty() || selectedCategories.contains(e.getCategory());
            boolean inAmount = e.getAmount() >= min && e.getAmount() <= max;
            if (inCategory && inAmount) {
                filtered.add(e);
            }
        }
        adapter.updateList(filtered);
    }

}
