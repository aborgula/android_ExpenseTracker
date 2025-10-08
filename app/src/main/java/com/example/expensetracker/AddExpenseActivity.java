package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleAddExpense, textNameLabel, textDateLabel, textAmountLabel;
    private TextInputEditText editName, editDate;
    private EditText editAmount;
    private MaterialButton buttonSaveExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        backButton = findViewById(R.id.back_button);
        titleAddExpense = findViewById(R.id.title_add_expense);

        textNameLabel = findViewById(R.id.text_name_label);
        textDateLabel = findViewById(R.id.text_date_label);
        textAmountLabel = findViewById(R.id.text_amount_label);

        editName = findViewById(R.id.edit_name);
        editDate = findViewById(R.id.edit_date);
        editAmount = findViewById(R.id.edit_amount);
        buttonSaveExpense = findViewById(R.id.button_save_expense);

        backButton.setOnClickListener(v -> onBackPressed());

        editDate.setOnClickListener(v -> showDatePickerDialog());

        buttonSaveExpense.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String date = editDate.getText().toString().trim();
            String amount = editAmount.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
                // 🔹 tutaj później dodasz zapis do bazy danych
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
}
