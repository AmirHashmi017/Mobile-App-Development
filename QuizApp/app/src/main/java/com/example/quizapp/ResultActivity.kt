package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val score = intent.getIntExtra("SCORE", 0)
        val total = intent.getIntExtra("TOTAL", 5)

        val tvScore: TextView = findViewById(R.id.tvScore)
        val tvResultMessage: TextView = findViewById(R.id.tvResultMessage)
        val tvScoreDetail: TextView = findViewById(R.id.tvScoreDetail)
        val btnRetry: Button = findViewById(R.id.btnRetry)

        tvScore.text = "$score/$total"
        tvScoreDetail.text = "You answered $score out of $total questions correctly"

        val percentage = (score.toFloat() / total.toFloat()) * 100
        tvResultMessage.text = when {
            percentage == 100f -> "🏆 Perfect Score!"
            percentage >= 80f -> "🌟 Excellent!"
            percentage >= 60f -> "👍 Good Job!"
            percentage >= 40f -> "📚 Keep Practicing!"
            else -> "💪 Don't Give Up!"
        }

        btnRetry.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}