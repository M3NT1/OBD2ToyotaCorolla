package com.obd2corolla.data.remote

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.obd2corolla.MainActivity
import com.obd2corolla.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foreground service to maintain OBD connection alive in background.
 * Shows persistent notification with connection status and disconnect action.
 */
class ObdConnectionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private var bluetoothManager: BluetoothManager? = null
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "ObdConnectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "OBD Device"
                val deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS) ?: ""
                startForegroundService(deviceName, deviceAddress)
            }
            ACTION_STOP -> {
                stopForegroundService()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService(deviceName: String, deviceAddress: String) {
        _isRunning.value = true
        
        val notification = createNotification(
            title = "OBD Connected",
            message = "Connected to $deviceName",
            isConnected = true
        )
        
        startForeground(NOTIFICATION_ID, notification)
        Log.d(TAG, "Started foreground service for $deviceName")
    }

    private fun stopForegroundService() {
        _isRunning.value = false
        
        // Disconnect from OBD if connected
        bluetoothManager?.let { manager ->
            try {
                kotlinx.coroutines.runBlocking {
                    manager.disconnect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Stopped foreground service")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.obd_connection_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.obd_connection_channel_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        title: String,
        message: String,
        isConnected: Boolean
    ): Notification {
        // Main tap action - open app
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Disconnect action
        val disconnectIntent = Intent(this, ObdConnectionService::class.java).apply {
            action = ACTION_STOP
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_bluetooth) // Will use default if not found
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (isConnected) {
            builder.addAction(
                R.drawable.ic_disconnect,
                getString(R.string.action_disconnect),
                disconnectPendingIntent
            )
        }

        return builder.build()
    }

    fun updateNotification(connectionState: BluetoothManager.ConnectionState, deviceName: String) {
        val (title, message, isConnected) = when (connectionState) {
            BluetoothManager.ConnectionState.CONNECTED -> 
                Triple("OBD Connected", "Connected to $deviceName", true)
            BluetoothManager.ConnectionState.CONNECTING -> 
                Triple("OBD Connecting", "Connecting to $deviceName...", false)
            BluetoothManager.ConnectionState.DISCONNECTED -> 
                Triple("OBD Disconnected", getString(R.string.obd_disconnected), false)
            BluetoothManager.ConnectionState.ERROR -> 
                Triple("OBD Error", "Connection error", false)
        }

        val notification = createNotification(title, message, isConnected)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _isRunning.value = false
        Log.d(TAG, "ObdConnectionService destroyed")
    }

    fun setBluetoothManager(manager: BluetoothManager) {
        this.bluetoothManager = manager
    }

    companion object {
        private const val TAG = "ObdConnectionService"
        const val CHANNEL_ID = "obd_connection"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.obd2corolla.ACTION_START_OBD_SERVICE"
        const val ACTION_STOP = "com.obd2corolla.ACTION_STOP_OBD_SERVICE"
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_DEVICE_ADDRESS = "device_address"

        fun startService(context: Context, deviceName: String, deviceAddress: String) {
            val intent = Intent(context, ObdConnectionService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DEVICE_NAME, deviceName)
                putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ObdConnectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}