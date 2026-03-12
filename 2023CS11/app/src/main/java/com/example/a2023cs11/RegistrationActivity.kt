package com.example.a2023cs11

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerEvent: Spinner
    private lateinit var rgGender: RadioGroup
    private lateinit var cbTerms: CheckBox
    private lateinit var btnPickDate: Button
    private lateinit var btnPickImage: Button
    private lateinit var btnSubmit: Button
    private lateinit var imgPreview: ImageView
    private lateinit var tvDate: TextView

    private var selectedDate = ""
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                imgPreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        etName       = findViewById(R.id.etName)
        etPhone      = findViewById(R.id.etPhone)
        etEmail      = findViewById(R.id.etEmail)
        spinnerEvent = findViewById(R.id.spinnerEvent)
        rgGender     = findViewById(R.id.rgGender)
        cbTerms      = findViewById(R.id.cbTerms)
        btnPickDate  = findViewById(R.id.btnPickDate)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnSubmit    = findViewById(R.id.btnSubmit)
        imgPreview   = findViewById(R.id.imgPreview)
        tvDate       = findViewById(R.id.tvDate)

        // Spinner
        ArrayAdapter.createFromResource(
            this, R.array.event_types, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEvent.adapter = adapter
        }

        // Date Picker
        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = "$day/${month + 1}/$year"
                tvDate.text = selectedDate
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Image Picker
        btnPickImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Submit
        btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun validateAndSubmit() {
        val name  = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val event = spinnerEvent.selectedItem.toString()
        val selectedGenderId = rgGender.checkedRadioButtonId

        if (name.isEmpty()) {
            etName.error = "Name is required"; etName.requestFocus(); return
        }
        if (phone.isEmpty() || phone.length < 10) {
            etPhone.error = "Enter valid phone number (min 10 digits)"; etPhone.requestFocus(); return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"; etEmail.requestFocus(); return
        }
        if (spinnerEvent.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select an event type", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select an event date", Toast.LENGTH_SHORT).show(); return
        }
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show(); return
        }
        if (!cbTerms.isChecked) {
            Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show(); return
        }

        val rbGender = findViewById<RadioButton>(selectedGenderId)
        val gender = rbGender.text.toString()

        AlertDialog.Builder(this)
            .setTitle("Confirm Registration")
            .setMessage("Name: $name\nEvent: $event\nDate: $selectedDate\n\nProceed with registration?")
            .setPositiveButton("Confirm") { _, _ ->
                val intent = Intent(this, ConfirmationActivity::class.java).apply {
                    putExtra("name", name)
                    putExtra("phone", phone)
                    putExtra("email", email)
                    putExtra("event", event)
                    putExtra("date", selectedDate)
                    putExtra("gender", gender)
                    selectedImageUri?.let { putExtra("imageUri", it.toString()) }
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}