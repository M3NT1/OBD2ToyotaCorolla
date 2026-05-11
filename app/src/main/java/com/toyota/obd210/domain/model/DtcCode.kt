package com.toyota.obd210.domain.model

data class DtcCode(
    val code: String,
    val description: String,
    val system: DtcSystem,
    val severity: DtcSeverity,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DtcSystem {
    ENGINE,
    TRANSMISSION,
    HYBRID,
    BRAKE,
    AIRBAG,
    LIGHTING,
    BODY,
    UNKNOWN
}

enum class DtcSeverity {
    INFO,
    WARNING,
    CRITICAL
}