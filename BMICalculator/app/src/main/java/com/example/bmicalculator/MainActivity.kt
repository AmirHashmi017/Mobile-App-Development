package com.example.bmicalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName   = findViewById<EditText>(R.id.etName)
        val etAge    = findViewById<EditText>(R.id.etAge)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnCalc  = findViewById<Button>(R.id.btnCalculate)

        btnCalc.setOnClickListener {
            val name   = etName.text.toString().trim()
            val age    = etAge.text.toString().trim()
            val height = etHeight.text.toString().trim()
            val weight = etWeight.text.toString().trim()

            if (name.isEmpty() || age.isEmpty() || height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightVal = height.toDoubleOrNull()
            val weightVal = weight.toDoubleOrNull()
            val ageVal    = age.toIntOrNull()

            if (heightVal == null || weightVal == null || ageVal == null) {
                Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (heightVal <= 0 || weightVal <= 0 || ageVal <= 0) {
                Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("NAME",   name)
                putExtra("AGE",    ageVal)
                putExtra("HEIGHT", heightVal)
                putExtra("WEIGHT", weightVal)
            }
            startActivity(intent)
        }
    }
}