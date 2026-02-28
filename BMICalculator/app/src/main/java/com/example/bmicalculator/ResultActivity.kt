package com.example.bmicalculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Retrieve data
        val name   = intent.getStringExtra("NAME") ?: "—"
        val age    = intent.getIntExtra("AGE", 0)
        val height = intent.getDoubleExtra("HEIGHT", 0.0)
        val weight = intent.getDoubleExtra("WEIGHT", 0.0)

        val heightM = height / 100.0
        val bmi     = weight / (heightM * heightM)

        val (category, colorRes, advice) = when {
            bmi < 18.5 -> Triple(
                "UNDERWEIGHT",
                R.color.colorUnderweight,
                "Your BMI is below the healthy range. Consider eating more nutrient-rich foods and consult a healthcare provider."
            )
            bmi <= 24.9 -> Triple(
                "NORMAL",
                R.color.colorNormal,
                "Great job! Your BMI is in the healthy range. Keep maintaining a balanced diet and regular exercise."
            )
            else -> Triple(
                "OVERWEIGHT",
                R.color.colorOverweight,
                "Your BMI is above the healthy range. Consider regular physical activity and a balanced diet. Consult a doctor for guidance."
            )
        }

        findViewById<TextView>(R.id.tvBmiValue).text     = String.format("%.1f", bmi)
        findViewById<TextView>(R.id.tvBmiCategory).apply {
            text = category
            setTextColor(ContextCompat.getColor(this@ResultActivity, colorRes))
        }
        findViewById<TextView>(R.id.tvName).text   = name
        findViewById<TextView>(R.id.tvAge).text    = "$age years"
        findViewById<TextView>(R.id.tvHeight).text = "${height} cm  (${String.format("%.2f", heightM)} m)"
        findViewById<TextView>(R.id.tvWeight).text = "$weight kg"
        findViewById<TextView>(R.id.tvAdvice).text = advice

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}