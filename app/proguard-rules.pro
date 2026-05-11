# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep OBD2 command classes
-keep class com.toyota.obd210.data.obd.commands.** { *; }
-keep class com.toyota.obd210.data.obd.model.** { *; }

# Keep DTC descriptions
-keep class com.toyota.obd210.data.local.DtcDescriptions { *; }

# Keep Toyota E210 settings registry
-keep class com.toyota.obd210.data.local.ToyotaE210SettingsRegistry { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }