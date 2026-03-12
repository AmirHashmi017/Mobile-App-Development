package com.example.cs160

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.Calendar

class RegistrationActivity : AppCompatActivity() {

    // UI components
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerEventType: Spinner
    private lateinit var btnSelectDate: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnUploadImage: Button
    private lateinit var btnBack: ImageButton
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var radioOther: RadioButton
    private lateinit var ivProfileImage: ImageView
    private lateinit var checkboxTerms: CheckBox

    // State
    private var selectedDate = ""
    private var selectedImageUri: Uri? = null

    // Image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    ivProfileImage.apply {
                        setImageBitmap(bitmap)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        setPadding(0, 0, 0, 0)
                    }
                    showToast("✓ Photo selected successfully!")
                } catch (e: IOException) {
                    showToast("Failed to load image")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initViews()
        setupSpinner()
        setupDatePicker()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        spinnerEventType = findViewById(R.id.spinnerEventType)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnBack = findViewById(R.id.btnBack)
        radioGroupGender = findViewById(R.id.radioGroupGender)
        radioMale = findViewById(R.id.radioMale)
        radioFemale = findViewById(R.id.radioFemale)
        radioOther = findViewById(R.id.radioOther)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        checkboxTerms = findViewById(R.id.checkboxTerms)
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.event_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEventType.adapter = adapter
        }

        spinnerEventType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                (view as? TextView)?.apply {
                    setTextColor(if (pos == 0) 0xFFB0BEC5.toInt() else 0xFFFFFFFF.toInt())
                    textSize = 14f
                    setPadding(16, 0, 16, 0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDatePicker() {
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                R.style.DatePickerTheme,
                { _, year, month, day ->
                    selectedDate = "$day/${month + 1}/$year"
                    btnSelectDate.text = "📅  $selectedDate"
                    btnSelectDate.setTextColor(0xFFFFFFFF.toInt())
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = calendar.timeInMillis
                show()
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        btnUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                showConfirmationDialog()
            }
        }
    }

    private fun validateForm(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        // Full Name
        when {
            fullName.isEmpty() -> {
                etFullName.error = "Full name is required"
                etFullName.requestFocus()
                showToast("❌ Please enter your full name")
                return false
            }
            fullName.length < 3 -> {
                etFullName.error = "Name must be at least 3 characters"
                etFullName.requestFocus()
                showToast("❌ Name must be at least 3 characters")
                return false
            }
            !fullName.matches(Regex("[a-zA-Z ]+")) -> {
                etFullName.error = "Name should contain only letters"
                etFullName.requestFocus()
                showToast("❌ Name should contain only letters and spaces")
                return false
            }
        }

        // Phone
        when {
            phone.isEmpty() -> {
                etPhone.error = "Phone number is required"
                etPhone.requestFocus()
                showToast("❌ Please enter your phone number")
                return false
            }
            !phone.matches(Regex("[0-9]{10,13}")) -> {
                etPhone.error = "Enter a valid phone number (10-13 digits)"
                etPhone.requestFocus()
                showToast("❌ Enter a valid phone number (10-13 digits)")
                return false
            }
        }

        // Email
        when {
            email.isEmpty() -> {
                etEmail.error = "Email address is required"
                etEmail.requestFocus()
                showToast("❌ Please enter your email address")
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Enter a valid email address"
                etEmail.requestFocus()
                showToast("❌ Please enter a valid email address")
                return false
            }
        }

        // Event Type
        if (spinnerEventType.selectedItemPosition == 0) {
            showToast("❌ Please select an event type")
            return false
        }

        // Date
        if (selectedDate.isEmpty()) {
            showToast("❌ Please select an event date")
            return false
        }

        // Gender
        if (radioGroupGender.checkedRadioButtonId == -1) {
            showToast("❌ Please select your gender")
            return false
        }

        // Terms
        if (!checkboxTerms.isChecked) {
            showToast("❌ Please accept the Terms and Conditions")
            return false
        }

        return true
    }

    private fun showConfirmationDialog() {
        val fullName = etFullName.text.toString().trim()
        val eventType = spinnerEventType.selectedItem.toString()

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Confirm Registration")
            .setMessage("You are about to register $fullName for $eventType.\n\nAre you sure you want to submit?")
            .setPositiveButton("Yes, Submit") { _, _ -> navigateToConfirmation() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun navigateToConfirmation() {
        val gender = when (radioGroupGender.checkedRadioButtonId) {
            R.id.radioMale   -> "Male"
            R.id.radioFemale -> "Female"
            R.id.radioOther  -> "Other"
            else             -> ""
        }

        Intent(this, ConfirmationActivity::class.java).apply {
            putExtra("fullName",   etFullName.text.toString().trim())
            putExtra("phone",      etPhone.text.toString().trim())
            putExtra("email",      etEmail.text.toString().trim())
            putExtra("eventType",  spinnerEventType.selectedItem.toString())
            putExtra("eventDate",  selectedDate)
            putExtra("gender",     gender)
            selectedImageUri?.let { putExtra("imageUri", it.toString()) }
        }.also { startActivity(it) }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}