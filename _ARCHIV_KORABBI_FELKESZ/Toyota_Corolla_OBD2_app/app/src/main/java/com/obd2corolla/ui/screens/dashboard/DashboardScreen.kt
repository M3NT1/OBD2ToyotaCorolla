package com.obd2corolla.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obd2corolla.data.remote.BluetoothManager
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import com.obd2corolla.ui.theme.ErrorRed
import com.obd2corolla.ui.theme.SuccessGreen
import com.obd2corolla.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    bluetoothManager: BluetoothManager,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val sensors by viewModel.sensors.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isSimulatedMode by viewModel.isSimulatedMode.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDevicePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (isSimulatedMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = WarningOrange.copy(alpha = 0.8f),
                                contentColor = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Simulated",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    ConnectionStatusChip(
                        connectionState = connectionState,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDevicePicker = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                BadgedBox(
                    badge = {
                        if (connectionState == ConnectionState.CONNECTED) {
                            Badge(
                                containerColor = SuccessGreen,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (connectionState == ConnectionState.CONNECTED) {
                            Icons.Default.BluetoothConnected
                        } else {
                            Icons.Default.Bluetooth
                        },
                        contentDescription = "Connect to device"
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sensors.chunked(2)) { rowSensors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowSensors.forEach { sensor ->
                            SensorCard(
                                sensor = sensor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowSensors.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    if (showDevicePicker) {
        DevicePickerDialog(
            bluetoothManager = bluetoothManager,
            onDeviceSelected = { device ->
                viewModel.connectToDevice(device)
                showDevicePicker = false
            },
            onDismiss = { showDevicePicker = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ConnectionStatusChip(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconColor, text) = when (connectionState) {
        ConnectionState.CONNECTED -> Triple(
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen,
            "Connected"
        )
        ConnectionState.CONNECTING -> Triple(
            WarningOrange.copy(alpha = 0.15f),
            WarningOrange,
            "Connecting..."
        )
        ConnectionState.DISCONNECTED -> Triple(
            Color.Gray.copy(alpha = 0.15f),
            Color.Gray,
            "Disconnected"
        )
        ConnectionState.ERROR -> Triple(
            ErrorRed.copy(alpha = 0.15f),
            ErrorRed,
            "Error"
        )
    }

    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(iconColor, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = iconColor
        )
    }
}