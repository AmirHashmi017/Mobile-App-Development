package com.example.signuploginapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                val ivProfile = findViewById<ImageView>(R.id.ivProfileImage)
                ivProfile.setImageURI(selectedImageUri)
                ivProfile.setPadding(0, 0, 0, 0)
                ivProfile.colorFilter = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ivProfile    = findViewById<ImageView>(R.id.ivProfileImage)
        val etName       = findViewById<EditText>(R.id.etName)
        val etEmail      = findViewById<EditText>(R.id.etEmail)
        val etPassword   = findViewById<EditText>(R.id.etPassword)
        val etPhone      = findViewById<EditText>(R.id.etPhone)
        val spinner      = findViewById<Spinner>(R.id.spinnerCountry)
        val rgGender     = findViewById<RadioGroup>(R.id.rgGender)
        val cbTech       = findViewById<CheckBox>(R.id.cbTech)
        val cbMusic      = findViewById<CheckBox>(R.id.cbMusic)
        val cbSports     = findViewById<CheckBox>(R.id.cbSports)
        val cbArt        = findViewById<CheckBox>(R.id.cbArt)
        val cbTerms      = findViewById<CheckBox>(R.id.cbTerms)
        val btnSignUp    = findViewById<Button>(R.id.btnSignUp)

        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.countries)
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(android.graphics.Color.WHITE)
                tv.textSize = 15f
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(android.graphics.Color.WHITE)
                tv.setBackgroundColor(android.graphics.Color.parseColor("#1A1A28"))
                tv.setPadding(32, 24, 32, 24)
                tv.textSize = 15f
                return view
            }
        }.also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spinner.adapter = adapter


        ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        btnSignUp.setOnClickListener {
            val name     = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val phone    = etPhone.text.toString().trim()
            val country  = spinner.selectedItem.toString()


            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (country == "Select Country") {
                Toast.makeText(this, "Please select your country", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (rgGender.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!cbTerms.isChecked) {
                Toast.makeText(this, "Please accept the Terms & Privacy Policy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbMale   -> "Male"
                R.id.rbFemale -> "Female"
                else          -> "Other"
            }


            val interests = mutableListOf<String>()
            if (cbTech.isChecked)   interests.add("Technology")
            if (cbMusic.isChecked)  interests.add("Music")
            if (cbSports.isChecked) interests.add("Sports")
            if (cbArt.isChecked)    interests.add("Art & Design")
            val interestsStr = if (interests.isEmpty()) "None" else interests.joinToString(", ")

            val intent = Intent(this, SuccessActivity::class.java).apply {
                putExtra("NAME",      name)
                putExtra("EMAIL",     email)
                putExtra("PHONE",     phone)
                putExtra("COUNTRY",   country)
                putExtra("GENDER",    gender)
                putExtra("INTERESTS", interestsStr)
                putExtra("IMAGE_URI", selectedImageUri?.toString())
            }
            startActivity(intent)
        }
    }
}