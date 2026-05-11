package com.toyota.obd210.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toyota.obd210.domain.model.CarModel
import com.toyota.obd210.domain.model.HiddenSetting
import com.toyota.obd210.domain.model.SettingCategory
import com.toyota.obd210.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenSettingsScreen(
    viewModel: HiddenSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        // Header with Car Model Selector
        CarModelSelectorHeader(
            currentModel = uiState.currentCarModel,
            isAutoDetected = uiState.isAutoDetected,
            detectedVin = uiState.detectedVin,
            onModelSelected = { viewModel.overrideCarModel(it) },
            onDetectClick = { viewModel.detectCarModel() },
            onResetClick = { viewModel.resetToAutoDetection() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Warning Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚠️ Warning",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Warning
                    )
                    Text(
                        text = "Changes require ELM327 v2.2/v2.3 adapter. Incorrect settings may affect vehicle operation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings by Category
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Door Lock Settings
                item {
                    SettingsCategoryCard(
                        title = "Door Lock",
                        icon = Icons.Default.Lock,
                        settings = uiState.settings.filter { it.category == SettingCategory.DOOR_LOCK },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Lighting Settings
                item {
                    SettingsCategoryCard(
                        title = "Lighting",
                        icon = Icons.Default.LightMode,
                        settings = uiState.settings.filter { it.category == SettingCategory.LIGHTING },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Safety Settings
                item {
                    SettingsCategoryCard(
                        title = "Toyota Safety Sense",
                        icon = Icons.Default.Security,
                        settings = uiState.settings.filter { it.category == SettingCategory.SAFETY },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Infotainment Settings
                item {
                    SettingsCategoryCard(
                        title = "Infotainment",
                        icon = Icons.Default.Speaker,
                        settings = uiState.settings.filter { it.category == SettingCategory.INFOTAINMENT },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Climate Settings
                item {
                    SettingsCategoryCard(
                        title = "Climate",
                        icon = Icons.Default.AcUnit,
                        settings = uiState.settings.filter { it.category == SettingCategory.CLIMATE },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Window Settings
                item {
                    SettingsCategoryCard(
                        title = "Windows",
                        icon = Icons.Default.Window,
                        settings = uiState.settings.filter { it.category == SettingCategory.WINDOWS },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Indicator/Blinker Settings
                item {
                    SettingsCategoryCard(
                        title = "Indicators",
                        icon = Icons.Default.Toll,
                        settings = uiState.settings.filter { it.category == SettingCategory.INDICATORS },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // Seatbelt Minder Settings
                item {
                    SettingsCategoryCard(
                        title = "Seatbelt Minder",
                        icon = Icons.Default.AirlineSeatReclineNormal,
                        settings = uiState.settings.filter { it.category == SettingCategory.SEATBELT },
                        onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                        isSettingSupported = { viewModel.isSettingSupported(it) }
                    )
                }
                
                // HUD Settings (if available)
                val hudSettings = uiState.settings.filter { it.category == SettingCategory.HUD }
                if (hudSettings.isNotEmpty()) {
                    item {
                        SettingsCategoryCard(
                            title = "Head-Up Display",
                            icon = Icons.Default.Dashboard,
                            settings = hudSettings,
                            onSettingChange = { setting, value -> viewModel.updateSetting(setting, value) },
                            isSettingSupported = { viewModel.isSettingSupported(it) }
                        )
                    }
                }
            }
        }
        
        // Status message
        uiState.message?.let { message ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isError) Error.copy(alpha = 0.2f) else Success.copy(alpha = 0.2f)
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
                        imageVector = if (uiState.isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (uiState.isError) Error else Success
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isError) Error else Success
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    settings: List<HiddenSetting>,
    onSettingChange: (HiddenSetting, Int) -> Unit,
    isSettingSupported: (HiddenSetting) -> Boolean
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            settings.forEachIndexed { index, setting ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = SurfaceVariant
                    )
                }
                SettingRow(
                    setting = setting,
                    isSupported = isSettingSupported(setting),
                    onValueChange = { value -> onSettingChange(setting, value) }
                )
            }
        }
    }
}

@Composable
fun SettingRow(
    setting: HiddenSetting,
    isSupported: Boolean,
    onValueChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isSupported) Modifier.alpha(0.5f) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = setting.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurface
                )
                if (!isSupported) {
                    Text(
                        text = setting.notAvailableReason ?: "Not available for this vehicle",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            if (isSupported) {
                Text(
                    text = setting.options.find { it.value == setting.currentValue }?.label 
                        ?: setting.currentValue.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Options selector - disabled if not supported
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            setting.options.take(5).forEach { option ->
                FilterChip(
                    selected = isSupported && option.value == setting.currentValue,
                    onClick = { if (isSupported) onValueChange(option.value) },
                    enabled = isSupported,
                    label = { 
                        Text(
                            text = option.label.take(8),
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = OnPrimary,
                        disabledContainerColor = SurfaceVariant,
                        disabledLabelColor = OnSurface.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Not available warning
        if (!isSupported) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Not supported for this vehicle model",
                    style = MaterialTheme.typography.bodySmall,
                    color = Warning
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarModelSelectorHeader(
    currentModel: CarModel,
    isAutoDetected: Boolean,
    detectedVin: String?,
    onModelSelected: (CarModel) -> Unit,
    onDetectClick: () -> Unit,
    onResetClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vehicle",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Model Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentModel.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = SurfaceVariant
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            CarModel.getAllModels().forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = model.displayName,
                                                fontWeight = if (model == currentModel) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (model.requiresHardware.isNotEmpty()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Requires specific hardware",
                                                    tint = OnSurface.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onModelSelected(model)
                                        expanded = false
                                    },
                                    leadingIcon = if (model == currentModel) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = Primary) }
                                    } else null
                                )
                            }
                        }
                    }
                    
                    // Detection status
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAutoDetected) {
                            Badge(
                                containerColor = Success.copy(alpha = 0.2f),
                                contentColor = Success
                            ) {
                                Text("AUTO")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            detectedVin?.let { vin ->
                                Text(
                                    text = "VIN: ${vin.takeLast(6)}...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            Badge(
                                containerColor = OnSurface.copy(alpha = 0.1f),
                                contentColor = OnSurface.copy(alpha = 0.6f)
                            ) {
                                Text("MANUAL")
                            }
                        }
                    }
                }
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Detect button
                OutlinedButton(
                    onClick = onDetectClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Detect")
                }
                
                // Reset button (only if auto-detected)
                if (isAutoDetected) {
                    TextButton(onClick = onResetClick) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }
    }
}
