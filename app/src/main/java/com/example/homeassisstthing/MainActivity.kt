package com.example.homeassisstthing

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Calendar
import java.util.Collections

// =================================================================
// SYSTEM THEME STRUCTURE DEFINITIONS
// =================================================================
data class PanelTheme(
    val bg: Color,
    val card: Color,
    val text: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val systemNameLabel: String,
    val menuDashboardLabel: String,
    val menuSettingsLabel: String,
    val telemetryHeaderLabel: String,
    val consumptionLabel: String,
    val blackoutModeButtonLabel: String
)

val CyberpunkTheme = PanelTheme(
    bg = Color(0xFF0D0E15), card = Color(0xFF161924), text = Color.White,
    textMuted = Color(0xFF7E8494), primaryAccent = Color(0xFF00F0FF), secondaryAccent = Color(0xFF00FF66),
    systemNameLabel = "HA_NODE_SYS_v4.2", menuDashboardLabel = "DASHBOARD", menuSettingsLabel = "SYSTEM SETTINGS",
    telemetryHeaderLabel = "HOUSE TELEMETRY STREAM", consumptionLabel = "CURRENT TOTAL HOME GRID CONSUMPTION LOAD",
    blackoutModeButtonLabel = "ENGAGE SYSTEM BACKLIGHT BLACKOUT MODE"
)

val PipBoyRadTheme = PanelTheme(
    bg = Color(0xFF050A05), card = Color(0xFF0D140D), text = Color(0xFF1FFF7F),
    textMuted = Color(0xFF128243), primaryAccent = Color(0xFF1FFF7F), secondaryAccent = Color(0xFF00A3E0),
    systemNameLabel = "ROBCO PIP-BOY OS v3.00", menuDashboardLabel = "STAT // DATA", menuSettingsLabel = "PRESETS // SETUP",
    telemetryHeaderLabel = "VAULT POWER MANAGEMENT NODE", consumptionLabel = "TOTAL POWER-GRID OUTFLOW // DISCHARGE CAPACITANCE",
    blackoutModeButtonLabel = "TOGGLE STEALTH FIELD PERIPHERAL TERMINATION"
)

val ObsidianStealthTheme = PanelTheme(
    bg = Color(0xFF000000), card = Color(0xFF111111), text = Color(0xFFE5E5E5),
    textMuted = Color(0xFF666666), primaryAccent = Color(0xFFFFFFFF), secondaryAccent = Color(0xFF333333),
    systemNameLabel = "STEALTH_TERMINAL", menuDashboardLabel = "OVERVIEW", menuSettingsLabel = "CONFIGS",
    telemetryHeaderLabel = "CORE DATA MONITOR", consumptionLabel = "CURRENT POWER LOAD SENSOR MATRIX",
    blackoutModeButtonLabel = "KILL SCREEN DISCHARGE"
)

val AmberTerminalTheme = PanelTheme(
    bg = Color(0xFF120A00), card = Color(0xFF1C1204), text = Color(0xFFFFB300),
    textMuted = Color(0xFF996B00), primaryAccent = Color(0xFFFF9100), secondaryAccent = Color(0xFF00E5FF),
    systemNameLabel = "INDUSTRIAL_AMBER_v1.08", menuDashboardLabel = "MATRIX STATS", menuSettingsLabel = "TERMINAL CONFIG",
    telemetryHeaderLabel = "CORE DISCHARGE OVERSEER PANEL", consumptionLabel = "GRID CONDUIT DISCHARGE COEFFICIENT RATIO",
    blackoutModeButtonLabel = "TERMINAL PHOSPHOR SHIELD DOWN"
)

val SolarizedLightTheme = PanelTheme(
    bg = Color(0xFFFDF6E3), card = Color(0xFFEEE8D5), text = Color(0xFF073642),
    textMuted = Color(0xFF93A1A1), primaryAccent = Color(0xFF268BD2), secondaryAccent = Color(0xFFD33682),
    systemNameLabel = "SOLARIZED_LIGHT_SYS", menuDashboardLabel = "Summary", menuSettingsLabel = "Preferences",
    telemetryHeaderLabel = "ENVIRONMENT METRICS BROADCAST", consumptionLabel = "TOTAL ENERGY LOAD FOOTPRINT REGISTERED",
    blackoutModeButtonLabel = "SUSPEND DISPLAY ILLUMINATION MATRIX"
)

val BloodlineTheme = PanelTheme(
    bg = Color(0xFF000000), card = Color(0xFF0F0505), text = Color(0xFFFF3333),
    textMuted = Color(0xFF7A1F1F), primaryAccent = Color(0xFFFF0000), secondaryAccent = Color(0xFFFFA500),
    systemNameLabel = "TACTICAL_BLOODLINE_NODE", menuDashboardLabel = "SENSORS OVERVIEW", menuSettingsLabel = "CHASSIS MATRIX MODS",
    telemetryHeaderLabel = "CRITICAL CHASSIS POWER FLUX NODE", consumptionLabel = "DOCK ENERGY RESOURCE DEPLETION COEFFICIENT",
    blackoutModeButtonLabel = "ENGAGE FULL STEALTH BLACKOUT SENSOR SYSTEM"
)

val AndroidMaterialTheme = PanelTheme(
    bg = Color(0xFF1A1C1E), card = Color(0xFF22252A), text = Color(0xFFE2E2E6),
    textMuted = Color(0xFF8C9199), primaryAccent = Color(0xFF82CFFF), secondaryAccent = Color(0xFFB4F1C5),
    systemNameLabel = "Android Kiosk OpenSurface", menuDashboardLabel = "Dashboard Overview", menuSettingsLabel = "System & Themes",
    telemetryHeaderLabel = "Home Framework Telemetry", consumptionLabel = "Current Grid Consumption Baseline",
    blackoutModeButtonLabel = "Enable Display Sleep Mode Override"
)

val AppleIOSTheme = PanelTheme(
    bg = Color(0xFFF2F2F7), card = Color(0xFFFFFFFF), text = Color(0xFF000000),
    textMuted = Color(0xFF8E8E93), primaryAccent = Color(0xFF007AFF), secondaryAccent = Color(0xFF34C759),
    systemNameLabel = "iOS Kiosk HomeKit Hub", menuDashboardLabel = "Home Status", menuSettingsLabel = "Hub Settings",
    telemetryHeaderLabel = "Connected Accessories Feed", consumptionLabel = "Total Residential Electricity Consumption Load",
    blackoutModeButtonLabel = "Turn Off Display Panel Matrix"
)

