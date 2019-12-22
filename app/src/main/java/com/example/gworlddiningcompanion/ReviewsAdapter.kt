package com.example.gworlddiningcompanion

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView

class ReviewsAdapter(val reviews: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_review, parent, false)

        return ReviewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviews.size
    }

    override fun onBindViewHolder(holder: ReviewsViewHolder, position: Int) {
        val currentReview = reviews[position]

        holder.ratingBar.rating = currentReview.rating
        holder.name.text = currentReview.name
        holder.description.text = currentReview.description
    }

    class ReviewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val name: TextView = view.findViewById(R.id.name)
        val description: TextView = view.findViewById(R.id.description)
    }
} 