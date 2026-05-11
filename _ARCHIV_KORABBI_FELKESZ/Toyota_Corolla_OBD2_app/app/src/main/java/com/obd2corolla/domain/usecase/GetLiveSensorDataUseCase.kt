package com.obd2corolla.domain.usecase

import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.domain.repository.ObdRepository
import javax.inject.Inject

class GetLiveSensorDataUseCase @Inject constructor(
    private val repository: ObdRepository
) {
    suspend operator fun invoke(): List<SensorData> {
        return repository.getSensorData()
    }
}