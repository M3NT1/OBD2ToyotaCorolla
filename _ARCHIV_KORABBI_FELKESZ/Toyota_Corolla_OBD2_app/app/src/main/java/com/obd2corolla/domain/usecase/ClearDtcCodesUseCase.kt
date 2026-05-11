package com.obd2corolla.domain.usecase

import com.obd2corolla.domain.repository.ObdRepository
import javax.inject.Inject

class ClearDtcCodesUseCase @Inject constructor(
    private val repository: ObdRepository
) {
    suspend operator fun invoke() {
        repository.clearDTCs()
    }
}