package com.example.signup_login_app

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvDetails = findViewById<TextView>(R.id.tvDetails)
        val ivProfile = findViewById<ImageView>(R.id.ivWelcomeProfile)

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val gender = intent.getStringExtra("gender")
        val country = intent.getStringExtra("country")
        val hobbies = intent.getStringExtra("hobbies")
        val imageUri = intent.getStringExtra("imageUri")

        tvWelcome.text = "Welcome, $name!"

        tvDetails.text = """
            Email: $email
            Gender: $gender
            Country: $country
            Hobbies: $hobbies
        """.trimIndent()

        if (imageUri != null && imageUri != "null") {
            ivProfile.setImageURI(Uri.parse(imageUri))
        }
    }
}