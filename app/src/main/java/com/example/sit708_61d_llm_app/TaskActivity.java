package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskActivity extends AppCompatActivity {

    private ApiService apiService;
    private ProgressBar loadingSpinner;

    // UI Elements
    private TextView tvProgress, tvQuestion, tvHintText;
    private RadioGroup rgAnswers;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private Button btnNext, btnSubmitTask, btnGetHint;

    // Data Storage
    private JsonArray allQuestions; // Holds the AI data
    private int currentQuestionIndex = 0; // Tracks which question we are on (0, 1, or 2)

    // Arrays to save data to pass to Results
    private String[] userAnswers = new String[3];
    private String[] questions = new String[3];
    private String[] correctAnswers = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // 1. Find UI Elements
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvHintText = findViewById(R.id.tvHintText);
        rgAnswers = findViewById(R.id.rgAnswers);
        rbOptionA = findViewById(R.id.rbOptionA); rbOptionB = findViewById(R.id.rbOptionB); rbOptionC = findViewById(R.id.rbOptionC); rbOptionD = findViewById(R.id.rbOptionD);
        btnNext = findViewById(R.id.btnNext);
        btnSubmitTask = findViewById(R.id.btnSubmitTask);
        btnGetHint = findViewById(R.id.btnGetHint);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        // 2. Set up Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().readTimeout(10, java.util.concurrent.TimeUnit.MINUTES).build())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 3. Load the Quiz
        loadQuizFromAI("Android Development");

        // 4. Handle Hint Button
        btnGetHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questions[currentQuestionIndex] != null) {
                    getHintFromAI(questions[currentQuestionIndex], tvHintText);
                }
            }
        });

        // 5. Handle Next Button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveCurrentAnswer()) {
                    currentQuestionIndex++;
                    displayCurrentQuestion();
                }
            }
        });

        // 6. Handle Submit Button
        btnSubmitTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveCurrentAnswer()) {
                    // Send all saved data to Results screen
                    Intent intent = new Intent(TaskActivity.this, ResultsActivity.class);
                    intent.putExtra("Q1", questions[0]); intent.putExtra("A1", userAnswers[0]); intent.putExtra("C1", correctAnswers[0]);
                    intent.putExtra("Q2", questions[1]); intent.putExtra("A2", userAnswers[1]); intent.putExtra("C2", correctAnswers[1]);
                    intent.putExtra("Q3", questions[2]); intent.putExtra("A3", userAnswers[2]); intent.putExtra("C3", correctAnswers[2]);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadQuizFromAI(String topic) {
        loadingSpinner.setVisibility(View.VISIBLE);
        tvProgress.setText("Downloading Quiz...");

        apiService.getQuiz(topic).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        allQuestions = response.body().getAsJsonArray("quiz");

                        // Extract and save the raw question and correct answer texts into our arrays
                        for (int i = 0; i < 3; i++) {
                            JsonObject qObj = allQuestions.get(i).getAsJsonObject();
                            questions[i] = qObj.get("question").getAsString();
                            correctAnswers[i] = qObj.get("correct_answer").getAsString();
                        }

                        // Show the very first question!
                        currentQuestionIndex = 0;
                        displayCurrentQuestion();

                    } catch (Exception e) {
                        tvProgress.setText("Error parsing quiz.");
                    }
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                tvProgress.setText("Failed to connect to AI server.");
            }
        });
    }

    private void displayCurrentQuestion() {
        // Update the Progress text
        tvProgress.setText("Question " + (currentQuestionIndex + 1) + " of 3");

        // Clear previous selections and hint
        rgAnswers.clearCheck();
        tvHintText.setText("");

        // Extract the current question object
        JsonObject currentObj = allQuestions.get(currentQuestionIndex).getAsJsonObject();

        // Set the Question Title
        tvQuestion.setText(currentObj.get("question").getAsString());

        // Set the 4 Radio Button Options
        JsonArray options = currentObj.getAsJsonArray("options");
        rbOptionA.setText(options.get(0).getAsString());
        rbOptionB.setText(options.get(1).getAsString());
        rbOptionC.setText(options.get(2).getAsString());
        rbOptionD.setText(options.get(3).getAsString());

        // Show/Hide Next and Submit buttons based on which question we are on
        if (currentQuestionIndex == 2) {
            btnNext.setVisibility(View.GONE); // Hide Next
            btnSubmitTask.setVisibility(View.VISIBLE); // Show Submit
        } else {
            btnNext.setVisibility(View.VISIBLE); // Show Next
            btnSubmitTask.setVisibility(View.GONE); // Hide Submit
        }
    }

    // Helper method to ensure they picked an answer before moving on
    private boolean saveCurrentAnswer() {
        int selectedId = rgAnswers.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
            return false;
        }

        RadioButton selectedBtn = findViewById(selectedId);
        userAnswers[currentQuestionIndex] = selectedBtn.getText().toString();
        return true;
    }

    private void getHintFromAI(String question, TextView hintView) {
        loadingSpinner.setVisibility(View.VISIBLE);
        hintView.setText("AI is generating a hint...");
        apiService.getHint(question).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    hintView.setText("Hint: " + response.body().get("hint").getAsString());
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                hintView.setText("Failed to get hint.");
            }
        });
    }
}