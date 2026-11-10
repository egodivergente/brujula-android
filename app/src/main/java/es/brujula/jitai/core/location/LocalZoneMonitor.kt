package es.brujula.jitai.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import android.os.Build
import androidx.core.content.ContextCompat
import es.brujula.jitai.core.model.PersonalZone
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class LocalGpsSnapshot(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long
)

/**
 * Procesamiento de ubicación 100% local en el dispositivo (Parte 2 · §2.2, §2.5 y CAME F3→M3).
 * Ninguna coordenada ni geocerca abandona jamás el teléfono. Funciona sin servicios de pago.
 */
class LocalZoneMonitor(private val context: Context) {

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun captureCurrentLocationOnDevice(): LocalGpsSnapshot? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && providers.isNotEmpty()) {
            val fresh = suspendCancellableCoroutine<Location?> { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                try {
                    manager.getCurrentLocation(
                        providers.first(),
                        signal,
                        context.mainExecutor
                    ) { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
            if (fresh != null) {
                return LocalGpsSnapshot(
                    latitude = fresh.latitude,
                    longitude = fresh.longitude,
                    accuracyMeters = fresh.accuracy,
                    timestampMillis = fresh.time
                )
            }
        }

        val bestLast = providers
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        return bestLast?.let {
            LocalGpsSnapshot(
                latitude = it.latitude,
                longitude = it.longitude,
                accuracyMeters = it.accuracy,
                timestampMillis = it.time
            )
        }
    }

    fun findMatchingZone(
        snapshot: LocalGpsSnapshot,
        zones: List<PersonalZone>
    ): Pair<PersonalZone, Float>? {
        val result = FloatArray(1)
        return zones
            .filter { it.enabled && it.latitude != null && it.longitude != null }
            .map { zone ->
                Location.distanceBetween(
                    snapshot.latitude,
                    snapshot.longitude,
                    zone.latitude!!,
                    zone.longitude!!,
                    result
                )
                zone to result[0]
            }
            .filter { (zone, distanceMeters) -> distanceMeters <= zone.radiusMeters }
            .minByOrNull { it.second }
    }
}
