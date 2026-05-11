package com.obd2corolla.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.obd2corolla.data.local.dao.DtcDao
import com.obd2corolla.data.local.dao.SensorCacheDao
import com.obd2corolla.data.local.dao.SettingsLogDao
import com.obd2corolla.data.local.entity.DtcEntity
import com.obd2corolla.data.local.entity.SensorCacheEntity
import com.obd2corolla.data.local.entity.SettingsLogEntity

@Database(
    entities = [DtcEntity::class, SensorCacheEntity::class, SettingsLogEntity::class],
    version = 1,
    exportSchema = true
)
abstract class OBD2Database : RoomDatabase() {
    abstract fun dtcDao(): DtcDao
    abstract fun sensorCacheDao(): SensorCacheDao
    abstract fun settingsLogDao(): SettingsLogDao
}