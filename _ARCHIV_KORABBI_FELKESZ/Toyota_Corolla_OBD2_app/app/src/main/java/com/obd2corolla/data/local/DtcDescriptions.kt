package com.obd2corolla.data.local

object DtcDescriptions {
    private val descriptions = mapOf(
        // === POWERTRAIN P0xxx ===
        "P0171" to "System too lean (Bank 1) — Air/fuel ratio sensor fault or vacuum leak (M20A-FXS hybrid common)",
        "P0172" to "System too rich (Bank 1) — Fuel injector leak or A/F sensor fault",
        "P0174" to "System too lean (Bank 2)",
        "P0175" to "System too rich (Bank 2)",
        "P0300" to "Random/Multiple cylinder misfire detected — Check ignition, fuel supply, compression",
        "P0301" to "Cylinder 1 misfire detected",
        "P0302" to "Cylinder 2 misfire detected",
        "P0303" to "Cylinder 3 misfire detected",
        "P0304" to "Cylinder 4 misfire detected",
        "P0325" to "Knock sensor 1 circuit (Bank 1) — Check KS wiring and ground",
        "P0335" to "Crankshaft position sensor A circuit — Check CKP sensor and wiring",
        "P0340" to "Camshaft position sensor circuit (Bank 1) — Check CMP sensor",
        "P0420" to "Catalyst system efficiency below threshold (Bank 1) — Common on E210 hybrid",
        "P0440" to "EVAP system malfunction",
        "P0441" to "EVAP system incorrect purge flow",
        "P0442" to "EVAP system leak detected (small leak)",
        "P0455" to "EVAP system leak detected (large leak)",
        "P0456" to "EVAP system leak detected (very small leak)",
        "P0500" to "Vehicle speed sensor malfunction — Check VSS signal",
        "P0504" to "Brake switch A/B correlation",
        "P0562" to "System voltage low — Check alternator and battery",
        "P0563" to "System voltage unstable — Check ground connections and alternator",
        "P0606" to "ECM/PCM processor fault — May require ECM replacement",
        "P06DA" to "Engine oil pressure sensor circuit",
        "P0A00" to "Engine coolant temperature sensor circuit",
        
        // === HYBRID SPECIFIC P0xxx ===
        "P0A7A" to "Hybrid battery pack deterioration — HV battery aging (E210 hybrid specific)",
        "P0A7F" to "HV battery SOC low — Charge hybrid battery",
        "P0A90" to "Drive motor A performance (MG2 issue) — Check MG2 inverter",
        "P0A91" to "Motor electronics overheating — Check MG1/MG2 coolant",
        "P0A92" to "Hybrid battery temperature sensor circuit",
        "P0A93" to "Hybrid battery fan system",
        "P0A94" to "HV battery junction circuit",
        "P0C4F" to "Drive Motor A/Torque Converter Clamping Circuit — MG2 torque sensor",
        "P0C50" to "Drive Motor B/Torque Converter Clamping Circuit — MG1 torque sensor",
        "P0D0F" to "Hybrid battery preconditioning",
        
        // === TOYOTA SPECIFIC P1xxx ===
        "P1135" to "Air fuel ratio sensor heater circuit (Bank 1) — A/F sensor heater fault",
        "P1155" to "Air fuel ratio sensor heater circuit (Bank 2)",
        "P1410" to "Air pump circuit — Check air injection system",
        "P1411" to "Air pump performance",
        "P1505" to "IACV opening angle circuit failure — Check idle air control valve",
        "P1510" to "Cold start air control system — Check AISC solenoid",
        "P1600" to "ECM battery voltage loss — Check ECU power supply",
        "P1602" to "Immobilizer system communication fault",
        
        // === BODY B1xxx ===
        "B0100" to "Driver seat belt buckle switch circuit",
        "B0101" to "Passenger seat belt buckle switch circuit",
        "B0102" to "Seat belt pretensioner circuit (driver)",
        "B0103" to "Seat belt pretensioner circuit (passenger)",
        "B1242" to "Multiplex communication circuit — LIN bus fault",
        "B1421" to "Alternator charging system fault — Check charging circuit",
        "B1800" to "Driver airbag circuit open",
        "B1801" to "Driver airbag circuit short",
        "B1805" to "Passenger airbag circuit open",
        "B1806" to "Passenger airbag circuit short",
        
        // === CHASSIS C0xxx ===
        "C0200" to "ABS actuator motor circuit",
        "C0205" to "ABS pump motor circuit",
        "C1225" to "Left front wheel speed sensor circuit",
        "C1226" to "Right front wheel speed sensor circuit",
        "C1235" to "Left rear wheel speed sensor circuit",
        "C1236" to "Right rear wheel speed sensor circuit",
        "C1242" to "Brake fluid level circuit",
        "C1300" to "VSC system malfunction — Check VSC sensors and pump",
        "C1340" to "ABS/EBD system malfunction",
        
        // === NETWORK U0xxx ===
        "U0073" to "CAN communication bus A (HS) — Check CAN harness",
        "U0100" to "Lost communication with ECM/PCM — Check ECM communication",
        "U0121" to "Lost communication with ABS/VSC — Check ABS ECU",
        "U0131" to "Lost communication with steering angle sensor",
        "U0140" to "Lost communication with body ECU",
        "U0155" to "Lost communication with hybrid battery ECU",
        "U0235" to "Lost communication with cruise control module",
        "U1000" to "CAN communication system — Check wiring and terminals"
    )
    
    fun getDescription(code: String): String {
        return descriptions[code.uppercase()] ?: generateGenericDescription(code)
    }
    
    private fun generateGenericDescription(code: String): String {
        return when {
            code.startsWith("P0") -> "Powertrain fault: $code — Check engine component"
            code.startsWith("P1") -> "Manufacturer-specific powertrain fault: $code"
            code.startsWith("P2") -> "Standard powertrain fault: $code"
            code.startsWith("B0") -> "Body system fault: $code — Check body control module"
            code.startsWith("B1") -> "Body system fault: $code — Check airbag/seatbelt system"
            code.startsWith("C0") -> "Chassis fault: $code — Check ABS/brake system"
            code.startsWith("U0") -> "Network communication fault: $code — Check CAN bus"
            else -> "Unknown fault code: $code"
        }
    }
}