package com.example.eventmanagementapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import com.example.smarteventmanager.databinding.ActivityConfirmationBinding

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmationBinding

    companion object {
        const val EXTRA_REGISTRATION_DATA = "extra_registration_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve registration data
        val data = intent.getSerializableExtra(EXTRA_REGISTRATION_DATA) as? RegistrationData

        if (data != null) {
            populateConfirmationData(data)
        }

        // Animate success icon
        animateSuccessIcon()

        // Back to home button
        binding.btnBackToHome.setOnClickListener {
            // Navigate back to MainActivity, clearing the back stack
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun populateConfirmationData(data: RegistrationData) {
        binding.tvConfirmName.text      = data.fullName
        binding.tvConfirmPhone.text     = data.phoneNumber
        binding.tvConfirmEmail.text     = data.email
        binding.tvConfirmEventType.text = data.eventType
        binding.tvConfirmDate.text      = data.eventDate
        binding.tvConfirmGender.text    = data.gender

        // Load selected image if available
        if (!data.imageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(data.imageUri)
                binding.ivConfirmImage.setImageURI(uri)
            } catch (e: Exception) {
                binding.ivConfirmImage.setImageResource(R.drawable.ic_person_placeholder)
            }
        } else {
            binding.ivConfirmImage.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    private fun animateSuccessIcon() {
        // Bounce/scale animation for success checkmark
        val scaleUp = ScaleAnimation(
            0f, 1.2f, 0f, 1.2f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply { duration = 400 }

        val scaleDown = ScaleAnimation(
            1.2f, 1f, 1.2f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            startOffset = 400
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 600
            fillAfter = true
        }

        val animSet = AnimationSet(false).apply {
            addAnimation(fadeIn)
            addAnimation(scaleUp)
            addAnimation(scaleDown)
            fillAfter = true
        }

        binding.ivSuccessCheck.startAnimation(animSet)

        // Fade in detail cards slightly delayed
        val cardFade = AlphaAnimation(0f, 1f).apply {
            duration = 600
            startOffset = 500
            fillAfter = true
        }
        binding.tvSuccessMessage.startAnimation(cardFade)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