data class ClimateScheduleSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String,
    val targetTemp: Float,
    val isHeatingOn: Boolean = true,
    val dayTarget: String = "WEEKDAYS"
)

class MainActivity : ComponentActivity() {

    private lateinit var haClient: HomeAssistantClient

    private fun formatDeviceState(entityId: String, rawState: String, domain: String): String {
        val upperState = rawState.uppercase().trim()
        if (entityId.startsWith("person.") || entityId.startsWith("device_tracker.")) {
            return when (upperState) {
                "HOME" -> "HOME"
                "NOT_HOME", "AWAY" -> "AWAY"
                else -> upperState
            }
        }
        if (domain == "binary_sensor") {
            return when {
                entityId.contains("motion") || entityId.contains("presence") || entityId.contains("occupancy") -> if (upperState == "ON") "DETECTED" else "CLEAR"
                entityId.contains("door") || entityId.contains("window") || entityId.contains("gate") || entityId.contains("contact") -> if (upperState == "ON") "OPEN" else "CLOSED"
                entityId.contains("battery") || entityId.contains("power") -> if (upperState == "ON") "LOW" else "NORMAL"
                entityId.contains("moisture") || entityId.contains("leak") || entityId.contains("water") -> if (upperState == "ON") "LEAK DETECTED" else "DRY"
                else -> if (upperState == "ON") "ACTIVE" else "INACTIVE"
            }
        }
        if (domain == "sensor") {
            if (upperState == "UNAVAILABLE" || upperState == "UNKNOWN") return "OFFLINE"
            return when {
                entityId.contains("temperature") -> "$rawState°C"
                entityId.contains("humidity") -> "$rawState%"
                entityId.contains("battery") -> "$rawState%"
                entityId.contains("illuminance") || entityId.contains("light") -> "$rawState LX"
                entityId.contains("power") -> "$rawState W"
                entityId.contains("energy") -> "$rawState KWH"
                else -> rawState
            }
        }
        return upperState
    }

    private fun parseStringToMillis(isoString: String): Long {
        return try {
            java.time.ZonedDateTime.parse(isoString).toInstant().toEpochMilli()
        } catch (e: Exception) {
            try {
                java.time.LocalDateTime.parse(isoString)
                    .toInstant(java.time.ZoneOffset.UTC)
                    .toEpochMilli()
            } catch (ex: Exception) {
                0L
            }
        }
    }

    private fun setWindowBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val windowInsetsController =
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setContent {
            val context = LocalView.current.context
            val sharedPrefs = remember {
                context.getSharedPreferences("ha_config_prefs", MODE_PRIVATE)
            }
            val view = LocalView.current
            val triggerInterfaceFeedback = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }

            var activeDetailedLight by remember { mutableStateOf<SmartDevice?>(null) }
            val activeTimersMinutesMap = remember { mutableStateMapOf<String, Int>() }
            val timerRemainingSecondsMap = remember { mutableStateMapOf<String, Int>() }
            val timerTargetEpochMap = remember { mutableStateMapOf<String, Long>() }

