package com.example.cs160

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION = 2800L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )

        val splashContent = findViewById<LinearLayout>(R.id.splashContent)
        animateSplash(splashContent)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, SPLASH_DURATION)
    }

    private fun animateSplash(view: View) {
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 800 }

        val scaleUp = ScaleAnimation(
            0.85f, 1f, 0.85f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply { duration = 800 }

        AnimationSet(true).apply {
            addAnimation(fadeIn)
            addAnimation(scaleUp)
            fillAfter = true
            view.startAnimation(this)
        }
    }
}