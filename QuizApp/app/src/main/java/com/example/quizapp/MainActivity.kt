package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            showQuizPromptDialog()
        }, 800)
    }

    private fun showQuizPromptDialog() {
        // Inflate custom dialog view containing the name EditText
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quiz_start, null)
        val etName = dialogView.findViewById<EditText>(R.id.etPlayerName)

        // Limit name to 30 characters
        etName.filters = arrayOf(InputFilter.LengthFilter(30))

        val dialog = AlertDialog.Builder(this, R.style.QuizDialogTheme)
            .setTitle("Ready to Test Yourself?")
            .setMessage("This quiz contains 5 questions.\nEnter your name to begin.")
            .setView(dialogView)
            .setPositiveButton("Let's Go! 🚀", null) // null — we override below to control dismiss
            .setNegativeButton("Not Yet") { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create()

        dialog.show()

        // Override positive button to validate before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Please enter your name"
                etName.requestFocus()
                return@setOnClickListener
            }
            dialog.dismiss()
            startActivity(
                Intent(this, QuizActivity::class.java).apply {
                    putExtra("PLAYER_NAME", name)
                }
            )
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}