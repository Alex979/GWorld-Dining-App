package com.example.gworlddiningcompanion

import com.google.android.gms.maps.model.LatLng

data class Business (
    val name: String,
    val address: String,
    val latLng: LatLng?,
    val rating: Double?,
    val id: String?,
    val url: String?
)