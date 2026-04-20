package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EditText etNewUsername = findViewById(R.id.etNewUsername);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etStudyLevel = findViewById(R.id.etStudyLevel);
        Button btnNextToInterests = findViewById(R.id.btnNextToInterests);

        btnNextToInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etNewUsername.getText().toString().trim();
                String pass = etNewPassword.getText().toString().trim();
                String name = etFullName.getText().toString().trim();
                String level = etStudyLevel.getText().toString().trim();

                if (user.isEmpty() || pass.isEmpty() || name.isEmpty() || level.isEmpty()) {
                    Toast.makeText(SetupActivity.this, "Please fill out all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save ALL data to the local notepad
                SharedPreferences prefs = getSharedPreferences("HelpHubDatabase", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("SAVED_USERNAME", user);
                editor.putString("SAVED_PASSWORD", pass);
                editor.putString("SAVED_NAME", name);
                editor.putString("SAVED_LEVEL", level);
                editor.apply();

                // Travel to the Interests Screen
                Intent intent = new Intent(SetupActivity.this, InterestsActivity.class);
                startActivity(intent);
                finish(); // Closes setup so they can't go back
            }
        });
    }
}