package com.example.sit708_61d_llm_app;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.google.gson.JsonObject;

public interface ApiService {


    @GET("getQuiz")
    Call<JsonObject> getQuiz(@Query("topic") String topic);


    @GET("getHint")
    Call<JsonObject> getHint(@Query("question") String question);


    @GET("getExplanation")
    Call<JsonObject> getExplanation(@Query("question") String question, @Query("answer") String answer);
}