package com.toyota.obd210.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toyota.obd210.data.obd.model.ConnectionState
import com.toyota.obd210.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Connection Status Card
        ConnectionStatusCard(
            connectionState = connectionState,
            adapterVoltage = uiState.adapterVoltage,
            onConnectClick = { viewModel.showDevicePicker() },
            onDisconnectClick = { viewModel.disconnect() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Speed Gauge
        SpeedGaugeCard(
            speed = uiState.speed,
            rpm = uiState.rpm
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Engine Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                modifier = Modifier.weight(1f),
                title = "Coolant",
                value = "${uiState.coolantTemp.toInt()}",
                unit = "°C",
                icon = Icons.Default.Thermostat
            )
            SensorCard(
                modifier = Modifier.weight(1f),
                title = "Throttle",
                value = "${uiState.throttlePosition.toInt()}",
                unit = "%",
                icon = Icons.Default.Speed
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Fuel & Load Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorCard(
                modifier = Modifier.weight(1f),
                title = "Fuel",
                value = "${uiState.fuelLevel.toInt()}",
                unit = "%",
                icon = Icons.Default.LocalGasStation
            )
            SensorCard(
                modifier = Modifier.weight(1f),
                title = "Load",
                value = "${uiState.engineLoad.toInt()}",
                unit = "%",
                icon = Icons.Default.TrendingUp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Gear Position
        SensorCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Gear",
            value = uiState.gear.toString(),
            unit = "",
            icon = Icons.Default.Settings
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Hybrid Section (if available)
        if (uiState.hybridBatterySoc > 0) {
            HybridStatusCard(
                batterySoc = uiState.hybridBatterySoc,
                evModeActive = uiState.evModeActive,
                motorTorque = uiState.motorTorque
            )
        }
    }
    
    // Device Picker Dialog
    if (uiState.showDevicePicker) {
        DevicePickerDialog(
            devices = uiState.availableDevices,
            onDeviceSelected = { viewModel.connectToDevice(it) },
            onDismiss = { viewModel.hideDevicePicker() }
        )
    }
}

@Composable
fun ConnectionStatusCard(
    connectionState: ConnectionState,
    adapterVoltage: Float,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (connectionState) {
                        ConnectionState.CONNECTED -> "Connected"
                        ConnectionState.CONNECTING -> "Connecting..."
                        ConnectionState.ERROR -> "Error"
                        ConnectionState.DISCONNECTED -> "Disconnected"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (connectionState) {
                        ConnectionState.CONNECTED -> Connected
                        ConnectionState.CONNECTING -> Connecting
                        ConnectionState.ERROR -> Error
                        ConnectionState.DISCONNECTED -> Disconnected
                    }
                )
                if (connectionState == ConnectionState.CONNECTED && adapterVoltage > 0) {
                    Text(
                        text = "Adapter: ${String.format("%.1f", adapterVoltage)}V",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Button(
                onClick = {
                    when (connectionState) {
                        ConnectionState.CONNECTED -> onDisconnectClick()
                        else -> onConnectClick()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connectionState == ConnectionState.CONNECTED) Error else Primary
                )
            ) {
                Icon(
                    imageVector = if (connectionState == ConnectionState.CONNECTED) 
                        Icons.Default.BluetoothDisabled else Icons.Default.Bluetooth,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (connectionState == ConnectionState.CONNECTED) "Disconnect" else "Connect"
                )
            }
        }
    }
}

@Composable
fun SpeedGaugeCard(
    speed: Float,
    rpm: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speed Display
            Text(
                text = speed.toInt().toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp,
                color = Primary
            )
            Text(
                text = "km/h",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // RPM Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RPM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = String.format("%.0f", rpm),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { (rpm / 6000f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    rpm > 5000 -> GaugeCritical
                    rpm > 4000 -> GaugeHigh
                    rpm > 3000 -> GaugeMedium
                    else -> GaugeLow
                },
                trackColor = SurfaceVariant,
            )
        }
    }
}

@Composable
fun SensorCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HybridStatusCard(
    batterySoc: Float,
    evModeActive: Boolean,
    motorTorque: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hybrid System",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                if (evModeActive) {
                    Surface(
                        color = Success,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "EV MODE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Battery SOC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Battery SOC",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${batterySoc.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { batterySoc / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    batterySoc < 20 -> GaugeCritical
                    batterySoc < 40 -> GaugeMedium
                    else -> Success
                },
                trackColor = SurfaceVariant,
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Motor Torque",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${motorTorque.toInt()} Nm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
        }
    }
}