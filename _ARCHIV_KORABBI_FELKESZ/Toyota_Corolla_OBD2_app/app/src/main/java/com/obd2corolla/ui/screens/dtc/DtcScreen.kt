package com.obd2corolla.ui.screens.dtc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obd2corolla.data.remote.BluetoothManager.ConnectionState
import com.obd2corolla.domain.model.DtcCode
import com.obd2corolla.domain.model.DtcSeverity
import com.obd2corolla.domain.model.SensorData
import com.obd2corolla.ui.theme.ErrorRed
import com.obd2corolla.ui.theme.InfoBlue
import com.obd2corolla.ui.theme.SuccessGreen
import com.obd2corolla.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DtcScreen(
    viewModel: DtcViewModel = hiltViewModel()
) {
    val allDTCs by viewModel.allDTCs.collectAsState()
    val criticalDTCs by viewModel.criticalDTCs.collectAsState()
    val pendingDTCs by viewModel.pendingDTCs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showClearDialog by viewModel.showClearDialog.collectAsState()
    val selectedDtc by viewModel.selectedDtc.collectAsState()
    val showFreezeFrame by viewModel.showFreezeFrame.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val freezeFrameData by viewModel.freezeFrameData.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    Text(
                        text = "Diagnostic Codes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.showClearDialog() },
                        enabled = connectionState == ConnectionState.CONNECTED && allDTCs.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear DTCs"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
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
            ConnectionStatusChip(connectionState = connectionState)

            TabRow(
                selectedTab = selectedTab,
                allCount = allDTCs.size,
                criticalCount = criticalDTCs.size,
                pendingCount = pendingDTCs.size,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && allDTCs.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    allDTCs.isEmpty() -> {
                        EmptyState()
                    }
                    else -> {
                        val displayDTCs = when (selectedTab) {
                            0 -> allDTCs
                            1 -> criticalDTCs
                            2 -> pendingDTCs
                            else -> allDTCs
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayDTCs) { dtc ->
                                DtcCard(
                                    dtc = dtc,
                                    onClick = { viewModel.selectDtcForFreezeFrame(dtc) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showClearDialog) {
            ClearDtcDialog(
                onConfirm = { viewModel.clearDTCs() },
                onDismiss = { viewModel.dismissClearDialog() }
            )
        }

        if (showFreezeFrame && selectedDtc != null) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissFreezeFrame() },
                sheetState = sheetState
            ) {
                FreezeFrameContent(
                    dtcCode = selectedDtc!!.code,
                    sensors = freezeFrameData,
                    onClose = { viewModel.dismissFreezeFrame() }
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusChip(connectionState: ConnectionState) {
    val (color, text) = when (connectionState) {
        ConnectionState.CONNECTED -> SuccessGreen to "Connected"
        ConnectionState.CONNECTING -> WarningOrange to "Connecting..."
        ConnectionState.DISCONNECTED -> Color.Gray to "Disconnected"
        ConnectionState.ERROR -> ErrorRed to "Error"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TabRow(
    selectedTab: Int,
    allCount: Int,
    criticalCount: Int,
    pendingCount: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("All ($allCount)") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        FilterChip(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Critical ($criticalCount)") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ErrorRed.copy(alpha = 0.2f)
            )
        )
        
        FilterChip(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Pending ($pendingCount)") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = InfoBlue.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun DtcCard(
    dtc: DtcCode,
    onClick: () -> Unit
) {
    val severityColor = when (dtc.severity) {
        DtcSeverity.INFO -> InfoBlue
        DtcSeverity.WARNING -> WarningOrange
        DtcSeverity.CRITICAL -> ErrorRed
    }

    val severityBadgeColor = when (dtc.severity) {
        DtcSeverity.INFO -> InfoBlue
        DtcSeverity.WARNING -> WarningOrange
        DtcSeverity.CRITICAL -> ErrorRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(severityColor)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dtc.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = severityBadgeColor),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = dtc.severity.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dtc.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Tap for freeze frame",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No DTC codes found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your vehicle is running smoothly!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ClearDtcDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear DTC Codes?") },
        text = {
            Text("This will reset all stored diagnostic codes and turn off the MIL indicator. Are you sure?")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Clear Codes",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun FreezeFrameContent(
    dtcCode: String,
    sensors: Map<String, SensorData>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Freeze Frame — $dtcCode",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sensor readings at time of fault",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sensorList = listOf(
                "010C" to "Engine RPM",
                "010D" to "Vehicle Speed",
                "0105" to "Coolant Temperature",
                "010F" to "Intake Air Temperature",
                "0111" to "Throttle Position",
                "0104" to "Engine Load",
                "0106" to "Short Term Fuel Trim B1",
                "0108" to "Long Term Fuel Trim B1",
                "0142" to "Control Module Voltage",
                "010E" to "Timing Advance",
                "0110" to "MAF Rate",
                "0143" to "Absolute Load",
                "012F" to "Fuel Tank Level",
                "0123" to "Intake Manifold Pressure"
            )
            
            items(sensorList) { (pid, name) ->
                val sensor = sensors[pid]
                FreezeFrameRow(
                    name = name,
                    value = sensor?.value,
                    unit = sensor?.unit ?: ""
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun FreezeFrameRow(
    name: String,
    value: Float?,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = if (value != null) {
                val displayValue = if (value == value.toInt().toFloat()) {
                    value.toInt().toString()
                } else {
                    String.format("%.1f", value)
                }
                "$displayValue $unit"
            } else {
                "—"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
