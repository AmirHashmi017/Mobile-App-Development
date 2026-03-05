package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var tvGreeting: TextView
    private lateinit var rgOptions1: RadioGroup
    private lateinit var rgOptions2: RadioGroup
    private lateinit var rgOptions3: RadioGroup
    private lateinit var rgOptions4: RadioGroup
    private lateinit var rgOptions5: RadioGroup
    private lateinit var btnSubmit: Button

    private val correctAnswers = intArrayOf(2, 1, 1, 2, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Receive player name
        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Player"

        scrollView   = findViewById(R.id.scrollView)
        tvGreeting   = findViewById(R.id.tvGreeting)   // add this TextView to activity_quiz.xml
        rgOptions1   = findViewById(R.id.rgOptions1)
        rgOptions2   = findViewById(R.id.rgOptions2)
        rgOptions3   = findViewById(R.id.rgOptions3)
        rgOptions4   = findViewById(R.id.rgOptions4)
        rgOptions5   = findViewById(R.id.rgOptions5)
        btnSubmit    = findViewById(R.id.btnSubmit)

        // Display personalised greeting at the top of the quiz
        tvGreeting.text = "Good luck, $playerName! 🎯"

        btnSubmit.setOnClickListener { submitQuiz(playerName) }
    }

    private fun submitQuiz(playerName: String) {
        val radioGroups = listOf(rgOptions1, rgOptions2, rgOptions3, rgOptions4, rgOptions5)

        for ((index, rg) in radioGroups.withIndex()) {
            if (rg.checkedRadioButtonId == -1) {
                Toast.makeText(
                    this,
                    "Please answer Question ${index + 1} before submitting.",
                    Toast.LENGTH_SHORT
                ).show()
                rg.requestFocus()
                scrollView.post {
                    val cardY = (rg.parent?.parent as? android.view.View)?.top ?: 0
                    scrollView.smoothScrollTo(0, cardY)
                }
                return
            }
        }

        val userAnswers = radioGroups.map { rg ->
            (0 until rg.childCount).indexOfFirst { i ->
                (rg.getChildAt(i) as? android.widget.RadioButton)?.isChecked == true
            }
        }

        val score = userAnswers.zip(correctAnswers.toList()).count { (user, correct) -> user == correct }

        startActivity(
            Intent(this, ResultActivity::class.java).apply {
                putExtra("SCORE", score)
                putExtra("TOTAL", radioGroups.size)
                putExtra("PLAYER_NAME", playerName)   // forward name to results screen too
            }
        )
        finish()
    }
}