package com.example.cs160

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnRegister).setOnClickListener { view ->
            // Subtle press animation then navigate
            view.animate()
                .scaleX(0.95f).scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    view.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(100)
                        .withEndAction {
                            startActivity(Intent(this, RegistrationActivity::class.java))
                            overridePendingTransition(
                                android.R.anim.slide_in_left,
                                android.R.anim.slide_out_right
                            )
                        }.start()
                }.start()
        }
    }
}