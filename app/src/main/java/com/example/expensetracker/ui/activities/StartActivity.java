package com.example.expensetracker.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.example.expensetracker.R;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class StartActivity extends AppCompatActivity {
    private MaterialButton buttonGetStarted;
    private TextView loginLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(StartActivity.this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_start);

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