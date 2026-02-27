package com.example.signup_login_app

data class User(
    val name: String,
    val email: String,
    val password: String,
    val gender: String,
    val hobbies: List<String>,
    val country: String
)