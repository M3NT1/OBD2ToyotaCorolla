package com.obd2corolla.domain.usecase

import com.obd2corolla.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateHiddenSettingUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(key: String, value: String) {
        repository.updateHiddenSetting(key, value)
    }
}