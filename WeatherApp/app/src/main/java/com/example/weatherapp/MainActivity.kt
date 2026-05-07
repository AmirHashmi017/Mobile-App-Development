package com.example.weatherapp

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    // ⚠️ Replace with your WeatherAPI.com API key
    private val API_KEY = "9c070aeb925c41c486160233260705"
    private val BASE_URL = "https://api.weatherapi.com/v1/current.json"

    // Views
    private lateinit var etCity: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar

    // Main card
    private lateinit var cardMain: CardView
    private lateinit var tvCity: TextView
    private lateinit var tvDate: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvTemperature: TextView
    private lateinit var tvCondition: TextView
    private lateinit var tvFeelsLike: TextView

    // Stats card
    private lateinit var cardStats: CardView
    private lateinit var statHumidity: View
    private lateinit var statWind: View
    private lateinit var statPressure: View
    private lateinit var statVisibility: View
    private lateinit var statUV: View
    private lateinit var statCloud: View

    // Sun card
    private lateinit var cardSun: CardView
    private lateinit var tvSunrise: TextView
    private lateinit var tvSunset: TextView
    private lateinit var tvPrecip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupListeners()

        // Load default city on launch
        fetchWeather("Islamabad")
    }

    private fun bindViews() {
        etCity = findViewById(R.id.etCity)
        btnSearch = findViewById(R.id.btnSearch)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)

        cardMain = findViewById(R.id.cardMain)
        tvCity = findViewById(R.id.tvCity)
        tvDate = findViewById(R.id.tvDate)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvCondition = findViewById(R.id.tvCondition)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)

        cardStats = findViewById(R.id.cardStats)
        statHumidity = findViewById(R.id.statHumidity)
        statWind = findViewById(R.id.statWind)
        statPressure = findViewById(R.id.statPressure)
        statVisibility = findViewById(R.id.statVisibility)
        statUV = findViewById(R.id.statUV)
        statCloud = findViewById(R.id.statCloud)

        cardSun = findViewById(R.id.cardSun)
        tvSunrise = findViewById(R.id.tvSunrise)
        tvSunset = findViewById(R.id.tvSunset)
        tvPrecip = findViewById(R.id.tvPrecip)
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            val city = etCity.text.toString().trim()
            if (city.isNotEmpty()) {
                hideKeyboard()
                fetchWeather(city)
            } else {
                showError("Please enter a city name")
            }
        }

        etCity.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val city = etCity.text.toString().trim()
                if (city.isNotEmpty()) {
                    hideKeyboard()
                    fetchWeather(city)
                }
                true
            } else false
        }
    }

    private fun fetchWeather(city: String) {
        showLoading(true)
        hideError()

        thread {
            try {
                val urlString = "$BASE_URL?key=$API_KEY&q=${city}&aqi=no"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().readText()
                    val json = JSONObject(response)
                    runOnUiThread { parseAndDisplay(json) }
                } else {
                    val errText = conn.errorStream?.bufferedReader()?.readText() ?: ""
                    val message = try {
                        JSONObject(errText).getJSONObject("error").getString("message")
                    } catch (e: Exception) {
                        "City not found. Please try again."
                    }
                    runOnUiThread { showError(message) }
                }
                conn.disconnect()
            } catch (e: Exception) {
                runOnUiThread { showError("Network error: ${e.localizedMessage}") }
            } finally {
                runOnUiThread { showLoading(false) }
            }
        }
    }

    private fun parseAndDisplay(json: JSONObject) {
        try {
            val location = json.getJSONObject("location")
            val current = json.getJSONObject("current")
            val condition = current.getJSONObject("condition")

            // Location
            val cityName = location.getString("name")
            val country = location.getString("country")
            tvCity.text = "$cityName, $country"

            // Date
            val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            tvDate.text = sdf.format(Date())

            // Temperature
            val tempC = current.getDouble("temp_c").toInt()
            val feelsC = current.getDouble("feelslike_c").toInt()
            tvTemperature.text = "$tempC°C"
            tvFeelsLike.text = "Feels like $feelsC°C"
            tvCondition.text = condition.getString("text")

            // Icon
            val iconUrl = "https:" + condition.getString("icon")
            thread {
                try {
                    val input = URL(iconUrl).openStream()
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)

                    runOnUiThread {
                        ivWeatherIcon.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Stats
            setStatView(statHumidity, "💧", "Humidity", "${current.getInt("humidity")}%")
            setStatView(statWind, "💨", "Wind", "${current.getDouble("wind_kph")} km/h\n${current.getString("wind_dir")}")
            setStatView(statPressure, "🔵", "Pressure", "${current.getDouble("pressure_mb")} mb")
            setStatView(statVisibility, "👁", "Visibility", "${current.getDouble("vis_km")} km")
            setStatView(statUV, "☀️", "UV Index", "${current.getDouble("uv")}")
            setStatView(statCloud, "☁️", "Cloud Cover", "${current.getInt("cloud")}%")

            // Sun info (from astro if available)
            tvSunrise.text = "—"
            tvSunset.text = "—"
            tvPrecip.text = "${current.getDouble("precip_mm")} mm"

            // Show all cards
            cardMain.visibility = View.VISIBLE
            cardStats.visibility = View.VISIBLE
            cardSun.visibility = View.VISIBLE

            // Animate in
            listOf(cardMain, cardStats, cardSun).forEachIndexed { i, card ->
                card.alpha = 0f
                card.translationY = 60f
                card.animate().alpha(1f).translationY(0f).setStartDelay((i * 100).toLong()).setDuration(400).start()
            }

        } catch (e: Exception) {
            showError("Error parsing data: ${e.localizedMessage}")
        }
    }

    private fun setStatView(view: View, emoji: String, label: String, value: String) {
        view.findViewById<TextView>(R.id.tvStatEmoji).text = emoji
        view.findViewById<TextView>(R.id.tvStatLabel).text = label
        view.findViewById<TextView>(R.id.tvStatValue).text = value
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !show
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
        cardMain.visibility = View.GONE
        cardStats.visibility = View.GONE
        cardSun.visibility = View.GONE
    }

    private fun hideError() {
        tvError.visibility = View.GONE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etCity.windowToken, 0)
    }
}