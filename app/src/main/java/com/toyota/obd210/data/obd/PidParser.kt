package com.toyota.obd210.data.obd

import com.toyota.obd210.data.obd.model.ObdResponse
import com.toyota.obd210.data.obd.model.ToyotaE210Pids
import com.toyota.obd210.domain.model.LiveSensorData

/**
 * Parser for OBD2 PID responses
 * Decodes raw ELM327 responses into meaningful sensor values
 */
object PidParser {
    
    /**
     * Parse OBD response and return value
     */
    fun parseResponse(response: ObdResponse): Float? {
        if (!response.success) return null
        
        val data = response.data.replace(" ", "").replace("\r", "").replace("\n", "")
        
        // Check for error responses
        if (data.contains("ERROR") || data.contains("NO DATA") || data.contains("?")) {
            return null
        }
        
        // Find data bytes (after mode and PID echo)
        val hexPattern = Regex("[0-9A-Fa-f]{2,}")
        val matches = hexPattern.findAll(data)
        
        val bytes = matches.map { it.value }.toList()
        if (bytes.size < 3) return null
        
        // Skip header bytes (first 2 after mode)
        val dataBytes = bytes.drop(2)
        
        return try {
            when (dataBytes.size) {
                1 -> dataBytes[0].toInt(16).toFloat()
                2 -> ((dataBytes[0].toInt(16) * 256) + dataBytes[1].toInt(16)).toFloat()
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse speed (PID 010D) - km/h
     */
    fun parseSpeed(response: ObdResponse): Float {
        return parseResponse(response) ?: 0f
    }
    
    /**
     * Parse RPM (PID 010C) - rpm/4
     */
    fun parseRpm(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return value / 4f
    }
    
    /**
     * Parse coolant temperature (PID 0105) - °C = A-40
     */
    fun parseCoolantTemp(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return value - 40f
    }
    
    /**
     * Parse throttle position (PID 0111) - % = A*100/255
     */
    fun parseThrottlePosition(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return (value * 100f) / 255f
    }
    
    /**
     * Parse fuel level (PID 012F) - % = A*100/255
     */
    fun parseFuelLevel(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return (value * 100f) / 255f
    }
    
    /**
     * Parse intake temperature (PID 010F) - °C = A-40
     */
    fun parseIntakeTemp(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return value - 40f
    }
    
    /**
     * Parse engine load (PID 0104) - % = A*100/255
     */
    fun parseEngineLoad(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return (value * 100f) / 255f
    }
    
    /**
     * Parse timing advance (PID 010E) - ° = A/2 - 64
     */
    fun parseTimingAdvance(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return (value / 2f) - 64f
    }
    
    /**
     * Parse intake pressure (PID 010B) - kPa = A
     */
    fun parseIntakePressure(response: ObdResponse): Float {
        return parseResponse(response) ?: 0f
    }
    
    /**
     * Parse MAF rate (PID 0110) - g/s = (A*256 + B) / 100
     */
    fun parseMafRate(response: ObdResponse): Float {
        val data = response.data.replace(" ", "").replace("\r", "").replace("\n", "")
        val hexPattern = Regex("[0-9A-Fa-f]{2,}")
        val matches = hexPattern.findAll(data).toList()
        
        if (matches.size < 3) return 0f
        
        val bytes = matches.map { it.value }
        val dataBytes = bytes.drop(2)
        
        if (dataBytes.size < 2) return 0f
        
        return try {
            val a = dataBytes[0].toInt(16)
            val b = dataBytes[1].toInt(16)
            ((a * 256) + b) / 100f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Parse battery voltage (PID 0142) - V = (A*256 + B) / 1000
     */
    fun parseBatteryVoltage(response: ObdResponse): Float {
        val data = response.data.replace(" ", "").replace("\r", "").replace("\n", "")
        val hexPattern = Regex("[0-9A-Fa-f]{2,}")
        val matches = hexPattern.findAll(data).toList()
        
        if (matches.size < 3) return 0f
        
        val bytes = matches.map { it.value }
        val dataBytes = bytes.drop(2)
        
        if (dataBytes.size < 2) return 0f
        
        return try {
            val a = dataBytes[0].toInt(16)
            val b = dataBytes[1].toInt(16)
            ((a * 256) + b) / 1000f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Parse ambient temperature (PID 0146) - °C = A-40
     */
    fun parseAmbientTemp(response: ObdResponse): Float {
        val value = parseResponse(response) ?: return 0f
        return value - 40f
    }
    
    /**
     * Parse gear position (PID 0151)
     */
    fun parseGear(response: ObdResponse): Int {
        return parseResponse(response)?.toInt() ?: 0
    }
    
    /**
     * Parse DTC response
     */
    fun parseDTCs(response: ObdResponse): List<String> {
        if (!response.success) return emptyList()
        
        val data = response.data.replace(" ", "").replace("\r", "").replace("\n", "")
        if (data.contains("ERROR") || data.contains("NO DATA")) return emptyList()
        
        val hexPattern = Regex("[0-9A-Fa-f]{4}")
        return hexPattern.findAll(data).map { it.value.uppercase() }.toList()
    }
}