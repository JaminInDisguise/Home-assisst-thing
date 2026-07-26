package com.example.homeassisstthing


import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SettingsView(
    // System Styling/Themes
    textMuted: Color,
    neonCyan: Color,
    neonGreen: Color,
    currentTextColor: Color,
    currentCardColor: Color,
    currentBgColor: Color,

    // State Values (Read)
    deviceList: List<SmartDevice>,
    customEntityAliases: Map<String, String>,
    connectionStatus: String,
    diagnosticPingResult: String,
    localIpAddress: String,
    targetHostname: String,

    // State Variables (Pass-through for local Mutables)
    autoThemeBySun: Boolean,
    sunDayThemeId: Int,
    sunNightThemeId: Int,
    selectedThemeId: Int,
    macro1Name: String,
    macro2Name: String,
    macro1Entities: MutableList<String>,
    macro2Entities: MutableList<String>,
    haIpAddress: String,
    haAccessToken: String,
    keepScreenAwake: Boolean,
    enableBurnInProtection: Boolean,
    enableSleepTimer: Boolean,
    sleepHour: Int,
    sleepMinute: Int,
    wakeHour: Int,
    wakeMinute: Int,
    wakeDurationMinutes: Float,

    // Event & Mutual Mutator Callbacks
    triggerInterfaceFeedback: () -> Unit,
    onAutoThemeBySunChange: (Boolean) -> Unit,
    onSunDayThemeIdChange: (Int) -> Unit,
    onSunNightThemeIdChange: (Int) -> Unit,
    onSelectedThemeIdChange: (Int) -> Unit,
    onMacro1NameChange: (String) -> Unit,
    onMacro2NameChange: (String) -> Unit,
    onHaIpAddressChange: (String) -> Unit,
    onHaAccessTokenChange: (String) -> Unit,
    onKeepScreenAwakeChange: (Boolean) -> Unit,
    onEnableBurnInProtectionChange: (Boolean) -> Unit,
    onEnableSleepTimerChange: (Boolean) -> Unit,
    onSleepTimeAdjustment: (hour: Int, minute: Int) -> Unit,
    onWakeTimeAdjustment: (hour: Int, minute: Int) -> Unit,
    onWakeDurationChange: (Float) -> Unit,
    schedulePath: String,
    onSchedulePathChange: (String) -> Unit,

    // External Core Actions
    onApplyAndReconnectHA: (ip: String, token: String) -> Unit,
    onRunVerificationPing: () -> Unit,
    onForceSystemBlackout: () -> Unit,
    onTriggerRenameDialog: (device: SmartDevice, rawCurrentAlias: String) -> Unit,
    formatDeviceState: (entityId: String, state: String, domain: String) -> String
) {
    // Isolated UI Control Flags for internal Settings navigation mechanics
    var showRawRegistry by remember { mutableStateOf(false) }
    var activeRegistryFilter by remember { mutableStateOf("ALL") }
    var expandedMacroSetup by remember { mutableStateOf(0) }

    if (!showRawRegistry) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    "SYSTEM CONFIGURATIONS",
                    color = textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Unified Appearance Configuration Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor),
                    border = BorderStroke(0.5.dp, textMuted.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Column {
                            Text(
                                "SYSTEM APPEARANCE ",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Manage display profile styles manually or hand control over to the sun.",
                                color = textMuted,
                                fontSize = 10.sp
                            )
                        }

                        // 1. Master Auto Switch Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(currentBgColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Change Theme Based on the sun", color = currentTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Auto-switch look based on Home Assistant dawn/dusk metrics.",
                                    color = textMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = autoThemeBySun,
                                onCheckedChange = { isChecked ->
                                    triggerInterfaceFeedback()
                                    onAutoThemeBySunChange(isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = neonCyan,
                                    checkedTrackColor = neonCyan.copy(alpha = 0.4f)
                                )
                            )
                        }

                        // 2. Conditional Interface Generation Block
                        if (autoThemeBySun) {
                            HorizontalDivider(color = textMuted.copy(alpha = 0.15f), thickness = 0.5.dp)

                            val themeNames = listOf("Cyber", "PipBoy", "Stealth", "Amber", "Solarized", "Bloodline", "Material", "Apple")

                            // Day Theme Sub-Grid Selection
                            Column {
                                Text("ASSIGN DAYTIME PROFILE", color = neonGreen, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    themeNames.forEachIndexed { idx, name ->
                                        val isSelected = sunDayThemeId == idx
                                        OutlinedButton(
                                            onClick = {
                                                triggerInterfaceFeedback()
                                                onSunDayThemeIdChange(idx)
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            border = BorderStroke(1.dp, if (isSelected) neonGreen else textMuted.copy(alpha = 0.15f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) neonGreen.copy(alpha = 0.08f) else Color.Transparent
                                            )
                                        ) {
                                            Text(name.take(4).uppercase(), color = if (isSelected) neonGreen else textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Night Theme Sub-Grid Selection
                            Column {
                                Text("ASSIGN NIGHTTIME PROFILE", color = neonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    themeNames.forEachIndexed { idx, name ->
                                        val isSelected = sunNightThemeId == idx
                                        OutlinedButton(
                                            onClick = {
                                                triggerInterfaceFeedback()
                                                onSunNightThemeIdChange(idx)
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            border = BorderStroke(1.dp, if (isSelected) neonCyan else textMuted.copy(alpha = 0.15f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isSelected) neonCyan.copy(alpha = 0.08f) else Color.Transparent
                                            )
                                        ) {
                                            Text(name.take(4).uppercase(), color = if (isSelected) neonCyan else textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            // MANUAL MODE: Render full static stream selector sheet
                            HorizontalDivider(color = textMuted.copy(alpha = 0.15f), thickness = 0.5.dp)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val themeItems = listOf(
                                    "CYBERPUNK // SYSTEM CORE" to 0,
                                    "PIP-BOY 3000 // RAD PHOSPHOR" to 1,
                                    "STEALTH MODE // OBSIDIAN ULTRA" to 2,
                                    "VINTAGE OS // AMBER PHOSPHOR" to 3,
                                    "SOLARIZED // DAYLIGHT CANVAS" to 4,
                                    "TACTICAL CORE // NEON CRIMSON" to 5,
                                    "ANDROID M3 // DYNAMIC TEAL" to 6,
                                    "APPLE IOS // LIGHT LUX SYSTEM" to 7
                                )

                                themeItems.forEach { (label, index) ->
                                    val isCurrent = selectedThemeId == index

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .background(
                                                if (isCurrent) neonCyan.copy(alpha = 0.08f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isCurrent) neonCyan else textMuted.copy(alpha = 0.15f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                triggerInterfaceFeedback()
                                                onSelectedThemeIdChange(index)
                                            }
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isCurrent) neonCyan else textMuted,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .border(1.dp, if (isCurrent) neonCyan else textMuted.copy(alpha = 0.4f), RoundedCornerShape(50.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(neonCyan, RoundedCornerShape(50.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MACRO CONFIGURATION PANEL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor),
                    border = BorderStroke(1.dp, textMuted.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MACRO CUSTOMASIZATION",
                            color = neonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Assign individual devices to custom groups for single-press activation loops.",
                            color = textMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { expandedMacroSetup = if (expandedMacroSetup == 1) 0 else 1 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (expandedMacroSetup == 1) neonCyan.copy(alpha = 0.05f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (expandedMacroSetup == 1) neonCyan else textMuted.copy(alpha = 0.3f))
                            ) {
                                Text("EDIT ${macro1Name.uppercase()}", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = { expandedMacroSetup = if (expandedMacroSetup == 2) 0 else 2 },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (expandedMacroSetup == 2) neonGreen.copy(alpha = 0.05f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (expandedMacroSetup == 2) neonGreen else textMuted.copy(alpha = 0.3f))
                            ) {
                                Text("EDIT ${macro2Name.uppercase()}", fontSize = 10.sp)
                            }
                        }

                        if (expandedMacroSetup != 0) {
                            val targetingMacro1 = expandedMacroSetup == 1
                            val activeMacroName = if (targetingMacro1) macro1Name else macro2Name
                            val activeSelectedList = if (targetingMacro1) macro1Entities else macro2Entities

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = textMuted.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = activeMacroName,
                                onValueChange = { newVal ->
                                    if (targetingMacro1) onMacro1NameChange(newVal) else onMacro2NameChange(newVal)
                                },
                                label = { Text("MACRO LABEL NAME", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = TextStyle(color = currentTextColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "TOGGLE TARGET ENTITIES (${activeSelectedList.size} SELECTED):",
                                color = textMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .padding(top = 6.dp)
                                    .border(1.dp, textMuted.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .background(currentBgColor.copy(alpha = 0.4f))
                            ) {
                                LazyColumn(
                                    modifier = Modifier.padding(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val allocatableDevices = deviceList.filter { it.domain == "light" || it.domain == "switch" }

                                    items(allocatableDevices) { device ->
                                        val isIncluded = activeSelectedList.contains(device.entityId)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isIncluded) neonCyan.copy(alpha = 0.05f) else Color.Transparent,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .clickable {
                                                    triggerInterfaceFeedback()
                                                    if (isIncluded) {
                                                        activeSelectedList.remove(device.entityId)
                                                    } else {
                                                        activeSelectedList.add(device.entityId)
                                                    }
                                                    // Trigger mutation save block via layout forcing update loop trigger
                                                    if (targetingMacro1) onMacro1NameChange(macro1Name) else onMacro2NameChange(macro2Name)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(device.friendlyName, color = currentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(device.entityId, color = textMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                            }
                                            Checkbox(
                                                checked = isIncluded,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(checkedColor = neonCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DYNAMIC SERVER CONFIG CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor),
                    border = BorderStroke(1.dp, textMuted.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SERVER CONNECTION CONFIGURATION",
                            color = neonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Configure local network pathing and authentication parameters below.",
                            color = textMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = haIpAddress,
                            onValueChange = { onHaIpAddressChange(it) },
                            label = { Text("HOME ASSISTANT ENDPOINT IP & PORT", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color = currentTextColor, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = textMuted.copy(alpha = 0.3f),
                                focusedLabelColor = neonCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = haAccessToken,
                            onValueChange = { onHaAccessTokenChange(it) },
                            label = { Text("LONG-LIVED ACCESS TOKEN", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color = currentTextColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = textMuted.copy(alpha = 0.3f),
                                focusedLabelColor = neonCyan
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = schedulePath,
                            onValueChange = { newPath ->
                                onSchedulePathChange(newPath)
                            },
                            label = { Text("Schedule Dashboard Path",fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            placeholder = { Text("e.g. schedule or heating-schedule") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(color = currentTextColor, fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan,
                                unfocusedBorderColor = textMuted.copy(alpha = 0.3f),
                                focusedLabelColor = neonCyan
                            )

                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                triggerInterfaceFeedback()
                                onApplyAndReconnectHA(haIpAddress, haAccessToken)
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = neonCyan.copy(alpha = 0.1f))
                        ) {
                            Text(
                                "APPLY & RE-CONNECT",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // CONNECTION DIAGNOSTICS & PING TESTER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor),
                    border = BorderStroke(1.dp, textMuted.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "NETWORK DIAGNOSTICS",
                            color = neonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Verify network integrity and physical path mapping directly from this node.",
                            color = textMuted,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PANEL LOCAL IP", color = currentTextColor, fontSize = 12.sp)
                            Text(text = localIpAddress, color = textMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        HorizontalDivider(color = textMuted.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TARGET HOSTNAME", color = currentTextColor, fontSize = 12.sp)
                            Text(text = targetHostname, color = textMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        HorizontalDivider(color = textMuted.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PING ROUTE STATUS", color = currentTextColor, fontSize = 12.sp)

                            val pingColor = when {
                                diagnosticPingResult.contains("SUCCESS") -> neonGreen
                                diagnosticPingResult.contains("TESTING") -> neonCyan
                                diagnosticPingResult.contains("FAILED") || diagnosticPingResult.contains("ERROR") -> Color(0xFFFF5555)
                                else -> textMuted
                            }
                            Text(
                                text = diagnosticPingResult,
                                color = pingColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                triggerInterfaceFeedback()
                                onRunVerificationPing()
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = neonCyan.copy(alpha = 0.02f)),
                            border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.25f))
                        ) {
                            Text(
                                "RUN VERIFICATION PING TRACE",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // POWER MANAGEMENT CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor),
                    border = BorderStroke(1.dp, textMuted.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("DISPLAY CONTROLS", color = currentTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("KEEP SCREEN ON", color = currentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Prevents device from locking and keeps the screen on", color = textMuted, fontSize = 10.sp)
                            }
                            Switch(
                                checked = keepScreenAwake,
                                onCheckedChange = {
                                    triggerInterfaceFeedback()
                                    onKeepScreenAwakeChange(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = neonCyan, checkedTrackColor = neonCyan.copy(alpha = 0.3f))
                            )
                        }

                        HorizontalDivider(color = textMuted.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("BURN-IN PROTECTION", color = currentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Periodically micro-shifts interface pixels to prevent static image retention", color = textMuted, fontSize = 10.sp)
                            }
                            Switch(
                                checked = enableBurnInProtection,
                                onCheckedChange = {
                                    triggerInterfaceFeedback()
                                    onEnableBurnInProtectionChange(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = neonCyan, checkedTrackColor = neonCyan.copy(alpha = 0.3f))
                            )
                        }

                        HorizontalDivider(color = textMuted.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NIGHT MODE", color = currentTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Set times for the screen to go blank for when you're sleeping. Touch screen to wake again. Still prevents device locking", color = textMuted, fontSize = 10.sp)
                            }
                            Switch(
                                checked = enableSleepTimer,
                                onCheckedChange = {
                                    triggerInterfaceFeedback()
                                    onEnableSleepTimerChange(it)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = neonCyan, checkedTrackColor = neonCyan.copy(alpha = 0.3f))
                            )
                        }

                        if (enableSleepTimer) {
                            val countdownText = remember(sleepHour, sleepMinute) {
                                val calendar = java.util.Calendar.getInstance()
                                val currentH = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                                val currentM = calendar.get(java.util.Calendar.MINUTE)
                                val currentTotalMinutes = (currentH * 60) + currentM
                                val targetTotalMinutes = (sleepHour * 60) + sleepMinute
                                var diff = targetTotalMinutes - currentTotalMinutes
                                if (diff <= 0) diff += 24 * 60
                                val hoursLeft = diff / 60
                                val minsLeft = diff % 60
                                "SYSTEM ENGAGEMENT IN: ${hoursLeft}H ${minsLeft}M"
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = countdownText,
                                color = neonCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = textMuted.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(bottom = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "BLACKOUT START", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = String.format("%02d:%02d", sleepHour, sleepMinute),
                                    color = if (enableSleepTimer) neonCyan else textMuted,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("H-", "H+", "M-", "M+").forEach { label ->
                                        OutlinedButton(
                                            onClick = {
                                                triggerInterfaceFeedback()
                                                var h = sleepHour
                                                var m = sleepMinute
                                                when (label) {
                                                    "H-" -> if (h > 0) h-- else h = 23
                                                    "H+" -> if (h < 23) h++ else h = 0
                                                    "M-" -> if (m >= 15) m -= 15 else m = 45
                                                    "M+" -> if (m <= 30) m += 15 else m = 0
                                                }
                                                onSleepTimeAdjustment(h, m)
                                            },
                                            enabled = enableSleepTimer,
                                            modifier = Modifier.width(38.dp).height(32.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            border = BorderStroke(1.dp, textMuted.copy(alpha = 0.2f))
                                        ) {
                                            Text(label, color = if (enableSleepTimer) currentTextColor else textMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "BLACKOUT WAKE", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = String.format("%02d:%02d", wakeHour, wakeMinute),
                                    color = if (enableSleepTimer) neonCyan else textMuted,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("H-", "H+", "M-", "M+").forEach { label ->
                                        OutlinedButton(
                                            onClick = {
                                                triggerInterfaceFeedback()
                                                var h = wakeHour
                                                var m = wakeMinute
                                                when (label) {
                                                    "H-" -> if (h > 0) h-- else h = 23
                                                    "H+" -> if (h < 23) h++ else h = 0
                                                    "M-" -> if (m >= 15) m -= 15 else m = 45
                                                    "M+" -> if (m <= 30) m += 15 else m = 0
                                                }
                                                onWakeTimeAdjustment(h, m)
                                            },
                                            enabled = enableSleepTimer,
                                            modifier = Modifier.width(38.dp).height(32.dp),
                                            contentPadding = PaddingValues(0.dp),
                                            border = BorderStroke(1.dp, textMuted.copy(alpha = 0.2f))
                                        ) {
                                            Text(label, color = if (enableSleepTimer) currentTextColor else textMuted, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = textMuted.copy(alpha = 0.1f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))

                        val displayedSeconds = (wakeDurationMinutes * 60).roundToInt()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SCREEN TIMEOUT DELAY", color = currentTextColor, fontSize = 11.sp)
                            Text(
                                text = if (displayedSeconds >= 60) "${displayedSeconds / 60}m ${displayedSeconds % 60}s" else "${displayedSeconds}s",
                                color = neonCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = wakeDurationMinutes,
                            onValueChange = { onWakeDurationChange(it) },
                            valueRange = 0.5f..5.0f,
                            colors = SliderDefaults.colors(thumbColor = neonCyan, activeTrackColor = neonCyan),
                            enabled = enableSleepTimer
                        )
                        HorizontalDivider(color = textMuted.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 12.dp))

                        OutlinedButton(
                            onClick = {
                                triggerInterfaceFeedback()
                                onForceSystemBlackout()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = neonCyan.copy(alpha = 0.03f)),
                            border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.3f))
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(color = neonCyan, shape = RoundedCornerShape(50.dp)))
                                Text(
                                    text = "FORCE SYSTEM BLACKOUT",
                                    color = neonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // Static Status Indicator Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = currentCardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("ACTIVE ENDPOINT RUNTIME", color = currentTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(haIpAddress, color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = connectionStatus.uppercase(),
                            color = neonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Button(
                    onClick = {
                        triggerInterfaceFeedback()
                        showRawRegistry = true
                        activeRegistryFilter = "ALL"
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = textMuted.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, textMuted.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("EXPLORE RAW SYSTEM ENTITIES", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "← BACK",
                    color = neonCyan,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable {
                        triggerInterfaceFeedback()
                        showRawRegistry = false
                    }.padding(end = 16.dp)
                )
                Text("GLOBAL REGISTRY ARCHIVE", color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "LIGHTS", "SWITCHES", "SENSORS").forEach { filterType ->
                    val isSelected = activeRegistryFilter == filterType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) neonCyan.copy(alpha = 0.12f) else currentCardColor,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) neonCyan else textMuted.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable {
                                triggerInterfaceFeedback()
                                activeRegistryFilter = filterType
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterType,
                            color = if (isSelected) neonCyan else textMuted,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            val filteredDeviceList = remember(deviceList, activeRegistryFilter) {
                deviceList.filter { dev ->
                    if (dev.entityId.contains("sun_next")) return@filter false
                    when (activeRegistryFilter) {
                        "LIGHTS" -> dev.domain == "light"
                        "SWITCHES" -> dev.domain == "switch"
                        "SENSORS" -> dev.domain == "sensor" || dev.domain == "binary_sensor"
                        else -> true
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredDeviceList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text("NO SEGMENTS MATCHING FILTER", color = textMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                } else {
                    items(filteredDeviceList) { device ->
                        val resolvedDisplayName = customEntityAliases[device.entityId] ?: device.friendlyName

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            triggerInterfaceFeedback()
                                            onTriggerRenameDialog(
                                                device,
                                                customEntityAliases[device.entityId] ?: ""
                                            )
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = currentCardColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = resolvedDisplayName, color = currentTextColor, fontSize = 15.sp)
                                    Text(text = device.entityId, color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }

                                val formattedStateText = formatDeviceState(device.entityId, device.state, device.domain)
                                val isStateActive = device.state == "ON" || device.state == "HOME"

                                Text(
                                    text = formattedStateText,
                                    color = if (isStateActive) neonGreen else textMuted,
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