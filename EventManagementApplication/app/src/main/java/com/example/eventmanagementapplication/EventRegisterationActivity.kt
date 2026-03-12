package com.example.eventmanagementapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smarteventmanager.databinding.ActivityEventRegistrationBinding
import java.util.*

class EventRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventRegistrationBinding

    // Holds selected image URI
    private var selectedImageUri: Uri? = null

    // Selected date string
    private var selectedDate: String = ""

    // Image picker launcher
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.ivProfileImage.setImageURI(uri)
                    binding.tvImageStatus.text = "✅ Image selected"
                    binding.tvImageStatus.setTextColor(getColor(R.color.success_green))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupDatePicker()
        setupImagePicker()
        setupSubmitButton()

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    // ─────────────────── Spinner Setup ───────────────────
    private fun setupSpinner() {
        val eventTypes = resources.getStringArray(R.array.event_types)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            eventTypes
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerEventType.adapter = adapter
    }

    // ─────────────────── Date Picker ───────────────────
    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    // Format: DD / MM / YYYY
                    selectedDate = String.format(
                        "%02d / %02d / %d",
                        selectedDayOfMonth,
                        selectedMonth + 1,
                        selectedYear
                    )
                    binding.tvSelectedDate.text = selectedDate
                    binding.tvSelectedDate.setTextColor(getColor(R.color.text_primary))
                },
                year, month, day
            )

            // Set minimum date to today
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.setTitle("Select Event Date")
            datePickerDialog.show()
        }
    }

    // ─────────────────── Image Picker ───────────────────
    private fun setupImagePicker() {
        binding.btnUploadImage.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("📷  Take Photo", "🖼️  Choose from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imagePickerLauncher.launch(intent)
    }

    // ─────────────────── Submit & Validation ───────────────────
    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                showConfirmationDialog()
            }
        }
    }

    private fun validateForm(): Boolean {

        // 1. Full Name
        val fullName = binding.etFullName.text.toString().trim()
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full name is required"
            binding.etFullName.requestFocus()
            showToast("⚠️ Please enter your full name")
            return false
        }
        if (fullName.length < 3) {
            binding.etFullName.error = "Name must be at least 3 characters"
            binding.etFullName.requestFocus()
            showToast("⚠️ Name must be at least 3 characters")
            return false
        }
        if (!fullName.matches(Regex("^[a-zA-Z ]+$"))) {
            binding.etFullName.error = "Name can only contain letters and spaces"
            binding.etFullName.requestFocus()
            showToast("⚠️ Name can only contain letters and spaces")
            return false
        }

        // 2. Phone Number
        val phone = binding.etPhoneNumber.text.toString().trim()
        if (phone.isEmpty()) {
            binding.etPhoneNumber.error = "Phone number is required"
            binding.etPhoneNumber.requestFocus()
            showToast("⚠️ Please enter your phone number")
            return false
        }
        val phoneDigits = phone.replace(Regex("[+\\s-]"), "")
        if (phoneDigits.length < 10 || phoneDigits.length > 13) {
            binding.etPhoneNumber.error = "Enter a valid phone number (10-13 digits)"
            binding.etPhoneNumber.requestFocus()
            showToast("⚠️ Enter a valid phone number")
            return false
        }

        // 3. Email
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "Email address is required"
            binding.etEmail.requestFocus()
            showToast("⚠️ Please enter your email address")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email address"
            binding.etEmail.requestFocus()
            showToast("⚠️ Please enter a valid email address")
            return false
        }

        // 4. Event Type
        if (binding.spinnerEventType.selectedItemPosition == 0) {
            showToast("⚠️ Please select an event type")
            return false
        }

        // 5. Event Date
        if (selectedDate.isEmpty()) {
            showToast("⚠️ Please select an event date")
            return false
        }

        // 6. Gender
        if (binding.radioGroupGender.checkedRadioButtonId == -1) {
            showToast("⚠️ Please select your gender")
            return false
        }

        // 7. Terms & Conditions
        if (!binding.checkboxTerms.isChecked) {
            showToast("⚠️ Please accept the Terms and Conditions")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ─────────────────── Alert Dialog before submitting ───────────────────
    private fun showConfirmationDialog() {
        val fullName = binding.etFullName.text.toString().trim()
        val eventType = binding.spinnerEventType.selectedItem.toString()

        AlertDialog.Builder(this)
            .setTitle("✅  Confirm Submission")
            .setMessage(
                "Are you sure you want to submit the registration?\n\n" +
                        "• Name: $fullName\n" +
                        "• Event: $eventType\n" +
                        "• Date: $selectedDate\n\n" +
                        "Please review and confirm."
            )
            .setPositiveButton("Yes, Submit") { _, _ ->
                navigateToConfirmation()
            }
            .setNegativeButton("Review Again") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToConfirmation() {
        // Get selected gender text
        val selectedGenderId = binding.radioGroupGender.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.radioMale   -> "Male"
            R.id.radioFemale -> "Female"
            R.id.radioOther  -> "Other"
            else             -> "Not Specified"
        }

        val data = RegistrationData(
            fullName    = binding.etFullName.text.toString().trim(),
            phoneNumber = binding.etPhoneNumber.text.toString().trim(),
            email       = binding.etEmail.text.toString().trim(),
            eventType   = binding.spinnerEventType.selectedItem.toString(),
            eventDate   = selectedDate,
            gender      = gender,
            imageUri    = selectedImageUri?.toString()
        )

        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra(ConfirmationActivity.EXTRA_REGISTRATION_DATA, data)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
