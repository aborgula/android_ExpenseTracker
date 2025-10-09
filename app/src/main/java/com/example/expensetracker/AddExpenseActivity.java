package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private ImageView backButton;
    private TextView titleAddExpense, textNameLabel, textDateLabel, textAmountLabel, textCategoryLabel;
    private TextInputEditText editName, editDate, editCategory;
    private EditText editAmount;
    private MaterialButton buttonSaveExpense;

    private String selectedCategory = "";
    private String selectedCategoryIconName = "";
    private int selectedCategoryIcon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        backButton = findViewById(R.id.back_button);
        titleAddExpense = findViewById(R.id.title_add_expense);

        textNameLabel = findViewById(R.id.text_name_label);
        textDateLabel = findViewById(R.id.text_date_label);
        textAmountLabel = findViewById(R.id.text_amount_label);
        textCategoryLabel = findViewById(R.id.text_category_label);


        editName = findViewById(R.id.edit_name);
        editDate = findViewById(R.id.edit_date);
        editAmount = findViewById(R.id.edit_amount);
        editCategory = findViewById(R.id.edit_category);
        buttonSaveExpense = findViewById(R.id.button_save_expense);

        backButton.setOnClickListener(v -> onBackPressed());

        editDate.setOnClickListener(v -> showDatePickerDialog());
        editCategory.setOnClickListener(v -> showCategoryBottomSheet());


        buttonSaveExpense.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String amountText = editAmount.getText().toString().trim();
            String category = editCategory.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || amountText.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                double amount = Double.parseDouble(amountText);
                String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

                // referencja do bazy
                com.google.firebase.database.DatabaseReference dbRef =
                        com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("expenses")
                                .child(userId);

                // wygenerowanie unikalnego klucza
                String expenseId = dbRef.push().getKey();

                if (expenseId != null) {
                    // dodajemy ID do obiektu Expense
                    Expense expense = new Expense(expenseId, name, date, amount, category, selectedCategoryIcon, userId);

                    dbRef.child(expenseId).setValue(expense)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Expense saved to Firebase!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving expense: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                }
            }
        });


    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
    }


    // ✅ FUNKCJA KTÓRA OTWIERA BOTTOM SHEET Z LISTĄ KATEGORII
    private void showCategoryBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_category, null);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_categories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lista kategorii z ikonkami
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Food", R.drawable.ic_food));
        categories.add(new Category("Transport", R.drawable.ic_transport));
        categories.add(new Category("Shopping", R.drawable.ic_shopping));
        categories.add(new Category("Entertainment", R.drawable.ic_entertainment));
        categories.add(new Category("Health", R.drawable.ic_health));
        categories.add(new Category("Bills", R.drawable.ic_bills));
        categories.add(new Category("Other", R.drawable.ic_other));


        // Adapter z listenerem - po kliknięciu kategoria się wybiera
        CategoryAdapter adapter = new CategoryAdapter(categories, category -> {
            selectedCategory = category.getName();
            selectedCategoryIcon = category.getIconResId();
            editCategory.setText(selectedCategory);
            bottomSheetDialog.dismiss();
        });


        recyclerView.setAdapter(adapter);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }
}
