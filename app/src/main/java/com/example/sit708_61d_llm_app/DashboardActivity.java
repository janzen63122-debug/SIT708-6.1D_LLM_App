package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Make sure your XML matches this name!

        // Find the text view (you might need to add a TextView with id tvWelcome to your activity_dashboard.xml)
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        Button btnStartQuiz = findViewById(R.id.btnStartQuiz); // Your button to go to TaskActivity

        // Open the local notepad to get the user's saved name
        SharedPreferences prefs = getSharedPreferences("HelpHubDatabase", MODE_PRIVATE);
        String savedName = prefs.getString("SAVED_NAME", "Student");

        // Display Hello [Name]
        if (tvWelcome != null) {
            tvWelcome.setText("Hello " + savedName + "!");
        }

        if (btnStartQuiz != null) {
            btnStartQuiz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, TaskActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}