package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class QuizActivity : AppCompatActivity() {
    private val baseURL = "https://flagcdn.com"
    private val numberOfQuestions = 10
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        getAllCountryCodesJSON(retrofit) { countryCodesJSON ->
            main(countryCodesJSON)
        }
    }

    // API Handler
    private fun getAllCountryCodesJSON(retrofit: Retrofit, callBack: (JSONObject) -> Unit) {
        val flagpediaAPI = retrofit.create(FlagpediaAPI::class.java)

        flagpediaAPI.getCodes().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val countryCodesJSON = JSONObject(response.body()?.string().toString())
                    callBack(countryCodesJSON)
                } else {
                    showConnectionError()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showConnectionError()
            }
        })
    }

    private fun showConnectionError() {
        Toast.makeText(this, "Check your Internet connection!", Toast.LENGTH_LONG).show()
    }

    private fun startResultActivity() {
        val name = intent.getBundleExtra("nameBundle")?.getString("name")

        val resultActivityIntent = Intent(this, ResultActivity::class.java)

        val resultBundle = Bundle().apply {
            putString("name", name)
            putInt("score", score)
        }

        resultActivityIntent.putExtra("resultBundle", resultBundle)
        startActivity(resultActivityIntent)
    }

    // main logic function
    private fun main(countryCodesJSON: JSONObject) {
        val answerCountryCodes = getRandomCountryCodes(countryCodesJSON)
        val answerCountryNames = countryCodesToCountryNames(countryCodesJSON, answerCountryCodes)
        val optionsCountryNames = answerCountryCodes.map { getRandomOptionsCountryNames(countryCodesJSON, it) }

        val questionList = answerCountryCodes.mapIndexed { index, code ->
            Question(index + 1, code, answerCountryNames[index], optionsCountryNames[index])
        }

        displayQuestionCard(0, questionList)
    }

    // helper functions
    private fun countryCodesToCountryNames(countryCodesJSON: JSONObject, countryCodesList: List<String>): List<String> {
        return countryCodesList.map { countryCodesJSON.optString(it) }
    }

    private fun getAllCountryCodesList(countryCodesJSON: JSONObject): List<String> {
        return countryCodesJSON.keys().asSequence()
            .filter { it.length <= 2 }
            .toList()
    }

    private fun getRandomCountryCodes(countryCodesJSON: JSONObject): List<String> {
        val allCountryCodesList = getAllCountryCodesList(countryCodesJSON)

        return allCountryCodesList.shuffled().take(numberOfQuestions)
    }

    private fun getRandomOptionsCountryNames(countryCodesJSON: JSONObject, answerCountryCode: String): List<String> {
        val allCountryCodesList = getAllCountryCodesList(countryCodesJSON)

        val optionsCountryCodesList = allCountryCodesList
            .shuffled()
            .take(4)
            .toMutableList()

        if (!optionsCountryCodesList.contains(answerCountryCode) && optionsCountryCodesList.size == 4) {
            optionsCountryCodesList.removeAt(0)
            optionsCountryCodesList.add(answerCountryCode)
        }

        return countryCodesToCountryNames(countryCodesJSON, optionsCountryCodesList.shuffled())
    }

    // frontend functions
    private fun downloadAndDisplayFlagImage(fileURL: String, imageView: ImageView) {
        Glide.with(this)
            .load(fileURL)
            .into(imageView)
    }

    private fun displayQuestionCard(questionIndex: Int, questionList: List<Question>) {
        val question = questionList[questionIndex]

        val tvQuestionNo = findViewById<TextView>(R.id.tv_question_no)
        val ivFlagImage = findViewById<ImageView>(R.id.iv_flag_img)
        val btnOptionsList = listOf<Button>(
            findViewById(R.id.btn_option1),
            findViewById(R.id.btn_option2),
            findViewById(R.id.btn_option3),
            findViewById(R.id.btn_option4)
        )
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        tvQuestionNo.text = getString(R.string.tv_number_text_default, question.id)
        downloadAndDisplayFlagImage("${baseURL}/w640/${question.countryCode}.png", ivFlagImage)

        var chosenOption = ""
        btnOptionsList.forEachIndexed { index, btn ->
            btn.text = question.options[index]
            btn.setOnClickListener {
                chosenOption = btn.text.toString()
            }
        }

        btnSubmit.setOnClickListener {
            if (chosenOption == question.answer) {
                validateChosenOption(chosenOption, question.answer, btnOptionsList, btnSubmit, questionIndex, questionList)
            } else if (chosenOption.isNotEmpty()) {
                validateChosenOption(chosenOption, question.answer, btnOptionsList, btnSubmit, questionIndex, questionList)
            }
        }
    }

    private fun validateChosenOption(chosenOption: String, answer: Any, btnOptionsList: List<Button>, btnSubmit: Button, questionIndex: Int, questionList: List<Question>) {
        btnOptionsList.forEach { btn ->
            if(btn.text == answer) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            }
            if(chosenOption != answer && btn.text == chosenOption) {
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        btnSubmit.text = getString(R.string.btn_submit_text_next)

        btnSubmit.setOnClickListener {
            btnOptionsList.forEach { btn ->
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_500))
            }
            btnSubmit.text = getString(R.string.btn_submit_text)

            if(chosenOption == answer){
                score += 1
            }

            if(questionIndex < numberOfQuestions - 1){
                displayQuestionCard(questionIndex + 1, questionList)
            }
            else {
                startResultActivity()
            }
        }
    }
}
