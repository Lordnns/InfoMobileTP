package com.example.infomobiletp

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OSMMapView(
    modifier: Modifier = Modifier,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("osmdroid", 0)
    Configuration.getInstance().load(context, prefs)
    Configuration.getInstance().userAgentValue = "com.example.infomobiletp" // Replace with your app Iosmapview D


    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // Set a layout size explicitly.
                layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                // Optionally, set a background color to see the bounds.
                setBackgroundColor(android.graphics.Color.LTGRAY)
                // Set the tile source – MAPNIK is a good default.
                setTileSource(TileSourceFactory.MAPNIK)
                // Set up the map controller to center on a default point (Paris)
                controller.setZoom(12.0)
                controller.setCenter(GeoPoint(48.8566, 2.3522))
                // Add a long-press listener that returns the current center.
                setOnLongClickListener {
                    // Use this.mapCenter to get the current center.
                    val center = this.mapCenter
                    onLocationSelected(center.latitude, center.longitude)
                    true
                }
            }
        }
    )
}

@Composable
fun LocationPickerScreen(
    onLocationPicked: (latitude: Double, longitude: Double) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        OSMMapView(
            modifier = Modifier.fillMaxSize(),
            onLocationSelected = { lat, lng ->
                // Call back to your parent with the selected coordinates.
                onLocationPicked(lat, lng)
            }
        )
        // Optionally add a close button overlay...
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Fermer")
        }
    }
}
