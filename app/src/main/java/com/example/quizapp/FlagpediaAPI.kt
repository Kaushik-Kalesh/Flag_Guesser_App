package com.example.quizapp

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface FlagpediaAPI {
    @GET("/en/codes.json")
    fun getCodes(): Call<ResponseBody>
}