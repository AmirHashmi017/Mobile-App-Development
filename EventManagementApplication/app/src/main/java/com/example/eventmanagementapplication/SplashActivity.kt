package com.example.eventmanagementapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.smarteventmanager.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    // Splash delay: 2.5 seconds
    private val SPLASH_DELAY = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate logo: scale + fade in
        val scaleAnim = ScaleAnimation(
            0.5f, 1.0f, 0.5f, 1.0f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 800
            fillAfter = true
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 800
            fillAfter = true
        }

        val animSet = AnimationSet(true).apply {
            addAnimation(scaleAnim)
            addAnimation(fadeIn)
        }

        binding.ivAppLogo.startAnimation(animSet)

        // Fade in text
        val textFade = AlphaAnimation(0f, 1f).apply {
            duration = 1000
            startOffset = 400
            fillAfter = true
        }
        binding.tvAppName.startAnimation(textFade)
        binding.tvWelcomeText.startAnimation(textFade)
        binding.divider.startAnimation(textFade)

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, SPLASH_DELAY)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // Smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
