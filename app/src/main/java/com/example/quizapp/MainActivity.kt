package com.example.quizapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.et_name)
        val btnStart = findViewById<Button>(R.id.btn_start)

        btnStart.setOnClickListener {
            val name = etName.text.trim().toString()

            val quizActivityIntent = Intent(this, QuizActivity::class.java)

            if(name.isEmpty()){
                Toast.makeText(this, "Empty Name!", Toast.LENGTH_SHORT).show()
            }
            else {
                val nameBundle = Bundle().apply {
                    putString("name", name)
                }

                quizActivityIntent.putExtra("nameBundle", nameBundle)
                startActivity(quizActivityIntent)
            }
        }
    }
}