package fhnw.emoba.thatsapp.data

import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


class GPSConnector(val activity: Activity) {
    private val FHNW = Coordinates(latitude = 47.480995, longitude = 8.211862, altitude = 352.0)

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val locationProvider by lazy { LocationServices.getFusedLocationProviderClient(activity) }

    init {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, 10)
    }

    @SuppressLint("MissingPermission")
    fun getLocation(
        onNewLocation: (coordinates: Coordinates) -> Unit,
        onFailure: (exception: Exception) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        if (PERMISSIONS.oneOfGranted()) {

            locationProvider.lastLocation   // das ist ein 'Task' - zugriff sensor
                .addOnSuccessListener(activity) {
                    if (it == null) {
                        // der Emulator liefert null, falls keine Location gesetzt ist.
                        // Diese Abfrage ist also nur noetig, wenn man den Emulator verwendet und diesen nicht richtig eingestellt hat.
                        onNewLocation(FHNW)
                    } else {
                        onNewLocation(Coordinates(it.longitude, it.latitude, it.altitude))
                    }
                }
                .addOnFailureListener(activity) {
                    onFailure(it)
                }
        } else {
            onPermissionDenied()
        }
    }

    private fun Array<String>.oneOfGranted(): Boolean = any { it.granted() }

    private fun String.granted(): Boolean =
        ActivityCompat.checkSelfPermission(activity, this) == PackageManager.PERMISSION_GRANTED
}