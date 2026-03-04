package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvProgressCount: TextView
    private lateinit var rgOptions: RadioGroup
    private lateinit var rbOption1: RadioButton
    private lateinit var rbOption2: RadioButton
    private lateinit var rbOption3: RadioButton
    private lateinit var rbOption4: RadioButton
    private lateinit var btnSubmit: Button

    private var currentQuestionIndex = 0
    private var score = 0
    private val userAnswers = IntArray(5) { -1 }

    private val questions = listOf(
        Question(
            "What is the capital of France?",
            listOf("London", "Berlin", "Paris", "Madrid"),
            2
        ),
        Question(
            "Which planet is known as the Red Planet?",
            listOf("Venus", "Mars", "Jupiter", "Saturn"),
            1
        ),
        Question(
            "What is 2 + 2 × 2?",
            listOf("8", "6", "4", "10"),
            1
        ),
        Question(
            "Who wrote the play 'Romeo and Juliet'?",
            listOf("Charles Dickens", "Mark Twain", "William Shakespeare", "Jane Austen"),
            2
        ),
        Question(
            "What is the largest ocean on Earth?",
            listOf("Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"),
            3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvQuestion = findViewById(R.id.tvQuestion)
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber)
        tvProgressCount = findViewById(R.id.tvProgressCount)
        rgOptions = findViewById(R.id.rgOptions)
        rbOption1 = findViewById(R.id.rbOption1)
        rbOption2 = findViewById(R.id.rbOption2)
        rbOption3 = findViewById(R.id.rbOption3)
        rbOption4 = findViewById(R.id.rbOption4)
        btnSubmit = findViewById(R.id.btnSubmit)

        loadQuestion()

        btnSubmit.setOnClickListener {
            val selectedId = rgOptions.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedIndex = when (selectedId) {
                R.id.rbOption1 -> 0
                R.id.rbOption2 -> 1
                R.id.rbOption3 -> 2
                R.id.rbOption4 -> 3
                else -> -1
            }

            userAnswers[currentQuestionIndex] = selectedIndex

            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion()
            } else {
                calculateScore()
                navigateToResult()
            }
        }
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        tvQuestion.text = question.questionText
        tvQuestionNumber.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        tvProgressCount.text = "${currentQuestionIndex + 1}/${questions.size}"

        rbOption1.text = question.options[0]
        rbOption2.text = question.options[1]
        rbOption3.text = question.options[2]
        rbOption4.text = question.options[3]

        rgOptions.clearCheck()

        // Restore previous answer if navigated back (optional)
        if (userAnswers[currentQuestionIndex] != -1) {
            when (userAnswers[currentQuestionIndex]) {
                0 -> rbOption1.isChecked = true
                1 -> rbOption2.isChecked = true
                2 -> rbOption3.isChecked = true
                3 -> rbOption4.isChecked = true
            }
        }

        // Update button text on last question
        btnSubmit.text = if (currentQuestionIndex == questions.size - 1) "Submit" else "Next"
    }

    private fun calculateScore() {
        score = 0
        for (i in questions.indices) {
            if (userAnswers[i] == questions[i].correctAnswerIndex) {
                score++
            }
        }
    }

    private fun navigateToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", questions.size)
        startActivity(intent)
        finish()
    }
}