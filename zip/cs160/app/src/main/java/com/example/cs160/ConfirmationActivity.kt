package com.example.cs160

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import kotlin.random.Random

class ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        bindData()
        setupListeners()
    }

    private fun bindData() {
        val fullName  = intent.getStringExtra("fullName")  ?: ""
        val phone     = intent.getStringExtra("phone")     ?: ""
        val email     = intent.getStringExtra("email")     ?: ""
        val eventType = intent.getStringExtra("eventType") ?: ""
        val eventDate = intent.getStringExtra("eventDate") ?: ""
        val gender    = intent.getStringExtra("gender")    ?: ""
        val imageUri  = intent.getStringExtra("imageUri")

        // Header
        findViewById<TextView>(R.id.tvConfirmName).text   = fullName
        findViewById<TextView>(R.id.tvConfirmGender).text = gender
        findViewById<TextView>(R.id.tvConfirmationId).text =
            "#SEM-2024-${String.format("%04d", Random.nextInt(1, 9999))}"

        // Participant details card
        findViewById<TextView>(R.id.tvDetailPhone).text     = phone
        findViewById<TextView>(R.id.tvDetailEmail).text     = email
        findViewById<TextView>(R.id.tvDetailEventType).text = eventType
        findViewById<TextView>(R.id.tvDetailDate).text      = eventDate

        // Summary card
        findViewById<TextView>(R.id.tvSummaryName).text      = fullName
        findViewById<TextView>(R.id.tvSummaryPhone).text     = phone
        findViewById<TextView>(R.id.tvSummaryEmail).text     = email
        findViewById<TextView>(R.id.tvSummaryEventType).text = eventType
        findViewById<TextView>(R.id.tvSummaryDate).text      = eventDate
        findViewById<TextView>(R.id.tvSummaryGender).text    = gender

        // Profile image
        imageUri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(it))
                findViewById<ImageView>(R.id.ivConfirmImage).apply {
                    setImageBitmap(bitmap)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setPadding(0, 0, 0, 0)
                }
            } catch (e: IOException) { /* keep default icon */ }
        }
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }.also { startActivity(it) }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}