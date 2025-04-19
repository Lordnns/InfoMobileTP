package com.example.infomobiletp

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import android.view.ViewGroup
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
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.view.isVisible
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker


@Composable
fun OSMMapView(
    modifier: Modifier = Modifier,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                tileProvider.tileSource = TileSourceFactory.MAPNIK
                setMultiTouchControls(true)
                controller.setZoom(12.0)
                controller.setCenter(GeoPoint(48.8566, 2.3522))

                // single‑tap listener
                overlays.add(
                    MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onLocationSelected(p.latitude, p.longitude)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    })
                )
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

@Composable
fun OSMMapViewWithMarkers(
    modifier: Modifier = Modifier,
    phonePoint: GeoPoint?,
    selectedPoint: GeoPoint?,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    Configuration.getInstance().load(context, prefs)
    Configuration.getInstance().userAgentValue = context.packageName

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                tileProvider.tileSource = TileSourceFactory.MAPNIK
                setMultiTouchControls(true)

                // phone location marker (blue)
                val phoneMarker = Marker(this).apply {
                    icon.setTint(Color.BLUE)
                    position = phonePoint ?: GeoPoint(0.0, 0.0)
                }
                // selected location marker (red), enable/disable to show/hide
                val selMarker = Marker(this).apply {
                    icon.setTint(Color.RED)
                    position = selectedPoint ?: GeoPoint(0.0, 0.0)
                    setVisible(selectedPoint != null)
                }

                overlays.add(phoneMarker)
                overlays.add(selMarker)

                // initial camera
                controller.setZoom(12.0)
                controller.setCenter(selectedPoint ?: phonePoint ?: GeoPoint(48.8566, 2.3522))

                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        onLocationSelected(p.latitude, p.longitude)
                        selMarker.position = p
                        selMarker.setVisible(true)
                        invalidate()
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean {
                        onLocationSelected(p.latitude, p.longitude)
                        selMarker.position = p
                        selMarker.setVisible(true)
                        invalidate()
                        return true
                    }
                }))
            }
        },
        update = { mapView ->
            // update phone marker
            phonePoint?.let {
                (mapView.overlays.filterIsInstance<Marker>()[0]).position = it
            }
            // update selected marker
            selectedPoint?.let {
                val m = mapView.overlays.filterIsInstance<Marker>()[1]
                m.position = it
                m.setVisible(true)
            }
            mapView.invalidate()
        }
    )
}
