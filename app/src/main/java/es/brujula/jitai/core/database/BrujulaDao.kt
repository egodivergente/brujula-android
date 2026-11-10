package es.brujula.jitai.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BrujulaDao {
    @Query("SELECT * FROM ema_checkins ORDER BY timestampMillis DESC")
    fun observeCheckIns(): Flow<List<EmaCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(entity: EmaCheckInEntity): Long

    @Query("SELECT COUNT(*) FROM ema_checkins")
    suspend fun countCheckIns(): Int

    @Query("SELECT * FROM personal_zones ORDER BY id ASC")
    fun observeZones(): Flow<List<PersonalZoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertZone(entity: PersonalZoneEntity): Long

    @Query("DELETE FROM personal_zones WHERE id = :zoneId")
    suspend fun deleteZone(zoneId: Long)

    @Query("SELECT * FROM day_plan_items ORDER BY id ASC")
    fun observeDayPlan(): Flow<List<DayPlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayPlanItem(entity: DayPlanItemEntity): Long

    @Update
    suspend fun updateDayPlanItem(entity: DayPlanItemEntity)

    @Query("DELETE FROM day_plan_items WHERE id = :itemId")
    suspend fun deleteDayPlanItem(itemId: Long)

    @Query("SELECT * FROM coping_sessions ORDER BY timestampMillis DESC")
    fun observeCopingSessions(): Flow<List<CopingSessionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCopingSession(entity: CopingSessionLogEntity): Long

    @Query("SELECT * FROM milestone_reflections ORDER BY checkpointWeek ASC, timestampMillis DESC")
    fun observeMilestoneReflections(): Flow<List<MilestoneReflectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestoneReflection(entity: MilestoneReflectionEntity): Long

    @Query("SELECT * FROM jitai_decisions ORDER BY timestampMillis DESC LIMIT 40")
    fun observeJitaiDecisions(): Flow<List<JitaiDecisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJitaiDecision(entity: JitaiDecisionEntity): Long

    @Query("UPDATE jitai_decisions SET userResponse = :response WHERE id = :decisionId")
    suspend fun updateDecisionResponse(decisionId: Long, response: String)

    @Query("DELETE FROM ema_checkins")
    suspend fun clearCheckIns()

    @Query("DELETE FROM personal_zones")
    suspend fun clearZones()

    @Query("DELETE FROM day_plan_items")
    suspend fun clearDayPlan()

    @Query("DELETE FROM coping_sessions")
    suspend fun clearCopingSessions()

    @Query("DELETE FROM milestone_reflections")
    suspend fun clearMilestones()

    @Query("DELETE FROM jitai_decisions")
    suspend fun clearDecisions()
}
