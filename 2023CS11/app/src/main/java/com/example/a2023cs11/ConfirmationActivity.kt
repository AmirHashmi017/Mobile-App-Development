package com.example.a2023cs11

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        val tvName    = findViewById<TextView>(R.id.tvName)
        val tvPhone   = findViewById<TextView>(R.id.tvPhone)
        val tvEmail   = findViewById<TextView>(R.id.tvEmail)
        val tvEvent   = findViewById<TextView>(R.id.tvEvent)
        val tvDate    = findViewById<TextView>(R.id.tvDate)
        val tvGender  = findViewById<TextView>(R.id.tvGender)
        val imgConfirm = findViewById<ImageView>(R.id.imgConfirm)

        intent.extras?.let { extras ->
            tvName.text   = "Full Name:   ${extras.getString("name")}"
            tvPhone.text  = "Phone:         ${extras.getString("phone")}"
            tvEmail.text  = "Email:           ${extras.getString("email")}"
            tvEvent.text  = "Event Type:  ${extras.getString("event")}"
            tvDate.text   = "Event Date:  ${extras.getString("date")}"
            tvGender.text = "Gender:        ${extras.getString("gender")}"

            val uriStr = extras.getString("imageUri")
            if (!uriStr.isNullOrEmpty()) {
                imgConfirm.setImageURI(Uri.parse(uriStr))
            }
        }
    }
}