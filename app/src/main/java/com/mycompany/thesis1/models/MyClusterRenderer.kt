package com.mycompany.thesis1.models

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.mycompany.thesis1.R

class MyClusterRenderer(
    context: Context,
    googleMap: GoogleMap?,
    clusterManager: ClusterManager<ClusterMarker>
): DefaultClusterRenderer<ClusterMarker>(context,googleMap,clusterManager) {

    private var iconGenerator: IconGenerator = IconGenerator(context)

    private val context = context


    override fun onBeforeClusterItemRendered(item: ClusterMarker, markerOptions: MarkerOptions) {
        iconGenerator.setColor(ContextCompat.getColor(context, item.color))
        val icon = iconGenerator.makeIcon(item.userInitials)
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.title).snippet(item.snippet)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<ClusterMarker>): Boolean {
        return false
    }

    fun setUpdateMarker(clusterMarker: ClusterMarker) {
        val marker = getMarker(clusterMarker)
        if(marker != null) marker.position = clusterMarker.position
    }

}