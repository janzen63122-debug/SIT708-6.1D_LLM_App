package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class InterestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);


        CheckBox[] boxes = new CheckBox[]{
                findViewById(R.id.cb1), findViewById(R.id.cb2), findViewById(R.id.cb3),
                findViewById(R.id.cb4), findViewById(R.id.cb5), findViewById(R.id.cb6),
                findViewById(R.id.cb7), findViewById(R.id.cb8), findViewById(R.id.cb9),
                findViewById(R.id.cb10)
        };

        Button btnFinishSetup = findViewById(R.id.btnFinishSetup);

        btnFinishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> selectedInterests = new ArrayList<>();

                for (CheckBox box : boxes) {
                    if (box != null && box.isChecked()) {
                        selectedInterests.add(box.getText().toString());
                    }
                }

                if (selectedInterests.isEmpty()) {
                    Toast.makeText(InterestsActivity.this, "Please select at least one interest!", Toast.LENGTH_SHORT).show();
                    return;
                }


                String interestsString = TextUtils.join(", ", selectedInterests);

                SharedPreferences prefs = getSharedPreferences("HelpHubDatabase", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("SAVED_INTERESTS", interestsString);
                editor.apply();

                Toast.makeText(InterestsActivity.this, "Setup Complete!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(InterestsActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}