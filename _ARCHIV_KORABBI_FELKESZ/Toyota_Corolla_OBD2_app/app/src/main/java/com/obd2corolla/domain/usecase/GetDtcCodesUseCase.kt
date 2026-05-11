package com.obd2corolla.domain.usecase

import com.obd2corolla.domain.model.DtcCode
import com.obd2corolla.domain.repository.ObdRepository
import javax.inject.Inject

class GetDtcCodesUseCase @Inject constructor(
    private val repository: ObdRepository
) {
    suspend operator fun invoke(): List<DtcCode> {
        return repository.getDTCs()
    }
}