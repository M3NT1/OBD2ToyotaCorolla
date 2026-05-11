package com.obd2corolla.domain.usecase

import com.obd2corolla.domain.model.HiddenSetting
import com.obd2corolla.domain.repository.SettingsRepository
import javax.inject.Inject

class GetHiddenSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): List<HiddenSetting> {
        return repository.getHiddenSettings()
    }
}