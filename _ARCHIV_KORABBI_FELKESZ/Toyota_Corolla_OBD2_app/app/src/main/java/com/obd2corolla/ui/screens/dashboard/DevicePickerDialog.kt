package com.obd2corolla.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import com.obd2corolla.ui.theme.SuccessGreen
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val TAG = "DevicePickerDialog"

@Composable
fun DevicePickerDialog(
    bluetoothManager: BluetoothManager,
    onDeviceSelected: (BluetoothManager.BluetoothDevice) -> Unit,
    onDismiss: () -> Unit,
    viewModel: DashboardViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val connectionState by bluetoothManager.connectionState.collectAsState()
    var devices by remember { mutableStateOf<List<BluetoothManager.BluetoothDevice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDevice by remember { mutableStateOf<BluetoothManager.BluetoothDevice?>(null) }
    var elm327Version by remember { mutableStateOf<String?>(null) }

    val lastConnectedDevice = remember { viewModel.getLastConnectedDevice() }

    LaunchedEffect(Unit) {
        isLoading = true
        // Try BLE scan first, fall back to classic scan
        try {
            devices = bluetoothManager.scanForBleDevices()
        } catch (e: Exception) {
            Log.e(TAG, "BLE scan failed, trying classic scan", e)
            devices = bluetoothManager.scanForDevices()
        }
        isLoading = false
        lastConnectedDevice?.let { device ->
            selectedDevice = devices.find { it.address == device.address }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Bluetooth Device",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (lastConnectedDevice != null) {
                Text(
                    text = "Last connected: ${lastConnectedDevice.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (devices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No paired devices found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Please pair with an ELM327 adapter in Bluetooth settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(devices) { device ->
                        DeviceItem(
                            device = device,
                            isSelected = selectedDevice?.address == device.address,
                            isConnected = connectionState == ConnectionState.CONNECTED &&
                                    selectedDevice?.address == device.address,
                            onSelect = {
                                selectedDevice = device
                                scope.launch {
                                    sheetState.hide()
                                    detectElm327Version(device) { version ->
                                        elm327Version = version
                                        onDeviceSelected(device)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (elm327Version != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ELM327 Version: $elm327Version",
                    style = MaterialTheme.typography.bodySmall,
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: BluetoothManager.BluetoothDevice,
    isSelected: Boolean,
    isConnected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isConnected) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private suspend fun detectElm327Version(
    device: BluetoothManager.BluetoothDevice,
    onVersionDetected: (String) -> Unit
) {
    // In a real implementation, this would send AT commands to detect ELM327 version
    // For now, we simulate version detection
    delay(500)
    onVersionDetected("v1.5")
}
