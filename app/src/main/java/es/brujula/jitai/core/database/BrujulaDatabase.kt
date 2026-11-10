package es.brujula.jitai.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import es.brujula.jitai.core.model.DayTimeBlock
import es.brujula.jitai.core.model.JitaiActionKind
import es.brujula.jitai.core.model.JitaiTriggerSource
import es.brujula.jitai.core.model.SubstanceContextTag
import es.brujula.jitai.core.model.ZoneCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmaCheckInEntity::class,
        PersonalZoneEntity::class,
        DayPlanItemEntity::class,
        CopingSessionLogEntity::class,
        MilestoneReflectionEntity::class,
        JitaiDecisionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class BrujulaDatabase : RoomDatabase() {

    abstract fun brujulaDao(): BrujulaDao

    companion object {
        @Volatile
        private var INSTANCE: BrujulaDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): BrujulaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrujulaDatabase::class.java,
                    "brujula_local_v2.db"
                )
                    .addCallback(SeedCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                INSTANCE?.brujulaDao()?.let { dao ->
                    seedInitialDataIfEmpty(dao)
                }
            }
        }
    }
}

suspend fun seedInitialDataIfEmpty(dao: BrujulaDao) {
    if (dao.countCheckIns() > 0) return
    val now = System.currentTimeMillis()
    val hour = 3_600_000L
    val day = 24 * hour

    val sampleCheckIns = listOf(
        EmaCheckInEntity(
            timestampMillis = now - 4 * day,
            mood = 6,
            craving = 5,
            sexualDesire = 6,
            perceivedControl = 6,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.STIMULANTS.name,
            personalNote = "Tarde cansada tras el trabajo.",
            isReevaluation = false
        ),
        EmaCheckInEntity(
            timestampMillis = now - 3 * day,
            mood = 7,
            craving = 2,
            sexualDesire = 8,
            perceivedControl = 8,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.NONE.name,
            personalNote = "Noche tranquila y buen ánimo.",
            isReevaluation = false
        ),
        EmaCheckInEntity(
            timestampMillis = now - 2 * day,
            mood = 6,
            craving = 4,
            sexualDesire = 5,
            perceivedControl = 7,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.NONE.name,
            personalNote = "Salí a caminar sin pantallas.",
            isReevaluation = false
        ),
        EmaCheckInEntity(
            timestampMillis = now - 5 * hour,
            mood = 8,
            craving = 2,
            sexualDesire = 6,
            perceivedControl = 8,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.NONE.name,
            personalNote = "Descansado y en calma.",
            isReevaluation = false
        )
    )
    sampleCheckIns.forEach { dao.insertCheckIn(it) }

    val sampleZones = listOf(
        PersonalZoneEntity(
            name = "Paseo del parque",
            category = ZoneCategory.GREEN.name,
            relativeX = -0.42f,
            relativeY = -0.34f,
            latitude = 40.4153,
            longitude = -3.6845,
            radiusMeters = 320,
            anchorReminder = "Caminar despacio quince minutos me despeja.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Casa de Dani",
            category = ZoneCategory.GREEN.name,
            relativeX = 0.36f,
            relativeY = -0.22f,
            latitude = 40.4220,
            longitude = -3.6980,
            radiusMeters = 240,
            anchorReminder = "Un lugar tranquilo donde hablar sin juicio.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Zona de ocio nocturno",
            category = ZoneCategory.YELLOW.name,
            relativeX = 0.16f,
            relativeY = 0.28f,
            latitude = 40.4202,
            longitude = -3.7058,
            radiusMeters = 300,
            anchorReminder = "Parar un segundo y ver qué me apetece de verdad.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Entorno de madrugada",
            category = ZoneCategory.RED.name,
            relativeX = -0.28f,
            relativeY = 0.44f,
            latitude = 40.4095,
            longitude = -3.7012,
            radiusMeters = 250,
            anchorReminder = "Mañana quiero despertarme descansado.",
            enabled = true
        )
    )
    sampleZones.forEach { dao.upsertZone(it) }

    val samplePlan = listOf(
        DayPlanItemEntity(
            title = "Movimiento suave y desayuno tranquilo",
            timeBlock = DayTimeBlock.MORNING.name,
            timeLabel = "10:00",
            isVulnerableWindow = false,
            chosenAlternative = "Estirar diez minutos junto a la ventana",
            completed = true
        ),
        DayPlanItemEntity(
            title = "Caída de la tarde con cansancio",
            timeBlock = DayTimeBlock.NIGHT.name,
            timeLabel = "21:30",
            isVulnerableWindow = true,
            chosenAlternative = "Ducha templada, cena rica y escribir a Dani",
            completed = false
        ),
        DayPlanItemEntity(
            title = "Si me desvelo de madrugada",
            timeBlock = DayTimeBlock.LATE_NIGHT.name,
            timeLabel = "01:30",
            isVulnerableWindow = true,
            chosenAlternative = "Dejar el móvil fuera, respirar 4·2·6 y luz cálida",
            completed = false
        )
    )
    samplePlan.forEach { dao.upsertDayPlanItem(it) }

    dao.insertCopingSession(
        CopingSessionLogEntity(
            timestampMillis = now - 2 * day + hour,
            toolId = "physiological_sigh",
            toolTitle = "Suspiro suave 4 · 2 · 6",
            durationSeconds = 180,
            helpfulRating = 5
        )
    )

    dao.upsertMilestoneReflection(
        MilestoneReflectionEntity(
            checkpointWeek = 0,
            timestampMillis = now - 21 * day,
            daysUsedLastMonth = 5,
            lossOfControlEpisodes = 2,
            wellbeingScore = 6,
            goalAttainmentScore = 6,
            reflectionNote = "Quiero cuidar mi sueño y recuperar fines de semana tranquilos."
        )
    )

    dao.insertJitaiDecision(
        JitaiDecisionEntity(
            timestampMillis = now - 5 * hour,
            triggerSource = JitaiTriggerSource.EMA_COMPLETED.name,
            actionKind = JitaiActionKind.NONE.name,
            headline = "Aguas tranquilas",
            explanation = "Todo en calma; tus espacios de respiro siguen aquí cuando quieras.",
            ruleTrace = "Regla v1: parámetros dentro de margen personal · deseo sexual independiente.",
            wasNotified = false,
            userResponse = "REVISADO"
        )
    )
}
