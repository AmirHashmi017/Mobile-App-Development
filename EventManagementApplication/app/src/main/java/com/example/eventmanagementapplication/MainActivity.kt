package com.example.eventmanagementapplication

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.smarteventmanager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate UI elements on entry
        animateViews()

        // Navigate to Event Registration Screen
        binding.btnRegisterEvent.setOnClickListener {
            val intent = Intent(this, EventRegistrationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun animateViews() {
        // Slide down animation for header
        val slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        slideDown.duration = 500
        binding.headerLayout.startAnimation(slideDown)

        // Fade in for card and button
        val fadeIn = android.view.animation.AlphaAnimation(0f, 1f).apply {
            duration = 700
            startOffset = 300
            fillAfter = true
        }
        binding.cardEventImage.startAnimation(fadeIn)
        binding.cardDescription.startAnimation(fadeIn)
        binding.btnRegisterEvent.startAnimation(fadeIn)
    }

    override fun onResume() {
        super.onResume()
        // Refresh animations when coming back
    }
}
