package com.obd2corolla.domain.model

data class DtcCode(
    val code: String,
    val description: String,
    val severity: DtcSeverity,
    val timestamp: Long
)

enum class DtcSeverity {
    INFO,
    WARNING,
    CRITICAL
}