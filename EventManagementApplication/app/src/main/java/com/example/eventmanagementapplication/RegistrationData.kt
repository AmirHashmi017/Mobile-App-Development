package com.example.eventmanagementapplication


import android.net.Uri
import java.io.Serializable

data class RegistrationData(
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val eventType: String,
    val eventDate: String,
    val gender: String,
    val imageUri: String? = null
) : Serializable
