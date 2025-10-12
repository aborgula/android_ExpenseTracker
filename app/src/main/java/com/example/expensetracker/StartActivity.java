package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;

import android.view.WindowManager;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class StartActivity extends AppCompatActivity {
    private static final String TAG = "StartActivity";

    private MaterialButton buttonGetStarted;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Jeśli użytkownik jest już zalogowany, od razu przejdź do MainActivity
        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish(); // zamyka StartActivity
            return;
        }

        setContentView(R.layout.activity_start);

        // INICJALIZACJA REALTIME DATABASE

        buttonGetStarted = findViewById(R.id.buttonGetStarted);
        loginLink = findViewById(R.id.loginLink);

        buttonGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}