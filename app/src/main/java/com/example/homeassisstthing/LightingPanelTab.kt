package com.example.homeassisstthing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.items




// Assuming your Device object schema looks similar to this.
// Adjust imports or exact property names if required.
data class DeviceItem(
    val entityId: String,
    val domain: String,
    val state: String,
    val friendlyName: String,
    val brightness: Float,
    val isColorCapable: Boolean
)

@Composable
fun LightingView(
    deviceList: List<SmartDevice>,
    customEntityAliases: Map<String, String>,
    activeDetailedLight: SmartDevice?,
    textMuted: Color,
    currentTextColor: Color,
    currentCardColor: Color,
    currentBgColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    activeTimersMinutesMap: MutableMap<String, Int>,
    timerTargetEpochMap: MutableMap<String, Long>,
    haClientInitialized: Boolean,
    onActiveDetailedLightChange: (SmartDevice?) -> Unit, // ← Changed here
    onRenameTriggered: (device: SmartDevice, currentAlias: String) -> Unit, // ← Changed here
    onToggleLight: (entityId: String, nextStateOn: Boolean) -> Unit,
    onSetBrightness: (entityId: String, brightness: Float) -> Unit,
    onSetColorTemp: (entityId: String, miredValue: Int) -> Unit,
    onSetRgbColor: (entityId: String, r: Int, g: Int, b: Int) -> Unit,
    onStartSleepTimer: (entityId: String, minutes: Int) -> Unit,
    onFeedbackTrigger: () -> Unit
) {
    val lightDevices = deviceList.filter { it.domain == "light" }

    if (activeDetailedLight == null) {
        // =================================================================
        // VIEW A: STANDARD LIGHTING LIST AND ON/OFF CONTROL
        // =================================================================
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "LIGHT CONTROLS (${lightDevices.size})",
                color = textMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lightDevices) { light ->
                    val isLightOn = light.state == "ON"
                    val resolvedDisplayName = customEntityAliases[light.entityId] ?: light.friendlyName

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        onFeedbackTrigger()
                                        onActiveDetailedLightChange(light)
                                    },
                                    onLongPress = {
                                        onFeedbackTrigger()
                                        val currentAlias = customEntityAliases[light.entityId] ?: ""
                                        onRenameTriggered(light, currentAlias)
                                    }
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = currentCardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = resolvedDisplayName,
                                        color = currentTextColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "TAP FOR ADVANCED CONTROLS",
                                        color = textMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Button(
                                    onClick = {
                                        onFeedbackTrigger()
                                        onToggleLight(light.entityId, !isLightOn)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isLightOn) neonGreen.copy(alpha = 0.15f) else textMuted.copy(alpha = 0.10f)
                                    )
                                ) {
                                    Text(
                                        text = if (isLightOn) "ON" else "OFF",
                                        color = if (isLightOn) neonGreen else textMuted,
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
    } else {
        // =================================================================
        // VIEW B: FOCUSED FINE-TUNING SCREEN CONTROL COCKPIT
        // =================================================================
        val currentTarget = activeDetailedLight
        val liveLight = deviceList.find { it.entityId == currentTarget.entityId } ?: currentTarget
        val isLightOn = liveLight.state == "ON"
        val resolvedDisplayName = customEntityAliases[liveLight.entityId] ?: liveLight.friendlyName

        var localSliderValue by remember(liveLight.entityId) { mutableStateOf(liveLight.brightness) }

        // Local states for RGB Color Selection Sliders
        var redValue by remember(liveLight.entityId) { mutableStateOf(255f) }
        var greenValue by remember(liveLight.entityId) { mutableStateOf(255f) }
        var blueValue by remember(liveLight.entityId) { mutableStateOf(255f) }

        // Track active timer duration
        val currentTimerMins = activeTimersMinutesMap[liveLight.entityId] ?: 0

        // Force UI text update cycle ticks
        var localTickTrigger by remember { mutableStateOf(0) }
        LaunchedEffect(currentTimerMins) {
            if (currentTimerMins > 0) {
                while (true) {
                    delay(1000)
                    localTickTrigger++
                }
            }
        }

        // Calculate remaining seconds dynamically
        val currentRemainingSecs = remember(liveLight.entityId, localTickTrigger) {
            val targetEpoch = timerTargetEpochMap[liveLight.entityId] ?: 0L
            val currentEpoch = System.currentTimeMillis() / 1000
            (targetEpoch - currentEpoch).coerceAtLeast(0L)
        }

        // Auto-clean maps if time runs out naturally
        LaunchedEffect(currentRemainingSecs) {
            if (currentTimerMins > 0 && currentRemainingSecs == 0L) {
                activeTimersMinutesMap[liveLight.entityId] = 0
                timerTargetEpochMap[liveLight.entityId] = 0L
            }
        }

        // Synchronize intensity slider live from external events
        LaunchedEffect(liveLight.brightness) {
            localSliderValue = liveLight.brightness
        }

        // Core Debouncer for Brightness Slider
        LaunchedEffect(localSliderValue) {
            if (isLightOn && Math.abs(localSliderValue - liveLight.brightness) > 1f) {
                delay(150)
                onSetBrightness(liveLight.entityId, localSliderValue)
            }
        }

        // Flood prevention for RGB parameters
        var lastSentRgb by remember(liveLight.entityId) { mutableStateOf(Triple(255, 255, 255)) }

        LaunchedEffect(redValue, greenValue, blueValue) {
            if (isLightOn) {
                delay(250)
                val currentR = redValue.toInt().coerceIn(0, 255)
                val currentG = greenValue.toInt().coerceIn(0, 255)
                val currentB = blueValue.toInt().coerceIn(0, 255)
                val currentTriple = Triple(currentR, currentG, currentB)

                if (currentTriple != lastSentRgb) {
                    onSetRgbColor(liveLight.entityId, currentR, currentG, currentB)
                    lastSentRgb = currentTriple
                }
            }
        }

        val cockpitScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(cockpitScrollState)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Top Header Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← BACK",
                    color = neonCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable {
                            onFeedbackTrigger()
                            onActiveDetailedLightChange(null)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }

            // Central Control Console Card with Ambient Glow
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = currentCardColor),
                border = BorderStroke(
                    width = if (isLightOn) 2.dp else 1.dp,
                    color = if (isLightOn) neonCyan.copy(alpha = 0.8f) else neonCyan.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Identity Header Segment
                    Column {
                        Text(
                            text = "ADVANCED LIGHT CONTROLS",
                            color = neonCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = resolvedDisplayName,
                            color = currentTextColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = liveLight.entityId.uppercase(),
                            color = textMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // System Status Readout Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(
                                if (isLightOn) neonGreen.copy(alpha = 0.06f) else textMuted.copy(alpha = 0.02f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (isLightOn) neonGreen.copy(alpha = 0.3f) else textMuted.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentRemainingSecs > 0) "SLEEP TIMER STARTED" else if (isLightOn) "LIGHT ON" else "LIGHT OFF",
                            color = if (currentRemainingSecs > 0) neonCyan else if (isLightOn) neonGreen else textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Master Toggle Command Button
                    Button(
                        onClick = {
                            onFeedbackTrigger()
                            onToggleLight(liveLight.entityId, !isLightOn)
                            if (!isLightOn && localSliderValue < 5f) {
                                localSliderValue = 100f
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLightOn) neonGreen.copy(alpha = 0.15f) else textMuted.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isLightOn) neonGreen.copy(alpha = 0.4f) else textMuted.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = if (isLightOn) "POWER COMMAND: OFF" else "POWER COMMAND: ON",
                            color = if (isLightOn) neonGreen else textMuted,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }

                    // Intensity Dimmer Slider Module
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LIGHT BRIGHTNESS CONTROL",
                                color = if (isLightOn) neonCyan else textMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isLightOn) "${localSliderValue.roundToInt()}%" else "OFFLINE",
                                color = if (isLightOn) neonCyan else textMuted,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = localSliderValue,
                            onValueChange = { if (isLightOn) localSliderValue = it },
                            valueRange = 1f..100f,
                            enabled = isLightOn,
                            colors = SliderDefaults.colors(
                                thumbColor = neonCyan,
                                activeTrackColor = neonCyan,
                                inactiveTrackColor = textMuted.copy(alpha = 0.2f)
                            )
                        )
                    }

                    // Split Layout Options Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left Matrix Pane: Color Temp Selection Grid
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "COLOR TEMPERATURE",
                                color = if (isLightOn) neonCyan else textMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )

                            val colorTemps = listOf(
                                Triple("COLD WHITE", 153, Color(0xFFDDF2FF)),
                                Triple("BALANCE", 300, Color(0xFFFFFFFE)),
                                Triple("WARM WHITE", 450, Color(0xFFFFE3A6))
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                colorTemps.forEach { (name, miredValue, indicatorColor) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .background(
                                                if (isLightOn) indicatorColor.copy(alpha = 0.12f) else textMuted.copy(alpha = 0.05f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isLightOn) indicatorColor.copy(alpha = 0.4f) else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable(enabled = isLightOn) {
                                                onFeedbackTrigger()
                                                onSetColorTemp(liveLight.entityId, miredValue)
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(indicatorColor, androidx.compose.foundation.shape.CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = name,
                                            color = if (isLightOn) currentTextColor else textMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Right Matrix Pane: Countdown Sleep Timers
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "SLEEP TIMER",
                                color = if (isLightOn) neonCyan else textMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val timeOptions = listOf(15, 30, 45)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                timeOptions.forEach { minutes ->
                                    val isCurrentTimer = currentTimerMins == minutes
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .background(
                                                if (!isLightOn) textMuted.copy(alpha = 0.05f)
                                                else if (isCurrentTimer) neonGreen.copy(alpha = 0.15f)
                                                else neonCyan.copy(alpha = 0.05f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isCurrentTimer) neonGreen else Color.Transparent,
                                                RoundedCornerShape(6.dp)
                                            )
                                            .clickable(enabled = isLightOn) {
                                                onFeedbackTrigger()
                                                if (isCurrentTimer) {
                                                    activeTimersMinutesMap[liveLight.entityId] = 0
                                                    timerTargetEpochMap[liveLight.entityId] = 0L
                                                } else {
                                                    val futureTargetEpoch = (System.currentTimeMillis() / 1000) + (minutes * 60)
                                                    activeTimersMinutesMap[liveLight.entityId] = minutes
                                                    timerTargetEpochMap[liveLight.entityId] = futureTargetEpoch
                                                    onStartSleepTimer(liveLight.entityId, minutes)
                                                }
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        val buttonText = if (isCurrentTimer && currentRemainingSecs > 0) {
                                            val mins = currentRemainingSecs / 60
                                            val secs = currentRemainingSecs % 60
                                            "CANCEL (${mins}m ${secs}s)"
                                        } else {
                                            "OFF IN $minutes MIN"
                                        }

                                        Text(
                                            text = buttonText,
                                            color = if (!isLightOn) textMuted else if (isCurrentTimer) neonGreen else currentTextColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chromatic Core Segment: RGB Mixing Sliders
                    if (liveLight.isColorCapable) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "RGB CONTROL",
                                color = if (isLightOn) neonCyan else textMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            // Red Mixing Track
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "R",
                                    color = if (isLightOn) Color.Red else textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(16.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = redValue,
                                    onValueChange = { if (isLightOn) redValue = it },
                                    valueRange = 0f..255f,
                                    enabled = isLightOn,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Red,
                                        activeTrackColor = Color.Red.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Green Mixing Track
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "G",
                                    color = if (isLightOn) Color.Green else textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(16.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = greenValue,
                                    onValueChange = { if (isLightOn) greenValue = it },
                                    valueRange = 0f..255f,
                                    enabled = isLightOn,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Green,
                                        activeTrackColor = Color.Green.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Blue Mixing Track
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "B",
                                    color = if (isLightOn) Color.Blue else textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(16.dp),
                                    fontFamily = FontFamily.Monospace
                                )
                                Slider(
                                    value = blueValue,
                                    onValueChange = { if (isLightOn) blueValue = it },
                                    valueRange = 0f..255f,
                                    enabled = isLightOn,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.Blue,
                                        activeTrackColor = Color.Blue.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}