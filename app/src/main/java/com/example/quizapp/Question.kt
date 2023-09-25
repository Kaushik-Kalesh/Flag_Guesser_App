package com.example.quizapp

data class Question(
    val id: Int,
    val countryCode: String,
    val answer: Any,
    val options: List<String>
)
