package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextView registerTitle;
    private TextInputEditText editEmail;
    private TextInputEditText editPassword;
    private MaterialButton buttonRegister;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Twój XML

        // Znajdź wszystkie widoki po ID
        registerTitle = findViewById(R.id.registerTitle);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        loginLink = findViewById(R.id.loginLink);

        // Kliknięcie Log In przenosi do LoginActivity
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Przyciski i inne funkcje będziemy dodawać później
    }
}
