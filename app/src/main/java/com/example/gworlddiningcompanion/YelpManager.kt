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

class YelpManager(val APIKey: String) {

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

    fun searchBusinesses(latLng: LatLng?, radius: Int, categories: String?): List<Business> {
        if (latLng == null) {
            return emptyList()
        }

        var url =
            "https://api.yelp.com/v3/businesses/search?latitude=${latLng?.latitude}&longitude=${latLng?.longitude}&radius=$radius&limit=50&term=food"
        if(categories != null)
            url += "&categories=$categories"

        // Building the request
        val request = Request.Builder()
            .url(url)
            .header(
                "Authorization",
                "Bearer $APIKey"
            )
            .build()

        // Actually makes the API call, blocking the thread until it completes
        val response = okHttpClient.newCall(request).execute()
        // Empty list of Businesses that we'll build up from the response
        val businesses = mutableListOf<Business>()
        // Get the JSON string body, if there was one
        val responseString = response.body?.string()
        // Make sure the server responded successfully, and with some JSON data
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {

            // Parse our JSON string
            val json = JSONObject(responseString)
            val businessesArray = json.getJSONArray("businesses")

            for(i in 0 until businessesArray.length()) {
                try {
                    val current = businessesArray.getJSONObject(i)
                    val name = current.getString("name")
                    val address = current.getJSONObject("location").getJSONArray("display_address").getString(0)
                    val coordinates = current.getJSONObject("coordinates")
                    val latitude = coordinates.getDouble("latitude")
                    val longitude = coordinates.getDouble("longitude")
                    val distance = current.getDouble("distance")
                    val rating = current.getDouble("rating")
                    val id = current.getString("id")
                    val url = current.getString("url")

                    // Ensure distance is within the circle (since yelp API sometimes returns results outside our range)
                    if(distance <= radius) {
                        businesses.add(
                            Business(
                                name = name,
                                latLng = LatLng(latitude, longitude),
                                address = address,
                                rating = rating,
                                id = id,
                                url = url
                            )
                        )
                    }
                } catch (e: JSONException) {
                    // Skip this result if all the required data could not be pulled
                }
            }
        }

        return businesses
    }

    fun getReviews(id: String): List<Review> {

        var url = "https://api.yelp.com/v3/businesses/$id/reviews"

        // Building the request
        val request = Request.Builder()
            .url(url)
            .header(
                "Authorization",
                "Bearer $APIKey"
            )
            .build()

        // Actually makes the API call, blocking the thread until it completes
        val response = okHttpClient.newCall(request).execute()
        // Empty list of Businesses that we'll build up from the response
        val reviews = mutableListOf<Review>()
        // Get the JSON string body, if there was one
        val responseString = response.body?.string()
        // Make sure the server responded successfully, and with some JSON data
        if (response.isSuccessful && !responseString.isNullOrEmpty()) {

            // Parse our JSON string
            val json = JSONObject(responseString)
            val reviewsArray = json.getJSONArray("reviews")

            for(i in 0 until reviewsArray.length()) {
                try {
                    val current = reviewsArray.getJSONObject(i)
                    val rating = current.getDouble("rating")
                    val name = current.getJSONObject("user").getString("name")
                    val text = current.getString("text")

                    reviews.add (
                        Review (
                            rating = rating.toFloat(),
                            name = name,
                            description = text
                        )
                    )
                } catch (e: JSONException) {
                    // Skip this result if all the required data could not be pulled
                }
            }
        }

        return reviews
    }
}