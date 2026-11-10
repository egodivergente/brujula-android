package es.brujula.jitai

import android.app.Application
import es.brujula.jitai.core.data.BrujulaRepository
import es.brujula.jitai.core.database.BrujulaDatabase
import es.brujula.jitai.core.datastore.PreferencesRepository
import es.brujula.jitai.core.jitai.BrujulaNotifier
import es.brujula.jitai.core.location.LocalZoneMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BrujulaApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: BrujulaRepository by lazy {
        val database = BrujulaDatabase.getInstance(this, applicationScope)
        val preferences = PreferencesRepository(this)
        val notifier = BrujulaNotifier(this)
        val zoneMonitor = LocalZoneMonitor(this)
        BrujulaRepository(
            dao = database.brujulaDao(),
            prefs = preferences,
            notifier = notifier,
            zoneMonitor = zoneMonitor
        )
    }
}
