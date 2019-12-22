package com.example.gworlddiningcompanion

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.*

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowCloseListener {

    private lateinit var map: GoogleMap
    private lateinit var infoWindowView: View
    private lateinit var viewReviewsButton: Button
    private lateinit var preferences: SharedPreferences

    private var searchRadius: Int = 1500
    private var categories: String? = null
    private var lastClickedBusiness: Business? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        preferences = getSharedPreferences("gworld-dining-companion", Context.MODE_PRIVATE)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchRadius = intent.getIntExtra("SEARCH_RADIUS", 1500)
        categories = intent.getStringExtra("CATEGORY_QUERY")

        infoWindowView = LayoutInflater.from(this).inflate(R.layout.marker_info_window, findViewById(R.id.map), false)

        viewReviewsButton = findViewById(R.id.viewReviewsButton)
        viewReviewsButton.isClickable = false
        viewReviewsButton.setOnClickListener {
            if(lastClickedBusiness != null) {
                val intent = Intent(this, ReviewsActivity::class.java)
                intent.putExtra("BUSINESS_ID", lastClickedBusiness?.id)
                intent.putExtra("BUSINESS_URL", lastClickedBusiness?.url)
                intent.putExtra("BUSINESS_NAME", lastClickedBusiness?.name)
                startActivity(intent)
            }
        }

        val firstTimeUser = preferences.getBoolean("FIRST_TIME_USER", true)

        if(firstTimeUser) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.map_instructions_title))
                .setMessage(getString(R.string.map_instructions))
                .setPositiveButton(getString(R.string.ok), null)
                .show()

            preferences.edit().putBoolean("FIRST_TIME_USER", false).apply()
        }
    }

    // Set map properties once ready to manipulate
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Navigate camera to zoom into DC
        val dc = LatLng(38.899848, -77.046027)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(dc, 14.0f))

        map.setOnMapLongClickListener(this)
        map.setOnMarkerClickListener(this)
        map.setOnInfoWindowCloseListener(this)
        map.setInfoWindowAdapter(this)
    }

    // Draw search radius on long click
    override fun onMapLongClick(p0: LatLng?) {
        map.clear()

        map.addCircle(CircleOptions()
            .center(p0)
            .radius(searchRadius.toDouble())
            .strokeColor(Color.argb(255, 252, 3, 57))
            .strokeWidth(3.0f)
            .fillColor(Color.argb(30, 252, 3, 161)))

        doAsync {
            // Search yelp api
            val yelpManager = YelpManager(getString(R.string.yelp_key))
            val businesses = yelpManager.searchBusinesses(p0, searchRadius, categories)

            // Get gworld restaurants
            val gworldManager = GWorldManager()
            val gworldRestaurants = gworldManager.getGWorldRestaurants()

            // Add markers for restaurants, differentiating between GWorld and Non-GWorld
            runOnUiThread {
                if(businesses.size > 0) {
                    businesses.forEach { business ->

                        // Check if business is a gworld restaurant
                        var isGWorld = false
                        gworldRestaurants.forEach { restaurant ->
                            if (restaurant.address == business.address) {
                                isGWorld = true
                            }
                        }

                        var markerTitle = "${business.name}\n${business.rating} stars"
                        val markerOptions =
                            MarkerOptions().position(business.latLng!!).title(markerTitle)
                        if (isGWorld)
                            markerOptions.icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                                )
                            )


                        val marker = map.addMarker(markerOptions)
                        marker.tag = business
                    }
                } else {
                    val toast = Toast.makeText(
                        this@MapsActivity,
                        getString(R.string.no_results),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()

                }
            }
        }
    }

    // Reuse the same info window view defined above, but change the contents
    override fun getInfoContents(marker: Marker?): View {
        val businessName: TextView = infoWindowView.findViewById(R.id.businessName)
        val businessRating: RatingBar = infoWindowView.findViewById(R.id.businessRating)

        val markerInfo = marker?.tag as Business
        businessName.text = markerInfo.name
        businessRating.rating = markerInfo.rating!!.toFloat()

        return infoWindowView
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        viewReviewsButton.isClickable = true
        viewReviewsButton.text = getString(R.string.view_reviews)

        lastClickedBusiness = marker?.tag as Business

        return false
    }

    override fun onInfoWindowClose(p0: Marker?) {
        viewReviewsButton.isClickable = false
        viewReviewsButton.text = getString(R.string.choose_a_location)
    }
}
