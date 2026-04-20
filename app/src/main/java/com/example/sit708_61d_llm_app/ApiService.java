package com.example.sit708_61d_llm_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.google.gson.JsonObject;

public interface ApiService {

    // 1. Gets the initial quiz
    @GET("getQuiz")
    Call<JsonObject> getQuiz(@Query("topic") String topic);

    // 2. Gets the hint for our Task screen
    @GET("getHint")
    Call<JsonObject> getHint(@Query("question") String question);

    // 3. Gets the evaluation for our Results screen
    @GET("getExplanation")
    Call<JsonObject> getExplanation(@Query("question") String question, @Query("answer") String answer);
}