package com.obd2corolla.di

import com.obd2corolla.data.local.dao.DtcDao
import com.obd2corolla.data.local.dao.SensorCacheDao
import com.obd2corolla.data.local.dao.SettingsLogDao
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.BluetoothManagerImpl
import com.obd2corolla.data.remote.Elm327CommandParser
import com.obd2corolla.data.remote.HiddenSettingsService
import com.obd2corolla.data.repository.ObdRepositoryImpl
import com.obd2corolla.data.repository.SettingsRepositoryImpl
import com.obd2corolla.domain.repository.ObdRepository
import com.obd2corolla.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBluetoothManager(): BluetoothManager {
        return BluetoothManagerImpl()
    }

    @Provides
    @Singleton
    fun provideElm327Parser(): Elm327CommandParser {
        return Elm327CommandParser()
    }

    @Provides
    @Singleton
    fun provideObdRepository(
        bluetoothManager: BluetoothManager,
        sensorCacheDao: SensorCacheDao,
        dtcDao: DtcDao
    ): ObdRepository {
        return ObdRepositoryImpl(bluetoothManager, sensorCacheDao, dtcDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsLogDao: SettingsLogDao,
        hiddenSettingsService: HiddenSettingsService
    ): SettingsRepository {
        return SettingsRepositoryImpl(settingsLogDao, hiddenSettingsService)
    }
}
