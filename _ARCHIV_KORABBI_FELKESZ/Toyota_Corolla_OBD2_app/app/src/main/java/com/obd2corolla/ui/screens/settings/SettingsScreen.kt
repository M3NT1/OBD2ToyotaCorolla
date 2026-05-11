package com.obd2corolla.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.obd2corolla.domain.model.HiddenSetting
import com.obd2corolla.ui.theme.ErrorRed

/**
 * Settings screen with Carista-style UI for Toyota hidden settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val settingsByCategory by viewModel.settingsByCategory.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val isV15Warning by viewModel.isV15Warning.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingUpdate by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.dismissError()
        }
    }

    // Show success snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissSuccess()
        }
    }

    // Confirmation dialog for setting update
    if (showConfirmDialog && pendingUpdate != null) {
        val (key, value) = pendingUpdate!!
        val setting = settings.find { it.key == key }
        val newValueLabel = setting?.options?.find { it.value == value }?.label ?: value.toString()
        
        AlertDialog(
            onDismissRequest = { 
                showConfirmDialog = false
                pendingUpdate = null 
            },
            title = { Text("Beállítás módosítása") },
            text = { 
                Text("Biztosan módosítod \"${setting?.name}\"?\n\nÚj érték: $newValueLabel") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmUpdate(key, value)
                        showConfirmDialog = false
                        pendingUpdate = null
                    }
                ) {
                    Text("Módosít")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        pendingUpdate = null
                    }
                ) {
                    Text("Mégse")
                }
            }
        )
    }

    // Reset confirmation dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Gyári beállítások visszaállítása") },
            text = { 
                Text("Biztosan visszaállítod az összes beállítást a gyári alapértelmezetre?") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("Visszaállít")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmDialog = false }
                ) {
                    Text("Mégse")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Beállítások",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Reset to defaults button
                    IconButton(onClick = { showResetConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Gyári beállítások"
                        )
                    }
                    // Connection status
                    ConnectionStateChip(connectionState = connectionState)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // v1.5 warning banner
            if (isV15Warning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "⚠️ Az adapter nem támogatja az írást!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = ErrorRed
                            )
                            Text(
                                text = "Szükséges: ELM327 v2.2+ (pl. OBDLink MX+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Content
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && settings.isEmpty() -> {
                        // Initial loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null && settings.isEmpty() -> {
                        // Error state with no data
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hiba a beállítások betöltése közben",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { viewModel.loadSettings() }) {
                                    Text("Újra")
                                }
                            }
                        }
                    }
                    else -> {
                        // Settings list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            settingsByCategory.forEach { (category, categorySettings) ->
                                item {
                                    CategoryHeader(category = category)
                                }
                                
                                items(
                                    items = categorySettings,
                                    key = { it.key }
                                ) { setting ->
                                    SettingItemCard(
                                        setting = setting,
                                        onValueChange = { newValue ->
                                            pendingUpdate = setting.key to newValue
                                            showConfirmDialog = true
                                        }
                                    )
                                }
                                
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Connection state chip shown in the app bar.
 */
@Composable
fun ConnectionStateChip(connectionState: com.obd2corolla.data.remote.BluetoothManager.ConnectionState) {
    val (color, text) = when (connectionState) {
        com.obd2corolla.data.remote.BluetoothManager.ConnectionState.CONNECTED -> 
            Color(0xFF4CAF50) to "Kapcsolódva"
        com.obd2corolla.data.remote.BluetoothManager.ConnectionState.CONNECTING -> 
            Color(0xFFFFC107) to "Kapcsolódás..."
        com.obd2corolla.data.remote.BluetoothManager.ConnectionState.DISCONNECTED -> 
            Color(0xFF9E9E9E) to "Nincs kapcsolat"
        com.obd2corolla.data.remote.BluetoothManager.ConnectionState.ERROR -> 
            ErrorRed to "Hiba"
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

/**
 * Category header with icon and title.
 */
@Composable
fun CategoryHeader(category: String) {
    val (icon, color) = getCategoryIconAndColor(category)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Single setting item card with Carista-style design.
 */
@Composable
fun SettingItemCard(
    setting: HiddenSetting,
    onValueChange: (Int) -> Unit
) {
    val (categoryIcon, categoryColor) = getCategoryIconAndColor(setting.category)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = setting.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = setting.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Current value / control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jelenlegi: ${setting.currentValueLabel()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Control based on setting type
                SettingControl(
                    setting = setting,
                    onValueChange = onValueChange
                )
            }
        }
    }
}

/**
 * Get the appropriate control for a setting based on its options.
 */
@Composable
fun SettingControl(
    setting: HiddenSetting,
    onValueChange: (Int) -> Unit
) {
    when {
        // Binary toggle (0/1 with On/Off labels)
        setting.options.size == 2 && 
        setting.options.all { it.value == 0 || it.value == 1 } &&
        (setting.options.find { it.value == 1 }?.label?.lowercase()?.contains("be") == true ||
         setting.options.find { it.value == 1 }?.label?.lowercase()?.contains("on") == true ||
         setting.options.find { it.value == 1 }?.label?.lowercase()?.contains("ki") == false) -> {
            // Toggle switch for binary settings
            val isOn = setting.currentValue == 1
            Switch(
                checked = isOn,
                onCheckedChange = { checked ->
                    onValueChange(if (checked) 1 else 0)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        
        // Multiple discrete options - show as dropdown chips
        setting.options.size <= 5 -> {
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                FilterChip(
                    selected = true,
                    onClick = { expanded = true },
                    label = { Text(setting.currentValueLabel()) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    setting.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                if (option.value != setting.currentValue) {
                                    onValueChange(option.value)
                                }
                                expanded = false
                            },
                            leadingIcon = if (option.value == setting.currentValue) {
                                { 
                                    Icon(
                                        imageVector = Icons.Default.ToggleOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    ) 
                                }
                            } else null
                        )
                    }
                }
            }
        }
        
        // Many options - show as selector with current value highlighted
        else -> {
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                TextButton(onClick = { expanded = true }) {
                    Text("Változtat")
                }
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    setting.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                if (option.value != setting.currentValue) {
                                    onValueChange(option.value)
                                }
                                expanded = false
                            },
                            leadingIcon = if (option.value == setting.currentValue) {
                                { 
                                    Icon(
                                        imageVector = Icons.Default.ToggleOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    ) 
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get icon and color for a category.
 */
fun getCategoryIconAndColor(category: String): Pair<ImageVector, Color> {
    return when (category) {
        "Ajtózár" -> Icons.Default.Lock to Color(0xFF2196F3)
        "Világítás" -> Icons.Default.Lightbulb to Color(0xFFFFC107)
        "Ablaktörlő" -> Icons.Default.Speed to Color(0xFF00BCD4)
        "Tükrök" -> Icons.Default.DirectionsCar to Color(0xFF4CAF50)
        "Hangjelző" -> Icons.Default.Speaker to Color(0xFFE91E63)
        "Klíma" -> Icons.Default.Build to Color(0xFF9C27B0)
        "Ablakok" -> Icons.Default.Window to Color(0xFF607D8B)
        else -> Icons.Default.ToggleOn to Color(0xFF9E9E9E)
    }
}