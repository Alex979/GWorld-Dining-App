package com.example.gworlddiningcompanion

import android.app.Activity
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject

class GWorldManager {

    // OkHttp is a library used to make network calls
    private val okHttpClient: OkHttpClient

    // This runs extra code when TwitterManager is initialized
    init {
        val builder = OkHttpClient.Builder()

        // This causes all network traffic to be logged to the console
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(logging)

        okHttpClient = builder.build()
    }

    fun getGWorldRestaurants(): List<Business> {

        val url = "http://www.mocky.io/v2/5d7e80913300008e00f0ad94"

        // Building the request
        val request = Request.Builder().url(url).build()

        // Actually makes the API call, blocking the thread until it completes
        val response = okHttpClient.newCall(request).execute()

        // Empty list of restaurants that we'll build up from the response
        val restaurants = mutableListOf<Business>()

        // Get the JSON string body, if there was one
        val responseString = response.body?.string()

        // Make sure the server responded successfully, and with some JSON data
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {

            // Parse our JSON string
            val json = JSONObject(responseString)
            val gworldArray = json.getJSONArray("gworld")

            for(i in 0 until gworldArray.length()) {
                try {
                    val current = gworldArray.getJSONObject(i)
                    val name = current.getString("name")
                    val address = current.getString("address")

                    // Ensure distance is within the circle (since yelp API sometimes returns results outside our range)
                    restaurants.add(
                        Business(
                            name = name,
                            address = address,
                            latLng = null,
                            rating = null,
                            id = null,
                            url = null
                        )
                    )
                } catch (e: JSONException) {
                    // Skip this result if all the required data could not be pulled
                }
            }
        }

        return restaurants
    }
}