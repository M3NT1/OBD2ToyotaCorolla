package com.obd2corolla.domain.model

enum class SensorType {
    STANDARD,
    TOYOTA_EXTENDED,
    HYBRID,
    TPMS
}

data class SensorData(
    val pid: String,
    val name: String,
    val value: Float,
    val unit: String,
    val timestamp: Long,
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val sensorType: SensorType = SensorType.STANDARD,
    val address: String = "",
    val isWriteable: Boolean = false
)