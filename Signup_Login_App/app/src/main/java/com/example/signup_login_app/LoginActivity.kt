package com.example.signup_login_app


import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        val etEmail = findViewById<EditText>(R.id.etLoginEmail)
        val etPassword = findViewById<EditText>(R.id.etLoginPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSuccess = findViewById<TextView>(R.id.tvSuccess)

        btnLogin.setOnClickListener {
            val btnGoToSignup = findViewById<Button>(R.id.btnGoToSignup)

            btnGoToSignup.setOnClickListener {
                startActivity(Intent(this, SignupActivity::class.java))
                finish()
            }
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            val user = UserStorage.users.find {
                it.email == email && it.password == password
            }

            if (user != null) {
                tvSuccess.text = "Successfully Logged In!"
                tvSuccess.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                tvSuccess.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}