package com.toyota.obd210.domain.model

data class SensorData(
    val pid: String,
    val name: String,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LiveSensorData(
    val speed: Float = 0f,
    val rpm: Float = 0f,
    val coolantTemp: Float = 0f,
    val fuelLevel: Float = 0f,
    val throttlePosition: Float = 0f,
    val engineLoad: Float = 0f,
    val intakeTemp: Float = 0f,
    val intakePressure: Float = 0f,
    val timingAdvance: Float = 0f,
    val mafRate: Float = 0f,
    val runTime: Int = 0,
    val fuelPressure: Float = 0f,
    val batteryVoltage: Float = 0f,
    val ambientTemp: Float = 0f,
    val gear: Int = 0,
    // Hybrid specific
    val hybridBatterySoc: Float = 0f,
    val motorTorque: Float = 0f,
    val hybridBatteryCurrent: Float = 0f,
    val hybridBatteryVoltage: Float = 0f,
    val evModeActive: Boolean = false
)