            var haIpAddress by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "ha_ip",
                        "192.168.1.xx"
                    ) ?: "192.168.1.xx"
                )
            }
            var haAccessToken by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "ha_token",
                        ""
                    ) ?: ""
                )
            }
            var selectedThemeMode by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "theme_mode",
                        0
                    )
                )
            }
            var keepScreenAwake by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "keep_awake",
                        false
                    )
                )
            }
            var enableSleepTimer by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "enable_sleep_timer",
                        false
                    )
                )
            }
            var enableBurnInProtection by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "burn_in_protection",
                        false
                    )
                )
            }
            var wakeDurationMinutes by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getFloat(
                        "wake_duration",
                        1.0f
                    )
                )
            }
            var schedulePath by rememberSaveable {
                mutableStateOf(sharedPrefs.getString("schedule_path", "schedule") ?: "schedule")
            }

            var sleepHour by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "sleep_hour",
                        23
                    )
                )
            }
            var sleepMinute by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "sleep_minute",
                        30
                    )
                )
            }
            var wakeHour by rememberSaveable { mutableStateOf(sharedPrefs.getInt("wake_hour", 6)) }
            var wakeMinute by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "wake_minute",
                        30
                    )
                )
            }
            var selectedTab by rememberSaveable { mutableStateOf(0) }
            var currentMetricIndex by rememberSaveable { mutableStateOf(0) }
            var connectionStatus by rememberSaveable { mutableStateOf("Disconnected") }
            var deviceList by rememberSaveable { mutableStateOf(listOf<SmartDevice>()) }
            var drilledRoomIndex by remember { mutableStateOf<Int?>(null) }
            var showScheduleScreen by rememberSaveable { mutableStateOf(false) }
            val roomMappings = remember { mutableStateMapOf<String, Pair<String, String>>() }
            val roomTargetStates = remember { mutableStateMapOf<String, Float>() }

            LaunchedEffect(deviceList) {
                val discoveredMappings = mutableMapOf<String, Pair<String, String>>()
                val discoveredTargets = mutableMapOf<String, Float>()
                val scheduleRegex = Regex("^input_text\\.(.+)_schedule(?:_\\d+)?$")

                val scheduleEntities = deviceList.filter { it.entityId.matches(scheduleRegex) }

                scheduleEntities.forEach { scheduleDevice ->
                    val matchResult = scheduleRegex.find(scheduleDevice.entityId)
                    val roomSlug = matchResult?.groupValues?.get(1) ?: ""

                    if (roomSlug.isNotEmpty()) {
                        val roomName = roomSlug.replace("_", " ").trim().uppercase()
                        val sensorsRegex = Regex("^input_text\\.${roomSlug}_sensors(?:_\\d+)?$")
                        val sensorsDevice = deviceList.find { it.entityId.matches(sensorsRegex) }

                        val sensorsData = sensorsDevice?.state ?: ""
                        val sensorParts = sensorsData.split("|")
                        val tempSensor = sensorParts.getOrNull(0) ?: ""
                        val humSensor = sensorParts.getOrNull(1) ?: ""

                        discoveredMappings[roomName] = Pair(tempSensor, humSensor)

                        val targetRegex = Regex("^input_number\\.${roomSlug}_target(?:_\\d+)?$")
                        val targetDevice = deviceList.find { it.entityId.matches(targetRegex) }
                        discoveredTargets[roomName] = targetDevice?.state?.toFloatOrNull() ?: 21.0f
                    }
                }

                discoveredMappings.forEach { (name, sensors) ->
                    if (roomMappings[name] != sensors) roomMappings[name] = sensors
                }
                discoveredTargets.forEach { (name, target) ->
                    if (roomTargetStates[name] != target) roomTargetStates[name] = target
                }
            }

            var burnInOffsetX by rememberSaveable { mutableStateOf(0f) }
            var burnInOffsetY by rememberSaveable { mutableStateOf(0f) }

            var nextDawnMillis by rememberSaveable { mutableStateOf(0L) }
            var nextDuskMillis by rememberSaveable { mutableStateOf(0L) }
            val currentTimeMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
            var selectedThemeId by rememberSaveable { mutableStateOf(0) }

            var autoThemeBySun by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "auto_theme_by_sun",
                        false
                    )
                )
            }
            var sunDayThemeId by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "sun_day_theme_id",
                        4
                    )
                )
            }
            var sunNightThemeId by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "sun_night_theme_id",
                        0
                    )
                )
            }

            var isInsideFakeSleep by rememberSaveable { mutableStateOf(false) }
            var manualWakeSnoozeUntil by rememberSaveable { mutableStateOf(0L) }
            var isManuallyBlackedOut by rememberSaveable { mutableStateOf(false) }
            var lastInteractionTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

            var macro1Name by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "macro_1_name",
                        "MACRO 1"
                    ) ?: "MACRO 1"
                )
            }
            val macro1Entities = remember {
                mutableStateListOf<String>().apply {
                    addAll(sharedPrefs.getStringSet("macro_1_entities", emptySet()) ?: emptySet())
                }
            }

            var macro2Name by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "macro_2_name",
                        "MACRO 2"
                    ) ?: "MACRO 2"
                )
            }
            val macro2Entities = remember {
                mutableStateListOf<String>().apply {
                    addAll(sharedPrefs.getStringSet("macro_2_entities", emptySet()) ?: emptySet())
                }
            }

            var diagnosticPingResult by rememberSaveable { mutableStateOf("NOT TESTED") }
            var entityToRenameInDialog by remember { mutableStateOf<SmartDevice?>(null) }
            var temporaryAliasInputText by remember { mutableStateOf("") }
            val customEntityAliases = remember {
                mutableStateMapOf<String, String>().apply {
                    sharedPrefs.all.forEach { entry ->
                        val k = entry.key
                        val v = entry.value
                        if (k.startsWith("alias_") && v is String) {
                            put(k.removePrefix("alias_"), v)
                        }
                    }
                }
            }

            val executeDynamicMacro = { entitiesToToggle: List<String> ->
                lifecycleScope.launch(Dispatchers.Default) {
                    entitiesToToggle.forEach { entityId ->
                        if (::haClient.isInitialized) {
                            val currentDevice = deviceList.find { it.entityId == entityId }
                            val isCurrentlyOn = currentDevice?.state == "ON"
                            haClient.toggleLight(entityId, !isCurrentlyOn)
                            withContext(Dispatchers.Main) {
                                deviceList = deviceList.map {
                                    if (it.entityId == entityId) it.copy(state = if (isCurrentlyOn) "OFF" else "ON") else it
                                }
                            }
                            delay(50)
                        }
                    }
                }
            }

            val getLocalIpAddress = {
                try {
                    val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                    var foundIp = "No Connection"
                    for (networkInterface in interfaces) {
                        val addresses = Collections.list(networkInterface.inetAddresses)
                        for (address in addresses) {
                            if (!address.isLoopbackAddress) {
                                val sAddr = address.hostAddress
                                if (sAddr.indexOf(':') < 0) foundIp = sAddr
                            }
                        }
                    }
                    foundIp
                } catch (ex: Exception) {
                    "Unknown"
                }
            }

            val runNetworkDiagnosticPing = {
                diagnosticPingResult = "TESTING LINK..."
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val cleanHost = haIpAddress.replace("http://", "")
                            .replace("https://", "")
                            .split(":")
                            .first()
                            .trim()

                        val address = InetAddress.getByName(cleanHost)
                        val isReachable = address.isReachable(2000)

                        withContext(Dispatchers.Main) {
                            diagnosticPingResult =
                                if (isReachable) "SUCCESS (REACHABLE)" else "FAILED (HOST UNREACHABLE)"
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            diagnosticPingResult = "ERROR: ${e.message?.uppercase()}"
                        }
                    }
                }
            }

            val initializeAndConnectHA = { targetIp: String, targetToken: String ->
                try {
                    if (::haClient.isInitialized) {
                        haClient.disconnect()
                    }
                } catch (e: Exception) {
                }

                val cleanIp =
                    targetIp.replace("http://", "").replace("https://", "").replace("ws://", "")
                        .replace("wss://", "")
                val formattedUrl = "ws://$cleanIp/api/websocket"

                connectionStatus = "Connecting..."
                haClient = HomeAssistantClient(
                    serverUrl = formattedUrl,
                    accessToken = targetToken,
                    onMessageReceived = { rawJson: String ->
                        try {
                            val root = org.json.JSONObject(rawJson)

                            // 1. Log Command Failures
                            if (root.optString("type") == "result" && !root.optBoolean(
                                    "success",
                                    true
                                )
                            ) {
                                val errorObj = root.optJSONObject("error")
                                val errorCode = errorObj?.optString("code") ?: "unknown"
                                val errorMessage = errorObj?.optString("message") ?: "No message"
                                Log.e(
                                    "HA_CLIENT",
                                    "API Command Failed! ID: ${root.optInt("id")} | Error: [$errorCode] $errorMessage"
                                )
                            }

                            // 2. Initial State Fetch (States Dump)
                            if (root.has("type") && root.getString("type") == "result" && root.has("result")) {
                                val resultsJson = root.get("result")
                                if (resultsJson is org.json.JSONArray) {
                                    val discoveredDevices = mutableListOf<SmartDevice>()

                                    for (i in 0 until resultsJson.length()) {
                                        val entityObj = resultsJson.getJSONObject(i)
                                        val entityId = entityObj.optString("entity_id")
                                        val domain = entityId.split(".").firstOrNull() ?: ""

                                        val rawState = entityObj.optString("state")
                                        val stateValue =
                                            if (domain == "input_text") rawState else rawState.uppercase()

                                        val attributesObj = entityObj.optJSONObject("attributes")
                                        val friendlyName =
                                            attributesObj?.optString("friendly_name") ?: entityId

                                        // Extract attributes into a Map so SmartDevice can access preset_mode, preset_modes, hvac_action, etc.
                                        val attributesMap = mutableMapOf<String, Any>()
                                        attributesObj?.keys()?.forEach { key ->
                                            attributesObj.get(key)
                                                ?.let { value -> attributesMap[key] = value }
                                        }

                                        if (entityId == "sensor.sun_next_dawn") nextDawnMillis =
                                            parseStringToMillis(stateValue)
                                        if (entityId == "sensor.sun_next_dusk") nextDuskMillis =
                                            parseStringToMillis(stateValue)

                                        val currentBright =
                                            attributesObj?.optInt("brightness", -1) ?: -1
                                        val initialBrightness =
                                            if (currentBright != -1) ((currentBright / 255f) * 100f) else 50f

                                        val colorModes =
                                            attributesObj?.optJSONArray("supported_color_modes")
                                        var hasColorSupport = false
                                        if (colorModes != null) {
                                            for (j in 0 until colorModes.length()) {
                                                val mode = colorModes.optString(j).lowercase()
                                                if (mode.contains("rgb") || mode.contains("hs") || mode.contains(
                                                        "xy"
                                                    )
                                                ) {
                                                    hasColorSupport = true
                                                    break
                                                }
                                            }
                                        }

                                        val currentTemp =
                                            attributesObj?.optDouble("current_temperature", 0.0)
                                                ?.toFloat() ?: 0f
                                        val targetTemp =
                                            attributesObj?.optDouble("temperature", 0.0)?.toFloat()
                                                ?: 0f

                                        val isTargetDomain = domain in listOf(
                                            "light",
                                            "switch",
                                            "sensor",
                                            "binary_sensor",
                                            "climate",
                                            "input_text",
                                            "input_number"
                                        )

                                        if (isTargetDomain) {
                                            discoveredDevices.add(
                                                SmartDevice(
                                                    entityId = entityId,
                                                    friendlyName = friendlyName,
                                                    state = stateValue,
                                                    domain = domain,
                                                    brightness = initialBrightness,
                                                    isExpanded = false,
                                                    isColorCapable = hasColorSupport,
                                                    currentTemperature = currentTemp,
                                                    targetTemperature = targetTemp,
                                                    attributes = attributesMap // <-- PASSED HERE
                                                )
                                            )
                                        }
                                    }
                                    deviceList = discoveredDevices.sortedBy { it.friendlyName }
                                    connectionStatus = "Connected"
                                }
                            }
                            // 3. Live WebSocket Event Stream
                            else if (root.has("type") && root.getString("type") == "event") {
                                val eventObj = root.optJSONObject("event")
                                if (eventObj != null) {
                                    val eventType = eventObj.optString("event_type")
                                    val dataObj = eventObj.optJSONObject("data")

                                    if (eventType == "state_changed") {
                                        val entityId = dataObj?.optString("entity_id") ?: ""
                                        val newStateObj = dataObj?.optJSONObject("new_state")

                                        if (entityId.isNotEmpty()) {
                                            if (newStateObj != null) {
                                                val rawState = newStateObj.optString("state") ?: ""
                                                val stateValue =
                                                    if (entityId.startsWith("input_text.")) rawState else rawState.uppercase()
                                                val attributesObj =
                                                    newStateObj.optJSONObject("attributes")
                                                val friendlyName =
                                                    attributesObj?.optString("friendly_name")
                                                        ?: entityId

                                                // Extract live updated attributes
                                                val attributesMap = mutableMapOf<String, Any>()
                                                attributesObj?.keys()?.forEach { key ->
                                                    attributesObj.get(key)?.let { value ->
                                                        attributesMap[key] = value
                                                    }
                                                }

                                                val currentBright =
                                                    attributesObj?.optDouble("brightness", -1.0)
                                                        ?: -1.0
                                                val updatedBrightness =
                                                    if (currentBright != -1.0) ((currentBright / 255.0) * 100.0).toFloat() else 50f

                                                val colorModes =
                                                    attributesObj?.optJSONArray("supported_color_modes")
                                                var hasColorSupport = false
                                                if (colorModes != null) {
                                                    for (j in 0 until colorModes.length()) {
                                                        val mode =
                                                            colorModes.optString(j).lowercase()
                                                        if (mode.contains("rgb") || mode.contains("hs") || mode.contains(
                                                                "xy"
                                                            )
                                                        ) {
                                                            hasColorSupport = true
                                                            break
                                                        }
                                                    }
                                                }

                                                val currentTemp = attributesObj?.optDouble(
                                                    "current_temperature",
                                                    0.0
                                                )?.toFloat() ?: 0f
                                                val targetTemp =
                                                    attributesObj?.optDouble("temperature", 0.0)
                                                        ?.toFloat() ?: 0f

                                                val existingIndex =
                                                    deviceList.indexOfFirst { it.entityId == entityId }
                                                if (existingIndex != -1) {
                                                    deviceList = deviceList.map { device ->
                                                        if (device.entityId == entityId) {
                                                            device.copy(
                                                                state = stateValue,
                                                                brightness = updatedBrightness,
                                                                isColorCapable = hasColorSupport,
                                                                currentTemperature = currentTemp,
                                                                targetTemperature = targetTemp,
                                                                attributes = attributesMap // <-- UPDATED HERE FOR REAL-TIME SYNC
                                                            )
                                                        } else device
                                                    }
                                                } else {
                                                    val domain =
                                                        entityId.split(".").firstOrNull() ?: ""
                                                    val isTargetDomain = domain in listOf(
                                                        "light",
                                                        "switch",
                                                        "sensor",
                                                        "binary_sensor",
                                                        "climate",
                                                        "input_text",
                                                        "input_number"
                                                    )
                                                    val isRealState = rawState != "unavailable"

                                                    if (isTargetDomain && isRealState) {
                                                        val newDevice = SmartDevice(
                                                            entityId = entityId,
                                                            friendlyName = friendlyName,
                                                            state = stateValue,
                                                            domain = domain,
                                                            brightness = updatedBrightness,
                                                            isExpanded = false,
                                                            isColorCapable = hasColorSupport,
                                                            currentTemperature = currentTemp,
                                                            targetTemperature = targetTemp,
                                                            attributes = attributesMap
                                                        )
                                                        deviceList =
                                                            (deviceList + newDevice).sortedBy { it.friendlyName }
                                                    }
                                                }
                                            } else {
                                                deviceList =
                                                    deviceList.filterNot { it.entityId == entityId }
                                            }
                                        }
                                    } else if (eventType == "entity_registry_updated") {
                                        val action = dataObj?.optString("action") ?: ""
                                        val entityId = dataObj?.optString("entity_id") ?: ""

                                        if (action == "remove" && entityId.isNotEmpty()) {
                                            deviceList =
                                                deviceList.filterNot { it.entityId == entityId }
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("HA_CLIENT", "HA_PARSER: Exception caught -> ${e.message}")
                        }
                    }
                ).apply {
                    onStatusChanged = { status -> connectionStatus = status }
                }
                haClient.connect()
            }

            LaunchedEffect(showScheduleScreen) {
                while (!showScheduleScreen) { // Only attempt background sync when NOT on Chrome Schedule!
                    if (connectionStatus == "Disconnected" || connectionStatus.contains("Failed")) {
                        initializeAndConnectHA(haIpAddress, haAccessToken)
                    }
                    delay(5000)
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    try {
                        if (::haClient.isInitialized) haClient.disconnect()
                    } catch (e: Exception) {
                    }
                }
            }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(4000)
                    currentMetricIndex = (currentMetricIndex + 1) % 3
                }
            }

            LaunchedEffect(enableBurnInProtection) {
                if (enableBurnInProtection) {
                    while (true) {
                        burnInOffsetX = ((-3..3).random()).toFloat()
                        burnInOffsetY = ((-3..3).random()).toFloat()
                        delay(60000)
                    }
                } else {
                    burnInOffsetX = 0f
                    burnInOffsetY = 0f
                }
            }

            LaunchedEffect(
                lastInteractionTime,
                wakeDurationMinutes,
                enableSleepTimer,
                isInsideFakeSleep,
                isManuallyBlackedOut
            ) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val timeoutMillis = (wakeDurationMinutes * 60 * 1000).toLong()

                    if (!isInsideFakeSleep && !isManuallyBlackedOut) {
                        if (now - lastInteractionTime > timeoutMillis) {
                            setWindowBrightness(0.15f)
                        } else {
                            setWindowBrightness(-1f)
                        }
                    }
                    delay(2000)
                }
            }

            LaunchedEffect(keepScreenAwake) {
                if (keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LaunchedEffect(
                enableSleepTimer,
                sleepHour,
                sleepMinute,
                wakeHour,
                wakeMinute,
                manualWakeSnoozeUntil
            ) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val calendar = Calendar.getInstance()
                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(Calendar.MINUTE)

                    if (enableSleepTimer) {
                        val currentMinutesSinceMidnight = currentHour * 60 + currentMinute
                        val sleepMinutesSinceMidnight = sleepHour * 60 + sleepMinute
                        val wakeMinutesSinceMidnight = wakeHour * 60 + wakeMinute

                        val isTargetSleepWindow =
                            if (sleepMinutesSinceMidnight > wakeMinutesSinceMidnight) {
                                currentMinutesSinceMidnight >= sleepMinutesSinceMidnight || currentMinutesSinceMidnight < wakeMinutesSinceMidnight
                            } else {
                                currentMinutesSinceMidnight in sleepMinutesSinceMidnight until wakeMinutesSinceMidnight
                            }

                        if (isTargetSleepWindow) {
                            if (now > manualWakeSnoozeUntil) {
                                if (!isInsideFakeSleep) {
                                    isInsideFakeSleep = true
                                    setWindowBrightness(0.01f)
                                }
                            }
                        } else {
                            if (isInsideFakeSleep) {
                                isInsideFakeSleep = false
                                setWindowBrightness(-1f)
                            }
                        }
                    }
                    delay(5000)
                }
            }

            val isDaytimeBySun = remember(currentTimeMillis, nextDawnMillis, nextDuskMillis) {
                if (nextDawnMillis == 0L || nextDuskMillis == 0L) true else nextDawnMillis > nextDuskMillis
            }

            val resolvedThemeId = if (autoThemeBySun) {
                if (isDaytimeBySun) sunDayThemeId else sunNightThemeId
            } else {
                selectedThemeId
            }

            val activeThemeColors = when (resolvedThemeId) {
                0 -> CyberpunkTheme
                1 -> PipBoyRadTheme
                2 -> ObsidianStealthTheme
                3 -> AmberTerminalTheme
                4 -> SolarizedLightTheme
                5 -> BloodlineTheme
                6 -> AndroidMaterialTheme
                7 -> AppleIOSTheme
                else -> AndroidMaterialTheme
            }

            val isDarkTheme = when (selectedThemeMode) {
                0 -> true
                1 -> false
                else -> !isDaytimeBySun
            }

            val darkBackground = activeThemeColors.bg
            val cardDark = activeThemeColors.card
            val currentTextColor = activeThemeColors.text
            val neonCyan = activeThemeColors.primaryAccent
            val neonGreen = activeThemeColors.secondaryAccent
            val textMuted = activeThemeColors.textMuted

            val currentCardColor = if (isDarkTheme) cardDark else activeThemeColors.card
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val drawerScope = rememberCoroutineScope()

            val menuItems = listOf(
                activeThemeColors.menuDashboardLabel,
                "LIGHTS",
                "CLIMATE",
                "SECURITY",
                activeThemeColors.menuSettingsLabel
            )

            MaterialTheme {
                if (showScheduleScreen) {
                    // 1. Renders the full screen view when triggered
                    ScheduleScreen(
                        haIpAddress = haIpAddress,
                        schedulePath = schedulePath,
                        onBackClick = { showScheduleScreen = false }
                    )
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = cardDark,
                                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                                modifier = Modifier
                                    .width(280.dp)
                                    .fillMaxHeight()
                                    .border(
                                        BorderStroke(1.dp, neonCyan.copy(alpha = 0.2f)),
                                        RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                        Column {
                                            Text(
                                                "Main Menu",
                                                color = textMuted,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "HOUSE CONTROL",
                                                color = neonCyan,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        HorizontalDivider(
                                            color = textMuted.copy(alpha = 0.2f),
                                            thickness = 1.dp
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            menuItems.forEachIndexed { index, title ->
                                                val isSelected = selectedTab == index
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp)
                                                        .background(
                                                            if (isSelected) neonCyan.copy(
                                                                alpha = 0.08f
                                                            ) else Color.Transparent,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) neonCyan.copy(alpha = 0.4f) else Color.Transparent,
                                                            RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            triggerInterfaceFeedback()
                                                            selectedTab = index
                                                            drawerScope.launch { drawerState.close() }
                                                        }
                                                        .padding(horizontal = 16.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = "0$index // $title",
                                                        color = if (isSelected) neonCyan else currentTextColor.copy(
                                                            alpha = 0.7f
                                                        ),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = activeThemeColors.systemNameLabel,
                                        color = textMuted.copy(alpha = 0.4f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(WindowInsets.systemBars)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    lastInteractionTime = System.currentTimeMillis()
                                    if (isManuallyBlackedOut) {
                                        triggerInterfaceFeedback()
                                        isManuallyBlackedOut = false
                                        setWindowBrightness(-1f)
                                    }
                                }
                        ) {
                            Surface(modifier = Modifier.fillMaxSize(), color = darkBackground) {
                                Column(
                                    modifier = Modifier
                                        .offset(x = burnInOffsetX.dp, y = burnInOffsetY.dp)
                                        .fillMaxSize()
                                        .padding(horizontal = 24.dp, vertical = 16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().weight(1f, fill = true)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .clickable {
                                                        triggerInterfaceFeedback()
                                                        drawerScope.launch { drawerState.open() }
                                                    }
                                                    .background(cardDark, RoundedCornerShape(6.dp))
                                                    .border(
                                                        1.dp,
                                                        neonCyan.copy(alpha = 0.2f),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Box(
                                                        modifier = Modifier.size(
                                                            width = 14.dp,
                                                            height = 2.dp
                                                        ).background(neonCyan)
                                                    )
                                                    Box(
                                                        modifier = Modifier.size(
                                                            width = 14.dp,
                                                            height = 2.dp
                                                        ).background(neonCyan)
                                                    )
                                                    Box(
                                                        modifier = Modifier.size(
                                                            width = 14.dp,
                                                            height = 2.dp
                                                        ).background(neonCyan)
                                                    )
                                                }
                                                Text(
                                                    "MENU",
                                                    color = neonCyan,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = menuItems[selectedTab],
                                                color = neonCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 2.sp,
                                                modifier = Modifier.align(Alignment.Center)
                                            )

                                            Column(
                                                modifier = Modifier.align(Alignment.CenterEnd),
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        triggerInterfaceFeedback()
                                                        isManuallyBlackedOut = true
                                                        setWindowBrightness(0.01f)
                                                    },
                                                    modifier = Modifier.width(95.dp).height(22.dp),
                                                    contentPadding = PaddingValues(
                                                        horizontal = 4.dp,
                                                        vertical = 0.dp
                                                    ),
                                                    shape = RoundedCornerShape(4.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        containerColor = neonCyan.copy(alpha = 0.04f)
                                                    ),
                                                    border = BorderStroke(
                                                        0.5.dp,
                                                        neonCyan.copy(alpha = 0.25f)
                                                    )
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            4.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.size(4.dp)
                                                                .background(
                                                                    color = neonCyan,
                                                                    shape = RoundedCornerShape(50.dp)
                                                                )
                                                        )
                                                        Text(
                                                            "SCREEN OFF",
                                                            color = neonCyan,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.clickable {
                                                        triggerInterfaceFeedback()
                                                        if (::haClient.isInitialized) haClient.connect()
                                                    },
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val statusColor = when {
                                                        connectionStatus == "Connected" -> neonGreen
                                                        connectionStatus.contains("Connecting") -> neonCyan
                                                        else -> Color(0xFFFF5555)
                                                    }

                                                    val radarAlpha by rememberInfiniteTransition(
                                                        label = ""
                                                    ).animateFloat(
                                                        initialValue = 0.4f, targetValue = 1.0f,
                                                        animationSpec = infiniteRepeatable(
                                                            animation = tween(
                                                                1500,
                                                                easing = androidx.compose.animation.core.FastOutSlowInEasing
                                                            ),
                                                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                                        ), label = ""
                                                    )

                                                    Box(
                                                        modifier = Modifier.size(6.dp).background(
                                                            color = statusColor.copy(alpha = radarAlpha),
                                                            shape = RoundedCornerShape(50.dp)
                                                        )
                                                    )
                                                    Text(
                                                        text = connectionStatus.uppercase(),
                                                        color = statusColor.copy(alpha = 0.8f),
                                                        fontSize = 9.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            contentAlignment = Alignment.TopCenter
                                        ) {
                                            when (selectedTab) {
                                                0 -> DashboardView(
                                                    textMuted = textMuted,
                                                    currentTextColor = currentTextColor,
                                                    currentCardColor = currentCardColor,
                                                    neonCyan = neonCyan,
                                                    neonGreen = neonGreen,
                                                    macro1Name = macro1Name,
                                                    macro1Entities = macro1Entities,
                                                    macro2Name = macro2Name,
                                                    macro2Entities = macro2Entities,
                                                    homeEnergyUsage = "342 W",
                                                    totalActiveLights = deviceList.count { it.domain == "light" && it.state == "ON" },
                                                    connectionStatus = connectionStatus,
                                                    currentMetricIndex = currentMetricIndex,
                                                    onMacro1Click = {
                                                        triggerInterfaceFeedback()
                                                        executeDynamicMacro(macro1Entities)
                                                    },
                                                    onMacro2Click = {
                                                        triggerInterfaceFeedback()
                                                        executeDynamicMacro(macro2Entities)
                                                    }
                                                )

                                                1 -> LightingView(
                                                    deviceList = deviceList,
                                                    customEntityAliases = customEntityAliases,
                                                    activeDetailedLight = activeDetailedLight,
                                                    textMuted = textMuted,
                                                    currentTextColor = currentTextColor,
                                                    currentCardColor = currentCardColor,
                                                    currentBgColor = darkBackground,
                                                    neonCyan = neonCyan,
                                                    neonGreen = neonGreen,
                                                    activeTimersMinutesMap = activeTimersMinutesMap,
                                                    timerTargetEpochMap = timerTargetEpochMap,
                                                    haClientInitialized = ::haClient.isInitialized,
                                                    onActiveDetailedLightChange = { light ->
                                                        activeDetailedLight = light
                                                    },
                                                    onRenameTriggered = { light, currentAlias ->
                                                        temporaryAliasInputText = currentAlias
                                                        entityToRenameInDialog = light
                                                    },
                                                    onToggleLight = { entityId, nextStateOn ->
                                                        if (::haClient.isInitialized) {
                                                            haClient.toggleLight(
                                                                entityId,
                                                                nextStateOn
                                                            )
                                                            deviceList = deviceList.map {
                                                                if (it.entityId == entityId) it.copy(
                                                                    state = if (nextStateOn) "ON" else "OFF"
                                                                ) else it
                                                            }
                                                        }
                                                    },
                                                    onSetBrightness = { entityId, value ->
                                                        if (::haClient.isInitialized) haClient.setLightBrightness(
                                                            entityId,
                                                            value
                                                        )
                                                    },
                                                    onSetColorTemp = { entityId, miredValue ->
                                                        if (::haClient.isInitialized) {
                                                            haClient.setLightColorTemp(
                                                                entityId,
                                                                miredValue
                                                            )
                                                            deviceList = deviceList.map {
                                                                if (it.entityId == entityId) it.copy(
                                                                    state = "ON"
                                                                ) else it
                                                            }
                                                        }
                                                    },
                                                    onSetRgbColor = { entityId, r, g, b ->
                                                        if (::haClient.isInitialized) haClient.setLightRgbColor(
                                                            entityId,
                                                            r,
                                                            g,
                                                            b
                                                        )
                                                    },
                                                    onStartSleepTimer = { entityId, minutes ->
                                                        if (::haClient.isInitialized) haClient.startSleepTimer(
                                                            entityId,
                                                            minutes
                                                        )
                                                    },
                                                    onFeedbackTrigger = { triggerInterfaceFeedback() }
                                                )

                                                2 -> ClimateControlTab(
                                                    haEntities = deviceList,
                                                    drilledRoomIndex = drilledRoomIndex,
                                                    onDrillRoom = { drilledRoomIndex = it },
                                                    roomTargetStates = roomTargetStates,
                                                    roomMappings = roomMappings,
                                                    currentBgColor = darkBackground,
                                                    currentTextColor = currentTextColor,
                                                    neonCyan = neonCyan,
                                                    neonGreen = neonGreen,
                                                    textMuted = textMuted,
                                                    triggerInterfaceFeedback = triggerInterfaceFeedback,
                                                    haIPAddress = haIpAddress,
                                                    schedulePath = schedulePath,
                                                    onOpenSchedule = { showScheduleScreen = true },
                                                    onUpdateTargetTemp = { entityId, targetTemp ->
                                                        if (::haClient.isInitialized) {
                                                            haClient.setClimateTemperature(
                                                                entityId,
                                                                targetTemp
                                                            )
                                                        }
                                                    },
                                                    onToggleClimateState = { entityId, turnOn ->
                                                        if (::haClient.isInitialized) {
                                                            haClient.setClimateHvacMode(
                                                                entityId,
                                                                if (turnOn) "heat" else "off"
                                                            )
                                                        }
                                                    },
                                                    onToggleScheduleState = { scheduleEntityId, turnOn ->
                                                        if (::haClient.isInitialized) {
                                                            val service =
                                                                if (turnOn) "turn_on" else "turn_off"
                                                            haClient.callService(
                                                                domain = "switch",
                                                                service = service,
                                                                entityId = scheduleEntityId
                                                            )
                                                        }
                                                    },
                                                    onSetPresetMode = { entityId: String, presetMode: String ->
                                                        if (::haClient.isInitialized) {
                                                            // 1. Send command to Home Assistant via WebSocket
                                                            haClient.setPresetMode(
                                                                entityId,
                                                                presetMode
                                                            )

                                                            // 2. Optimistically update local device state so the text changes on screen IMMEDIATELY
                                                            deviceList = deviceList.map { device ->
                                                                if (device.entityId == entityId) {
                                                                    val updatedAttributes =
                                                                        device.attributes.toMutableMap()
                                                                            .apply {
                                                                                put(
                                                                                    "preset_mode",
                                                                                    presetMode
                                                                                )
                                                                            }
                                                                    device.copy(attributes = updatedAttributes)
                                                                } else {
                                                                    device
                                                                }
                                                            }
                                                        }
                                                    }
                                                )

                                                3 -> SecurityControlTab(
                                                    currentBgColor = darkBackground,
                                                    currentTextColor = currentTextColor,
                                                    neonCyan = neonCyan,
                                                    neonGreen = neonGreen,
                                                    textMuted = textMuted,
                                                    triggerInterfaceFeedback = triggerInterfaceFeedback
                                                )

                                                4 -> SettingsView(
                                                    textMuted = textMuted,
                                                    neonCyan = neonCyan,
                                                    neonGreen = neonGreen,
                                                    currentTextColor = currentTextColor,
                                                    currentCardColor = currentCardColor,
                                                    currentBgColor = darkBackground,
                                                    deviceList = deviceList,
                                                    customEntityAliases = customEntityAliases,
                                                    connectionStatus = connectionStatus,
                                                    diagnosticPingResult = diagnosticPingResult,
                                                    localIpAddress = getLocalIpAddress(),
                                                    targetHostname = haIpAddress.split(":")
                                                        .firstOrNull() ?: "",
                                                    autoThemeBySun = autoThemeBySun,
                                                    sunDayThemeId = sunDayThemeId,
                                                    sunNightThemeId = sunNightThemeId,
                                                    selectedThemeId = selectedThemeId,
                                                    macro1Name = macro1Name,
                                                    macro2Name = macro2Name,
                                                    macro1Entities = macro1Entities,
                                                    macro2Entities = macro2Entities,
                                                    haIpAddress = haIpAddress,
                                                    haAccessToken = haAccessToken,
                                                    keepScreenAwake = keepScreenAwake,
                                                    enableBurnInProtection = enableBurnInProtection,
                                                    enableSleepTimer = enableSleepTimer,
                                                    sleepHour = sleepHour,
                                                    sleepMinute = sleepMinute,
                                                    wakeHour = wakeHour,
                                                    wakeMinute = wakeMinute,
                                                    wakeDurationMinutes = wakeDurationMinutes,
                                                    triggerInterfaceFeedback = { triggerInterfaceFeedback() },
                                                    schedulePath = schedulePath,
                                                    onSchedulePathChange = { newPath ->
                                                        schedulePath = newPath
                                                        sharedPrefs.edit()
                                                            .putString("schedule_path", newPath)
                                                            .apply()
                                                    },
                                                    onAutoThemeBySunChange = {
                                                        autoThemeBySun = it
                                                        sharedPrefs.edit()
                                                            .putBoolean("auto_theme_by_sun", it)
                                                            .apply()
                                                    },
                                                    onSunDayThemeIdChange = {
                                                        sunDayThemeId = it
                                                        sharedPrefs.edit()
                                                            .putInt("sun_day_theme_id", it).apply()
                                                    },
                                                    onSunNightThemeIdChange = {
                                                        sunNightThemeId = it
                                                        sharedPrefs.edit()
                                                            .putInt("sun_night_theme_id", it)
                                                            .apply()
                                                    },
                                                    onSelectedThemeIdChange = {
                                                        selectedThemeId = it
                                                    },
                                                    onMacro1NameChange = {
                                                        macro1Name = it
                                                        sharedPrefs.edit()
                                                            .putString("macro_1_name", it).apply()
                                                    },
                                                    onMacro2NameChange = {
                                                        macro2Name = it
                                                        sharedPrefs.edit()
                                                            .putString("macro_2_name", it).apply()
                                                    },
                                                    onHaIpAddressChange = {
                                                        haIpAddress = it
                                                        sharedPrefs.edit().putString("ha_ip", it)
                                                            .apply()
                                                    },
                                                    onHaAccessTokenChange = {
                                                        haAccessToken = it
                                                        sharedPrefs.edit().putString("ha_token", it)
                                                            .apply()
                                                    },
                                                    onKeepScreenAwakeChange = {
                                                        keepScreenAwake = it
                                                        sharedPrefs.edit()
                                                            .putBoolean("keep_awake", it).apply()
                                                    },
                                                    onEnableBurnInProtectionChange = {
                                                        enableBurnInProtection = it
                                                        sharedPrefs.edit()
                                                            .putBoolean("burn_in_protection", it)
                                                            .apply()
                                                    },
                                                    onEnableSleepTimerChange = {
                                                        enableSleepTimer = it
                                                        sharedPrefs.edit()
                                                            .putBoolean("enable_sleep_timer", it)
                                                            .apply()
                                                    },
                                                    onSleepTimeAdjustment = { h, m ->
                                                        sleepHour = h
                                                        sleepMinute = m
                                                        sharedPrefs.edit().putInt("sleep_hour", h)
                                                            .putInt("sleep_minute", m).apply()
                                                    },
                                                    onWakeTimeAdjustment = { h, m ->
                                                        wakeHour = h
                                                        wakeMinute = m
                                                        sharedPrefs.edit().putInt("wake_hour", h)
                                                            .putInt("wake_minute", m).apply()
                                                    },
                                                    onWakeDurationChange = {
                                                        wakeDurationMinutes = it
                                                        sharedPrefs.edit()
                                                            .putFloat("wake_duration", it).apply()
                                                    },
                                                    onApplyAndReconnectHA = { ip, token ->
                                                        try {
                                                            initializeAndConnectHA(ip, token)
                                                        } catch (e: Exception) {
                                                            e.printStackTrace()
                                                        }
                                                    },
                                                    onRunVerificationPing = { runNetworkDiagnosticPing() },
                                                    onForceSystemBlackout = {
                                                        isManuallyBlackedOut = true
                                                        setWindowBrightness(0.01f)
                                                    },
                                                    onTriggerRenameDialog = { device, rawCurrentAlias ->
                                                        temporaryAliasInputText = rawCurrentAlias
                                                        entityToRenameInDialog = device
                                                    },
                                                    formatDeviceState = { id, state, dom ->
                                                        formatDeviceState(
                                                            id,
                                                            state,
                                                            dom
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (entityToRenameInDialog != null) {
                                val targetDevice = entityToRenameInDialog!!
                                AlertDialog(
                                    onDismissRequest = { entityToRenameInDialog = null },
                                    containerColor = currentCardColor,
                                    shape = RoundedCornerShape(14.dp),
                                    title = {
                                        Text(
                                            "CUSTOM LOCAL ALIAS",
                                            color = neonCyan,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                "Override the native friendly name for this individual panel node view.",
                                                color = textMuted,
                                                fontSize = 11.sp
                                            )
                                            Text(
                                                "Original: ${targetDevice.friendlyName}",
                                                color = textMuted.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            OutlinedTextField(
                                                value = temporaryAliasInputText,
                                                onValueChange = { temporaryAliasInputText = it },
                                                placeholder = {
                                                    Text(
                                                        "Enter clean display name...",
                                                        fontSize = 12.sp,
                                                        color = textMuted.copy(alpha = 0.5f)
                                                    )
                                                },
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    color = currentTextColor,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 13.sp
                                                ),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = neonCyan,
                                                    unfocusedBorderColor = textMuted.copy(alpha = 0.3f)
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                triggerInterfaceFeedback()
                                                val cleanText = temporaryAliasInputText.trim()
                                                if (cleanText.isNotEmpty()) {
                                                    customEntityAliases[targetDevice.entityId] =
                                                        cleanText
                                                    sharedPrefs.edit().putString(
                                                        "alias_${targetDevice.entityId}",
                                                        cleanText
                                                    ).apply()
                                                } else {
                                                    customEntityAliases.remove(targetDevice.entityId)
                                                    sharedPrefs.edit()
                                                        .remove("alias_${targetDevice.entityId}")
                                                        .apply()
                                                }
                                                entityToRenameInDialog = null
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = neonCyan.copy(
                                                    alpha = 0.12f
                                                )
                                            ),
                                            border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                "SAVE ALIAS",
                                                color = neonCyan,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { entityToRenameInDialog = null }) {
                                            Text(
                                                "CANCEL",
                                                color = textMuted,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                )
                            }

                            AnimatedVisibility(
                                visible = isInsideFakeSleep || isManuallyBlackedOut,
                                enter = fadeIn(animationSpec = tween(1000)),
                                exit = fadeOut(animationSpec = tween(1000))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black)
                                        .clickable {
                                            triggerInterfaceFeedback()
                                            if (isInsideFakeSleep) {
                                                isInsideFakeSleep = false
                                                val activeTimeoutMillis =
                                                    (wakeDurationMinutes * 60f * 1000f).toLong()
                                                manualWakeSnoozeUntil =
                                                    System.currentTimeMillis() + activeTimeoutMillis
                                            }
                                            if (isManuallyBlackedOut) isManuallyBlackedOut = false
                                            lastInteractionTime = System.currentTimeMillis()
                                            setWindowBrightness(-1f)
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    private fun deleteZoneHelpers(roomSlug: String, deviceList: List<SmartDevice>) {
        val currentDevices = deviceList.toList()
        val suffixes = listOf("schedule", "target", "sensors")

        for (suffix in suffixes) {
            val targetRegex = Regex("^(?:input_text|input_number|number|sensor)\\.${roomSlug}_${suffix}(?:_\\d+)?$")
            val realDevice = currentDevices.find { device -> targetRegex.matches(device.entityId) }

            if (realDevice != null) {
                if (::haClient.isInitialized) {
                    CoroutineScope(Dispatchers.IO).launch {
                        haClient.deleteHelperEntity(realDevice.entityId)
                    }
                }
            }
        }
    }
}