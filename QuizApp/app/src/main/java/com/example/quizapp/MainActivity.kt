package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scrollView: ScrollView
    private lateinit var rgOptions1: RadioGroup
    private lateinit var rgOptions2: RadioGroup
    private lateinit var rgOptions3: RadioGroup
    private lateinit var rgOptions4: RadioGroup
    private lateinit var rgOptions5: RadioGroup
    private lateinit var btnSubmit: Button

    // correctAnswerIndex: index of the correct option (0-based)
    private val correctAnswers = intArrayOf(2, 1, 1, 2, 3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scrollView  = findViewById(R.id.scrollView)   // add android:id="@+id/scrollView" to the root ScrollView
        rgOptions1  = findViewById(R.id.rgOptions1)
        rgOptions2  = findViewById(R.id.rgOptions2)
        rgOptions3  = findViewById(R.id.rgOptions3)
        rgOptions4  = findViewById(R.id.rgOptions4)
        rgOptions5  = findViewById(R.id.rgOptions5)
        btnSubmit   = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            submitQuiz()
        }
    }

    private fun submitQuiz() {
        val radioGroups = listOf(rgOptions1, rgOptions2, rgOptions3, rgOptions4, rgOptions5)

        // ── Validation: find the first unanswered question ──
        for ((index, rg) in radioGroups.withIndex()) {
            if (rg.checkedRadioButtonId == -1) {
                val questionNumber = index + 1
                Toast.makeText(
                    this,
                    "Please answer Question $questionNumber before submitting.",
                    Toast.LENGTH_SHORT
                ).show()

                // Scroll to the unanswered card so the user can see it
                rg.requestFocus()
                scrollView.post {
                    val cardY = (rg.parent?.parent as? android.view.View)?.top ?: 0
                    scrollView.smoothScrollTo(0, cardY)
                }
                return
            }
        }

        // ── All answered — calculate score ──
        val userAnswers = radioGroups.map { rg ->
            when (rg.checkedRadioButtonId) {
                // IDs follow the pattern q{N}_opt{M}
                // We derive the 0-based index from which button in the group is checked
                else -> {
                    val checkedIndex = (0 until rg.childCount).indexOfFirst { i ->
                        (rg.getChildAt(i) as? android.widget.RadioButton)?.isChecked == true
                    }
                    checkedIndex
                }
            }
        }

        val score = userAnswers.zip(correctAnswers.toList()).count { (user, correct) -> user == correct }

        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL", radioGroups.size)
        startActivity(intent)
        finish()
    }
}