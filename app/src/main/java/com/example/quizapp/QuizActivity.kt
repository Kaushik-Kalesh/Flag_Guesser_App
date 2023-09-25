package com.example.quizapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        val retrofit = Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        getAllCountryCodesJS0N(retrofit, ::main)
    }

    // API Handler
    private fun getAllCountryCodesJS0N(retrofit: Retrofit, callBack: (JSONObject) -> Unit) {
        val flagpediaAPI = retrofit.create(FlagpediaAPI::class.java)

        flagpediaAPI.getCodes().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val countryCodesJSON = JSONObject(response.body()?.string().toString())
                    callBack(countryCodesJSON)
                } else {
                    Toast.makeText(applicationContext, "Check your Internet connection!", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(applicationContext, "Check your Internet connection!", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun startResultActivity(score: Int) {
        val nameBundle = intent.getBundleExtra("nameBundle")
        val name = nameBundle?.getString("name")

        val resultActivityIntent = Intent(this, ResultActivity::class.java)

        val resultBundle = Bundle()
        resultBundle.putString("name", name)
        resultBundle.putInt("score", score)

        resultActivityIntent.putExtra("resultBundle", resultBundle)
        startActivity(resultActivityIntent)
    }

    // main logic function
    private fun main(countryCodesJSON: JSONObject){
        val answerCountryCodes: List<String> = getRandomCountryCodes(countryCodesJSON)
        val answerCountryNames: List<String> = countryCodesToCountryNames(countryCodesJSON, answerCountryCodes)
        val optionsCountryNames: MutableList<List<String>> = mutableListOf()
        repeat(answerCountryCodes.size) {
            optionsCountryNames.add(getRandomOptionsCountryNames(countryCodesJSON, answerCountryCodes[it]))
        }

        val questionList = mutableListOf<Question>()
        repeat(answerCountryCodes.size) {
            questionList.add(Question(it + 1, answerCountryCodes[it], answerCountryNames[it], optionsCountryNames[it]))
        }

        displayQuestionCards(questionList, 0, 0)
    }

    // main helper functionS
    private fun countryCodesToCountryNames(countryCodesJSON: JSONObject, countryCodesList: List<String>): List<String> {
        val countryNamesList = mutableListOf<String>()
        repeat(countryCodesList.size) {
            countryNamesList.add(countryCodesJSON.get(countryCodesList[it]).toString())
        }

        return countryNamesList
    }

    private fun getAllCountryCodesList(countryCodesJSON: JSONObject): List<String> {
        val allCountryCodesList = mutableListOf<String>()

        val countryCodesJSONIterator = countryCodesJSON.keys()
        while(countryCodesJSONIterator.hasNext()){
            val code: String = countryCodesJSONIterator.next()
            if(code.length <= 2){
                allCountryCodesList.add(code)
            }
        }

        return allCountryCodesList
    }

    private fun getRandomCountryCodes(countryCodesJSON: JSONObject): List<String> {
        val allCountryCodesList: List<String> = getAllCountryCodesList(countryCodesJSON)

        val min = 0
        val max: Int = allCountryCodesList.size - 1
        val count = numberOfQuestions

        val countryCodesList = mutableListOf<String>()

        val randomList: List<Int> = (min..max).shuffled().take(count)
        repeat(randomList.take(count).size) {
            countryCodesList.add(allCountryCodesList[randomList[it]])
        }

        return countryCodesList
    }

    private fun getRandomOptionsCountryNames(countryCodesJSON: JSONObject, answerCountryCode: String): List<String> {
        val allCountryCodesList: List<String> = getAllCountryCodesList(countryCodesJSON)

        val min = 0
        val max: Int = allCountryCodesList.size
        val count = 4

        val optionsCountryCodesList = mutableListOf<String>()

        val randomList = (min..max).shuffled().take(count)
        repeat(randomList.take(count).size) {
            optionsCountryCodesList.add(allCountryCodesList[randomList[it]])
        }

        if(!optionsCountryCodesList.contains(answerCountryCode) && optionsCountryCodesList.size == 4){
            optionsCountryCodesList.removeAt(0)
            optionsCountryCodesList.add(answerCountryCode)
        }

        return countryCodesToCountryNames(countryCodesJSON, optionsCountryCodesList.shuffled())
    }


    // frontend functions
    private fun displayQuestionCards(questionList: List<Question>, it: Int, score: Int) {
        displayQuestionCard(questionList[it]) { result ->
            if(it < numberOfQuestions - 1){
                displayQuestionCards(questionList, it + 1, if(result) score + 1 else score)
            }
            else{
                startResultActivity(score)
            }
        }
    }

    private fun displayQuestionCard(question: Question, callBack: (Boolean) -> Unit) {
        val tvQuestionNo = findViewById<TextView>(R.id.tv_question_no)
        val ivFlagImage = findViewById<ImageView>(R.id.iv_flag_img)
        val btnOptionsList = listOf<Button>(
            findViewById(R.id.btn_option1),
            findViewById(R.id.btn_option2),
            findViewById(R.id.btn_option3),
            findViewById(R.id.btn_option4)
        )
        val btnNext = findViewById<Button>(R.id.btn_next)

        tvQuestionNo.text = getString(R.string.tv_number_text_default, question.id)
        downloadAndDisplayFlagImage(this, "${baseURL}/w640/${question.countryCode}.png", ivFlagImage)
        repeat(btnOptionsList.size) {
            btnOptionsList[it].text = question.options[it]
        }

        var chosenOption = ""

        btnOptionsList[0].setOnClickListener {
            chosenOption = btnOptionsList[0].text.toString()
        }
        btnOptionsList[1].setOnClickListener {
            chosenOption = btnOptionsList[1].text.toString()
        }
        btnOptionsList[2].setOnClickListener {
            chosenOption = btnOptionsList[2].text.toString()
        }
        btnOptionsList[3].setOnClickListener {
            chosenOption = btnOptionsList[3].text.toString()
        }

        btnNext.setOnClickListener {
            if(chosenOption == question.answer){
                callBack(true)
            }
            else if(chosenOption.isNotEmpty()){
                callBack(false)
            }
        }
    }

    private fun downloadAndDisplayFlagImage(context: Context, fileURL: String, imageView: ImageView) {
        val glide = Glide.with(context)

        val requestBuilder = glide.load(fileURL)

        requestBuilder.into(imageView)
    }
}