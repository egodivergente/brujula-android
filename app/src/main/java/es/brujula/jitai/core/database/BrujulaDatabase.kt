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
        GhbVoluntaryLogEntity::class,
        CopingSessionLogEntity::class,
        MilestoneReflectionEntity::class,
        JitaiDecisionEntity::class
    ],
    version = 1,
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
                    "brujula_local_v1.db"
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

    // 1. Evolución EMA realista (incluye ejemplo de deseo sexual alto sin craving -> cero alertas)
    val sampleCheckIns = listOf(
        EmaCheckInEntity(
            timestampMillis = now - 4 * day,
            mood = 6,
            craving = 5,
            sexualDesire = 6,
            perceivedControl = 6,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.MEPHEDRONE.name,
            personalNote = "Semana densa de trabajo; cansancio al salir.",
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
            personalNote = "Cita tranquila; deseo presente y buen timón personal.",
            isReevaluation = false
        ),
        EmaCheckInEntity(
            timestampMillis = now - 2 * day,
            mood = 5,
            craving = 6,
            sexualDesire = 5,
            perceivedControl = 5,
            availableForSupport = true,
            isDriving = false,
            substanceTag = SubstanceContextTag.METHAMPHETAMINE.name,
            personalNote = "Viernes tarde; activé plan alternativo con paseo.",
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
            personalNote = "Descansado y con margen amplio para hoy.",
            isReevaluation = false
        )
    )
    sampleCheckIns.forEach { dao.insertCheckIn(it) }

    // 2. Zonas personales configurables (procesamiento exclusivamente en el dispositivo)
    val sampleZones = listOf(
        PersonalZoneEntity(
            name = "Parque / Paseo de despeje",
            category = ZoneCategory.GREEN.name,
            relativeX = -0.42f,
            relativeY = -0.35f,
            latitude = 40.4153,
            longitude = -3.6845,
            radiusMeters = 320,
            anchorReminder = "Caminar 15 minutos sin auriculares me devuelve calma.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Casa de Dani · Red de confianza",
            category = ZoneCategory.GREEN.name,
            relativeX = 0.38f,
            relativeY = -0.22f,
            latitude = 40.4220,
            longitude = -3.6980,
            radiusMeters = 220,
            anchorReminder = "Espacio donde puedo hablar sin filtros ni juicios.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Zona de ocio nocturno · Viernes",
            category = ZoneCategory.YELLOW.name,
            relativeX = 0.14f,
            relativeY = 0.28f,
            latitude = 40.4202,
            longitude = -3.7058,
            radiusMeters = 300,
            anchorReminder = "Si paso por aquí cansado, conviene hacer un check-in breve.",
            enabled = true
        ),
        PersonalZoneEntity(
            name = "Entorno habitual de sesión larga",
            category = ZoneCategory.RED.name,
            relativeX = -0.28f,
            relativeY = 0.46f,
            latitude = 40.4095,
            longitude = -3.7012,
            radiusMeters = 240,
            anchorReminder = "Recuerda tu meta de descansar el domingo; puedes pedir apoyo.",
            enabled = true
        )
    )
    sampleZones.forEach { dao.upsertZone(it) }

    // 3. Planificación flexible Mi día (actividades y alternativas ante ventanas vulnerables)
    val samplePlan = listOf(
        DayPlanItemEntity(
            title = "Entrenamiento suave e hidratación",
            timeBlock = DayTimeBlock.MORNING.name,
            timeLabel = "10:30 h",
            isVulnerableWindow = false,
            chosenAlternative = "Estiramientos de 10 min en casa si llueve",
            completed = true
        ),
        DayPlanItemEntity(
            title = "Revisión rápida de rumbo personal",
            timeBlock = DayTimeBlock.AFTERNOON.name,
            timeLabel = "18:30 h",
            isVulnerableWindow = false,
            chosenAlternative = "Completar Check-in EMA de 20 segundos",
            completed = false
        ),
        DayPlanItemEntity(
            title = "Salida del trabajo con fatiga acumulada",
            timeBlock = DayTimeBlock.NIGHT.name,
            timeLabel = "22:00 – 00:30 h",
            isVulnerableWindow = true,
            chosenAlternative = "Ducha templada, cenar caliente y escribir a Dani antes de abrir apps",
            completed = false
        ),
        DayPlanItemEntity(
            title = "Insomnio o notificaciones de madrugada",
            timeBlock = DayTimeBlock.LATE_NIGHT.name,
            timeLabel = "01:30 – 04:00 h",
            isVulnerableWindow = true,
            chosenAlternative = "Modo silencio 30 min + respiración 4-2-6 + cine tranquilo en salón",
            completed = false
        )
    )
    samplePlan.forEach { dao.upsertDayPlanItem(it) }

    // 4. Práctica previa de habilidad y evaluación basal Semana 0
    dao.insertCopingSession(
        CopingSessionLogEntity(
            timestampMillis = now - 2 * day + hour,
            toolId = "physiological_sigh",
            toolTitle = "Respiración fisiológica 4-2-6",
            durationSeconds = 180,
            helpfulRating = 5
        )
    )

    dao.upsertMilestoneReflection(
        MilestoneReflectionEntity(
            checkpointWeek = 0,
            timestampMillis = now - 21 * day,
            daysUsedLastMonth = 6,
            lossOfControlEpisodes = 3,
            wellbeingScore = 5,
            goalAttainmentScore = 5,
            reflectionNote = "Inicio pactado con enfermería: priorizar sueño, hidratación y reducir sesiones largas."
        )
    )

    dao.insertJitaiDecision(
        JitaiDecisionEntity(
            timestampMillis = now - 5 * hour,
            triggerSource = JitaiTriggerSource.EMA_COMPLETED.name,
            actionKind = JitaiActionKind.NONE.name,
            headline = "Rumbo estable registrado",
            explanation = "Craving 2/10 y control percibido 8/10: no se activa intervención automática; las herramientas siguen abiertas manualmente.",
            ruleTrace = "Regla v1: craving < 7 y control > 3 · Deseo sexual evaluado por separado sin activar avisos.",
            wasNotified = false,
            userResponse = "REVISADO"
        )
    )
}
