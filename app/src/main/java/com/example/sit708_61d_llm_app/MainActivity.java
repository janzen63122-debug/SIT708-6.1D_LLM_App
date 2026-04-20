package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Find the UI Elements
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        SharedPreferences sharedPreferences = getSharedPreferences("HelpHubDatabase", MODE_PRIVATE);

        // --- CREATE ACCOUNT BUTTON LOGIC ---
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just open the door to the Setup screen! No checks needed here.
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
            }
        });

        // --- LOGIN BUTTON LOGIC ---
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String typedUsername = etUsername.getText().toString().trim();
                String typedPassword = etPassword.getText().toString().trim();

                // 1. Check if empty (Added this based on your exact catch!)
                if (typedUsername.isEmpty() || typedPassword.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in both fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Read saved data
                String savedUsername = sharedPreferences.getString("SAVED_USERNAME", null);
                String savedPassword = sharedPreferences.getString("SAVED_PASSWORD", null);

                if (savedUsername == null || savedPassword == null) {
                    Toast.makeText(MainActivity.this, "No account found. Please create one first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Verify credentials
                if (typedUsername.equals(savedUsername) && typedPassword.equals(savedPassword)) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // 4. Move straight to Dashboard (skipping setup)
                    Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Incorrect Username or Password.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}