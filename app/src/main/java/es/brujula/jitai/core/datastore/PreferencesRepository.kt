package es.brujula.jitai.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import es.brujula.jitai.core.model.JitaiExperimentalSettings
import es.brujula.jitai.core.model.NurseNavigatorProfile
import es.brujula.jitai.core.model.ProgramGoalType
import es.brujula.jitai.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.brujulaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "brujula_jitai_prefs"
)

class PreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val CRAVING_THRESHOLD = intPreferencesKey("craving_threshold")
        val CONTROL_THRESHOLD = intPreferencesKey("control_threshold")
        val MAX_DAILY_NOTIFICATIONS = intPreferencesKey("max_daily_notifications")
        val QUIET_START_HOUR = intPreferencesKey("quiet_start_hour")
        val QUIET_END_HOUR = intPreferencesKey("quiet_end_hour")
        val PAUSED_UNTIL_MILLIS = longPreferencesKey("paused_until_millis")
        val REEVAL_DUE_AT_MILLIS = longPreferencesKey("reeval_due_at_millis")
        val NOTIFICATIONS_SENT_TODAY = intPreferencesKey("notifications_sent_today")
        val NOTIFICATIONS_EPOCH_DAY = longPreferencesKey("notifications_epoch_day")
        val OPTIONAL_GPS_ENABLED = booleanPreferencesKey("optional_gps_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DISCREET_MODE_ENABLED = booleanPreferencesKey("discreet_mode_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val LARGE_TEXT_ENABLED = booleanPreferencesKey("large_text_enabled")
        val THEME_PREF = stringPreferencesKey("theme_preference")
        val PERSONAL_MOTTO = stringPreferencesKey("personal_motto")
        val PROGRAM_GOAL = stringPreferencesKey("program_goal")
        val PROGRAM_WEEK = intPreferencesKey("program_week")
        val ACTIVE_ZONE_ID = longPreferencesKey("active_zone_id")
        val RULE_VERSION = intPreferencesKey("rule_version")
        val RULE_HISTORY_JSON = stringPreferencesKey("rule_history_json")
        val NURSE_PROFILE_JSON = stringPreferencesKey("nurse_profile_json")
    }

    val settingsFlow: Flow<JitaiExperimentalSettings> = context.brujulaDataStore.data.map { prefs ->
        val todayEpoch = LocalDate.now().toEpochDay()
        val storedEpoch = prefs[Keys.NOTIFICATIONS_EPOCH_DAY] ?: todayEpoch
        val sentToday = if (storedEpoch == todayEpoch) {
            prefs[Keys.NOTIFICATIONS_SENT_TODAY] ?: 0
        } else {
            0
        }
        val history = prefs[Keys.RULE_HISTORY_JSON]?.let {
            runCatching { json.decodeFromString<List<String>>(it) }.getOrNull()
        } ?: listOf(
            "v1 · Hipótesis inicial pactada: craving ≥7/10 o control ≤3/10, tope 2 avisos/día, silencio 23:00–08:00 y pausa de 4 h tras rechazo."
        )

        val zoneIdRaw = prefs[Keys.ACTIVE_ZONE_ID] ?: -1L

        JitaiExperimentalSettings(
            cravingThreshold = (prefs[Keys.CRAVING_THRESHOLD] ?: 7).coerceIn(5, 9),
            controlThreshold = (prefs[Keys.CONTROL_THRESHOLD] ?: 3).coerceIn(1, 5),
            maxDailyNotifications = (prefs[Keys.MAX_DAILY_NOTIFICATIONS] ?: 2).coerceIn(1, 2),
            quietHoursStartHour = (prefs[Keys.QUIET_START_HOUR] ?: 23).coerceIn(0, 23),
            quietHoursEndHour = (prefs[Keys.QUIET_END_HOUR] ?: 8).coerceIn(0, 23),
            rejectionPauseHours = 4,
            pausedUntilMillis = prefs[Keys.PAUSED_UNTIL_MILLIS] ?: 0L,
            reevalDueAtMillis = prefs[Keys.REEVAL_DUE_AT_MILLIS] ?: 0L,
            notificationsSentToday = sentToday,
            notificationsEpochDay = todayEpoch,
            optionalGpsEnabled = prefs[Keys.OPTIONAL_GPS_ENABLED] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            discreetModeEnabled = prefs[Keys.DISCREET_MODE_ENABLED] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            largeTextEnabled = prefs[Keys.LARGE_TEXT_ENABLED] ?: false,
            themePreference = prefs[Keys.THEME_PREF]?.let {
                runCatching { ThemePreference.valueOf(it) }.getOrNull()
            } ?: ThemePreference.SYSTEM,
            personalMotto = prefs[Keys.PERSONAL_MOTTO]
                ?: "Orientarme sin juzgarme; elegir mi próximo paso.",
            programGoal = prefs[Keys.PROGRAM_GOAL]?.let {
                runCatching { ProgramGoalType.valueOf(it) }.getOrNull()
            } ?: ProgramGoalType.HARM_REDUCTION,
            currentProgramWeek = (prefs[Keys.PROGRAM_WEEK] ?: 4).coerceIn(1, 12),
            activeZoneId = if (zoneIdRaw > 0L) zoneIdRaw else null,
            ruleVersion = prefs[Keys.RULE_VERSION] ?: 1,
            ruleChangeHistory = history
        )
    }

    val nurseProfileFlow: Flow<NurseNavigatorProfile> = context.brujulaDataStore.data.map { prefs ->
        prefs[Keys.NURSE_PROFILE_JSON]?.let {
            runCatching { json.decodeFromString<NurseNavigatorProfile>(it) }.getOrNull()
        } ?: NurseNavigatorProfile()
    }

    suspend fun updateExperimentalThresholds(
        cravingThreshold: Int,
        controlThreshold: Int,
        maxDailyNotifications: Int,
        quietStartHour: Int,
        quietEndHour: Int
    ) {
        context.brujulaDataStore.edit { prefs ->
            val currentVersion = prefs[Keys.RULE_VERSION] ?: 1
            val nextVersion = currentVersion + 1
            val oldHistory = prefs[Keys.RULE_HISTORY_JSON]?.let {
                runCatching { json.decodeFromString<List<String>>(it) }.getOrNull()
            } ?: emptyList()

            val stamp = LocalDate.now().toString() + " " +
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            val safeCap = maxDailyNotifications.coerceIn(1, 2)
            val entry = "v$nextVersion ($stamp) · Craving ≥${cravingThreshold}/10, control ≤${controlThreshold}/10, tope $safeCap/día, silencio ${"%02d".format(quietStartHour)}:00–${"%02d".format(quietEndHour)}:00."

            prefs[Keys.CRAVING_THRESHOLD] = cravingThreshold.coerceIn(5, 9)
            prefs[Keys.CONTROL_THRESHOLD] = controlThreshold.coerceIn(1, 5)
            prefs[Keys.MAX_DAILY_NOTIFICATIONS] = safeCap
            prefs[Keys.QUIET_START_HOUR] = quietStartHour.coerceIn(0, 23)
            prefs[Keys.QUIET_END_HOUR] = quietEndHour.coerceIn(0, 23)
            prefs[Keys.RULE_VERSION] = nextVersion
            prefs[Keys.RULE_HISTORY_JSON] = json.encodeToString((listOf(entry) + oldHistory).take(12))
        }
    }

    suspend fun recordNotificationSent() {
        val today = LocalDate.now().toEpochDay()
        context.brujulaDataStore.edit { prefs ->
            val storedDay = prefs[Keys.NOTIFICATIONS_EPOCH_DAY] ?: today
            val current = if (storedDay == today) (prefs[Keys.NOTIFICATIONS_SENT_TODAY] ?: 0) else 0
            prefs[Keys.NOTIFICATIONS_EPOCH_DAY] = today
            prefs[Keys.NOTIFICATIONS_SENT_TODAY] = current + 1
        }
    }

    suspend fun activateFourHourRejectionPause(nowMillis: Long = System.currentTimeMillis()) {
        val fourHoursMillis = 4L * 60L * 60L * 1000L
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.PAUSED_UNTIL_MILLIS] = nowMillis + fourHoursMillis
        }
    }

    suspend fun clearRejectionPause() {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.PAUSED_UNTIL_MILLIS] = 0L
        }
    }

    suspend fun scheduleTwentyMinuteReevaluation(nowMillis: Long = System.currentTimeMillis()) {
        val twentyMinMillis = 20L * 60L * 1000L
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.REEVAL_DUE_AT_MILLIS] = nowMillis + twentyMinMillis
        }
    }

    suspend fun clearTwentyMinuteReevaluation() {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.REEVAL_DUE_AT_MILLIS] = 0L
        }
    }

    suspend fun setOptionalGpsEnabled(enabled: Boolean) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.OPTIONAL_GPS_ENABLED] = enabled
            if (!enabled) {
                prefs[Keys.ACTIVE_ZONE_ID] = -1L
            }
        }
    }

    suspend fun setActiveZoneId(zoneId: Long?) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_ZONE_ID] = zoneId ?: -1L
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setDiscreetModeEnabled(enabled: Boolean) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.DISCREET_MODE_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setLargeTextEnabled(enabled: Boolean) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.LARGE_TEXT_ENABLED] = enabled
        }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.THEME_PREF] = theme.name
        }
    }

    suspend fun updatePersonalOrientation(
        motto: String,
        goal: ProgramGoalType,
        week: Int
    ) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.PERSONAL_MOTTO] = motto.trim().ifBlank {
                "Orientarme sin juzgarme; elegir mi próximo paso."
            }
            prefs[Keys.PROGRAM_GOAL] = goal.name
            prefs[Keys.PROGRAM_WEEK] = week.coerceIn(1, 12)
        }
    }

    suspend fun updateNurseProfile(profile: NurseNavigatorProfile) {
        context.brujulaDataStore.edit { prefs ->
            prefs[Keys.NURSE_PROFILE_JSON] = json.encodeToString(profile)
        }
    }

    suspend fun resetAllPreferences() {
        context.brujulaDataStore.edit { it.clear() }
    }
}
