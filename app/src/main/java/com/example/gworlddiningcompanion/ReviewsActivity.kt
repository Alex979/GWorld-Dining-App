package com.example.gworlddiningcompanion

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.doAsync

class ReviewsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var detailsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        val businessID = intent.getStringExtra("BUSINESS_ID")
        val businessURL = intent.getStringExtra("BUSINESS_URL")
        val businessName = intent.getStringExtra("BUSINESS_NAME")

        title = businessName

        detailsButton = findViewById(R.id.fullDetailsButton)

        detailsButton.setOnClickListener {
            val uri: Uri = Uri.parse(businessURL)
            val webIntent = Intent(Intent.ACTION_VIEW, uri)

            startActivity(webIntent)
        }

        doAsync {
            // Search yelp api
            val yelpManager = YelpManager(getString(R.string.yelp_key))
            val reviews = yelpManager.getReviews(businessID)

            runOnUiThread {
                if(reviews.size > 0) {
                    recyclerView = findViewById(R.id.recyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@ReviewsActivity)
                    recyclerView.adapter = ReviewsAdapter(reviews)
                } else {
                    AlertDialog.Builder(this@ReviewsActivity)
                        .setTitle(getString(R.string.no_reviews_title))
                        .setMessage(getString(R.string.no_reviews))
                        .setPositiveButton(getString(R.string.ok)) { _ , _ ->
                            finish()
                        }
                        .show()
                }
            }
        }
    }
}
