package com.example.infomobiletp

import android.Manifest
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import android.location.Geocoder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
) {
    var addressInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(key1 = locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            try {
                val location = fusedLocationClient.lastLocation.await()
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Store a reference to MapView
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            label = { Text("Entrez une adresse") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocationName(addressInput, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val location = LatLng(addresses[0].latitude, addresses[0].longitude)
                            mapViewRef?.getMapAsync { map ->
                                map.setCameraPosition(
                                    CameraPosition.Builder()
                                        .target(location)
                                        .zoom(14.0)
                                        .build()
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text("Rechercher")
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = modifier,
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this // Save reference
                        layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        onCreate(null)
                        getMapAsync { map: MapLibreMap ->
                            map.setStyle("https://demotiles.maplibre.org/style.json") {
                                map.addOnMapClickListener { point: LatLng ->
                                    onLocationSelected(point.latitude, point.longitude)
                                    true
                                }
                                val center = currentLocation ?: LatLng(48.8566, 2.3522)
                                map.setCameraPosition(
                                    CameraPosition.Builder()
                                        .target(center)
                                        .zoom(12.0)
                                        .build()
                                )
                            }
                        }
                    }
                },
                update = { mapView ->
                    mapView.onResume()
                    currentLocation?.let { loc ->
                        mapView.getMapAsync { map ->
                            map.setCameraPosition(
                                CameraPosition.Builder()
                                    .target(loc)
                                    .zoom(12.0)
                                    .build()
                            )
                        }
                    }
                }
            )
        }
    }
}
