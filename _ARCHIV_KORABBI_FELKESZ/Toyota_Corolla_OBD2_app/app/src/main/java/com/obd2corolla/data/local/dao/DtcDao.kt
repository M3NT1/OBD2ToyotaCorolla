package com.obd2corolla.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.obd2corolla.data.local.entity.DtcEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DtcDao {
    @Query("SELECT * FROM dtc_codes ORDER BY timestamp DESC")
    fun getAllDTCs(): Flow<List<DtcEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDTC(dtc: DtcEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dtcs: List<DtcEntity>)

    @Query("DELETE FROM dtc_codes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM dtc_codes")
    suspend fun getCount(): Int
}