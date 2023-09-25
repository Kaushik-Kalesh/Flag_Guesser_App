package com.example.quizapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvCongratsName = findViewById<TextView>(R.id.tv_congrats_name)
        val tvScore = findViewById<TextView>(R.id.tv_score)

        val resultBundle = intent.getBundleExtra("resultBundle")
        val name = resultBundle?.getString("name")
        val score = resultBundle?.getInt("score")

        tvCongratsName.text = getString(R.string.tv_congrats_text_default, if(score!! <= 3) "Try Again" else "Congrats", name)
        tvScore.text = getString(R.string.tv_result_text_default, score)
    }
}