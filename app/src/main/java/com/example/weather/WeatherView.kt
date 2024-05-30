package com.example.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.withContext as WC


class WeatherView : ViewModel(){
    var weatherInfo: JSONObject? = null

    fun fetchWeatherInfo(apiKey: String, latitude: Double, longitude: Double, date: String){
        viewModelScope.launch{
            val info = WC(Dispatchers.IO) {
                getWeatherData(apiKey, latitude, longitude, date)
            }
            weatherInfo = info
        }
    }

    private fun getWeatherData(apiKey: String, latitude: Double, longitude: Double, date: String): JSONObject? {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parseDate = dateFormat.parse(date)
        val timestamp = parseDate.time / 1000

        val url = "https://api.openweathermap.org/data/2.5/onecall/timemachine?lat={lat}&lon={lon}&dt={timestamp}&appid={apiKey}&units=metric"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if(!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseData = response.body?.string() ?: return null
            return JSONObject(responseData)
        }
    }
}
