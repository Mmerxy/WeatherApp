package com.example.weather

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {


    /* All of this is to create a periodic task that updates the weather info every 10 mins since
    the API only updates approx every 10 mins
     */

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This is just to make it so the the information displayed updates every 10 minutes
        //basically creates a thread and starts it
        runnable = object : Runnable {
            override fun run() {
                weatherTask().execute()
                handler.postDelayed(this, 300000) // 10 minutes
            }
        }


        handler.post(runnable)
    }


    //This is to make sure that when the app isn't open it doesnt continue making api requests
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }


    //it is the setup for the app does before it fetches the weather so it makes the progress bar visible and hides the main layout until the weather data is there
    inner class weatherTask() : AsyncTask<String, Void, String?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        //This just reads the weather info from the URL and returns it as a string
        override fun doInBackground(vararg params: String?): String? {
            return try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=Marietta,us&units=imperial&APPID=fd32cc6e95804dd2b863b5bc666bf5ee").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                Log.e("WeatherTask", "Error fetching weather data", e)
                null
            }
        }

        //handl
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                Log.d("WeatherTask", "API Response: $result")
                try {
                    //Creates a JSON object and parses it into temp wind etc
                    val jsonObj = JSONObject(result)
                    val main = jsonObj.getJSONObject("main")
                    val sys = jsonObj.getJSONObject("sys")
                    val wind = jsonObj.getJSONObject("wind")
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                    //formats the dates
                    val updatedAt: Long = jsonObj.getLong("dt")
                    val date = Date(updatedAt * 1000)
                    val dateFormat = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.ENGLISH)
                    dateFormat.timeZone = TimeZone.getDefault() // Set the time zone to the default time zone of the device
                    val updatedAtText = "Updated at: " + dateFormat.format(date)

                    //extracts the remaining weather info and formats them
                    val temp = main.getString("temp") + "°F"
                    val tempMin = "Min Temp: " + main.getString("temp_min") + "°F"
                    val tempMax = "Max Temp: " + main.getString("temp_max") + "°F"
                    val pressure = main.getString("pressure") + " hPa"
                    val humidity = main.getString("humidity") + " %"

                    val sunrise: Long = sys.getLong("sunrise")
                    val sunset: Long = sys.getLong("sunset")
                    val windSpeed = wind.getString("speed") + " m/s"
                    val weatherDescription = weather.getString("description").capitalize(Locale.ROOT)

                    val address = jsonObj.getString("name") + ", " + sys.getString("country")

                    //formats the text according to the TextViews in the main activity xml file
                    findViewById<TextView>(R.id.address).text = address
                    findViewById<TextView>(R.id.updated_at).text = updatedAtText
                    findViewById<TextView>(R.id.status).text = weatherDescription
                    findViewById<TextView>(R.id.temp).text = temp
                    findViewById<TextView>(R.id.temp_min).text = tempMin
                    findViewById<TextView>(R.id.temp_max).text = tempMax
                    findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                    findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                    findViewById<TextView>(R.id.wind).text = windSpeed
                    findViewById<TextView>(R.id.pressure).text = pressure
                    findViewById<TextView>(R.id.humidity).text = humidity

                    findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                    findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("WeatherTask", "Error parsing weather data", e)
                    findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                    findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
                }
            } else {
                //if for whatever reason my code doesnt work it displays an error message
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}

