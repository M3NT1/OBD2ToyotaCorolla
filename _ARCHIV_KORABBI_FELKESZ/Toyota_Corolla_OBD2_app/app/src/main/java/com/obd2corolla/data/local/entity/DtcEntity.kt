package com.obd2corolla.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dtc_codes")
data class DtcEntity(
    @PrimaryKey
    val code: String,
    val description: String,
    val severity: String,
    val timestamp: Long
)