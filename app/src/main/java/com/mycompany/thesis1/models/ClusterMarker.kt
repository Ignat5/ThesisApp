package com.mycompany.thesis1.models

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.mycompany.thesis1.R

class ClusterMarker (
    latitude: Double,
    longitude: Double,
    title: String = "title",
    snippet: String = "snippet",
    userInitials: String,
    color: Int = R.color.teal_200,
    userId: String = ""
): ClusterItem {

    var location: LatLng = LatLng(latitude, longitude)
    private val title: String = title
    private val snippet: String = snippet
    var userInitials: String = userInitials
    val color: Int = color
    val userId: String = userId

    override fun getPosition(): LatLng = location

    override fun getTitle(): String? = title

    override fun getSnippet(): String? = snippet

    override fun toString(): String {
        return "userId: $userId, userInitials: $userInitials, latitude: ${position.latitude}, longitude: ${position.longitude}"
    }
}