package com.example.signup_login_app

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.net.Uri

class SignupActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private fun isValidName(name: String): Boolean {
        val nameRegex = Regex("^[A-Za-z ]{4,}$")
        return nameRegex.matches(name)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return emailRegex.matches(email)
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^.{8}$")
        return passwordRegex.matches(password)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            findViewById<ImageView>(R.id.ivProfile).setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val radioGender = findViewById<RadioGroup>(R.id.radioGender)
        val cbSports = findViewById<CheckBox>(R.id.cbSports)
        val cbMusic = findViewById<CheckBox>(R.id.cbMusic)
        val cbReading = findViewById<CheckBox>(R.id.cbReading)
        val spCountry = findViewById<Spinner>(R.id.spCountry)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        val countries = arrayOf("Pakistan", "USA", "UK", "Canada")
        spCountry.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            countries
        )

        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        btnSignup.setOnClickListener {

            val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)

            btnGoToLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            val ivProfile = findViewById<ImageView>(R.id.ivProfile)
            val btnPickImage = findViewById<Button>(R.id.btnPickImage)

            btnPickImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 100)
            }

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (!isValidName(name)) {
                etName.error = "Name must be at least 4 letters and contain only alphabets"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                etEmail.error = "Invalid Email Format"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                etPassword.error = "Password must be exactly 8 characters"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            val genderId = radioGender.checkedRadioButtonId
            val gender = if (genderId != -1)
                findViewById<RadioButton>(genderId).text.toString()
            else ""

            val hobbies = ArrayList<String>()
            if (cbSports.isChecked) hobbies.add("Sports")
            if (cbMusic.isChecked) hobbies.add("Music")
            if (cbReading.isChecked) hobbies.add("Reading")

            val country = spCountry.selectedItem.toString()

            val user = User(name, email, password, gender, hobbies, country)
            UserStorage.users.add(user)

            Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("email", email)
            intent.putExtra("gender", gender)
            intent.putExtra("country", country)
            intent.putExtra("hobbies", hobbies.joinToString(", "))
            intent.putExtra("imageUri", imageUri.toString())

            startActivity(intent)
            finish()
        }
    }
}