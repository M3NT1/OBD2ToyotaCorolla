package com.obd2corolla.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_cache")
data class SensorCacheEntity(
    @PrimaryKey
    val pid: String,
    val name: String,
    val value: Float,
    val unit: String,
    val timestamp: Long,
    val minValue: Float = 0f,
    val maxValue: Float = 100f
)