package com.example.homeassisstthing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.homeassisstthing.SmartDevice
import kotlin.math.cos
import kotlin.math.sin
import android.util.Log
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.clip
import android.app.Activity
import android.os.Build
import android.webkit.WebSettings
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// --- INLINE VECTOR ICONS ---
private val IconArrowBack: ImageVector
    get() = ImageVector.Builder(name = "ArrowBack", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineTo(13.42f, 5.41f)
            lineTo(12f, 4f)
            lineTo(4f, 12f)
            lineTo(12f, 20f)
            lineTo(13.41f, 18.59f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            close()
        }.build()

private val IconAdd: ImageVector
    get() = ImageVector.Builder(name = "Add", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(19f, 13f)
            horizontalLineTo(13f)
            verticalLineTo(19f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(11f)
            verticalLineTo(5f)
            horizontalLineTo(13f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()

private val IconRemove: ImageVector
    get() = ImageVector.Builder(name = "Remove", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(19f, 13f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(19f)
            close()
        }.build()

private val IconPower: ImageVector
    get() = ImageVector.Builder(name = "Power", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
            moveTo(13f, 3f)
            horizontalLineTo(11f)
            verticalLineTo(13f)
            horizontalLineTo(13f)
            close()
            moveTo(17.83f, 5.17f)
            lineTo(16.41f, 6.59f)
            curveTo(18.02f, 7.91f, 19f, 9.87f, 19f, 12f)
            curveTo(19f, 15.87f, 15.87f, 19f, 12f, 19f)
            curveTo(8.13f, 19f, 5f, 15.87f, 5f, 12f)
            curveTo(5f, 9.87f, 5.98f, 7.91f, 7.58f, 6.58f)
            lineTo(6.17f, 5.17f)
            curveTo(4.21f, 6.81f, 3f, 9.26f, 3f, 12f)
            curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
            curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
            curveTo(21f, 9.26f, 19.79f, 6.81f, 17.83f, 5.17f)
            close()
        }.build()

@Composable
fun ClimateControlTab(
    haEntities: List<SmartDevice>,
    roomTargetStates: Map<String, Float>,
    roomMappings: Map<String, Pair<String, String>>,
    currentBgColor: Color,
    currentTextColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    textMuted: Color,
    onToggleClimateState: (String, Boolean) -> Unit,
    onToggleScheduleState: (String, Boolean) -> Unit,
    onUpdateTargetTemp: (String, Float) -> Unit,
    onSetPresetMode: (String, String) -> Unit,
    onDrillRoom: (Int?) -> Unit,
    drilledRoomIndex: Int?,
    haIPAddress: String,
    schedulePath: String = "schedule",
    onOpenSchedule: () -> Unit,
    triggerInterfaceFeedback: () -> Unit
) {
    val climateEntities = remember(haEntities) {
        haEntities.filter { device ->
            val id = device.entityId.lowercase()
            val domain = device.domain.lowercase()

            val isClimateDomain = domain == "climate" || id.startsWith("climate.")
            val isHelperThermostat = id.contains("thermostat") &&
                    !id.startsWith("sensor.") &&
                    !id.startsWith("switch.") &&
                    !id.startsWith("binary_sensor.") &&
                    !id.startsWith("number.")

            val isDiagnosticNoise = id.contains("consumption") ||
                    id.contains("voltage") ||
                    id.contains("current") ||
                    id.contains("signal") ||
                    id.contains("cloud") ||
                    id.contains("led") ||
                    id.contains("auto_off") ||
                    id.contains("overload")

            (isClimateDomain || isHelperThermostat) && !isDiagnosticNoise
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor)
    ) {
        if (climateEntities.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "NO CLIMATE CONTROLLERS DETECTED",
                    color = textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        } else if (drilledRoomIndex != null && drilledRoomIndex in climateEntities.indices) {
            val selectedDevice = climateEntities[drilledRoomIndex]

            val rawFriendlyName = selectedDevice.friendlyName.ifBlank {
                (selectedDevice.attributes["friendly_name"] as? String)
                    ?: selectedDevice.entityId.substringAfter(".").replace("_", " ")
            }
            val displayName = rawFriendlyName.replace(Regex("(?i)\\b(\\w+)\\s+\\1\\b"), "$1").trim()

            val currentTemp = (selectedDevice.attributes["current_temperature"] as? Number)?.toFloat()
                ?: selectedDevice.currentTemperature.takeIf { it > 0f }
                ?: selectedDevice.state.toFloatOrNull()
                ?: 20.0f

            val targetTemp = roomTargetStates[selectedDevice.entityId]
                ?: (selectedDevice.attributes["temperature"] as? Number)?.toFloat()
                ?: (selectedDevice.attributes["target_temperature"] as? Number)?.toFloat()
                ?: selectedDevice.targetTemperature
                ?: 21.0f

            // 1. Extract room keywords ONCE (shared by both humidity & schedule lookups)
            val ignoreWords = setOf("climate", "heater", "thermostat", "temp", "sensors", "humidity", "device")
            val entitySlug = selectedDevice.entityId.substringAfter("climate.").lowercase()
            val friendlySlug = selectedDevice.friendlyName.lowercase()

            val roomKeywords = (entitySlug.split("_") + friendlySlug.split(" "))
                .map { it.filter { char -> char.isLetterOrDigit() } }
                .filter { it.length >= 3 && !ignoreWords.contains(it) }
                .distinct()

            // 2. Dynamic Humidity Lookup
            val climateHumidity = (selectedDevice.attributes["current_humidity"] as? Number)?.toInt()
                ?: (selectedDevice.attributes["humidity"] as? Number)?.toInt()

            val humidity = climateHumidity ?: run {
                val matchingSensor = haEntities.find { device ->
                    if (device.domain != "sensor") return@find false
                    val sensorId = device.entityId.lowercase()
                    val sensorName = device.friendlyName.lowercase()

                    val isHumiditySensor = sensorId.contains("humidity") || sensorName.contains("humidity")
                    val matchesRoom = roomKeywords.any { word -> sensorId.contains(word) || sensorName.contains(word) }

                    isHumiditySensor && matchesRoom
                }

                matchingSensor?.state?.toFloatOrNull()?.toInt() ?: 45
            }

            // 3. Dynamic Schedule Lookup
            val matchingScheduleSwitch = haEntities.find { device ->
                if (device.domain != "switch") return@find false
                val entityId = device.entityId.lowercase()
                val friendlyName = device.friendlyName.lowercase()

                val isSchedule = entityId.contains("schedule") || friendlyName.contains("schedule")
                val matchesRoom = roomKeywords.any { word -> entityId.contains(word) || friendlyName.contains(word) }

                isSchedule && matchesRoom
            }

            val isScheduleActive = matchingScheduleSwitch?.state == "on"

            // 4. System Mode Status
            val hvacMode = selectedDevice.state.lowercase()
            val isPowerOn = hvacMode != "off"
            val isHeatingMode = hvacMode == "heat" || hvacMode == "auto" || isPowerOn

            // 5. Extract HVAC action attributes
            val hvacAction = (selectedDevice.attributes["hvac_action"] as? String
                ?: selectedDevice.attributes["action"] as? String
                ?: selectedDevice.attributes["current_action"] as? String)?.lowercase() ?: ""

            // 6. Heating Burner Status Logic
            val isExplicitlyHeating = hvacAction == "heating" || hvacAction == "heating_up"
            val isDemandHeating = hvacAction.isBlank() && isPowerOn && (targetTemp > currentTemp)
            val isActivelyHeating = (isExplicitlyHeating || isDemandHeating) && isPowerOn

            // 7. Preset Mode & Cycling Logic
            val currentPreset = (selectedDevice.attributes["preset_mode"] as? String)
                ?.lowercase()
                ?: "none"

            val rawPresetModes: List<String> = when (val raw = selectedDevice.attributes["preset_modes"]) {
                is List<*> -> raw.mapNotNull { it?.toString()?.lowercase() }
                is Array<*> -> raw.mapNotNull { it?.toString()?.lowercase() }
                else -> emptyList()
            }

            val availablePresets = rawPresetModes.ifEmpty {
                listOf("none", "away", "comfort", "eco", "home", "sleep", "activity")
            }

            val onCyclePreset: () -> Unit = {
                triggerInterfaceFeedback()
                val currentIndex = availablePresets.indexOfFirst { it.equals(currentPreset, ignoreCase = true) }
                val nextIndex = if (currentIndex == -1 || currentIndex == availablePresets.lastIndex) 0 else currentIndex + 1
                val nextPreset = availablePresets[nextIndex]

                onSetPresetMode(selectedDevice.entityId, nextPreset)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        triggerInterfaceFeedback()
                        onDrillRoom(null)
                    }) {
                        Icon(IconArrowBack, contentDescription = "Back", tint = currentTextColor)
                    }
                    Text(
                        text = displayName.uppercase(),
                        color = currentTextColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                HardwareThermostatView(
                    currentTemp = currentTemp,
                    targetTemp = targetTemp,
                    humidity = humidity,
                    isHeatingMode = isHeatingMode && isPowerOn,
                    isActivelyHeating = isActivelyHeating && isPowerOn,
                    isPowerOn = isPowerOn,
                    currentPreset = currentPreset,
                    isScheduleActive = isScheduleActive,
                    scheduleEntity = matchingScheduleSwitch,
                    currentBgColor = currentBgColor,
                    currentTextColor = currentTextColor,
                    neonCyan = neonCyan,
                    haIPAddress = haIPAddress,
                    schedulePath = schedulePath,
                    textMuted = textMuted,
                    onOpenSchedule = onOpenSchedule,

                    onPowerClick = {
                        triggerInterfaceFeedback()
                        onToggleClimateState(selectedDevice.entityId, !isPowerOn)
                    },
                    onDecreaseTemp = {
                        triggerInterfaceFeedback()
                        onUpdateTargetTemp(selectedDevice.entityId, (targetTemp - 0.5f).coerceAtLeast(7.0f))
                    },
                    onIncreaseTemp = {
                        triggerInterfaceFeedback()
                        onUpdateTargetTemp(selectedDevice.entityId, (targetTemp + 0.5f).coerceAtMost(35.0f))
                    },
                    onCyclePreset = {
                        triggerInterfaceFeedback()
                        onCyclePreset()
                    },
                    onToggleSchedule = {
                        triggerInterfaceFeedback()
                        Log.d("SCHEDULE_DEBUG", "Schedule Button Tapped! Entity Found: ${matchingScheduleSwitch?.entityId}")
                        matchingScheduleSwitch?.entityId?.let { scheduleId ->
                            onToggleScheduleState(scheduleId, !isScheduleActive)
                        }
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(climateEntities) { index, device ->
                    val rawFriendlyName = device.friendlyName.ifBlank {
                        (device.attributes["friendly_name"] as? String)
                            ?: device.entityId.substringAfter(".").replace("_", " ")
                    }
                    val displayName = rawFriendlyName.replace(Regex("(?i)\\b(\\w+)\\s+\\1\\b"), "$1").trim()

                    val currentTemp = (device.attributes["current_temperature"] as? Number)?.toFloat()
                        ?: device.currentTemperature.takeIf { it > 0f }
                        ?: device.state.toFloatOrNull()
                        ?: 20.0f

                    val targetTemp = roomTargetStates[device.entityId]
                        ?: (device.attributes["temperature"] as? Number)?.toFloat()
                        ?: (device.attributes["target_temperature"] as? Number)?.toFloat()
                        ?: device.targetTemperature
                        ?: 21.0f

                    val isPowerOn = device.state.lowercase() != "off"

                    // --- Dynamic Theme Calculations ---
                    // Determines card background dynamically (White for Light Theme, Dark Surface for Dark Theme)
                    val cardSurfaceColor = remember(currentBgColor) {
                        if (currentBgColor.red > 0.5f && currentBgColor.green > 0.5f && currentBgColor.blue > 0.5f) {
                            Color.White // Light Theme Card Surface
                        } else {
                            Color(0xFF161B22) // Dark Theme Card Surface
                        }
                    }

                    val inactivePillBg = remember(currentBgColor) {
                        if (currentBgColor.red > 0.5f && currentBgColor.green > 0.5f && currentBgColor.blue > 0.5f) {
                            Color(0xFFEEEEEE) // Light Theme OFF Button Surface
                        } else {
                            Color(0xFF2A2E3D) // Dark Theme OFF Button Surface
                        }
                    }

                    // --- Calculate Heating Status ---
                    val hvacAction = (device.attributes["hvac_action"] as? String
                        ?: device.attributes["action"] as? String
                        ?: device.attributes["current_action"] as? String)?.lowercase() ?: ""

                    val isExplicitlyHeating = hvacAction == "heating" || hvacAction == "heating_up"
                    val isDemandHeating = hvacAction.isBlank() && isPowerOn && (targetTemp > currentTemp)
                    val isActivelyHeating = (isExplicitlyHeating || isDemandHeating) && isPowerOn

                    // Dynamic Climate Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                triggerInterfaceFeedback()
                                onDrillRoom(index)
                            },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardSurfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = displayName.uppercase(),
                                    color = currentTextColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "TARGET: ${"%.1f".format(targetTemp)}°C  |  CURRENT: ${"%.1f".format(currentTemp)}°C",
                                    color = textMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                // Heating Status Indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (isActivelyHeating) Color(0xFFFF5252) else textMuted.copy(alpha = 0.3f),
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                    Text(
                                        text = if (isActivelyHeating) "HEATING: ACTIVE" else "HEATING: IDLE",
                                        color = if (isActivelyHeating) Color(0xFFD32F2F) else textMuted.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Dynamic Cyber Pill Switch
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                                    .background(if (isPowerOn) neonGreen.copy(alpha = 0.2f) else inactivePillBg)
                                    .clickable {
                                        triggerInterfaceFeedback()
                                        onToggleClimateState(device.entityId, !isPowerOn)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isPowerOn) "ON" else "OFF",
                                    color = if (isPowerOn) neonGreen else textMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareThermostatView(
    currentTemp: Float,
    targetTemp: Float,
    humidity: Int,
    isHeatingMode: Boolean,
    isActivelyHeating: Boolean,
    isPowerOn: Boolean,
    currentPreset: String, // Pass active preset from HA
    isScheduleActive: Boolean,
    scheduleEntity: SmartDevice?,
    currentBgColor: Color,
    currentTextColor: Color,
    neonCyan: Color,
    textMuted: Color,
    haIPAddress: String,
    schedulePath: String = "heating-schedule",
    onPowerClick: () -> Unit,
    onDecreaseTemp: () -> Unit,
    onIncreaseTemp: () -> Unit,
    onCyclePreset: () -> Unit, // Callback to trigger next preset in HA
    onOpenSchedule: () -> Unit,
    onToggleSchedule: () -> Unit
) {
    var showScheduleDialog by remember { mutableStateOf(false) }
    var isHoldMode by remember { mutableStateOf(false) }
    val activeOrange = Color(0xFFE0562D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor)
    ) {
        // =========================================================================
        // 1. TOP SECTION: Status Header & Temperature Dial
        // =========================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header status row (Online & Active Status Indicator above dial)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "● ONLINE",
                    color = neonCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = when {
                        !isPowerOn -> "POWER OFF"
                        isActivelyHeating -> "HEATING ACTIVE"
                        else -> "TARGET REACHED"
                    },
                    color = if (isPowerOn) activeOrange else textMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Temp Dial (Shifted to Top)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                ThermostatDial(
                    currentTemp = currentTemp,
                    targetTemp = targetTemp,
                    isActivelyHeating = isActivelyHeating,
                    currentTextColor = currentTextColor,
                    neonCyan = neonCyan,
                    textMuted = textMuted,
                    activeOrange = activeOrange
                )
            }
        }

        // =========================================================================
        // 2. MIDDLE SECTION: Information Grid (Shifted Under Dial)
        // =========================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.weight(1f)) {
                // PRESET TILE (Cycles Presets)
                TextGridTile(
                    title = "PRESET",
                    subtext = currentPreset.uppercase().ifBlank { "NONE" },
                    isActive = currentPreset.isNotBlank() && currentPreset.lowercase() != "none",
                    textColor = currentTextColor,
                    accentColor = neonCyan,
                    borderColor = textMuted.copy(alpha = 0.2f),
                    onClick = onCyclePreset,
                    modifier = Modifier.weight(1f)
                )
                // SCHEDULE TILE -> OPENS DIALOG WINDOW!
                TextGridTile(
                    title = "SCHEDULE",
                    subtext = if (isScheduleActive) "ACTIVE" else "OFF",
                    isActive = isScheduleActive,
                    textColor = currentTextColor,
                    accentColor = neonCyan,
                    borderColor = textMuted.copy(alpha = 0.2f),
                    onClick = { onOpenSchedule() }, // Opens scheduling window
                    modifier = Modifier.weight(1f)
                )
            }
            Row(modifier = Modifier.weight(1f)) {
                // HUMIDITY TILE
                TextGridTile(
                    title = "HUMIDITY",
                    subtext = "$humidity%",
                    isActive = false,
                    textColor = currentTextColor,
                    accentColor = neonCyan,
                    borderColor = textMuted.copy(alpha = 0.2f),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                // MODE TILE
                TextGridTile(
                    title = "MODE",
                    subtext = if (isHeatingMode) "HEAT" else "IDLE",
                    isActive = isHeatingMode,
                    textColor = currentTextColor,
                    accentColor = activeOrange,
                    borderColor = textMuted.copy(alpha = 0.2f),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // =========================================================================
        // 3. BOTTOM SECTION: Control Navigation Bar (Original Preserved)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(currentBgColor)
                .border(0.5.dp, textMuted.copy(alpha = 0.3f))
        ) {
            NavButton(
                icon = IconPower,
                tint = if (isPowerOn) activeOrange else textMuted,
                borderColor = textMuted.copy(alpha = 0.3f),
                onClick = onPowerClick,
                modifier = Modifier.weight(1f)
            )
            NavButton(
                icon = IconRemove,
                tint = currentTextColor,
                borderColor = textMuted.copy(alpha = 0.3f),
                onClick = onDecreaseTemp,
                modifier = Modifier.weight(1f)
            )
            NavButton(
                icon = IconAdd,
                tint = currentTextColor,
                borderColor = textMuted.copy(alpha = 0.3f),
                onClick = onIncreaseTemp,
                modifier = Modifier.weight(1f)
            )
        }


    }
    // =========================================================================
    // 4. DIALOG POPUP WINDOW
    // =========================================================================
    if (showScheduleDialog) {
        ScheduleWindowDialog(
            isScheduleActive = isScheduleActive,
            scheduleEntity = scheduleEntity,
            currentTextColor = currentTextColor,
            currentBgColor = currentBgColor,
            neonCyan = neonCyan,
            textMuted = textMuted,
            activeOrange = activeOrange,
            haIpAddress = haIPAddress,
            schedulePath = schedulePath,
            onDismiss = { showScheduleDialog = false },
            onToggleSchedule = {
                onToggleSchedule() // Toggles HA schedule switch
            }
        )
    }
}

@Composable
private fun TextGridTile(
    title: String,
    subtext: String,
    isActive: Boolean = false,
    textColor: Color,
    accentColor: Color,
    borderColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, borderColor)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                color = if (isActive) accentColor else textColor.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    tint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(0.5.dp, borderColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ThermostatDial(
    currentTemp: Float,
    targetTemp: Float,
    isActivelyHeating: Boolean,
    currentTextColor: Color,
    neonCyan: Color,
    textMuted: Color,
    activeOrange: Color
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(210.dp)) {
            val strokeWidth = 24f

            // 1. Calculate TRUE Center & Radius
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension - strokeWidth) / 2f

            // 2. Derive Bounding Box TopLeft directly from true center
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            val startAngle = 135f
            val sweepAngle = 270f

            // Track Background Arc
            drawArc(
                color = textMuted.copy(alpha = 0.2f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Current Temp Active Arc Fill
            val progressPercentage = ((currentTemp - 10f) / (32f - 10f)).coerceIn(0f, 1f)
            val currentSweep = sweepAngle * progressPercentage

            drawArc(
                color = textMuted.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. TARGET TEMP INDICATOR PIN (Now perfectly centered)
            val targetPercentage = ((targetTemp - 10f) / (32f - 10f)).coerceIn(0f, 1f)
            val targetAngle = startAngle + (sweepAngle * targetPercentage)
            val angleInRadians = Math.toRadians(targetAngle.toDouble())

            // Start line from inner arc edge to outer arc edge (plus slight extension pin)
            val innerRadius = radius - (strokeWidth / 2f) - 2f
            val outerRadius = radius + (strokeWidth / 2f) + 6f

            val lineStart = Offset(
                x = center.x + innerRadius * cos(angleInRadians).toFloat(),
                y = center.y + innerRadius * sin(angleInRadians).toFloat()
            )
            val lineEnd = Offset(
                x = center.x + outerRadius * cos(angleInRadians).toFloat(),
                y = center.y + outerRadius * sin(angleInRadians).toFloat()
            )

            drawLine(
                color = activeOrange,
                start = lineStart,
                end = lineEnd,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Inner Text & Temperature Readouts remain unchanged...
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isActivelyHeating) {
                Text(
                    text = "🔥 HEATING",
                    color = activeOrange,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            val whole = currentTemp.toInt()
            val decimal = ((currentTemp - whole) * 10).toInt()

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$whole",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Light,
                    color = currentTextColor
                )
                Text(
                    text = ".$decimal",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                    color = currentTextColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "°C",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = currentTextColor,
                    modifier = Modifier.padding(bottom = 22.dp, start = 2.dp)
                )
            }

            Text(
                text = "Room temperature",
                fontSize = 11.sp,
                color = textMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "%.1f".format(targetTemp),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonCyan
                )
                Text(
                    text = "°C",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonCyan,
                    modifier = Modifier.padding(top = 2.dp, start = 1.dp)
                )
            }
        }
    }
}



@Composable
fun ScheduleWindowDialog(
    isScheduleActive: Boolean,
    scheduleEntity: SmartDevice?,
    currentTextColor: Color,
    currentBgColor: Color,
    neonCyan: Color,
    textMuted: Color,
    activeOrange: Color,
    haIpAddress: String,
    schedulePath: String = "schedule", // <--- Custom Dashboard Path (e.g. "schedule", "heating-schedule")
    onDismiss: () -> Unit,
    onToggleSchedule: () -> Unit
) {
    // 1. Re-enforce Immersive Mode inside the Dialog Window
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.parent as? DialogWindowProvider)?.window
            ?: (view.context as? Activity)?.window

        window?.let { win ->
            val controller = WindowCompat.getInsetsController(win, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            // Re-hide system bars on the main app activity when closing dialog
            (view.context as? Activity)?.window?.let { mainWin ->
                val mainController = WindowCompat.getInsetsController(mainWin, mainWin.decorView)
                mainController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                mainController.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // 2. Format the custom URL dynamically
    val formattedUrl = remember(haIpAddress, schedulePath) {
        val cleanIp = haIpAddress.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .trimEnd('/')

        val cleanPath = schedulePath.trim().removePrefix("/")

        "http://$cleanIp/$cleanPath"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false // <--- Prevents Compose from stealing tap events from HA popups
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF161B22),
            border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)  // <--- Expanded to 95% width
                .fillMaxHeight(0.92f) // <--- Expanded to 92% height so "Add" button isn't cut off
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HEATING SCHEDULE",
                        color = currentTextColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isScheduleActive) "ON" else "OFF",
                            color = if (isScheduleActive) neonCyan else textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = isScheduleActive,
                            onCheckedChange = { onToggleSchedule() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = neonCyan,
                                checkedTrackColor = neonCyan.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Divider(color = textMuted.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Embedded Web Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, textMuted.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                // 1. Force full touch focus delegation to the WebView root
                                isClickable = true
                                isFocusable = true
                                isFocusableInTouchMode = true
                                requestFocusFromTouch()

                                // 2. Disable clipping so root-level modal overlays aren't truncated
                                clipChildren = false
                                clipToPadding = false

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        return false
                                    }
                                }

                                webChromeClient = object : android.webkit.WebChromeClient() {
                                    // Ensure JS popup/modal creation isn't blocked by host app
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: android.os.Message?
                                    ): Boolean {
                                        val transport = resultMsg?.obj as? WebView.WebViewTransport
                                        transport?.webView = view
                                        resultMsg?.sendToTarget()
                                        return true
                                    }
                                }

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true

                                    // Crucial for Shadow DOM viewport calculations:
                                    useWideViewPort = true
                                    loadWithOverviewMode = false // Force native scale so Shadow DOM popups map 1:1 with screen pixels

                                    // Allow JS window & modal creation
                                    javaScriptCanOpenWindowsAutomatically = true
                                    setSupportMultipleWindows(false)

                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                                    // Standard Mobile Chrome User-Agent
                                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }

                                loadUrl(formattedUrl)
                            }
                        },
                        update = { webView ->
                            if (webView.url != formattedUrl) {
                                webView.loadUrl(formattedUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = neonCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "DONE",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}