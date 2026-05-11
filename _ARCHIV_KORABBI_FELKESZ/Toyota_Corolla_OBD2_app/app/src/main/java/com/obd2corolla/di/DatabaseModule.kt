package com.obd2corolla.di

import android.content.Context
import androidx.room.Room
import com.obd2corolla.data.local.OBD2Database
import com.obd2corolla.data.local.dao.DtcDao
import com.obd2corolla.data.local.dao.SensorCacheDao
import com.obd2corolla.data.local.dao.SettingsLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OBD2Database {
        return Room.databaseBuilder(
            context,
            OBD2Database::class.java,
            "obd2_database"
        ).build()
    }

    @Provides
    fun provideDtcDao(database: OBD2Database): DtcDao {
        return database.dtcDao()
    }

    @Provides
    fun provideSensorCacheDao(database: OBD2Database): SensorCacheDao {
        return database.sensorCacheDao()
    }

    @Provides
    fun provideSettingsLogDao(database: OBD2Database): SettingsLogDao {
        return database.settingsLogDao()
    }
}