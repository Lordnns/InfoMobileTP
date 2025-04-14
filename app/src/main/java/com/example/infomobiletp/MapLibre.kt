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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Vérifie la permission d'accès à la localisation.
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    // Demande la permission dès que le composable est affiché (si ce n'est pas déjà accordé).
    LaunchedEffect(key1 = locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Stocke la localisation actuelle (null si non disponible ou permission refusée)
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // Récupération de la dernière localisation avec FusedLocationProviderClient
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

    // Création du MapView via AndroidView.
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                val mapView = MapView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    onCreate(null)
                    getMapAsync { map: MapLibreMap ->
                        // Utilise l'URL du style par défaut de MapLibre.
                        map.setStyle("https://demotiles.maplibre.org/style.json") { style ->
                            map.addOnMapClickListener { point: LatLng ->
                                onLocationSelected(point.latitude, point.longitude)
                                true
                            }
                            // Centre la carte sur la position actuelle si disponible, sinon sur Paris.
                            val center = currentLocation ?: LatLng(48.8566, 2.3522)
                            map.cameraPosition = CameraPosition.Builder()
                                .target(center)
                                .zoom(12.0)
                                .build()
                        }
                    }
                }
                mapView
            },
            update = { mapView ->
                mapView.onResume()
                // Si la localisation actuelle est disponible, on recentre éventuellement la carte.
                currentLocation?.let { loc ->
                    mapView.getMapAsync { map ->
                        map.cameraPosition = CameraPosition.Builder()
                            .target(loc)
                            .zoom(12.0)
                            .build()
                    }
                }
            }
        )
    }
}
