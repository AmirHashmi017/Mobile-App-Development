package com.example.signuploginapp

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val name      = intent.getStringExtra("NAME")      ?: "—"
        val email     = intent.getStringExtra("EMAIL")     ?: "—"
        val phone     = intent.getStringExtra("PHONE")     ?: "—"
        val country   = intent.getStringExtra("COUNTRY")   ?: "—"
        val gender    = intent.getStringExtra("GENDER")    ?: "—"
        val interests = intent.getStringExtra("INTERESTS") ?: "None"
        val imageUriStr = intent.getStringExtra("IMAGE_URI")

        findViewById<TextView>(R.id.tvResultName).text      = name
        findViewById<TextView>(R.id.tvResultEmail).text     = email
        findViewById<TextView>(R.id.tvResultPhone).text     = phone
        findViewById<TextView>(R.id.tvResultCountry).text   = country
        findViewById<TextView>(R.id.tvResultGender).text    = gender
        findViewById<TextView>(R.id.tvResultInterests).text = interests


        if (!imageUriStr.isNullOrEmpty()) {
            val ivResult = findViewById<ImageView>(R.id.ivResultImage)
            ivResult.setImageURI(Uri.parse(imageUriStr))
            ivResult.setPadding(0, 0, 0, 0)
            ivResult.colorFilter = null
        }

        findViewById<Button>(R.id.btnGoBack).setOnClickListener {
            finish()
        }
    }
}