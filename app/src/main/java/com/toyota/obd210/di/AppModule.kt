package com.toyota.obd210.di

import android.content.Context
import com.toyota.obd210.data.obd.BluetoothConnector
import com.toyota.obd210.data.obd.Elm327Protocol
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideBluetoothConnector(
        @ApplicationContext context: Context
    ): BluetoothConnector {
        return BluetoothConnector(context)
    }
    
    @Provides
    @Singleton
    fun provideElm327Protocol(): Elm327Protocol {
        return Elm327Protocol()
    }
}