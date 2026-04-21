package com.example.sit708_61d_llm_app;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private JsonArray allQuestions;
    private int currentQuestionIndex = 0;


    private String[] userAnswers    = new String[3];
    private String[] questions      = new String[3];
    private String[] correctAnswers = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);


        tvProgress      = findViewById(R.id.tvProgress);
        tvQuestion      = findViewById(R.id.tvQuestion);
        tvHintText      = findViewById(R.id.tvHintText);
        rgAnswers       = findViewById(R.id.rgAnswers);
        rbOptionA       = findViewById(R.id.rbOptionA);
        rbOptionB       = findViewById(R.id.rbOptionB);
        rbOptionC       = findViewById(R.id.rbOptionC);
        rbOptionD       = findViewById(R.id.rbOptionD);
        btnNext         = findViewById(R.id.btnNext);
        btnSubmitTask   = findViewById(R.id.btnSubmitTask);
        btnGetHint      = findViewById(R.id.btnGetHint);
        loadingSpinner  = findViewById(R.id.loadingSpinner);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())

                .client(new OkHttpClient.Builder()
                        .readTimeout(10, java.util.concurrent.TimeUnit.MINUTES)
                        .build())
                .build();
        apiService = retrofit.create(ApiService.class);


        SharedPreferences prefs = getSharedPreferences("HelpHubDatabase", MODE_PRIVATE);
        String topic = prefs.getString("SAVED_INTERESTS", "General Knowledge");
        loadQuizFromAI(topic);


        btnGetHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questions[currentQuestionIndex] != null) {
                    getHintFromAI(questions[currentQuestionIndex], tvHintText);
                }
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveCurrentAnswer()) {
                    currentQuestionIndex++;
                    displayCurrentQuestion();
                }
            }
        });


        btnSubmitTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveCurrentAnswer()) {

                    Intent intent = new Intent(TaskActivity.this, ResultsActivity.class);
                    intent.putExtra("Q1", questions[0]);
                    intent.putExtra("A1", userAnswers[0]);
                    intent.putExtra("C1", correctAnswers[0]);
                    intent.putExtra("Q2", questions[1]);
                    intent.putExtra("A2", userAnswers[1]);
                    intent.putExtra("C2", correctAnswers[1]);
                    intent.putExtra("Q3", questions[2]);
                    intent.putExtra("A3", userAnswers[2]);
                    intent.putExtra("C3", correctAnswers[2]);
                    startActivity(intent);
                }
            }
        });
    }


    private void loadQuizFromAI(String topic) {
        loadingSpinner.setVisibility(View.VISIBLE);
        tvProgress.setText("Generating quiz on: " + topic + " …");

        apiService.getQuiz(topic).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        allQuestions = response.body().getAsJsonArray("quiz");

                        if (allQuestions == null || allQuestions.size() == 0) {
                            tvProgress.setText("AI returned an empty quiz. Please try again.");
                            return;
                        }


                        int count = Math.min(allQuestions.size(), 3);
                        for (int i = 0; i < count; i++) {
                            JsonObject qObj = allQuestions.get(i).getAsJsonObject();
                            questions[i]      = qObj.get("question").getAsString();
                            correctAnswers[i] = qObj.get("correct_answer").getAsString();
                        }


                        currentQuestionIndex = 0;
                        displayCurrentQuestion();

                    } catch (Exception e) {
                        tvProgress.setText("Error parsing quiz: " + e.getMessage());
                    }
                } else {
                    tvProgress.setText("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                tvProgress.setText("Failed to connect to AI server.\n" +
                        "Make sure the Flask server is running on your computer.");
            }
        });
    }


    private void displayCurrentQuestion() {

        if (allQuestions == null || currentQuestionIndex >= allQuestions.size()) {
            tvProgress.setText("No more questions available.");
            return;
        }


        tvProgress.setText("Question " + (currentQuestionIndex + 1) + " of 3");


        rgAnswers.clearCheck();
        tvHintText.setText("");


        JsonObject currentObj = allQuestions.get(currentQuestionIndex).getAsJsonObject();


        tvQuestion.setText(currentObj.get("question").getAsString());


        JsonArray options = currentObj.getAsJsonArray("options");
        rbOptionA.setText(options.get(0).getAsString());
        rbOptionB.setText(options.get(1).getAsString());
        rbOptionC.setText(options.get(2).getAsString());
        rbOptionD.setText(options.get(3).getAsString());


        if (currentQuestionIndex == 2) {
            btnNext.setVisibility(View.GONE);
            btnSubmitTask.setVisibility(View.VISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
            btnSubmitTask.setVisibility(View.GONE);
        }
    }


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
        hintView.setText("AI is generating a hint…");

        apiService.getHint(question).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    hintView.setText("Hint: " + response.body().get("hint").getAsString());
                } else {
                    hintView.setText("Could not load hint (server error " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                hintView.setText("Failed to get hint. Check server connection.");
            }
        });
    }
}
