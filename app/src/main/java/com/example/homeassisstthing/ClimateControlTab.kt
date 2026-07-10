package com.example.homeassisstthing

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.sortedWith

@Composable
fun ClimateControlTab(
    deviceList: List<SmartDevice>,
    haClient: HomeAssistantClient?,
    drilledRoomIndex: Int?,
    onDrillRoom: (Int?) -> Unit,
    roomTargetStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Float>,
    roomMappings: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Pair<String, String>>,
    currentBgColor: Color,
    currentTextColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    textMuted: Color,
    triggerInterfaceFeedback: () -> Unit
) {
    val roomSyncCooldowns = remember { mutableStateMapOf<String, Long>() }
    val masterSwitchEntity = "input_boolean.heating_master_switch"

    val roomNames: List<String> = remember(roomMappings.size, roomMappings.keys) {
        roomMappings.keys.toList().sorted()
    }

    val masterSwitchDevice = deviceList.find { it.entityId == masterSwitchEntity }
    val isMasterHeatingOn = masterSwitchDevice?.state == "ON"

    val availableSensors = remember(deviceList) {
        deviceList.filter { it.domain == "sensor" }.map { it.entityId }.sorted()
    }

    var sensoryPickerTargetMode by remember { mutableStateOf<String?>(null) }
    val selectedRoomName = if (drilledRoomIndex != null && drilledRoomIndex < roomNames.size) roomNames[drilledRoomIndex] else ""

    // Tracks whether the schedule matrix is active (True) or if the room is in full manual mode (False)
    val roomScheduleEnabledStates = remember { mutableStateMapOf<String, Boolean>() }

    // Temporary mock database mapped by room names.
    val mockRoomSchedules = remember {
        mutableStateMapOf<String, List<ClimateScheduleSlot>>(
            "Airing Cupboard" to listOf(
                ClimateScheduleSlot(time = "06:30", targetTemp = 21.5f, isHeatingOn = true, dayTarget = "WEEKDAYS"),
                ClimateScheduleSlot(time = "09:00", targetTemp = 15.0f, isHeatingOn = false, dayTarget = "WEEKDAYS"),
                ClimateScheduleSlot(time = "08:30", targetTemp = 22.0f, isHeatingOn = true, dayTarget = "WEEKENDS")
            )
        )
    }

    // =====================================================================
    // AUTOMATIC HOME ASSISTANT DATA IMPORT ON RESTART
    // =====================================================================
    LaunchedEffect(deviceList, roomNames) {
        roomNames.forEach { roomName ->
            val dynamicSlug = roomName.lowercase().trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
            val scheduleDevice = deviceList.find { it.entityId.matches(targetRegex) }

            if (scheduleDevice == null) return@forEach

            val rawStateString = scheduleDevice.state
            val lastEditTime = roomSyncCooldowns[roomName] ?: 0L
            val isCoolingDown = (System.currentTimeMillis() - lastEditTime) < 1500L

            if (!rawStateString.isNullOrEmpty() && rawStateString != "unknown" && rawStateString != "unavailable" && !isCoolingDown) {
                try {
                    if (rawStateString.startsWith("[")) return@forEach

                    val parsedSlots = rawStateString.split(";").filter { it.isNotBlank() }.map { slotRaw ->
                        val parts = slotRaw.split(",")
                        ClimateScheduleSlot(
                            id = java.util.UUID.randomUUID().toString(),
                            time = parts.getOrNull(0) ?: "12:00",
                            targetTemp = parts.getOrNull(1)?.toFloatOrNull() ?: 20.0f,
                            isHeatingOn = parts.getOrNull(2) == "1",
                            dayTarget = parts.getOrNull(3) ?: "WEEKDAYS"
                        )
                    }

                    val currentLocal = mockRoomSchedules[roomName] ?: emptyList()
                    val structuralLocal = currentLocal.map { "${it.time},${it.targetTemp},${it.isHeatingOn},${it.dayTarget}" }
                    val structuralIncoming = parsedSlots.map { "${it.time},${it.targetTemp},${it.isHeatingOn},${it.dayTarget}" }

                    if (structuralLocal != structuralIncoming) {
                        mockRoomSchedules[roomName] = parsedSlots
                    }
                } catch (e: Exception) {
                    Log.e("HA_RESTART", "Failed to parse compact string for $roomName: ${e.message}")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ----------------------------------------------------
            // VIEW A: MAIN ZONE DIRECTORY MATRIX (MATCHES MAIN LIGHTS PAGE)
            // ----------------------------------------------------
            if (drilledRoomIndex == null) {
                // Title Header
                Text(
                    text = "CLIMATE CONTROLS (${roomNames.size})",
                    color = textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Master Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(currentBgColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .border(1.dp, textMuted.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("GLOBAL HEATING", color = currentTextColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("TAP SWITCH TO ALTER RUNTIME", color = textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 75.dp, height = 38.dp)
                            .background(if (isMasterHeatingOn) neonGreen.copy(alpha = 0.15f) else textMuted.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .border(1.dp, if (isMasterHeatingOn) neonGreen else textMuted.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                val targetService = if (isMasterHeatingOn) "turn_off" else "turn_on"
                                val genericSwitchJson = """{"id":${System.currentTimeMillis().toInt()},"type":"call_service","domain":"homeassistant","service":"$targetService","service_data":{"entity_id":"$masterSwitchEntity"}}""".replace(" ", "")
                                haClient?.sendCustomJson(genericSwitchJson)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMasterHeatingOn) "ON" else "OFF",
                            color = if (isMasterHeatingOn) neonGreen else textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Individual Zone Rows
                roomNames.forEachIndexed { index, roomName ->
                    val (tempId, _) = roomMappings[roomName] ?: Pair("", "")
                    val sensorDevice = deviceList.find { it.entityId == tempId }
                    val rTemp = sensorDevice?.state?.toFloatOrNull() ?: 0.0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentBgColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .border(1.dp, textMuted.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .clickable { onDrillRoom(index) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(roomName, color = currentTextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("TAP FOR ADVANCED CONTROLS", color = textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        Text(
                            text = if (rTemp > 0f) "${String.format("%.1f", rTemp)}°C" else "--°C",
                            color = if (rTemp > 0f) neonCyan else textMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(textMuted.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // REGISTER NEW CLIMATE ZONE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(currentBgColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, neonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            triggerInterfaceFeedback()
                            val nextZoneNum = roomNames.size + 1
                            val newZoneName = "NEW ZONE $nextZoneNum"
                            haClient?.createHelperEntities(newZoneName)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ SYSTEM REGISTER NEW CLIMATE ZONE", color = neonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

            } else {
                // ----------------------------------------------------
                // DELEGATED VIEW B: ISOLATED ADVANCED CONSOLE CARD
                // ----------------------------------------------------
                IsolatedClimateConsole(
                    roomName = selectedRoomName,
                    deviceList = deviceList,
                    haClient = haClient,
                    onDrillRoom = onDrillRoom,
                    roomTargetStates = roomTargetStates,
                    roomMappings = roomMappings,
                    roomScheduleEnabledStates = roomScheduleEnabledStates,
                    mockRoomSchedules = mockRoomSchedules,
                    roomSyncCooldowns = roomSyncCooldowns,
                    currentBgColor = currentBgColor,
                    currentTextColor = currentTextColor,
                    neonCyan = neonCyan,
                    neonGreen = neonGreen,
                    textMuted = textMuted,
                    triggerInterfaceFeedback = triggerInterfaceFeedback,
                    onOpenSensorPicker = { targetMode -> sensoryPickerTargetMode = targetMode }
                )
            }
        }

        // FULL SCREEN OVERLAY PICKER
        if (sensoryPickerTargetMode != null) {
            val currentPair = roomMappings[selectedRoomName] ?: Pair("", "")

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { sensoryPickerTargetMode = null }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .background(currentBgColor, RoundedCornerShape(16.dp))
                        .border(2.dp, neonCyan, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "CHOOSE ${sensoryPickerTargetMode} ATTACHMENT", color = neonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    androidx.compose.material3.HorizontalDivider(color = textMuted.copy(alpha = 0.3f))

                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSensors) { sensorId ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(textMuted.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                                    .border(1.dp, textMuted.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        triggerInterfaceFeedback()
                                        val roomSlug = selectedRoomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                        val currentMapping = roomMappings[selectedRoomName] ?: Pair("", "")

                                        if (sensoryPickerTargetMode == "TEMP") {
                                            haClient?.updateRoomSensors(roomSlug, sensorId, currentMapping.second)
                                        } else {
                                            haClient?.updateRoomSensors(roomSlug, currentMapping.first, sensorId)
                                        }

                                        sensoryPickerTargetMode = null
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(sensorId, color = currentTextColor, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(textMuted.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clickable { sensoryPickerTargetMode = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ABORT ASSIGNMENT", color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// =====================================================================
// SUB-COMPOSABLE COMPONENT TO ESCAPE 64KB METHOD JVM BYTES LIMIT
// =====================================================================
@Composable
fun IsolatedClimateConsole(
    roomName: String,
    deviceList: List<SmartDevice>,
    haClient: HomeAssistantClient?,
    onDrillRoom: (Int?) -> Unit,
    roomTargetStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Float>,
    roomMappings: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Pair<String, String>>,
    roomScheduleEnabledStates: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Boolean>,
    mockRoomSchedules: androidx.compose.runtime.snapshots.SnapshotStateMap<String, List<ClimateScheduleSlot>>,
    roomSyncCooldowns: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Long>,
    currentBgColor: Color,
    currentTextColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    textMuted: Color,
    triggerInterfaceFeedback: () -> Unit,
    onOpenSensorPicker: (String) -> Unit
) {
    val (tempEntityId, humidityEntityId) = roomMappings[roomName] ?: Pair("", "")
    val rTemp = deviceList.find { it.entityId == tempEntityId }?.state?.toFloatOrNull() ?: 0.0f
    val rHum = deviceList.find { it.entityId == humidityEntityId }?.state ?: "--"
    val activeRoomTarget = roomTargetStates[roomName] ?: 21.0f

    var isEditingName by remember { mutableStateOf(false) }
    var nameDraftText by remember { mutableStateOf("") }
    var showSchedulerSubmenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isEditingName) {
        if (isEditingName) nameDraftText = roomName
    }

    LaunchedEffect(roomName) {
        isEditingName = false
        showDeleteConfirmation = false
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Navigation Link Button
        Row(
            modifier = Modifier
                .clickable { onDrillRoom(null) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← BACK", color = neonCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        // MAIN ADVANCED CONSOLE CARD WINDOW
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(2.dp, neonCyan, RoundedCornerShape(16.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("ADVANCED CLIMATE CONTROLS", color = neonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

            // Room Title Header Block
            Column {
                if (isEditingName) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = nameDraftText,
                            onValueChange = { nameDraftText = it },
                            textStyle = TextStyle(color = currentTextColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(neonCyan),
                            modifier = Modifier
                                .weight(1f)
                                .background(currentBgColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .border(1.dp, neonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                        Text(
                            text = "✔",
                            color = neonGreen,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                triggerInterfaceFeedback()
                                val cleanDraft = nameDraftText.trim()
                                if (cleanDraft.isNotBlank() && cleanDraft != roomName) {
                                    val oldSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                    val newSlug = cleanDraft.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

                                    haClient?.renameHelperEntity(oldSlug, newSlug, cleanDraft, "target")
                                    haClient?.renameHelperEntity(oldSlug, newSlug, cleanDraft, "schedule")
                                    haClient?.renameHelperEntity(oldSlug, newSlug, cleanDraft, "sensors")

                                    onDrillRoom(null)
                                }
                                isEditingName = false
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { triggerInterfaceFeedback(); isEditingName = true }
                            )
                    ) {
                        Text(roomName, color = currentTextColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
                Text("ZONE.${roomName.replace(" ", "_").uppercase()}", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            // 1. TOP INTERACTIVE SECTION: SETPOINT READOUT + STACKED BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentBgColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, textMuted.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TARGET SETPOINT", color = neonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${String.format("%.1f", activeRoomTarget)}°C",
                        color = currentTextColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 52.dp, height = 34.dp)
                            .background(neonCyan.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                            .border(1.dp, neonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                val newTarget = activeRoomTarget + 0.5f
                                roomTargetStates[roomName] = newTarget

                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                haClient?.setInputNumberHelperValue(
                                    entityId = "input_number.${dynamicSlug}_target",
                                    value = newTarget
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▲", color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 52.dp, height = 34.dp)
                            .background(textMuted.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .border(1.dp, textMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                val newTarget = activeRoomTarget - 0.5f
                                roomTargetStates[roomName] = newTarget

                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                haClient?.setInputNumberHelperValue(
                                    entityId = "input_number.${dynamicSlug}_target",
                                    value = newTarget
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▼", color = currentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. BOTTOM TELEMETRY SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(currentBgColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AMBIENT TEMPERATURE", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        text = if (tempEntityId.isNotEmpty()) "${String.format("%.1f", rTemp)}°C" else "N/A",
                        color = currentTextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                androidx.compose.material3.HorizontalDivider(color = textMuted.copy(alpha = 0.1f), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("RELATIVE HUMIDITY", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    val formattedHumidity = remember(rHum) {
                        val numericHum = rHum.toFloatOrNull()
                        if (numericHum != null) "${numericHum.toInt()}%" else "N/A"
                    }
                    Text(
                        text = if (humidityEntityId.isNotEmpty() && rHum != "--") formattedHumidity else "N/A",
                        color = currentTextColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 2.5 CLIMATE SCHEDULER SYSTEM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(currentBgColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                    .border(1.dp, textMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .clickable { triggerInterfaceFeedback(); showSchedulerSubmenu = !showSchedulerSubmenu }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Heating Scheduler", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text(if (showSchedulerSubmenu) "CLOSE ▲" else "OPEN ▼", color = neonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            if (showSchedulerSubmenu) {
                var activePickerSlotId by remember { mutableStateOf<String?>(null) }
                var activePickerCurrentTime by remember { mutableStateOf("12:00") }

                val activeSchedule = mockRoomSchedules[roomName] ?: emptyList()
                val isScheduleEnabled = roomScheduleEnabledStates[roomName] ?: true

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // MASTER AUTOMATION OVERRIDE SWITCH
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isScheduleEnabled) neonCyan.copy(alpha = 0.05f) else Color.Red.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            .border(1.dp, if (isScheduleEnabled) neonCyan.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                val nextState = !isScheduleEnabled
                                roomScheduleEnabledStates[roomName] = nextState

                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                    ?: "input_text.${dynamicSlug}_schedule"

                                haClient?.updateRoomScheduleMatrix(
                                    entityId = actualEntityId,
                                    slots = activeSchedule,
                                    isEngineEnabled = nextState
                                )
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isScheduleEnabled) "CRON ENGINE: ACTIVE" else "CRON ENGINE: BYPASSED",
                                color = if (isScheduleEnabled) neonCyan else Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isScheduleEnabled) "System executing target timeline parameters" else "System locked to manual setpoint override",
                                color = textMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 68.dp, height = 24.dp)
                                .background(if (isScheduleEnabled) neonCyan.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, if (isScheduleEnabled) neonCyan else Color.Red, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isScheduleEnabled) "RUNNING" else "MUTED",
                                color = if (isScheduleEnabled) neonCyan else Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // TIMELINE ROWS
                    if (activeSchedule.isEmpty()) {
                        Text(
                            text = "NO RUNTIME PROFILE DETECTED. SYSTEM RUNS PASSIVE MANUAL TARGETS.",
                            color = textMuted.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        val alphaModifier = if (isScheduleEnabled) 1.0f else 0.4f

                        activeSchedule.sortedWith(compareBy({ it.dayTarget }, { it.time })).forEach { slot ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(textMuted.copy(alpha = 0.15f * alphaModifier))
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 50.dp, height = 24.dp)
                                                .background(
                                                    if (slot.isHeatingOn) neonGreen.copy(alpha = 0.12f * alphaModifier) else textMuted.copy(alpha = 0.1f * alphaModifier),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .border(1.dp, (if (slot.isHeatingOn) neonGreen else textMuted).copy(alpha = alphaModifier), RoundedCornerShape(4.dp))
                                                .clickable(enabled = isScheduleEnabled) {
                                                    triggerInterfaceFeedback()
                                                    val updatedList = activeSchedule.map {
                                                        if (it.id == slot.id) it.copy(isHeatingOn = !it.isHeatingOn) else it
                                                    }
                                                    roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                    mockRoomSchedules[roomName] = updatedList

                                                    val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                    val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                                    val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                                        ?: "input_text.${dynamicSlug}_schedule"

                                                    haClient?.updateRoomScheduleMatrix(
                                                        entityId = actualEntityId,
                                                        slots = updatedList,
                                                        isEngineEnabled = isScheduleEnabled
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (slot.isHeatingOn) "ON" else "OFF",
                                                color = (if (slot.isHeatingOn) neonGreen else textMuted).copy(alpha = alphaModifier),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .background(textMuted.copy(alpha = 0.05f * alphaModifier), RoundedCornerShape(4.dp))
                                                .border(1.dp, textMuted.copy(alpha = 0.15f * alphaModifier), RoundedCornerShape(4.dp))
                                                .clickable(enabled = isScheduleEnabled) {
                                                    triggerInterfaceFeedback()
                                                    activePickerSlotId = slot.id
                                                    activePickerCurrentTime = slot.time
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⏱", color = textMuted.copy(alpha = alphaModifier), fontSize = 11.sp)
                                            Text(slot.time, color = currentTextColor.copy(alpha = alphaModifier), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                    }

                                    Text(
                                        text = "✕",
                                        color = Color.Red.copy(alpha = 0.6f * alphaModifier),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable(enabled = isScheduleEnabled) {
                                                triggerInterfaceFeedback()
                                                val updatedList = activeSchedule.filter { it.id != slot.id }
                                                roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                mockRoomSchedules[roomName] = updatedList

                                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                                val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                                    ?: "input_text.${dynamicSlug}_schedule"

                                                haClient?.updateRoomScheduleMatrix(
                                                    entityId = actualEntityId,
                                                    slots = updatedList,
                                                    isEngineEnabled = isScheduleEnabled
                                                )
                                            }
                                            .padding(horizontal = 6.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(neonCyan.copy(alpha = 0.08f * alphaModifier), RoundedCornerShape(4.dp))
                                            .border(1.dp, neonCyan.copy(alpha = 0.3f * alphaModifier), RoundedCornerShape(4.dp))
                                            .clickable(enabled = isScheduleEnabled) {
                                                triggerInterfaceFeedback()
                                                val dayCycles = listOf("WEEKDAYS", "WEEKENDS", "EVERYDAY", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                                                val nextIdx = (dayCycles.indexOf(slot.dayTarget) + 1) % dayCycles.size

                                                val updatedList = activeSchedule.map {
                                                    if (it.id == slot.id) it.copy(dayTarget = dayCycles[nextIdx]) else it
                                                }
                                                mockRoomSchedules[roomName] = updatedList

                                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                                val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                                    ?: "input_text.${dynamicSlug}_schedule"

                                                haClient?.updateRoomScheduleMatrix(
                                                    entityId = actualEntityId,
                                                    slots = updatedList,
                                                    isEngineEnabled = isScheduleEnabled
                                                )
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(slot.dayTarget, color = neonCyan.copy(alpha = alphaModifier), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    }

                                    if (slot.isHeatingOn) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "—",
                                                color = neonCyan.copy(alpha = alphaModifier),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.clickable(enabled = isScheduleEnabled) {
                                                    triggerInterfaceFeedback()
                                                    val updatedList = activeSchedule.map {
                                                        if (it.id == slot.id) it.copy(targetTemp = it.targetTemp - 0.5f) else it
                                                    }
                                                    roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                    mockRoomSchedules[roomName] = updatedList

                                                    val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                    val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                                    val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                                        ?: "input_text.${dynamicSlug}_schedule"

                                                    haClient?.updateRoomScheduleMatrix(
                                                        entityId = actualEntityId,
                                                        slots = updatedList,
                                                        isEngineEnabled = isScheduleEnabled
                                                    )
                                                }.padding(horizontal = 6.dp)
                                            )

                                            Text(
                                                text = "${String.format("%.1f", slot.targetTemp)}°C",
                                                color = neonCyan.copy(alpha = alphaModifier),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1
                                            )

                                            Text(
                                                text = "+",
                                                color = neonCyan.copy(alpha = alphaModifier),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.clickable(enabled = isScheduleEnabled) {
                                                    triggerInterfaceFeedback()
                                                    val updatedList = activeSchedule.map {
                                                        if (it.id == slot.id) it.copy(targetTemp = it.targetTemp + 0.5f) else it
                                                    }
                                                    roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                    mockRoomSchedules[roomName] = updatedList

                                                    val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                    val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                                    val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                                        ?: "input_text.${dynamicSlug}_schedule"

                                                    haClient?.updateRoomScheduleMatrix(
                                                        entityId = actualEntityId,
                                                        slots = updatedList,
                                                        isEngineEnabled = isScheduleEnabled
                                                    )
                                                }.padding(horizontal = 6.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "CHANNEL INACTIVE",
                                            color = textMuted.copy(alpha = 0.35f * alphaModifier),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Append New Matrix Element Trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(neonCyan.copy(alpha = if (isScheduleEnabled) 0.05f else 0.01f), RoundedCornerShape(6.dp))
                            .border(1.dp, neonCyan.copy(alpha = if (isScheduleEnabled) 0.3f else 0.05f), RoundedCornerShape(6.dp))
                            .clickable(enabled = isScheduleEnabled) {
                                triggerInterfaceFeedback()
                                val fallbackSlot = ClimateScheduleSlot(
                                    id = java.util.UUID.randomUUID().toString(),
                                    time = "12:00",
                                    targetTemp = 20.0f,
                                    isHeatingOn = true,
                                    dayTarget = "WEEKDAYS"
                                )

                                val updatedList = activeSchedule + fallbackSlot
                                roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                mockRoomSchedules[roomName] = updatedList

                                val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                    ?: "input_text.${dynamicSlug}_schedule"

                                haClient?.updateRoomScheduleMatrix(
                                    entityId = actualEntityId,
                                    slots = updatedList,
                                    isEngineEnabled = isScheduleEnabled
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ APPEND NEW TIMELINE TRIGGER",
                            color = neonCyan.copy(alpha = if (isScheduleEnabled) 1.0f else 0.2f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // NATIVE WHEEL PICKER OVERLAY POPUP
                if (activePickerSlotId != null) {
                    val parsedTimeParts = activePickerCurrentTime.split(":")
                    val initialHour = parsedTimeParts.getOrNull(0)?.toIntOrNull() ?: 12
                    val initialMinute = parsedTimeParts.getOrNull(1)?.toIntOrNull() ?: 0

                    @OptIn(ExperimentalMaterial3Api::class)
                    val timePickerState = androidx.compose.material3.rememberTimePickerState(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                        is24Hour = true
                    )

                    @OptIn(ExperimentalMaterial3Api::class)
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { activePickerSlotId = null },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    triggerInterfaceFeedback()
                                    val formattedHour = timePickerState.hour.toString().padStart(2, '0')
                                    val formattedMinute = timePickerState.minute.toString().padStart(2, '0')
                                    val newTimeString = "$formattedHour:$formattedMinute"

                                    val updatedSchedule = activeSchedule.map {
                                        if (it.id == activePickerSlotId) it.copy(time = newTimeString) else it
                                    }
                                    mockRoomSchedules[roomName] = updatedSchedule
                                    activePickerSlotId = null

                                    val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                    val targetRegex = Regex("^input_text\\.${dynamicSlug}_schedule(?:_\\d+)?$", RegexOption.IGNORE_CASE)
                                    val actualEntityId = deviceList.find { it.entityId.matches(targetRegex) }?.entityId
                                        ?: "input_text.${dynamicSlug}_schedule"

                                    haClient?.updateRoomScheduleMatrix(
                                        entityId = actualEntityId,
                                        slots = updatedSchedule,
                                        isEngineEnabled = roomScheduleEnabledStates[roomName] ?: true
                                    )
                                    activePickerSlotId = null
                                }
                            ) {
                                Text("ACCEPT", color = neonCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { activePickerSlotId = null }) {
                                Text("CANCEL", color = textMuted, fontFamily = FontFamily.Monospace)
                            }
                        },
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.TimePicker(state = timePickerState)
                            }
                        },
                        containerColor = currentBgColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // SECONDARY UTILITIES SUB-PACK
            var showHardwareEngine by remember { mutableStateOf(false) }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(currentBgColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .border(1.dp, textMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .clickable { triggerInterfaceFeedback(); showHardwareEngine = !showHardwareEngine }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("HARDWARE INTEGRATION LINK", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(if (showHardwareEngine) "CLOSE ▲" else "OPEN ▼", color = neonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                if (showHardwareEngine) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(currentBgColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .border(1.dp, textMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { triggerInterfaceFeedback(); onOpenSensorPicker("TEMP") }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = tempEntityId.ifEmpty { "ATTACH TEMP SENSOR ID..." }, color = if (tempEntityId.isEmpty()) textMuted else currentTextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(currentBgColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .border(1.dp, textMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { triggerInterfaceFeedback(); onOpenSensorPicker("HUMIDITY") }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = humidityEntityId.ifEmpty { "ATTACH HUMIDITY SENSOR ID..." }, color = if (humidityEntityId.isEmpty()) textMuted else currentTextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // 3. SECURE DESTRUCTIVE PROTOCOL ACTION BOX
            if (!showDeleteConfirmation) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .background(Color.Red.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable {
                            triggerInterfaceFeedback()
                            showDeleteConfirmation = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("PURGE CLIMATE ZONE FROM MATRIX", color = Color.Red.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .background(textMuted.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .border(1.dp, textMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                showDeleteConfirmation = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CANCEL", color = currentTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .height(38.dp)
                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .border(2.dp, Color.Red, RoundedCornerShape(6.dp))
                            .clickable {
                                triggerInterfaceFeedback()
                                val slugToDelete = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

                                haClient?.deleteHelperEntity(slugToDelete, "target")
                                haClient?.deleteHelperEntity(slugToDelete, "schedule")
                                haClient?.deleteHelperEntity(slugToDelete, "sensors")

                                showDeleteConfirmation = false
                                onDrillRoom(null)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "CONFIRM", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, maxLines = 1)
                    }
                }
            }
        }
    }
}