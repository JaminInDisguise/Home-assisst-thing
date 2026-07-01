package com.example.homeassisstthing

import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Calendar
import kotlin.math.roundToInt
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.saveable.Saver
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.TextFieldValue
import okhttp3.MediaType.Companion.toMediaType



// =================================================================
// SYSTEM THEME STRUCTURE DEFINITIONS (Colors & Text Packages)
// =================================================================
data class PanelTheme(
    val bg: Color,
    val card: Color,
    val text: Color,
    val textMuted: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,

    // --- DYNAMIC TEXT STRINGS PER LOOK ---
    val systemNameLabel: String,
    val menuDashboardLabel: String,
    val menuSettingsLabel: String,
    val telemetryHeaderLabel: String,
    val consumptionLabel: String,
    val blackoutModeButtonLabel: String
)

// Global Theme Presets Matrix
val CyberpunkTheme = PanelTheme(
    bg = Color(0xFF0D0E15),
    card = Color(0xFF161924),
    text = Color.White,
    textMuted = Color(0xFF7E8494),
    primaryAccent = Color(0xFF00F0FF),  // Neon Cyan
    secondaryAccent = Color(0xFF00FF66), // Neon Green

    systemNameLabel = "HA_NODE_SYS_v4.2",
    menuDashboardLabel = "DASHBOARD",
    menuSettingsLabel = "SYSTEM SETTINGS",
    telemetryHeaderLabel = "HOUSE TELEMETRY STREAM",
    consumptionLabel = "CURRENT TOTAL HOME GRID CONSUMPTION LOAD",
    blackoutModeButtonLabel = "ENGAGE SYSTEM BACKLIGHT BLACKOUT MODE"
)

val PipBoyRadTheme = PanelTheme(
    bg = Color(0xFF050A05),
    card = Color(0xFF0D140D),
    text = Color(0xFF1FFF7F),
    textMuted = Color(0xFF128243),
    primaryAccent = Color(0xFF1FFF7F),
    secondaryAccent = Color(0xFF00A3E0),

    systemNameLabel = "ROBCO PIP-BOY OS v3.00",
    menuDashboardLabel = "STAT // DATA",
    menuSettingsLabel = "PRESETS // SETUP",
    telemetryHeaderLabel = "VAULT POWER MANAGEMENT NODE",
    consumptionLabel = "TOTAL POWER-GRID OUTFLOW // DISCHARGE CAPACITANCE",
    blackoutModeButtonLabel = "TOGGLE STEALTH FIELD PERIPHERAL TERMINATION"
)

val ObsidianStealthTheme = PanelTheme(
    bg = Color(0xFF000000),
    card = Color(0xFF111111),
    text = Color(0xFFE5E5E5),
    textMuted = Color(0xFF666666),
    primaryAccent = Color(0xFFFFFFFF),
    secondaryAccent = Color(0xFF333333),

    systemNameLabel = "STEALTH_TERMINAL",
    menuDashboardLabel = "OVERVIEW",
    menuSettingsLabel = "CONFIGS",
    telemetryHeaderLabel = "CORE DATA MONITOR",
    consumptionLabel = "CURRENT POWER LOAD SENSOR MATRIX",
    blackoutModeButtonLabel = "KILL SCREEN DISCHARGE"
)

val AmberTerminalTheme = PanelTheme(
    bg = Color(0xFF120A00),
    card = Color(0xFF1C1204),
    text = Color(0xFFFFB300),
    textMuted = Color(0xFF996B00),
    primaryAccent = Color(0xFFFF9100),
    secondaryAccent = Color(0xFF00E5FF),

    systemNameLabel = "INDUSTRIAL_AMBER_v1.08",
    menuDashboardLabel = "MATRIX STATS",
    menuSettingsLabel = "TERMINAL CONFIG",
    telemetryHeaderLabel = "CORE DISCHARGE OVERSEER PANEL",
    consumptionLabel = "GRID CONDUIT DISCHARGE COEFFICIENT RATIO",
    blackoutModeButtonLabel = "TERMINAL PHOSPHOR SHIELD DOWN"
)

val SolarizedLightTheme = PanelTheme(
    bg = Color(0xFFFDF6E3),
    card = Color(0xFFEEE8D5),
    text = Color(0xFF073642),
    textMuted = Color(0xFF93A1A1),
    primaryAccent = Color(0xFF268BD2),
    secondaryAccent = Color(0xFFD33682),

    systemNameLabel = "SOLARIZED_LIGHT_SYS",
    menuDashboardLabel = "Summary",
    menuSettingsLabel = "Preferences",
    telemetryHeaderLabel = "ENVIRONMENT METRICS BROADCAST",
    consumptionLabel = "TOTAL ENERGY LOAD FOOTPRINT REGISTERED",
    blackoutModeButtonLabel = "SUSPEND DISPLAY ILLUMINATION MATRIX"
)

val BloodlineTheme = PanelTheme(
    bg = Color(0xFF000000),
    card = Color(0xFF0F0505),
    text = Color(0xFFFF3333),
    textMuted = Color(0xFF7A1F1F),
    primaryAccent = Color(0xFFFF0000),
    secondaryAccent = Color(0xFFFFA500),

    systemNameLabel = "TACTICAL_BLOODLINE_NODE",
    menuDashboardLabel = "SENSORS OVERVIEW",
    menuSettingsLabel = "CHASSIS MATRIX MODS",
    telemetryHeaderLabel = "CRITICAL CHASSIS POWER FLUX NODE",
    consumptionLabel = "DOCK ENERGY RESOURCE DEPLETION COEFFICIENT",
    blackoutModeButtonLabel = "ENGAGE FULL STEALTH BLACKOUT SENSOR SYSTEM"
)

val AndroidMaterialTheme = PanelTheme(
    bg = Color(0xFF1A1C1E),              // Material You Dark Background (Pixel Style)
    card = Color(0xFF22252A),            // Surface Container
    text = Color(0xFFE2E2E6),            // On-surface primary text
    textMuted = Color(0xFF8C9199),       // Variant neutral text
    primaryAccent = Color(0xFF82CFFF),   // Material Dynamic Teal-Blue Accent
    secondaryAccent = Color(0xFFB4F1C5), // Material Dynamic Mint Green

    systemNameLabel = "Android Kiosk OpenSurface",
    menuDashboardLabel = "Dashboard Overview",
    menuSettingsLabel = "System & Themes",
    telemetryHeaderLabel = "Home Framework Telemetry",
    consumptionLabel = "Current Grid Consumption Baseline",
    blackoutModeButtonLabel = "Enable Display Sleep Mode Override"
)

val AppleIOSTheme = PanelTheme(
    bg = Color(0xFFF2F2F7),              // iOS System Gray 6 (Light Background)
    card = Color(0xFFFFFFFF),            // Pure White iOS Card Stack Background
    text = Color(0xFF000000),            // iOS Label Primary Black
    textMuted = Color(0xFF8E8E93),       // iOS System Gray
    primaryAccent = Color(0xFF007AFF),   // Iconic iOS System Blue
    secondaryAccent = Color(0xFF34C759), // Iconic iOS System Green

    systemNameLabel = "iOS Kiosk HomeKit Hub",
    menuDashboardLabel = "Home Status",
    menuSettingsLabel = "Hub Settings",
    telemetryHeaderLabel = "Connected Accessories Feed",
    consumptionLabel = "Total Residential Electricity Consumption Load",
    blackoutModeButtonLabel = "Turn Off Display Panel Matrix"
)
// Climate schedule
data class ClimateScheduleSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String, // e.g., "08:00"
    val targetTemp: Float, // e.g., 21.0f
    val isHeatingOn: Boolean = true, // ON or OFF state
    val dayTarget: String = "WEEKDAYS" // "WEEKDAYS", "WEEKENDS", "MON", "TUE", etc.
)



var updateRoomScheduleBridge: ((String, List<ClimateScheduleSlot>?) -> Unit)? = null

class MainActivity : ComponentActivity() {

    private lateinit var haClient: HomeAssistantClient

    // Friendly names for entity states
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
                entityId.contains("motion") || entityId.contains("presence") || entityId.contains("occupancy") -> {
                    if (upperState == "ON") "DETECTED" else "CLEAR"
                }

                entityId.contains("door") || entityId.contains("window") || entityId.contains("gate") || entityId.contains(
                    "contact"
                ) -> {
                    if (upperState == "ON") "OPEN" else "CLOSED"
                }

                entityId.contains("battery") || entityId.contains("power") -> {
                    if (upperState == "ON") "LOW" else "NORMAL"
                }

                entityId.contains("moisture") || entityId.contains("leak") || entityId.contains("water") -> {
                    if (upperState == "ON") "LEAK DETECTED" else "DRY"
                }

                else -> {
                    if (upperState == "ON") "ACTIVE" else "INACTIVE"
                }
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

    // Brightness control
    private fun setWindowBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Tell the window to layout beyond screen boundaries
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // 2. Force the modern Insets Controller into strict system UI behavior
        val windowInsetsController =
            androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)

        // This behaves like a kiosk: swiping reveals the bars temporarily, then they auto-hide
        windowInsetsController.systemBarsBehavior =
            androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Hide both the notification bar and the navigation bar completely
        windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val sharedPrefs = remember {
                context.getSharedPreferences(
                    "ha_config_prefs",
                    android.content.Context.MODE_PRIVATE
                )
            }
            val view = LocalView.current
            val triggerInterfaceFeedback = {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                view.playSoundEffect(SoundEffectConstants.CLICK)
            }

            // =================================================================
            // STATE REGISTRY STORAGE
            // =================================================================

            // Track which individual light is actively being focused on for advanced sub-controls
            var activeDetailedLight by remember { mutableStateOf<SmartDevice?>(null) }

            // maps to keep track of timers across ALL lights even when switching views!
            val activeTimersMinutesMap = remember { mutableStateMapOf<String, Int>() }
            val timerRemainingSecondsMap = remember { mutableStateMapOf<String, Int>() }
            val timerTargetEpochMap = remember { mutableStateMapOf<String, Long>() }

            //Remember HA IP
            var haIpAddress by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "ha_ip",
                        "192.168.1.xx"
                    ) ?: "192.168.1.xx"
                )
            }

            //Remember access token
            var haAccessToken by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getString(
                        "ha_token",
                        ""
                    ) ?: ""
                )
            }

            //Remember selected theme
            var selectedThemeMode by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getInt(
                        "theme_mode",
                        0
                    )
                )
            }
            //Remember keep awake setting
            var keepScreenAwake by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "keep_awake",
                        false
                    )
                )
            }
            // Remember sleep timer setting
            var enableSleepTimer by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "enable_sleep_timer",
                        false
                    )
                )
            }
            //Remember screen burn in setting
            var enableBurnInProtection by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getBoolean(
                        "burn_in_protection",
                        false
                    )
                )
            }
            //Remember screen time out duration
            var wakeDurationMinutes by rememberSaveable {
                mutableStateOf(
                    sharedPrefs.getFloat(
                        "wake_duration",
                        1.0f
                    )
                )
            }

            //Remember time set for screen black out on
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
            //Remember time set for screen black out off
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
            var showRawRegistry by rememberSaveable { mutableStateOf(false) }
            var drilledRoomIndex by remember { mutableStateOf<Int?>(null) }
            // ------------------------------------------------------------------
// ------------------------------------------------------------------
// CRASH-PROOF PERSISTENT STORAGE ENGINE
// ------------------------------------------------------------------
// Hook into the sharedPreferences backend using your existing line 314 context variable
            val sharedPreferences = remember { context.getSharedPreferences("climate_panel_storage", android.content.Context.MODE_PRIVATE) }

// 1. DYNAMICALLY LOAD ROOM MAPPINGS FROM DISK ON BOOT
            val roomMappings = remember {
                mutableStateMapOf<String, Pair<String, String>>().apply {
                    val savedKeys = sharedPreferences.getStringSet("climate_room_keys", null)

                    if (savedKeys.isNullOrEmpty()) {
                        // Pristine fresh install out-of-the-box defaults
                        put("Airing Cupboard", Pair("sensor.temp_sensors_airing_cupboard_temperature", "sensor.airing_cupboard_humidity"))
                        put("Living Room", Pair("", ""))
                        put("Master Bedroom", Pair("", ""))
                        put("Kitchen Zone", Pair("", ""))
                        put("Hallway", Pair("", ""))
                    } else {
                        // Restore every customized item previously stored on disk
                        savedKeys.forEach { roomKey ->
                            val tempEntity = sharedPreferences.getString("cfg_${roomKey}_temp", "") ?: ""
                            val humEntity = sharedPreferences.getString("cfg_${roomKey}_hum", "") ?: ""
                            put(roomKey, Pair(tempEntity, humEntity))
                        }
                    }
                }
            }


// 2. DYNAMICALLY LOAD OVERRIDE TEMPERATURE TARGETS FROM DISK
            val roomTargetStates = remember {
                mutableStateMapOf<String, Float>().apply {
                    roomMappings.keys.forEach { roomKey ->
                        val savedTarget = sharedPreferences.getFloat("target_${roomKey}", 21.0f)
                        put(roomKey, savedTarget)
                    }
                }
            }

// 3. BACKGROUND AUTO-SAVE DAEMON
            LaunchedEffect(roomMappings.toMap(), roomTargetStates.toMap()) {
                sharedPreferences.edit().apply {
                    putStringSet("climate_room_keys", roomMappings.keys)

                    roomMappings.forEach { (roomKey, entities) ->
                        putString("cfg_${roomKey}_temp", entities.first)
                        putString("cfg_${roomKey}_hum", entities.second)
                    }

                    roomTargetStates.forEach { (roomKey, targetValue) ->
                        putFloat("target_${roomKey}", targetValue)
                    }

                    apply()
                }
            }


            var activeRegistryFilter by rememberSaveable { mutableStateOf("ALL") }

            var burnInOffsetX by rememberSaveable { mutableStateOf(0f) }
            var burnInOffsetY by rememberSaveable { mutableStateOf(0f) }

            var nextDawnMillis by rememberSaveable { mutableStateOf(0L) }
            var nextDuskMillis by rememberSaveable { mutableStateOf(0L) }
            val currentTimeMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
            var selectedThemeId by rememberSaveable { mutableStateOf(0) }
            //auto theme change based on dawn/dusk from homeassist
            var autoThemeBySun by rememberSaveable {
                mutableStateOf(sharedPrefs.getBoolean("auto_theme_by_sun", false))
            }
            var sunDayThemeId by rememberSaveable {
                mutableStateOf(sharedPrefs.getInt("sun_day_theme_id", 4)) // Defaults to Solarized Light (Index 4)
            }
            var sunNightThemeId by rememberSaveable {
                mutableStateOf(sharedPrefs.getInt("sun_night_theme_id", 0)) // Defaults to Cyberpunk (Index 0)
            }

            var isInsideFakeSleep by rememberSaveable { mutableStateOf(false) }
            var manualWakeSnoozeUntil by rememberSaveable { mutableStateOf(0L) }
            var isManuallyBlackedOut by rememberSaveable { mutableStateOf(false) }
            var lastInteractionTime by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

            // Macro Configuration States
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
                    addAll(
                        sharedPrefs.getStringSet(
                            "macro_1_entities",
                            emptySet()
                        ) ?: emptySet()
                    )
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
                    addAll(
                        sharedPrefs.getStringSet(
                            "macro_2_entities",
                            emptySet()
                        ) ?: emptySet()
                    )
                }
            }

            // Network Diagnostic State
            var diagnosticPingResult by rememberSaveable { mutableStateOf("NOT TESTED") }

            // Entity Aliasing State
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

            // Get IP of Device
            val getLocalIpAddress = {
                try {
                    val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                    var foundIp = "No Connection"
                    for (networkInterface in interfaces) {
                        val addresses = Collections.list(networkInterface.inetAddresses)
                        for (address in addresses) {
                            if (!address.isLoopbackAddress) {
                                val sAddr = address.hostAddress
                                val isIPv4 = sAddr.indexOf(':') < 0
                                if (isIPv4) foundIp = sAddr
                            }
                        }
                    }
                    foundIp
                } catch (ex: Exception) {
                    "Unknown"
                }
            }

            //Ping test to HA Machine
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

            // Connection to home assistant
            val initializeAndConnectHA = { targetIp: String, targetToken: String ->
                try {
                    if (::haClient.isInitialized) {
                        haClient.disconnect()
                    }
                } catch (e: Exception) {}

                val cleanIp = targetIp.replace("http://", "").replace("https://", "").replace("ws://", "").replace("wss://", "")
                val formattedUrl = "ws://$cleanIp/api/websocket"

                connectionStatus = "Connecting..."
                haClient = HomeAssistantClient(
                    serverUrl = formattedUrl,
                    accessToken = targetToken,
                    onMessageReceived = { rawJson ->
                        try {
                            val root = org.json.JSONObject(rawJson)

                            if (root.has("type") && root.getString("type") == "result" && root.has("result")) {
                                val resultsJson = root.get("result")
                                if (resultsJson is org.json.JSONArray) {
                                    val discoveredDevices = mutableListOf<SmartDevice>()
                                    for (i in 0 until resultsJson.length()) {
                                        val entityObj = resultsJson.getJSONObject(i)
                                        val entityId = entityObj.optString("entity_id")
                                        val domain = entityId.split(".").firstOrNull() ?: ""

                                        // SMART CASE SELECTION FOR INITIAL FETCH
                                        val rawState = entityObj.optString("state")
                                        val stateValue = if (domain == "input_text") rawState else rawState.uppercase()

                                        val attributes = entityObj.optJSONObject("attributes")
                                        val friendlyName = attributes?.optString("friendly_name") ?: entityId

                                        if (entityId == "sensor.sun_next_dawn") nextDawnMillis = parseStringToMillis(stateValue)
                                        if (entityId == "sensor.sun_next_dusk") nextDuskMillis = parseStringToMillis(stateValue)

                                        val currentBright = attributes?.optInt("brightness", -1) ?: -1
                                        val initialBrightness = if (currentBright != -1) ((currentBright / 255f) * 100f) else 50f

                                        val colorModes = attributes?.optJSONArray("supported_color_modes")
                                        var hasColorSupport = false
                                        if (colorModes != null) {
                                            for (j in 0 until colorModes.length()) {
                                                val mode = colorModes.optString(j).lowercase()
                                                if (mode.contains("rgb") || mode.contains("hs") || mode.contains("xy")) {
                                                    hasColorSupport = true
                                                    break
                                                }
                                            }
                                        }

                                        val currentTemp = attributes?.optDouble("current_temperature", 0.0)?.toFloat() ?: 0f
                                        val targetTemp = attributes?.optDouble("temperature", 0.0)?.toFloat() ?: 0f

                                        if (domain == "light" || domain == "switch" || domain == "sensor" || domain == "binary_sensor" || domain == "climate" || domain == "input_text") {
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
                                                    targetTemperature = targetTemp
                                                )
                                            )
                                        }
                                    }
                                    deviceList = discoveredDevices.sortedBy { it.friendlyName }
                                    connectionStatus = "Connected"
                                }
                            } else if (root.has("type") && root.getString("type") == "event") {
                                val eventObj = root.optJSONObject("event")
                                if (eventObj != null && eventObj.optString("event_type") == "state_changed") {
                                    val dataObj = eventObj.optJSONObject("data")
                                    val entityId = dataObj?.optString("entity_id") ?: ""
                                    val newStateObj = dataObj?.optJSONObject("new_state")

                                    if (entityId.isNotEmpty() && newStateObj != null) {
                                        // SMART CASE SELECTION FOR LIVE STREAMING EVENTS
                                        val rawState = newStateObj.optString("state") ?: ""
                                        val stateValue = if (entityId.startsWith("input_text.")) rawState else rawState.uppercase()

                                        val attributes = newStateObj.optJSONObject("attributes")
                                        val currentBright = attributes?.optDouble("brightness", -1.0) ?: -1.0
                                        val updatedBrightness = if (currentBright != -1.0) ((currentBright / 255.0) * 100.0).toFloat() else 50f

                                        val colorModes = attributes?.optJSONArray("supported_color_modes")
                                        var hasColorSupport = false
                                        if (colorModes != null) {
                                            for (j in 0 until colorModes.length()) {
                                                val mode = colorModes.optString(j).lowercase()
                                                if (mode.contains("rgb") || mode.contains("hs") || mode.contains("xy")) {
                                                    hasColorSupport = true
                                                    break
                                                }
                                            }
                                        }

                                        val currentTemp = attributes?.optDouble("current_temperature", 0.0)?.toFloat() ?: 0f
                                        val targetTemp = attributes?.optDouble("temperature", 0.0)?.toFloat() ?: 0f

                                        deviceList = deviceList.map { device ->
                                            if (device.entityId == entityId) {
                                                device.copy(
                                                    state = stateValue,
                                                    brightness = updatedBrightness,
                                                    isColorCapable = hasColorSupport,
                                                    currentTemperature = currentTemp,
                                                    targetTemperature = targetTemp
                                                )
                                            } else device
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            System.err.println("HA_PARSER: Exception caught -> ${e.message}")
                        }
                    }
                )

                haClient.connect()
            }


            //Connection retry
            LaunchedEffect(Unit) {
                while (true) {
                    if (connectionStatus == "Disconnected" || connectionStatus.contains("Failed")) {
                        initializeAndConnectHA(haIpAddress, haAccessToken)
                    }
                    delay(5000)
                    if (connectionStatus == "Connected") break
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

            //Burn in protection, moves pixels randomly
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

            //Screen blackout
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

            //Keep screen on
            LaunchedEffect(keepScreenAwake) {
                if (keepScreenAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            //screen black out
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


            // =================================================================
            // THEME RESOLUTION ENGINE
            // =================================================================

            // Evaluate Home Assistant Solar Epoch parameters to deduce light cycle state
            val isDaytimeBySun = remember(currentTimeMillis, nextDawnMillis, nextDuskMillis) {
                if (nextDawnMillis == 0L || nextDuskMillis == 0L) true else nextDawnMillis > nextDuskMillis
            }

            // Evaluate theme ID according to user override properties
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


            val cardLight = activeThemeColors.card
            val lightBackground = activeThemeColors.bg

            val currentCardColor = if (isDarkTheme) cardDark else cardLight
            val currentBgColor = if (isDarkTheme) darkBackground else lightBackground

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val drawerScope = rememberCoroutineScope()

            val menuItems = listOf(
                activeThemeColors.menuDashboardLabel,
                "LIGHTS",
                "CLIMATE",
                "SECURITY",
                activeThemeColors.menuSettingsLabel
            )

            // =================================================================
            // MAIN MENU LAYOUT TREE
            // =================================================================
            MaterialTheme {
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
                                        Text("Main Menu", color = textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Text("HOUSE CONTROL", color = neonCyan, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                                    }

                                    HorizontalDivider(color = textMuted.copy(alpha = 0.2f), thickness = 1.dp)

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        menuItems.forEachIndexed { index, title ->
                                            val isSelected = selectedTab == index
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .background(if (isSelected) neonCyan.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(8.dp))
                                                    .border(1.dp, if (isSelected) neonCyan.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
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
                                                    color = if (isSelected) neonCyan else currentTextColor.copy(alpha = 0.7f),
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
                            val totalActiveLights =
                                deviceList.count { it.domain == "light" && it.state == "ON" }
                            val homeEnergyUsageDevice = deviceList.firstOrNull {
                                it.entityId.contains("energy") || it.entityId.contains("power")
                            }
                            val homeEnergyUsage = if (homeEnergyUsageDevice != null) {
                                formatDeviceState(
                                    homeEnergyUsageDevice.entityId,
                                    homeEnergyUsageDevice.state,
                                    homeEnergyUsageDevice.domain
                                )
                            } else "342 W"
                            Column(
                                modifier = Modifier
                                    .offset(x = burnInOffsetX.dp, y = burnInOffsetY.dp)
                                    .fillMaxSize()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = true)) {
                                    val radarAlpha by rememberInfiniteTransition(label = "").animateFloat(
                                        initialValue = 0.4f, targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse), label = ""
                                    )



                                    // MENU BUTTON
                                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .clickable {
                                                    triggerInterfaceFeedback()
                                                    drawerScope.launch { drawerState.open() }
                                                }
                                                .background(cardDark, RoundedCornerShape(6.dp))
                                                .border(1.dp, neonCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Box(modifier = Modifier.size(width = 14.dp, height = 2.dp).background(neonCyan))
                                                Box(modifier = Modifier.size(width = 14.dp, height = 2.dp).background(neonCyan))
                                                Box(modifier = Modifier.size(width = 14.dp, height = 2.dp).background(neonCyan))
                                            }
                                            Text("MENU", color = neonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }

                                        // MAIN TITLE
                                        Text(
                                            text = menuItems[selectedTab],
                                            color = neonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )

                                        // SYSTEM UTILITIES PANEL (Stacked on the right edge)
                                        Column(
                                            modifier = Modifier.align(Alignment.CenterEnd),
                                            horizontalAlignment = Alignment.End, // Keeps everything flush right
                                            verticalArrangement = Arrangement.spacedBy(4.dp) // Perfect gap spacing
                                        ) {

                                            // GLOBAL SCREEN OFF BUTTON
                                            OutlinedButton(
                                                onClick = {
                                                    triggerInterfaceFeedback()
                                                    isManuallyBlackedOut = true
                                                    setWindowBrightness(0.01f)
                                                },
                                                modifier = Modifier
                                                    .width(95.dp)
                                                    .height(22.dp),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(containerColor = neonCyan.copy(alpha = 0.04f)),
                                                border = BorderStroke(0.5.dp, neonCyan.copy(alpha = 0.25f))
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(modifier = Modifier.size(4.dp).background(color = neonCyan, shape = RoundedCornerShape(50.dp)))
                                                    Text(
                                                        "SCREEN OFF",
                                                        color = neonCyan,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }

                                            // 2. CONNECTION STATUS INDICATOR
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

                                                val radarAlpha by androidx.compose.animation.core.rememberInfiniteTransition(label = "")
                                                    .animateFloat(
                                                        initialValue = 0.4f,
                                                        targetValue = 1.0f,
                                                        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                                            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
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
                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        when (selectedTab) {
                                            // ---------------------------------------------------------
                                            // INDEX 00 // CORE DASHBOARD VIEW
                                            // ---------------------------------------------------------
                                            0 -> {

                                                    Column(
                                                        modifier = Modifier.fillMaxSize(),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {

                                                        Text(
                                                            "QUICK ACTIONS",
                                                            color = textMuted,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier.align(Alignment.Start)
                                                                .padding(bottom = 8.dp)
                                                        )

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth()
                                                                .padding(bottom = 16.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                        ) {
                                                            //Macro buttons
                                                            Button(
                                                                onClick = {
                                                                    triggerInterfaceFeedback()
                                                                    executeDynamicMacro(macro1Entities)
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                                    .height(50.dp),
                                                                shape = RoundedCornerShape(10.dp),
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = neonCyan.copy(alpha = 0.12f)
                                                                ),
                                                                border = BorderStroke(
                                                                    1.dp,
                                                                    neonCyan.copy(alpha = 0.4f)
                                                                )
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(
                                                                        macro1Name.uppercase(),
                                                                        color = neonCyan,
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                    Text(
                                                                        "${macro1Entities.size} DEVICES",
                                                                        color = textMuted,
                                                                        fontSize = 8.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }
                                                            }

                                                            Button(
                                                                onClick = {
                                                                    triggerInterfaceFeedback()
                                                                    executeDynamicMacro(macro2Entities)
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                                    .height(50.dp),
                                                                shape = RoundedCornerShape(10.dp),
                                                                colors = ButtonDefaults.buttonColors(
                                                                    containerColor = neonGreen.copy(alpha = 0.12f)
                                                                ),
                                                                border = BorderStroke(
                                                                    1.dp,
                                                                    neonGreen.copy(alpha = 0.4f)
                                                                )
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(
                                                                        macro2Name.uppercase(),
                                                                        color = neonGreen,
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                    Text(
                                                                        "${macro2Entities.size} DEVICES",
                                                                        color = textMuted,
                                                                        fontSize = 8.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        //carosell, wants changing
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth()
                                                                .height(160.dp),
                                                            shape = RoundedCornerShape(16.dp),
                                                            colors = CardDefaults.cardColors(containerColor = currentCardColor),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                neonCyan.copy(alpha = 0.2f)
                                                            )
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize()
                                                                    .padding(24.dp)
                                                            ) {
                                                                Crossfade(
                                                                    targetState = currentMetricIndex,
                                                                    animationSpec = tween(600),
                                                                    label = ""
                                                                ) { index ->
                                                                    Column(
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        verticalArrangement = Arrangement.SpaceBetween
                                                                    ) {
                                                                        when (index) {
                                                                            0 -> {
                                                                                Column {
                                                                                    Text(
                                                                                        "HOUSE METRICS // SLOT 01",
                                                                                        color = neonCyan,
                                                                                        fontSize = 10.sp,
                                                                                        fontFamily = FontFamily.Monospace
                                                                                    ); Spacer(
                                                                                    modifier = Modifier.height(
                                                                                        12.dp
                                                                                    )
                                                                                ); Text(
                                                                                    "CURRENT GRID LOAD",
                                                                                    color = textMuted,
                                                                                    fontSize = 14.sp
                                                                                ); Text(
                                                                                    text = homeEnergyUsage,
                                                                                    color = currentTextColor,
                                                                                    fontSize = 34.sp,
                                                                                    fontWeight = FontWeight.ExtraBold
                                                                                )
                                                                                }
                                                                            }

                                                                            1 -> {
                                                                                Column {
                                                                                    Text(
                                                                                        "HOUSE METRICS // SLOT 02",
                                                                                        color = neonCyan,
                                                                                        fontSize = 10.sp,
                                                                                        fontFamily = FontFamily.Monospace
                                                                                    ); Spacer(
                                                                                    modifier = Modifier.height(
                                                                                        12.dp
                                                                                    )
                                                                                ); Text(
                                                                                    "ACTIVE LIGHT SYSTEMS",
                                                                                    color = textMuted,
                                                                                    fontSize = 14.sp
                                                                                ); Text(
                                                                                    text = "$totalActiveLights SYSTEM ON",
                                                                                    color = if (totalActiveLights > 0) neonGreen else currentTextColor,
                                                                                    fontSize = 34.sp,
                                                                                    fontWeight = FontWeight.ExtraBold
                                                                                )
                                                                                }
                                                                            }

                                                                            2 -> {
                                                                                Column {
                                                                                    Text(
                                                                                        "HOUSE METRICS // SLOT 03",
                                                                                        color = neonCyan,
                                                                                        fontSize = 10.sp,
                                                                                        fontFamily = FontFamily.Monospace
                                                                                    ); Spacer(
                                                                                    modifier = Modifier.height(
                                                                                        12.dp
                                                                                    )
                                                                                ); Text(
                                                                                    "SERVER TELEMETRY LINK",
                                                                                    color = textMuted,
                                                                                    fontSize = 14.sp
                                                                                ); Text(
                                                                                    text = connectionStatus,
                                                                                    color = neonGreen,
                                                                                    fontSize = 34.sp,
                                                                                    fontWeight = FontWeight.ExtraBold
                                                                                )
                                                                                }
                                                                            }
                                                                        }
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                                6.dp
                                                                            ),
                                                                            modifier = Modifier.align(
                                                                                Alignment.End
                                                                            )
                                                                        ) {
                                                                            repeat(3) { dotIndex ->
                                                                                Box(
                                                                                    modifier = Modifier.size(
                                                                                        6.dp
                                                                                    ).background(
                                                                                        color = if (currentMetricIndex == dotIndex) neonCyan else textMuted.copy(
                                                                                            alpha = 0.3f
                                                                                        ),
                                                                                        shape = RoundedCornerShape(
                                                                                            50.dp
                                                                                        )
                                                                                    )
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }


                                                    }
                                                }

                                            // ---------------------------------------------------------
                                            // INDEX 01 // LIGHTING  PANEL VIEW
                                            // ---------------------------------------------------------
                                            1 -> {
                                                val lightDevices =
                                                    deviceList.filter { it.domain == "light" }


                                                if (activeDetailedLight == null) {
                                                    // VIEW A: THE STANDARD LIGHTING LIST AND ON OFF CONTROL
                                                    Column(modifier = Modifier.fillMaxSize()) {
                                                        Text(
                                                            "LIGHT CONTROLS (${lightDevices.size})",
                                                            color = textMuted,
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            modifier = Modifier.padding(bottom = 8.dp)
                                                        )

                                                        LazyColumn(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.spacedBy(
                                                                10.dp
                                                            )
                                                        ) {
                                                            items(lightDevices) { light ->
                                                                val isLightOn = light.state == "ON"
                                                                val resolvedDisplayName =
                                                                    customEntityAliases[light.entityId]
                                                                        ?: light.friendlyName

                                                                Card(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .pointerInput(Unit) {
                                                                            detectTapGestures(
                                                                                onTap = {
                                                                                    triggerInterfaceFeedback()
                                                                                    // TAP ACTION: Selects this light to enter the sub-screen view
                                                                                    activeDetailedLight =
                                                                                        light
                                                                                },
                                                                                onLongPress = {
                                                                                    triggerInterfaceFeedback()
                                                                                    temporaryAliasInputText =
                                                                                        customEntityAliases[light.entityId]
                                                                                            ?: ""
                                                                                    entityToRenameInDialog =
                                                                                        light
                                                                                }
                                                                            )
                                                                        },
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    colors = CardDefaults.cardColors(
                                                                        containerColor = currentCardColor
                                                                    )
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.padding(
                                                                            16.dp
                                                                        )
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Column(
                                                                                modifier = Modifier.weight(
                                                                                    1f
                                                                                )
                                                                            ) {
                                                                                Text(
                                                                                    text = resolvedDisplayName,
                                                                                    color = currentTextColor,
                                                                                    fontSize = 16.sp,
                                                                                    fontWeight = FontWeight.Bold
                                                                                )
                                                                                Text(
                                                                                    "TAP FOR ADVANCED CONTROLS",
                                                                                    color = textMuted,
                                                                                    fontSize = 11.sp,
                                                                                    fontFamily = FontFamily.Monospace
                                                                                )
                                                                            }
                                                                            Button(
                                                                                onClick = {
                                                                                    triggerInterfaceFeedback()
                                                                                    if (::haClient.isInitialized) {
                                                                                        haClient.toggleLight(
                                                                                            light.entityId,
                                                                                            !isLightOn
                                                                                        )
                                                                                        deviceList =
                                                                                            deviceList.map {
                                                                                                if (it.entityId == light.entityId) it.copy(
                                                                                                    state = if (isLightOn) "OFF" else "ON"
                                                                                                ) else it
                                                                                            }
                                                                                    }
                                                                                },
                                                                                colors = ButtonDefaults.buttonColors(
                                                                                    containerColor = if (isLightOn) neonGreen.copy(
                                                                                        alpha = 0.15f
                                                                                    ) else textMuted.copy(
                                                                                        alpha = 0.10f
                                                                                    )
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
                                                    // VIEW B: THE FOCUSED FINE-TUNING SCREEN CONTROL COCKPIT WITH AMBIENT GLOW & RGBW SLIDERS
                                                    val currentTarget = activeDetailedLight!!
                                                    val liveLight =
                                                        deviceList.find { it.entityId == currentTarget.entityId }
                                                            ?: currentTarget
                                                    val isLightOn = liveLight.state == "ON"
                                                    val resolvedDisplayName =
                                                        customEntityAliases[liveLight.entityId]
                                                            ?: liveLight.friendlyName

                                                    var localSliderValue by remember(liveLight.entityId) {
                                                        mutableStateOf(
                                                            liveLight.brightness
                                                        )
                                                    }

                                                    // Local states for RGB Color Selection Sliders
                                                    var redValue by remember(liveLight.entityId) {
                                                        mutableStateOf(
                                                            255f
                                                        )
                                                    }
                                                    var greenValue by remember(liveLight.entityId) {
                                                        mutableStateOf(
                                                            255f
                                                        )
                                                    }
                                                    var blueValue by remember(liveLight.entityId) {
                                                        mutableStateOf(
                                                            255f
                                                        )
                                                    }

                                                    // track which timer duration button is active (15, 30, 45)
                                                    val currentTimerMins =
                                                        activeTimersMinutesMap[liveLight.entityId]
                                                            ?: 0

                                                    // Track a ticking state locally just to force the UI text to update every second while looking at it
                                                    var localTickTrigger by remember {
                                                        mutableStateOf(
                                                            0
                                                        )
                                                    }
                                                    LaunchedEffect(currentTimerMins) {
                                                        if (currentTimerMins > 0) {
                                                            while (true) {
                                                                delay(1000)
                                                                localTickTrigger++
                                                            }
                                                        }
                                                    }

                                                    // Calculate remaining seconds dynamically by comparing the target timestamp to current system time
                                                    val currentRemainingSecs = remember(
                                                        liveLight.entityId,
                                                        localTickTrigger
                                                    ) {
                                                        val targetEpoch =
                                                            timerTargetEpochMap[liveLight.entityId]
                                                                ?: 0L
                                                        val currentEpoch =
                                                            System.currentTimeMillis() / 1000
                                                        (targetEpoch - currentEpoch).coerceAtLeast(
                                                            0L
                                                        )
                                                    }

                                                    // Auto-clean maps if time runs out naturally while looking at it
                                                    LaunchedEffect(currentRemainingSecs) {
                                                        if (currentTimerMins > 0 && currentRemainingSecs == 0L) {
                                                            activeTimersMinutesMap[liveLight.entityId] =
                                                                0
                                                            timerTargetEpochMap[liveLight.entityId] =
                                                                0L
                                                        }
                                                    }

                                                    // Synchronize intensity slider live from external websocket events
                                                    LaunchedEffect(liveLight.brightness) {
                                                        localSliderValue = liveLight.brightness
                                                    }

                                                    // Core Debouncer for Brightness
                                                    LaunchedEffect(localSliderValue) {
                                                        if (isLightOn && Math.abs(localSliderValue - liveLight.brightness) > 1f) {
                                                            delay(150)
                                                            if (::haClient.isInitialized) {
                                                                haClient.setLightBrightness(
                                                                    liveLight.entityId,
                                                                    localSliderValue
                                                                )
                                                            }
                                                        }
                                                    }

                                                    // Flood prevention on rgb sliders
                                                    var lastSentRgb by remember(liveLight.entityId) {
                                                        mutableStateOf(
                                                            Triple(255, 255, 255)
                                                        )
                                                    }

                                                    LaunchedEffect(
                                                        redValue,
                                                        greenValue,
                                                        blueValue
                                                    ) {
                                                        if (isLightOn) {
                                                            delay(250)
                                                            val currentR =
                                                                redValue.toInt().coerceIn(0, 255)
                                                            val currentG =
                                                                greenValue.toInt().coerceIn(0, 255)
                                                            val currentB =
                                                                blueValue.toInt().coerceIn(0, 255)
                                                            val currentTriple =
                                                                Triple(currentR, currentG, currentB)

                                                            if (currentTriple != lastSentRgb && ::haClient.isInitialized) {
                                                                haClient.setLightRgbColor(
                                                                    liveLight.entityId,
                                                                    currentR,
                                                                    currentG,
                                                                    currentB
                                                                )
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
                                                                "← BACK",
                                                                color = neonCyan,
                                                                fontWeight = FontWeight.Bold,
                                                                fontFamily = FontFamily.Monospace,
                                                                fontSize = 12.sp,
                                                                modifier = Modifier
                                                                    .clickable {
                                                                        triggerInterfaceFeedback()
                                                                        activeDetailedLight = null
                                                                    }
                                                                    .padding(
                                                                        vertical = 8.dp,
                                                                        horizontal = 4.dp
                                                                    )
                                                            )
                                                        }

                                                        // Central Control Console Card with Ambient Glow Border Integration
                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .wrapContentHeight(),
                                                            shape = RoundedCornerShape(16.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            ),
                                                            border = BorderStroke(
                                                                width = if (isLightOn) 2.dp else 1.dp,
                                                                color = if (isLightOn) neonCyan.copy(
                                                                    alpha = 0.8f
                                                                ) else neonCyan.copy(alpha = 0.15f)
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .wrapContentHeight()
                                                                    .padding(20.dp),
                                                                verticalArrangement = Arrangement.spacedBy(
                                                                    18.dp
                                                                )
                                                            ) {

                                                                // Identity Header Segment
                                                                Column {
                                                                    Text(
                                                                        "ADVANCED LIGHT CONTROLS",
                                                                        color = neonCyan,
                                                                        fontSize = 10.sp,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        letterSpacing = 1.sp
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            2.dp
                                                                        )
                                                                    )
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

                                                                // TOP POSITION 1: System Status Readout Block
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(44.dp)
                                                                        .background(
                                                                            if (isLightOn) neonGreen.copy(
                                                                                alpha = 0.06f
                                                                            ) else textMuted.copy(
                                                                                alpha = 0.02f
                                                                            ),
                                                                            RoundedCornerShape(10.dp)
                                                                        )
                                                                        .border(
                                                                            1.dp,
                                                                            if (isLightOn) neonGreen.copy(
                                                                                alpha = 0.3f
                                                                            ) else textMuted.copy(
                                                                                alpha = 0.1f
                                                                            ),
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

                                                                // TOP POSITION 2: On off switch
                                                                Button(
                                                                    onClick = {
                                                                        triggerInterfaceFeedback()
                                                                        if (::haClient.isInitialized) {
                                                                            haClient.toggleLight(
                                                                                liveLight.entityId,
                                                                                !isLightOn
                                                                            )
                                                                            deviceList =
                                                                                deviceList.map {
                                                                                    if (it.entityId == liveLight.entityId) it.copy(
                                                                                        state = if (isLightOn) "OFF" else "ON"
                                                                                    ) else it
                                                                                }
                                                                            if (!isLightOn && localSliderValue < 5f) {
                                                                                localSliderValue =
                                                                                    100f
                                                                            }
                                                                        }
                                                                    },
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(50.dp),
                                                                    shape = RoundedCornerShape(10.dp),
                                                                    colors = ButtonDefaults.buttonColors(
                                                                        containerColor = if (isLightOn) neonGreen.copy(
                                                                            alpha = 0.15f
                                                                        ) else textMuted.copy(alpha = 0.12f)
                                                                    ),
                                                                    border = BorderStroke(
                                                                        1.dp,
                                                                        if (isLightOn) neonGreen.copy(
                                                                            alpha = 0.4f
                                                                        ) else textMuted.copy(alpha = 0.2f)
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

                                                                // MODULE 1: BRIGHTNESS SLIDER MODULE
                                                                Column(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(
                                                                            currentBgColor.copy(
                                                                                alpha = 0.3f
                                                                            ),
                                                                            RoundedCornerShape(12.dp)
                                                                        )
                                                                        .padding(14.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                                    ) {
                                                                        Text(
                                                                            "LIGHT BRIGHTNESS CONTROL",
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
                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            4.dp
                                                                        )
                                                                    )
                                                                    Slider(
                                                                        value = localSliderValue,
                                                                        onValueChange = {
                                                                            if (isLightOn) localSliderValue =
                                                                                it
                                                                        },
                                                                        valueRange = 1f..100f,
                                                                        enabled = isLightOn,
                                                                        colors = SliderDefaults.colors(
                                                                            thumbColor = neonCyan,
                                                                            activeTrackColor = neonCyan,
                                                                            inactiveTrackColor = textMuted.copy(
                                                                                alpha = 0.2f
                                                                            )
                                                                        )
                                                                    )
                                                                }

                                                                // TWO-COLUMN INTERFACE FOR COLOR TEMP & SLEEP RUNTIME
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        14.dp
                                                                    )
                                                                ) {

                                                                    // LEFT COLUMN: COLOR TEMP SELECTION PALETTE
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                currentBgColor.copy(
                                                                                    alpha = 0.3f
                                                                                ),
                                                                                RoundedCornerShape(
                                                                                    12.dp
                                                                                )
                                                                            )
                                                                            .padding(14.dp)
                                                                    ) {
                                                                        Text(
                                                                            "COLOR TEMPERATURE",
                                                                            color = if (isLightOn) neonCyan else textMuted,
                                                                            fontSize = 11.sp,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier.padding(
                                                                                bottom = 10.dp
                                                                            )
                                                                        )

                                                                        val colorTemps = listOf(
                                                                            Triple(
                                                                                "COLD WHITE",
                                                                                153,
                                                                                Color(0xFFDDF2FF)
                                                                            ),
                                                                            Triple(
                                                                                "BALANCE",
                                                                                300,
                                                                                Color(0xFFFFFFFE)
                                                                            ),
                                                                            Triple(
                                                                                "WARM WHITE",
                                                                                450,
                                                                                Color(0xFFFFE3A6)
                                                                            )
                                                                        )

                                                                        Column(
                                                                            verticalArrangement = Arrangement.spacedBy(
                                                                                8.dp
                                                                            )
                                                                        ) {
                                                                            colorTemps.forEach { (name, miredValue, indicatorColor) ->
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .height(36.dp)
                                                                                        .background(
                                                                                            if (isLightOn) indicatorColor.copy(
                                                                                                alpha = 0.12f
                                                                                            ) else textMuted.copy(
                                                                                                alpha = 0.05f
                                                                                            ),
                                                                                            RoundedCornerShape(
                                                                                                6.dp
                                                                                            )
                                                                                        )
                                                                                        .border(
                                                                                            1.dp,
                                                                                            if (isLightOn) indicatorColor.copy(
                                                                                                alpha = 0.4f
                                                                                            ) else Color.Transparent,
                                                                                            RoundedCornerShape(
                                                                                                6.dp
                                                                                            )
                                                                                        )
                                                                                        .clickable(
                                                                                            enabled = isLightOn
                                                                                        ) {
                                                                                            triggerInterfaceFeedback()
                                                                                            if (::haClient.isInitialized) {
                                                                                                haClient.setLightColorTemp(
                                                                                                    liveLight.entityId,
                                                                                                    miredValue
                                                                                                )
                                                                                                deviceList =
                                                                                                    deviceList.map {
                                                                                                        if (it.entityId == liveLight.entityId) it.copy(
                                                                                                            state = "ON"
                                                                                                        ) else it
                                                                                                    }
                                                                                            }
                                                                                        },
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.Center
                                                                                ) {
                                                                                    Box(
                                                                                        modifier = Modifier.size(
                                                                                            8.dp
                                                                                        )
                                                                                            .background(
                                                                                                indicatorColor,
                                                                                                androidx.compose.foundation.shape.CircleShape
                                                                                            )
                                                                                    )
                                                                                    Spacer(
                                                                                        modifier = Modifier.width(
                                                                                            8.dp
                                                                                        )
                                                                                    )
                                                                                    Text(
                                                                                        name,
                                                                                        color = if (isLightOn) currentTextColor else textMuted,
                                                                                        fontSize = 10.sp,
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        fontFamily = FontFamily.Monospace
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    // RIGHT COLUMN: COUNTDOWN SLEEP TIMER MODULE
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .background(
                                                                                currentBgColor.copy(
                                                                                    alpha = 0.3f
                                                                                ),
                                                                                RoundedCornerShape(
                                                                                    12.dp
                                                                                )
                                                                            )
                                                                            .padding(14.dp)
                                                                    ) {
                                                                        Text(
                                                                            "SLEEP TIMER",
                                                                            color = if (isLightOn) neonCyan else textMuted,
                                                                            fontSize = 11.sp,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            fontWeight = FontWeight.Bold
                                                                        )

                                                                        Spacer(
                                                                            modifier = Modifier.height(
                                                                                10.dp
                                                                            )
                                                                        )

                                                                        val timeOptions =
                                                                            listOf(15, 30, 45)
                                                                        Column(
                                                                            verticalArrangement = Arrangement.spacedBy(
                                                                                8.dp
                                                                            )
                                                                        ) {
                                                                            timeOptions.forEach { minutes ->
                                                                                val isCurrentTimer =
                                                                                    currentTimerMins == minutes
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .height(36.dp)
                                                                                        .background(
                                                                                            if (!isLightOn) textMuted.copy(
                                                                                                alpha = 0.05f
                                                                                            )
                                                                                            else if (isCurrentTimer) neonGreen.copy(
                                                                                                alpha = 0.15f
                                                                                            )
                                                                                            else neonCyan.copy(
                                                                                                alpha = 0.05f
                                                                                            ),
                                                                                            RoundedCornerShape(
                                                                                                6.dp
                                                                                            )
                                                                                        )
                                                                                        .border(
                                                                                            1.dp,
                                                                                            if (isCurrentTimer) neonGreen else Color.Transparent,
                                                                                            RoundedCornerShape(
                                                                                                6.dp
                                                                                            )
                                                                                        )
                                                                                        .clickable(
                                                                                            enabled = isLightOn
                                                                                        ) {
                                                                                            triggerInterfaceFeedback()
                                                                                            if (isCurrentTimer) {
                                                                                                // CANCEL ACTION
                                                                                                activeTimersMinutesMap[liveLight.entityId] =
                                                                                                    0
                                                                                                timerTargetEpochMap[liveLight.entityId] =
                                                                                                    0L
                                                                                            } else {
                                                                                                // START TIMER ACTION
                                                                                                val futureTargetEpoch =
                                                                                                    (System.currentTimeMillis() / 1000) + (minutes * 60)

                                                                                                activeTimersMinutesMap[liveLight.entityId] =
                                                                                                    minutes
                                                                                                timerTargetEpochMap[liveLight.entityId] =
                                                                                                    futureTargetEpoch

                                                                                                if (::haClient.isInitialized) {
                                                                                                    haClient.startSleepTimer(
                                                                                                        liveLight.entityId,
                                                                                                        minutes
                                                                                                    )
                                                                                                }
                                                                                            }
                                                                                        },
                                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                                    horizontalArrangement = Arrangement.Center
                                                                                ) {
                                                                                    val buttonText =
                                                                                        if (isCurrentTimer && currentRemainingSecs > 0) {
                                                                                            val mins =
                                                                                                currentRemainingSecs / 60
                                                                                            val secs =
                                                                                                currentRemainingSecs % 60
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

                                                                // MODULE 3: RGBW CHROMATIC COLOR CONTROL PANEL
                                                                if (liveLight.isColorCapable) {
                                                                    Column(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .background(
                                                                                currentBgColor.copy(
                                                                                    alpha = 0.3f
                                                                                ),
                                                                                RoundedCornerShape(
                                                                                    12.dp
                                                                                )
                                                                            )
                                                                            .padding(14.dp),
                                                                        verticalArrangement = Arrangement.spacedBy(
                                                                            6.dp
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "RBG CONTROL",
                                                                            color = if (isLightOn) neonCyan else textMuted,
                                                                            fontSize = 11.sp,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            fontWeight = FontWeight.Bold,
                                                                            modifier = Modifier.padding(
                                                                                bottom = 4.dp
                                                                            )
                                                                        )

                                                                        // RED Control Track
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            modifier = Modifier.fillMaxWidth()
                                                                        ) {
                                                                            Text(
                                                                                "R",
                                                                                color = if (isLightOn) Color.Red else textMuted,
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                modifier = Modifier.width(
                                                                                    16.dp
                                                                                ),
                                                                                fontFamily = FontFamily.Monospace
                                                                            )
                                                                            Slider(
                                                                                value = redValue,
                                                                                onValueChange = {
                                                                                    if (isLightOn) redValue =
                                                                                        it
                                                                                },
                                                                                valueRange = 0f..255f,
                                                                                enabled = isLightOn,
                                                                                colors = SliderDefaults.colors(
                                                                                    thumbColor = Color.Red,
                                                                                    activeTrackColor = Color.Red.copy(
                                                                                        alpha = 0.6f
                                                                                    )
                                                                                ),
                                                                                modifier = Modifier.weight(
                                                                                    1f
                                                                                )
                                                                            )
                                                                        }

                                                                        // GREEN Control Track
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            modifier = Modifier.fillMaxWidth()
                                                                        ) {
                                                                            Text(
                                                                                "G",
                                                                                color = if (isLightOn) Color.Green else textMuted,
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                modifier = Modifier.width(
                                                                                    16.dp
                                                                                ),
                                                                                fontFamily = FontFamily.Monospace
                                                                            )
                                                                            Slider(
                                                                                value = greenValue,
                                                                                onValueChange = {
                                                                                    if (isLightOn) greenValue =
                                                                                        it
                                                                                },
                                                                                valueRange = 0f..255f,
                                                                                enabled = isLightOn,
                                                                                colors = SliderDefaults.colors(
                                                                                    thumbColor = Color.Green,
                                                                                    activeTrackColor = Color.Green.copy(
                                                                                        alpha = 0.6f
                                                                                    )
                                                                                ),
                                                                                modifier = Modifier.weight(
                                                                                    1f
                                                                                )
                                                                            )
                                                                        }

                                                                        // BLUE Control Track
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            modifier = Modifier.fillMaxWidth()
                                                                        ) {
                                                                            Text(
                                                                                "B",
                                                                                color = if (isLightOn) Color.Blue else textMuted,
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                modifier = Modifier.width(
                                                                                    16.dp
                                                                                ),
                                                                                fontFamily = FontFamily.Monospace
                                                                            )
                                                                            Slider(
                                                                                value = blueValue,
                                                                                onValueChange = {
                                                                                    if (isLightOn) blueValue =
                                                                                        it
                                                                                },
                                                                                valueRange = 0f..255f,
                                                                                enabled = isLightOn,
                                                                                colors = SliderDefaults.colors(
                                                                                    thumbColor = Color.Blue,
                                                                                    activeTrackColor = Color.Blue.copy(
                                                                                        alpha = 0.6f
                                                                                    )
                                                                                ),
                                                                                modifier = Modifier.weight(
                                                                                    1f
                                                                                )
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // ---------------------------------------------------------
                                            // INDEX 02 // ENVIRONMENTAL CLIMATE VIEW
                                            // ---------------------------------------------------------
                                            2 -> {
                                                ClimateControlTab(
                                                    deviceList = deviceList,
                                                    haClient = haClient,
                                                    drilledRoomIndex = drilledRoomIndex,
                                                    onDrillRoom = { drilledRoomIndex = it },
                                                    roomTargetStates = roomTargetStates,
                                                    roomMappings = roomMappings,
                                                    currentBgColor = darkBackground, currentTextColor = currentTextColor,
                                                    neonCyan = neonCyan, neonGreen = neonGreen, textMuted = textMuted,
                                                    triggerInterfaceFeedback = triggerInterfaceFeedback
                                                )
                                            }

                                            // ---------------------------------------------------------
                                            // INDEX 03 // PERIMETER SECURITY VIEW
                                            // ---------------------------------------------------------
                                            3 -> {
                                                SecurityControlTab(
                                                    currentBgColor = darkBackground, currentTextColor = currentTextColor,
                                                    neonCyan = neonCyan, neonGreen = neonGreen, textMuted = textMuted,
                                                    triggerInterfaceFeedback = triggerInterfaceFeedback
                                                )
                                            }

                                            // ---------------------------------------------------------
                                            // INDEX 04 // SYSTEM SETTINGS VIEW
                                            // ---------------------------------------------------------
                                            4 -> {if (!showRawRegistry) {
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
                                                                            autoThemeBySun = isChecked
                                                                            sharedPrefs.edit().putBoolean("auto_theme_by_sun", isChecked).apply()
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
                                                                                        sunDayThemeId = idx
                                                                                        sharedPrefs.edit().putInt("sun_day_theme_id", idx).apply()
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
                                                                                        sunNightThemeId = idx
                                                                                        sharedPrefs.edit().putInt("sun_night_theme_id", idx).apply()
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
                                                                                        selectedThemeId = index
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
                                                        var expandedMacroSetup by remember {
                                                            mutableStateOf(
                                                                0
                                                            )
                                                        }

                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            ),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                textMuted.copy(alpha = 0.15f)
                                                            )
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
                                                                    modifier = Modifier.padding(
                                                                        bottom = 12.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        8.dp
                                                                    )
                                                                ) {
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            expandedMacroSetup =
                                                                                if (expandedMacroSetup == 1) 0 else 1
                                                                        },
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        ),
                                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                            containerColor = if (expandedMacroSetup == 1) neonCyan.copy(
                                                                                alpha = 0.05f
                                                                            ) else Color.Transparent
                                                                        ),
                                                                        border = BorderStroke(
                                                                            1.dp,
                                                                            if (expandedMacroSetup == 1) neonCyan else textMuted.copy(
                                                                                alpha = 0.3f
                                                                            )
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "EDIT ${macro1Name.uppercase()}",
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }

                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            expandedMacroSetup =
                                                                                if (expandedMacroSetup == 2) 0 else 2
                                                                        },
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        ),
                                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                            containerColor = if (expandedMacroSetup == 2) neonGreen.copy(
                                                                                alpha = 0.05f
                                                                            ) else Color.Transparent
                                                                        ),
                                                                        border = BorderStroke(
                                                                            1.dp,
                                                                            if (expandedMacroSetup == 2) neonGreen else textMuted.copy(
                                                                                alpha = 0.3f
                                                                            )
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "EDIT ${macro2Name.uppercase()}",
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                }

                                                                if (expandedMacroSetup != 0) {
                                                                    val targetingMacro1 =
                                                                        expandedMacroSetup == 1
                                                                    val activeMacroName =
                                                                        if (targetingMacro1) macro1Name else macro2Name
                                                                    val activeSelectedList =
                                                                        if (targetingMacro1) macro1Entities else macro2Entities

                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            14.dp
                                                                        )
                                                                    )
                                                                    HorizontalDivider(
                                                                        color = textMuted.copy(
                                                                            alpha = 0.2f
                                                                        )
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            12.dp
                                                                        )
                                                                    )

                                                                    OutlinedTextField(
                                                                        value = activeMacroName,
                                                                        onValueChange = { newVal ->
                                                                            if (targetingMacro1) {
                                                                                macro1Name = newVal
                                                                                sharedPrefs.edit()
                                                                                    .putString(
                                                                                        "macro_1_name",
                                                                                        newVal
                                                                                    ).apply()
                                                                            } else {
                                                                                macro2Name = newVal
                                                                                sharedPrefs.edit()
                                                                                    .putString(
                                                                                        "macro_2_name",
                                                                                        newVal
                                                                                    ).apply()
                                                                            }
                                                                        },
                                                                        label = {
                                                                            Text(
                                                                                "MACRO LABEL NAME",
                                                                                fontSize = 9.sp,
                                                                                fontFamily = FontFamily.Monospace
                                                                            )
                                                                        },
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        singleLine = true,
                                                                        textStyle = TextStyle(
                                                                            color = currentTextColor,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            fontSize = 12.sp
                                                                        )
                                                                    )

                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            12.dp
                                                                        )
                                                                    )
                                                                    Text(
                                                                        "TOGGLE TARGET ENTITIES (${activeSelectedList.size} SELECTED):",
                                                                        color = textMuted,
                                                                        fontSize = 10.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )

                                                                    Box(
                                                                        modifier = Modifier.fillMaxWidth()
                                                                            .height(180.dp)
                                                                            .padding(top = 6.dp)
                                                                            .border(
                                                                                1.dp,
                                                                                textMuted.copy(alpha = 0.15f),
                                                                                RoundedCornerShape(8.dp)
                                                                            ).background(
                                                                                currentBgColor.copy(
                                                                                    alpha = 0.4f
                                                                                )
                                                                            )
                                                                    ) {
                                                                        LazyColumn(
                                                                            modifier = Modifier.padding(
                                                                                6.dp
                                                                            ),
                                                                            verticalArrangement = Arrangement.spacedBy(
                                                                                4.dp
                                                                            )
                                                                        ) {
                                                                            val allocatableDevices =
                                                                                deviceList.filter { it.domain == "light" || it.domain == "switch" }

                                                                            items(allocatableDevices) { device ->
                                                                                val isIncluded =
                                                                                    activeSelectedList.contains(
                                                                                        device.entityId
                                                                                    )
                                                                                Row(
                                                                                    modifier = Modifier
                                                                                        .fillMaxWidth()
                                                                                        .background(
                                                                                            if (isIncluded) neonCyan.copy(
                                                                                                alpha = 0.05f
                                                                                            ) else Color.Transparent,
                                                                                            RoundedCornerShape(
                                                                                                4.dp
                                                                                            )
                                                                                        )
                                                                                        .clickable {
                                                                                            triggerInterfaceFeedback()
                                                                                            if (isIncluded) {
                                                                                                activeSelectedList.remove(
                                                                                                    device.entityId
                                                                                                )
                                                                                            } else {
                                                                                                activeSelectedList.add(
                                                                                                    device.entityId
                                                                                                )
                                                                                            }
                                                                                            val key =
                                                                                                if (targetingMacro1) "macro_1_entities" else "macro_2_entities"
                                                                                            sharedPrefs.edit()
                                                                                                .putStringSet(
                                                                                                    key,
                                                                                                    activeSelectedList.toSet()
                                                                                                )
                                                                                                .apply()
                                                                                        }
                                                                                        .padding(
                                                                                            horizontal = 8.dp,
                                                                                            vertical = 6.dp
                                                                                        ),
                                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                                    verticalAlignment = Alignment.CenterVertically
                                                                                ) {
                                                                                    Column {
                                                                                        Text(
                                                                                            device.friendlyName,
                                                                                            color = currentTextColor,
                                                                                            fontSize = 12.sp,
                                                                                            fontWeight = FontWeight.Bold
                                                                                        )
                                                                                        Text(
                                                                                            device.entityId,
                                                                                            color = textMuted,
                                                                                            fontSize = 9.sp,
                                                                                            fontFamily = FontFamily.Monospace
                                                                                        )
                                                                                    }
                                                                                    Checkbox(
                                                                                        checked = isIncluded,
                                                                                        onCheckedChange = null,
                                                                                        colors = CheckboxDefaults.colors(
                                                                                            checkedColor = neonCyan
                                                                                        )
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
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            ),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                textMuted.copy(alpha = 0.15f)
                                                            )
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
                                                                    modifier = Modifier.padding(
                                                                        bottom = 12.dp
                                                                    )
                                                                )

                                                                OutlinedTextField(
                                                                    value = haIpAddress,
                                                                    onValueChange = {
                                                                        haIpAddress =
                                                                            it; sharedPrefs.edit()
                                                                        .putString("ha_ip", it)
                                                                        .apply()
                                                                    },
                                                                    label = {
                                                                        Text(
                                                                            "HOME ASSISTANT ENDPOINT IP & PORT",
                                                                            fontSize = 10.sp,
                                                                            fontFamily = FontFamily.Monospace
                                                                        )
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    singleLine = true,
                                                                    textStyle = TextStyle(
                                                                        color = currentTextColor,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        fontSize = 13.sp
                                                                    ),
                                                                    colors = OutlinedTextFieldDefaults.colors(
                                                                        focusedBorderColor = neonCyan,
                                                                        unfocusedBorderColor = textMuted.copy(
                                                                            alpha = 0.3f
                                                                        ),
                                                                        focusedLabelColor = neonCyan
                                                                    )
                                                                )

                                                                Spacer(modifier = Modifier.height(12.dp))

                                                                OutlinedTextField(
                                                                    value = haAccessToken,
                                                                    onValueChange = {
                                                                        haAccessToken =
                                                                            it; sharedPrefs.edit()
                                                                        .putString("ha_token", it)
                                                                        .apply()
                                                                    },
                                                                    label = {
                                                                        Text(
                                                                            "LONG-LIVED ACCESS TOKEN",
                                                                            fontSize = 10.sp,
                                                                            fontFamily = FontFamily.Monospace
                                                                        )
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    singleLine = true,
                                                                    textStyle = TextStyle(
                                                                        color = currentTextColor,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        fontSize = 11.sp
                                                                    ),
                                                                    colors = OutlinedTextFieldDefaults.colors(
                                                                        focusedBorderColor = neonCyan,
                                                                        unfocusedBorderColor = textMuted.copy(
                                                                            alpha = 0.3f
                                                                        ),
                                                                        focusedLabelColor = neonCyan
                                                                    )
                                                                )

                                                                Spacer(modifier = Modifier.height(12.dp))

                                                                Button(
                                                                    onClick = {
                                                                        triggerInterfaceFeedback()
                                                                        try {
                                                                            initializeAndConnectHA(
                                                                                haIpAddress,
                                                                                haAccessToken
                                                                            )
                                                                        } catch (e: Exception) {
                                                                            e.printStackTrace()
                                                                        }
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .height(38.dp),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    colors = ButtonDefaults.buttonColors(
                                                                        containerColor = neonCyan.copy(
                                                                            alpha = 0.1f
                                                                        )
                                                                    )
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
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            ),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                textMuted.copy(alpha = 0.1f)
                                                            )
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
                                                                    modifier = Modifier.padding(
                                                                        bottom = 12.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp),
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        "PANEL LOCAL IP",
                                                                        color = currentTextColor,
                                                                        fontSize = 12.sp
                                                                    )
                                                                    Text(
                                                                        text = getLocalIpAddress(),
                                                                        color = textMuted,
                                                                        fontSize = 12.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }

                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.1f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 6.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp),
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        "TARGET HOSTNAME",
                                                                        color = currentTextColor,
                                                                        fontSize = 12.sp
                                                                    )
                                                                    Text(
                                                                        text = haIpAddress.split(":")
                                                                            .first(),
                                                                        color = textMuted,
                                                                        fontSize = 12.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }

                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.1f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 6.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(
                                                                        "PING ROUTE STATUS",
                                                                        color = currentTextColor,
                                                                        fontSize = 12.sp
                                                                    )

                                                                    val pingColor = when {
                                                                        diagnosticPingResult.contains(
                                                                            "SUCCESS"
                                                                        ) -> neonGreen

                                                                        diagnosticPingResult.contains(
                                                                            "TESTING"
                                                                        ) -> neonCyan

                                                                        diagnosticPingResult.contains(
                                                                            "FAILED"
                                                                        ) || diagnosticPingResult.contains(
                                                                            "ERROR"
                                                                        ) -> Color(0xFFFF5555)

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
                                                                        runNetworkDiagnosticPing()
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .height(38.dp),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                                        containerColor = neonCyan.copy(
                                                                            alpha = 0.02f
                                                                        )
                                                                    ),
                                                                    border = BorderStroke(
                                                                        1.dp,
                                                                        neonCyan.copy(alpha = 0.25f)
                                                                    )
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
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            ),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                textMuted.copy(alpha = 0.1f)
                                                            )
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp)) {
                                                                Text(
                                                                    "DISPLAY CONTROLS",
                                                                    color = currentTextColor,
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Spacer(modifier = Modifier.height(10.dp))

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "KEEP SCREEN ON",
                                                                            color = currentTextColor,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Medium
                                                                        )
                                                                        Text(
                                                                            "Prevents device from locking and keeps the screen on",
                                                                            color = textMuted,
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                    Switch(
                                                                        checked = keepScreenAwake,
                                                                        onCheckedChange = {
                                                                            triggerInterfaceFeedback()
                                                                            keepScreenAwake =
                                                                                it; sharedPrefs.edit()
                                                                            .putBoolean(
                                                                                "keep_awake",
                                                                                it
                                                                            ).apply()
                                                                        },
                                                                        colors = SwitchDefaults.colors(
                                                                            checkedThumbColor = neonCyan,
                                                                            checkedTrackColor = neonCyan.copy(
                                                                                alpha = 0.3f
                                                                            )
                                                                        )
                                                                    )
                                                                }

                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.15f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 8.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "BURN-IN PROTECTION",
                                                                            color = currentTextColor,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Medium
                                                                        )
                                                                        Text(
                                                                            "Periodically micro-shifts interface pixels to prevent static image retention",
                                                                            color = textMuted,
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                    Switch(
                                                                        checked = enableBurnInProtection,
                                                                        onCheckedChange = {
                                                                            triggerInterfaceFeedback()
                                                                            enableBurnInProtection =
                                                                                it; sharedPrefs.edit()
                                                                            .putBoolean(
                                                                                "burn_in_protection",
                                                                                it
                                                                            ).apply()
                                                                        },
                                                                        colors = SwitchDefaults.colors(
                                                                            checkedThumbColor = neonCyan,
                                                                            checkedTrackColor = neonCyan.copy(
                                                                                alpha = 0.3f
                                                                            )
                                                                        )
                                                                    )
                                                                }

                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.15f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 8.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        )
                                                                    ) {
                                                                        Text(
                                                                            "NIGHT MODE",
                                                                            color = currentTextColor,
                                                                            fontSize = 12.sp,
                                                                            fontWeight = FontWeight.Medium
                                                                        )
                                                                        Text(
                                                                            "Set times for the screen to go blank for when you're sleeping. Touch screen to wake again. Still prevents device locking",
                                                                            color = textMuted,
                                                                            fontSize = 10.sp
                                                                        )
                                                                    }
                                                                    Switch(
                                                                        checked = enableSleepTimer,
                                                                        onCheckedChange = {
                                                                            triggerInterfaceFeedback()
                                                                            enableSleepTimer =
                                                                                it; sharedPrefs.edit()
                                                                            .putBoolean(
                                                                                "enable_sleep_timer",
                                                                                it
                                                                            ).apply()
                                                                        },
                                                                        colors = SwitchDefaults.colors(
                                                                            checkedThumbColor = neonCyan,
                                                                            checkedTrackColor = neonCyan.copy(
                                                                                alpha = 0.3f
                                                                            )
                                                                        )
                                                                    )
                                                                }

                                                                if (enableSleepTimer) {
                                                                    val countdownText = remember(
                                                                        sleepHour,
                                                                        sleepMinute
                                                                    ) {
                                                                        val calendar =
                                                                            java.util.Calendar.getInstance()
                                                                        val currentH =
                                                                            calendar.get(java.util.Calendar.HOUR_OF_DAY)
                                                                        val currentM =
                                                                            calendar.get(java.util.Calendar.MINUTE)
                                                                        val currentTotalMinutes =
                                                                            (currentH * 60) + currentM
                                                                        val targetTotalMinutes =
                                                                            (sleepHour * 60) + sleepMinute
                                                                        var diff =
                                                                            targetTotalMinutes - currentTotalMinutes
                                                                        if (diff <= 0) diff += 24 * 60
                                                                        val hoursLeft = diff / 60
                                                                        val minsLeft = diff % 60
                                                                        "SYSTEM ENGAGEMENT IN: ${hoursLeft}H ${minsLeft}M"
                                                                    }
                                                                    Spacer(
                                                                        modifier = Modifier.height(
                                                                            10.dp
                                                                        )
                                                                    )
                                                                    Text(
                                                                        text = countdownText,
                                                                        color = neonCyan,
                                                                        fontSize = 12.sp,
                                                                        fontFamily = FontFamily.Monospace,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                }

                                                                Spacer(modifier = Modifier.height(10.dp))
                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.1f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        bottom = 8.dp
                                                                    )
                                                                )

                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(
                                                                        16.dp
                                                                    )
                                                                ) {
                                                                    Column(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        ),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text(
                                                                            text = "BLACKOUT START",
                                                                            color = textMuted,
                                                                            fontSize = 11.sp,
                                                                            fontFamily = FontFamily.Monospace
                                                                        )
                                                                        Text(
                                                                            text = String.format(
                                                                                "%02d:%02d",
                                                                                sleepHour,
                                                                                sleepMinute
                                                                            ),
                                                                            color = if (enableSleepTimer) neonCyan else textMuted,
                                                                            fontSize = 24.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            modifier = Modifier.padding(
                                                                                vertical = 4.dp
                                                                            )
                                                                        )
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                                4.dp
                                                                            )
                                                                        ) {
                                                                            listOf(
                                                                                "H-",
                                                                                "H+",
                                                                                "M-",
                                                                                "M+"
                                                                            ).forEach { label ->
                                                                                OutlinedButton(
                                                                                    onClick = {
                                                                                        triggerInterfaceFeedback()
                                                                                        when (label) {
                                                                                            "H-" -> if (sleepHour > 0) sleepHour-- else sleepHour =
                                                                                                23

                                                                                            "H+" -> if (sleepHour < 23) sleepHour++ else sleepHour =
                                                                                                0

                                                                                            "M-" -> if (sleepMinute >= 15) sleepMinute -= 15 else sleepMinute =
                                                                                                45

                                                                                            "M+" -> if (sleepMinute <= 30) sleepMinute += 15 else sleepMinute =
                                                                                                0
                                                                                        }
                                                                                        sharedPrefs.edit()
                                                                                            .putInt(
                                                                                                "sleep_hour",
                                                                                                sleepHour
                                                                                            )
                                                                                            .putInt(
                                                                                                "sleep_minute",
                                                                                                sleepMinute
                                                                                            )
                                                                                            .apply()
                                                                                    },
                                                                                    enabled = enableSleepTimer,
                                                                                    modifier = Modifier.width(
                                                                                        38.dp
                                                                                    ).height(32.dp),
                                                                                    contentPadding = PaddingValues(
                                                                                        0.dp
                                                                                    ),
                                                                                    border = BorderStroke(
                                                                                        1.dp,
                                                                                        textMuted.copy(
                                                                                            alpha = 0.2f
                                                                                        )
                                                                                    )
                                                                                ) {
                                                                                    Text(
                                                                                        label,
                                                                                        color = if (enableSleepTimer) currentTextColor else textMuted,
                                                                                        fontSize = 10.sp
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Column(
                                                                        modifier = Modifier.weight(
                                                                            1f
                                                                        ),
                                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                                    ) {
                                                                        Text(
                                                                            text = "BLACKOUT WAKE",
                                                                            color = textMuted,
                                                                            fontSize = 11.sp,
                                                                            fontFamily = FontFamily.Monospace
                                                                        )
                                                                        Text(
                                                                            text = String.format(
                                                                                "%02d:%02d",
                                                                                wakeHour,
                                                                                wakeMinute
                                                                            ),
                                                                            color = if (enableSleepTimer) neonCyan else textMuted,
                                                                            fontSize = 24.sp,
                                                                            fontWeight = FontWeight.Bold,
                                                                            fontFamily = FontFamily.Monospace,
                                                                            modifier = Modifier.padding(
                                                                                vertical = 4.dp
                                                                            )
                                                                        )
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.spacedBy(
                                                                                4.dp
                                                                            )
                                                                        ) {
                                                                            listOf(
                                                                                "H-",
                                                                                "H+",
                                                                                "M-",
                                                                                "M+"
                                                                            ).forEach { label ->
                                                                                OutlinedButton(
                                                                                    onClick = {
                                                                                        triggerInterfaceFeedback()
                                                                                        when (label) {
                                                                                            "H-" -> if (wakeHour > 0) wakeHour-- else wakeHour =
                                                                                                23

                                                                                            "H+" -> if (wakeHour < 23) wakeHour++ else wakeHour =
                                                                                                0

                                                                                            "M-" -> if (wakeMinute >= 15) wakeMinute -= 15 else wakeMinute =
                                                                                                45

                                                                                            "M+" -> if (wakeMinute <= 30) wakeMinute += 15 else wakeMinute =
                                                                                                0
                                                                                        }
                                                                                        sharedPrefs.edit()
                                                                                            .putInt(
                                                                                                "wake_hour",
                                                                                                wakeHour
                                                                                            )
                                                                                            .putInt(
                                                                                                "wake_minute",
                                                                                                wakeMinute
                                                                                            )
                                                                                            .apply()
                                                                                    },
                                                                                    enabled = enableSleepTimer,
                                                                                    modifier = Modifier.width(
                                                                                        38.dp
                                                                                    ).height(32.dp),
                                                                                    contentPadding = PaddingValues(
                                                                                        0.dp
                                                                                    ),
                                                                                    border = BorderStroke(
                                                                                        1.dp,
                                                                                        textMuted.copy(
                                                                                            alpha = 0.2f
                                                                                        )
                                                                                    )
                                                                                ) {
                                                                                    Text(
                                                                                        label,
                                                                                        color = if (enableSleepTimer) currentTextColor else textMuted,
                                                                                        fontSize = 10.sp
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                Spacer(modifier = Modifier.height(10.dp))
                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.1f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 10.dp
                                                                    )
                                                                )

                                                                val displayedSeconds =
                                                                    (wakeDurationMinutes * 60).roundToInt()
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                                ) {
                                                                    Text(
                                                                        "SCREEN TIMEOUT DELAY",
                                                                        color = currentTextColor,
                                                                        fontSize = 11.sp
                                                                    )
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
                                                                    onValueChange = {
                                                                        wakeDurationMinutes =
                                                                            it; sharedPrefs.edit()
                                                                        .putFloat(
                                                                            "wake_duration",
                                                                            it
                                                                        ).apply()
                                                                    },
                                                                    valueRange = 0.5f..5.0f,
                                                                    colors = SliderDefaults.colors(
                                                                        thumbColor = neonCyan,
                                                                        activeTrackColor = neonCyan
                                                                    ),
                                                                    enabled = enableSleepTimer
                                                                )
                                                                HorizontalDivider(
                                                                    color = textMuted.copy(
                                                                        alpha = 0.15f
                                                                    ),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(
                                                                        vertical = 12.dp
                                                                    )
                                                                )

                                                                OutlinedButton(
                                                                    onClick = {
                                                                        triggerInterfaceFeedback()
                                                                        isManuallyBlackedOut =
                                                                            true; setWindowBrightness(
                                                                        0.01f
                                                                    )
                                                                    },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .height(42.dp),
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                                        containerColor = neonCyan.copy(
                                                                            alpha = 0.03f
                                                                        )
                                                                    ),
                                                                    border = BorderStroke(
                                                                        1.dp,
                                                                        neonCyan.copy(alpha = 0.3f)
                                                                    )
                                                                ) {
                                                                    Row(
                                                                        horizontalArrangement = Arrangement.spacedBy(
                                                                            8.dp
                                                                        ),
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier.size(
                                                                                6.dp
                                                                            ).background(
                                                                                color = neonCyan,
                                                                                shape = RoundedCornerShape(
                                                                                    50.dp
                                                                                )
                                                                            )
                                                                        )
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
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = currentCardColor
                                                            )
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(12.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Column {
                                                                    Text(
                                                                        "ACTIVE ENDPOINT RUNTIME",
                                                                        color = currentTextColor,
                                                                        fontSize = 13.sp,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    Text(
                                                                        haIpAddress,
                                                                        color = textMuted,
                                                                        fontSize = 11.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
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
                                                                showRawRegistry =
                                                                    true; activeRegistryFilter =
                                                                "ALL"
                                                            },
                                                            modifier = Modifier.fillMaxWidth()
                                                                .height(42.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = textMuted.copy(
                                                                    alpha = 0.1f
                                                                )
                                                            ),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                textMuted.copy(alpha = 0.3f)
                                                            ),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Text(
                                                                "EXPLORE RAW SYSTEM ENTITIES",
                                                                color = textMuted,
                                                                fontSize = 11.sp,
                                                                fontFamily = FontFamily.Monospace
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .padding(bottom = 8.dp),
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
                                                        Text(
                                                            "GLOBAL REGISTRY ARCHIVE",
                                                            color = textMuted,
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth()
                                                            .padding(bottom = 12.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            6.dp
                                                        )
                                                    ) {
                                                        listOf(
                                                            "ALL",
                                                            "LIGHTS",
                                                            "SWITCHES",
                                                            "SENSORS"
                                                        ).forEach { filterType ->
                                                            val isSelected =
                                                                activeRegistryFilter == filterType
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .background(
                                                                        color = if (isSelected) neonCyan.copy(
                                                                            alpha = 0.12f
                                                                        ) else currentCardColor,
                                                                        shape = RoundedCornerShape(6.dp)
                                                                    )
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = if (isSelected) neonCyan else textMuted.copy(
                                                                            alpha = 0.2f
                                                                        ),
                                                                        shape = RoundedCornerShape(6.dp)
                                                                    )
                                                                    .clickable {
                                                                        triggerInterfaceFeedback()
                                                                        activeRegistryFilter =
                                                                            filterType
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

                                                    val filteredDeviceList =
                                                        remember(deviceList, activeRegistryFilter) {
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
                                                                Box(
                                                                    modifier = Modifier.fillMaxWidth()
                                                                        .padding(top = 32.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        "NO SEGMENTS MATCHING FILTER",
                                                                        color = textMuted,
                                                                        fontSize = 12.sp,
                                                                        fontFamily = FontFamily.Monospace
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            items(filteredDeviceList) { device ->
                                                                val resolvedDisplayName =
                                                                    customEntityAliases[device.entityId]
                                                                        ?: device.friendlyName

                                                                Card(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .pointerInput(Unit) {
                                                                            detectTapGestures(
                                                                                onLongPress = {
                                                                                    triggerInterfaceFeedback()
                                                                                    temporaryAliasInputText =
                                                                                        customEntityAliases[device.entityId]
                                                                                            ?: ""
                                                                                    entityToRenameInDialog =
                                                                                        device
                                                                                }
                                                                            )
                                                                        },
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    colors = CardDefaults.cardColors(
                                                                        containerColor = currentCardColor.copy(
                                                                            alpha = 0.6f
                                                                        )
                                                                    )
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.padding(
                                                                            14.dp
                                                                        ),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Column(
                                                                            modifier = Modifier.weight(
                                                                                1f
                                                                            )
                                                                        ) {
                                                                            Text(
                                                                                text = resolvedDisplayName,
                                                                                color = currentTextColor,
                                                                                fontSize = 15.sp
                                                                            )
                                                                            Text(
                                                                                text = device.entityId,
                                                                                color = textMuted,
                                                                                fontSize = 11.sp,
                                                                                fontFamily = FontFamily.Monospace
                                                                            )
                                                                        }

                                                                        val formattedStateText =
                                                                            formatDeviceState(
                                                                                device.entityId,
                                                                                device.state,
                                                                                device.domain
                                                                            )
                                                                        val isStateActive =
                                                                            device.state == "ON" || device.state == "HOME"

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


                                                //Column(
                                                //    modifier = Modifier.fillMaxSize().padding(16.dp),
                                                //    verticalArrangement = Arrangement.spacedBy(12.dp),
                                                //    horizontalAlignment = Alignment.Start
                                                //) {
                                                //    Text(
                                                //        "CENTRAL CHASSIS SETTINGS GATEWAY CONFIG",
                                                //        color = textMuted,
                                                //        fontSize = 11.sp,
                                                //        fontFamily = FontFamily.Monospace,
                                                //        modifier = Modifier.padding(bottom = 8.dp)
                                                //    )


                                                }
                                            }

                                        }
                                    }
                                }

                            }
                        // ENTITY ALIAS RENAME OVERLAY DIALOG
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

                        // SYSTEM BACKLIGHT FADE LAYER OVERLAY
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

                AnimatedVisibility(
                    visible = isManuallyBlackedOut,
                    enter = fadeIn(animationSpec = tween(1000)), exit = fadeOut(animationSpec = tween(1000))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black).clickable {
                            triggerInterfaceFeedback()
                            isManuallyBlackedOut = false
                            setWindowBrightness(-1f)
                        }
                    )
                }

            }
        }
    }
}

// ====================================================================================
// EXTRA CHASSIS DECKS: Standalone screen component blocks (Top-Level Scope)
// ====================================================================================

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

    var isEditingName by remember { mutableStateOf(false) }
    var nameDraftText by remember { mutableStateOf("") }

    var showSchedulerSubmenu by remember { mutableStateOf(false) }
    // Tracks whether the schedule matrix is active (True) or if the room is in full manual mode (False)
    val roomScheduleEnabledStates = remember { mutableStateMapOf<String, Boolean>() }

    // Temporary mock database mapped by room names.
    // This gives the UI data to render until we link up the Home Assistant text string parsing.
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
            val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val targetEntityId = "input_text.${dynamicSlug}_schedule"

            // Find this room's text helper entity inside the current device list
            val scheduleDevice = deviceList.find { it.entityId == targetEntityId }
            val rawStateString = scheduleDevice?.state

            // Only parse if we have a valid state and aren't actively editing this room
            val lastEditTime = roomSyncCooldowns[roomName] ?: 0L
            val isCoolingDown = (System.currentTimeMillis() - lastEditTime) < 1500L

            if (!rawStateString.isNullOrEmpty() && rawStateString != "unknown" && rawStateString != "unavailable" && !isCoolingDown) {
                try {
                    // Check if it's the old JSON format or our new compact format
                    if (!rawStateString.startsWith("[")) {
                        // COMPACT PARSER: Format is "time,temp,heating,day;time,temp,heating,day"
                        val parsedSlots = rawStateString.split(";").filter { it.isNotBlank() }.map { slotRaw ->
                            val parts = slotRaw.split(",")
                            ClimateScheduleSlot(
                                id = java.util.UUID.randomUUID().toString(), // Automatically generate a clean local ID
                                time = parts.getOrNull(0) ?: "12:00",
                                targetTemp = parts.getOrNull(1)?.toFloatOrNull() ?: 20.0f,
                                isHeatingOn = parts.getOrNull(2) == "1",
                                dayTarget = parts.getOrNull(3) ?: "WEEKDAYS"
                            )
                        }

                        // Only update UI if the count or data is different to avoid layout loops
                        val currentLocal = mockRoomSchedules[roomName] ?: emptyList()
                        if (currentLocal.size != parsedSlots.size || currentLocal.map { it.time } != parsedSlots.map { it.time }) {
                            mockRoomSchedules[roomName] = parsedSlots
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HA_IMPORT", "Failed to parse schedule format for $roomName: ${e.message}")
                }
            }
        }
    }

// =====================================================================
// FIXED AUTOMATIC HOME ASSISTANT DATA IMPORT ON RESTART
// =====================================================================
    LaunchedEffect(deviceList, roomNames) {
        val textEntities = deviceList.filter { it.entityId.startsWith("input_text", ignoreCase = true) }
        Log.d("HA_DIAGNOSTIC", "--- DUMPING ALL INPUT_TEXT ENTITIES FOUND (${textEntities.size} found) ---")
        textEntities.forEach {
            Log.d("HA_DIAGNOSTIC", " -> Found: '${it.entityId}' with state: '${it.state}'")
        }
        Log.d("HA_DIAGNOSTIC", "--------------------------------------------------------")
        roomNames.forEach { roomName ->
            // Clean up the name string reliably
            val dynamicSlug = roomName.lowercase().trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val targetEntityId = "input_text.${dynamicSlug}_schedule"

            // Find this room's text helper entity inside the current device list (case-insensitive check)
            val scheduleDevice = deviceList.find { it.entityId.equals(targetEntityId, ignoreCase = true) }

            if (scheduleDevice == null) {
                // Log this as a low-level debug instead of an error, because deviceList updates continuously as the app connects
                Log.d("HA_RESTART", "Waiting for entity '$targetEntityId' to load into deviceList...")
                return@forEach
            }

            val rawStateString = scheduleDevice.state
            Log.d("HA_RESTART", "Found entity '$targetEntityId' for room '$roomName'. Raw state value: \"$rawStateString\"")

            val lastEditTime = roomSyncCooldowns[roomName] ?: 0L
            val isCoolingDown = (System.currentTimeMillis() - lastEditTime) < 1500L

            if (!rawStateString.isNullOrEmpty() && rawStateString != "unknown" && rawStateString != "unavailable" && !isCoolingDown) {
                try {
                    // Ignore legacy JSON layouts
                    if (rawStateString.startsWith("[")) {
                        Log.w("HA_RESTART", "Ignoring legacy JSON format found for $roomName: $rawStateString")
                        return@forEach
                    }

                    // Parse the compact format: "time,temp,heating,day;time,temp,heating,day"
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

                    // Compare only the structural properties to avoid continuous loop re-renders
                    val structuralLocal = currentLocal.map { "${it.time},${it.targetTemp},${it.isHeatingOn},${it.dayTarget}" }
                    val structuralIncoming = parsedSlots.map { "${it.time},${it.targetTemp},${it.isHeatingOn},${it.dayTarget}" }

                    if (structuralLocal != structuralIncoming) {
                        Log.d("HA_RESTART", "Importing ${parsedSlots.size} slots successfully for $roomName.")
                        mockRoomSchedules[roomName] = parsedSlots
                    }
                } catch (e: Exception) {
                    Log.e("HA_RESTART", "Failed to parse compact string for $roomName: ${e.message}")
                }
            }
        }
    }



    // Safety confirmation gate variable declared at the root level
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(isEditingName) {
        if (isEditingName) nameDraftText = selectedRoomName
    }

    // Auto-resets local interaction flags when moving back and forth between zones
    LaunchedEffect(drilledRoomIndex) {
        isEditingName = false
        showDeleteConfirmation = false
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

                // Master Switch Row (Styled like a premium card)
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
                        // Weight modifier allows flexible line wrapping for custom zone names without crashing layout
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

                // ===  REGISTER NEW CLIMATE ZONE ===
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
                            val cleanSlug = newZoneName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

                            if (!roomMappings.containsKey(newZoneName)) {
                                // 1. Track locally in the Android App layout state
                                roomMappings[newZoneName] = Pair("sensor.${cleanSlug}_temperature", "sensor.${cleanSlug}_humidity")
                                roomTargetStates[newZoneName] = 21.0f

                                // 2. Fire helper creation requests over the active socket channels instantly!
                                haClient?.createHelperEntities(newZoneName)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ SYSTEM REGISTER NEW CLIMATE ZONE", color = neonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

            } else {
                // ----------------------------------------------------
                // VIEW B: ISOLATED CLIMATE CONTROLS
                // ----------------------------------------------------
                val roomName = selectedRoomName
                val (tempEntityId, humidityEntityId) = roomMappings[roomName] ?: Pair("", "")

                val rTemp = deviceList.find { it.entityId == tempEntityId }?.state?.toFloatOrNull() ?: 0.0f
                val rHum = deviceList.find { it.entityId == humidityEntityId }?.state ?: "--"
                val activeRoomTarget = roomTargetStates[roomName] ?: 21.0f

                // Navigation Link Button (Matches "← BACK")
                Row(
                    modifier = Modifier
                        .clickable { onDrillRoom(null) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("← BACK", color = neonCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // MAIN ADVANCED CONSOLE CARD WINDOW (TACTICAL THERMOSTAT THEME)
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
                                            // 1. Generate both slugs for Home Assistant
                                            val oldSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                            val newSlug = cleanDraft.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

                                            // 2. Tell Home Assistant to migrate both helpers in its registry live
                                            haClient?.renameHelperEntity(oldSlug = oldSlug, newSlug = newSlug, newDisplayName = cleanDraft, isNumberHelper = true)  // Renames input_number target
                                            haClient?.renameHelperEntity(oldSlug = oldSlug, newSlug = newSlug, newDisplayName = cleanDraft, isNumberHelper = false) // Renames input_text schedule

                                            // 3. Capture all old map state metrics safely
                                            val currentMappingData = roomMappings[roomName] ?: Pair("sensor.${oldSlug}_temperature", "sensor.${oldSlug}_humidity")
                                            val currentTargetData = roomTargetStates[roomName] ?: 21.0f
                                            val currentScheduleData = mockRoomSchedules[roomName] ?: emptyList()
                                            val currentEnabledState = roomScheduleEnabledStates[roomName] ?: true

                                            // 4. Deconstruct old states entirely from local state tracking
                                            roomMappings.remove(roomName)
                                            roomTargetStates.remove(roomName)
                                            mockRoomSchedules.remove(roomName)
                                            roomScheduleEnabledStates.remove(roomName)

                                            // 5. Build rewritten updated mappings pointing to the clean slug parameters
                                            roomMappings[cleanDraft] = Pair("sensor.${newSlug}_temperature", "sensor.${newSlug}_humidity")
                                            roomTargetStates[cleanDraft] = currentTargetData
                                            mockRoomSchedules[cleanDraft] = currentScheduleData
                                            roomScheduleEnabledStates[cleanDraft] = currentEnabledState

                                            // 6. Update your dynamic room names index list if it exists in your view parameters
                                            // Paste this fix instead:
                                            val nameIndex = roomNames.indexOf(roomName)
                                            if (nameIndex != -1) {
                                                // If it's a Compose SnapshotStateList, this removes and inserts cleanly
                                                if (roomNames is androidx.compose.runtime.snapshots.SnapshotStateList) {
                                                    roomNames.removeAt(nameIndex)
                                                    roomNames.add(nameIndex, cleanDraft)
                                                } else {
                                                    // If it's a standard mutable list casting check fallback
                                                    (roomNames as? MutableList<String>)?.set(nameIndex, cleanDraft)
                                                }
                                            }

                                            // Instantly force index to point to null or the new element cleanly
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

                    // ----------------------------------------------------
                    // 1. TOP INTERACTIVE SECTION: SETPOINT READOUT + STACKED BUTTONS
                    // ----------------------------------------------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentBgColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, textMuted.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Target Setpoint Display
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

                        // Stacked Adjustment Triggers (▲ above ▼)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Increment button
                            Box(
                                modifier = Modifier
                                    .size(width = 52.dp, height = 34.dp)
                                    .background(neonCyan.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                    .border(1.dp, neonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        triggerInterfaceFeedback()
                                        val newTarget = activeRoomTarget + 0.5f
                                        roomTargetStates[roomName] = newTarget

                                        // === LIVE UPDATE HOME ASSISTANT HELPER ===
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

                            // Decrement button
                            Box(
                                modifier = Modifier
                                    .size(width = 52.dp, height = 34.dp)
                                    .background(textMuted.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                    .border(1.dp, textMuted.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        triggerInterfaceFeedback()
                                        val newTarget = activeRoomTarget - 0.5f // (This fixes the decrement bug too!)
                                        roomTargetStates[roomName] = newTarget

                                        // === LIVE UPDATE HOME ASSISTANT HELPER ===
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

                    // ----------------------------------------------------
                    // 2. BOTTOM TELEMETRY SECTION: AMBIENT TEMP & HUMIDITY MATRIX
                    // ----------------------------------------------------
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(currentBgColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ambient Temperature Row
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

                        // Relative Humidity Row
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

                    // ----------------------------------------------------
                    // 2.5 ClIMATE SCHEDULER SYSTEM
                    // ----------------------------------------------------
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

                        // === MOVE THIS LINE HERE (Above the Master Switch Row) ===
                        val activeSchedule = mockRoomSchedules[roomName] ?: emptyList()

                        // Get the master state for this specific room, defaulting to true (Active)
                        val isScheduleEnabled = roomScheduleEnabledStates[roomName] ?: true

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // ----------------------------------------------------
                            // MASTER AUTOMATION OVERRIDE SWITCH
                            // ----------------------------------------------------
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
                                        haClient?.updateRoomScheduleMatrix(
                                            entityId = "input_text.${dynamicSlug}_schedule",
                                            slots = activeSchedule, // <-- CHANGE 'updatedList' TO 'activeSchedule' HERE!
                                            isEngineEnabled = nextState // <-- ALSO CHANGE 'isScheduleEnabled' TO 'nextState' HERE!
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

                                // Cyber Action Status Box on the right side
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

                            // ----------------------------------------------------
                            // TIMELINE ROWS (Visually faded out if master switch is off)
                            // ----------------------------------------------------
                            //val activeSchedule = mockRoomSchedules[roomName] ?: emptyList()

                            if (activeSchedule.isEmpty()) {
                                Text(
                                    text = "NO RUNTIME PROFILE DETECTED. SYSTEM RUNS PASSIVE MANUAL TARGETS.",
                                    color = textMuted.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                // Apply a global visual modifier group opacity fade if disabled
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
                                        // LINE 1: POWER SWITCH | CLOCK TIME CLICKER | INLINE DELETE
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Primary Power Switch Node
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

                                                            // 1. Calculate the updated state array locally
                                                            val updatedList = activeSchedule.map {
                                                                if (it.id == slot.id) it.copy(isHeatingOn = !it.isHeatingOn) else it
                                                            }
                                                            roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                            mockRoomSchedules[roomName] = updatedList

                                                            // 2. === DYNAMIC HOME ASSISTANT LIVE SYNC ===
                                                            val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                            haClient?.updateRoomScheduleMatrix(
                                                                entityId = "input_text.${dynamicSlug}_schedule",
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

                                                // Interactive Time Link
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

                                            // Destructive Inline cross
                                            Text(
                                                text = "✕",
                                                color = Color.Red.copy(alpha = 0.6f * alphaModifier),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable(enabled = isScheduleEnabled) {
                                                        triggerInterfaceFeedback()

                                                        // 1. Filter out the targeted element and save it
                                                        val updatedList = activeSchedule.filter { it.id != slot.id }
                                                        roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                        mockRoomSchedules[roomName] = updatedList

                                                        // 2. === DYNAMIC HOME ASSISTANT LIVE SYNC ===
                                                        val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                        haClient?.updateRoomScheduleMatrix(
                                                            entityId = "input_text.${dynamicSlug}_schedule",
                                                            slots = updatedList,
                                                            isEngineEnabled = isScheduleEnabled
                                                        )
                                                    }
                                                    .padding(horizontal = 6.dp)
                                            )
                                        }

                                        // LINE 2: DAY MATRIX TARGET SELECTOR & INDEPENDENT TEMPERATURE DECK
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Day Target configuration trigger
                                            Box(
                                                modifier = Modifier
                                                    .background(neonCyan.copy(alpha = 0.08f * alphaModifier), RoundedCornerShape(4.dp))
                                                    .border(1.dp, neonCyan.copy(alpha = 0.3f * alphaModifier), RoundedCornerShape(4.dp))
                                                    .clickable(enabled = isScheduleEnabled) {
                                                        triggerInterfaceFeedback()
                                                        val dayCycles = listOf("WEEKDAYS", "WEEKENDS", "EVERYDAY", "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                                                        val nextIdx = (dayCycles.indexOf(slot.dayTarget) + 1) % dayCycles.size

                                                        // 1. Map the updated state locally and assign it to a variable
                                                        val updatedList = activeSchedule.map {
                                                            if (it.id == slot.id) it.copy(dayTarget = dayCycles[nextIdx]) else it
                                                        }
                                                        mockRoomSchedules[roomName] = updatedList

                                                        // 2. LIVE SYNC EVENT TO HOME ASSISTANT
                                                        val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                        haClient?.updateRoomScheduleMatrix(
                                                            entityId = "input_text.${dynamicSlug}_schedule",
                                                            slots = updatedList,
                                                            isEngineEnabled = isScheduleEnabled
                                                        )
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(slot.dayTarget, color = neonCyan.copy(alpha = alphaModifier), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                            }

                                            // Temperature Target settings
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

                                                            // 1. Calculate the local list state change properly
                                                            val updatedList = activeSchedule.map {
                                                                if (it.id == slot.id) it.copy(targetTemp = it.targetTemp - 0.5f) else it
                                                            }
                                                            roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                            mockRoomSchedules[roomName] = updatedList

                                                            // 2. Broadcast the live matrix update to the helper
                                                            val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                            haClient?.updateRoomScheduleMatrix(
                                                                entityId = "input_text.${dynamicSlug}_schedule",
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

                                                            // 1. Calculate the local list state change properly
                                                            val updatedList = activeSchedule.map {
                                                                if (it.id == slot.id) it.copy(targetTemp = it.targetTemp + 0.5f) else it
                                                            }
                                                            roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                                            mockRoomSchedules[roomName] = updatedList

                                                            // 2. Broadcast the live matrix update to the helper
                                                            val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                                            haClient?.updateRoomScheduleMatrix(
                                                                entityId = "input_text.${dynamicSlug}_schedule",
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

                            // Action Trigger Button: Append New Matrix Element
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .background(neonCyan.copy(alpha = if (isScheduleEnabled) 0.05f else 0.01f), RoundedCornerShape(6.dp))
                                    .border(1.dp, neonCyan.copy(alpha = if (isScheduleEnabled) 0.3f else 0.05f), RoundedCornerShape(6.dp))
                                    .clickable(enabled = isScheduleEnabled) {
                                        triggerInterfaceFeedback()

                                        // 1. Build your new slot item matching your ClimateScheduleSlot model
                                        val fallbackSlot = ClimateScheduleSlot(
                                            id = java.util.UUID.randomUUID().toString(), // Adding a unique ID so it can be safely targeted later
                                            time = "12:00",
                                            targetTemp = 20.0f,
                                            isHeatingOn = true,
                                            dayTarget = "WEEKDAYS"
                                        )

                                        // 2. Append to local map
                                        val updatedList = activeSchedule + fallbackSlot
                                        roomSyncCooldowns[roomName] = System.currentTimeMillis()
                                        mockRoomSchedules[roomName] = updatedList

                                        // 3. === LIVE NETWORK SYNC EVENT ===
                                        val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                        haClient?.updateRoomScheduleMatrix(
                                            entityId = "input_text.${dynamicSlug}_schedule",
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

                                            val activeSchedule = mockRoomSchedules[roomName] ?: emptyList()
                                            val updatedSchedule = activeSchedule.map {
                                                if (it.id == activePickerSlotId) it.copy(time = newTimeString) else it
                                            }
                                            mockRoomSchedules[roomName] = updatedSchedule
                                            activePickerSlotId = null

                                            // === DYNAMIC HOME ASSISTANT LIVE SYNC ===
                                            // 1. Generate the unique slug for whatever room this is on the fly
                                            val dynamicSlug = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                                            val targetEntityId = "input_text.${dynamicSlug}_schedule"

                                            // 2. Fire the updated schedule array to your client engine
                                            haClient?.updateRoomScheduleMatrix(
                                                entityId = targetEntityId,
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

                    // SECONDARY UTILITIES SUB-PACK (HARDWARE ENG)
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
                                        .clickable { triggerInterfaceFeedback(); sensoryPickerTargetMode = "TEMP" }
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
                                        .clickable { triggerInterfaceFeedback(); sensoryPickerTargetMode = "HUMIDITY" }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(text = humidityEntityId.ifEmpty { "ATTACH HUMIDITY SENSOR ID..." }, color = if (humidityEntityId.isEmpty()) textMuted else currentTextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // ----------------------------------------------------
                    // 3. SECURE DESTRUCTIVE PROTOCOL ACTION BOX (WITH CONFIRMATION GATEWAY)
                    // ----------------------------------------------------
                    if (!showDeleteConfirmation) {
                        // Normal State Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Color.Red.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable {
                                    triggerInterfaceFeedback()
                                    showDeleteConfirmation = true // Armed
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PURGE CLIMATE ZONE FROM MATRIX", color = Color.Red.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        // Confirmation State Armed Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel / Abort
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .background(textMuted.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .border(1.dp, textMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        triggerInterfaceFeedback()
                                        showDeleteConfirmation = false // Safe reset
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CANCEL", color = currentTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            // Executioner Command Link
// 1. Grab the context right here outside the click block (Legal Composable context!)
                            val currentContext = androidx.compose.ui.platform.LocalContext.current

                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(38.dp)
                                    .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .border(2.dp, Color.Red, RoundedCornerShape(6.dp))
                                    .clickable {
                                        triggerInterfaceFeedback()

                                        val slugToDelete = roomName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

                                        // 2. Tell Home Assistant to completely destroy both helpers
                                        haClient?.deleteHelperEntity(slug = slugToDelete, isNumberHelper = true)
                                        haClient?.deleteHelperEntity(slug = slugToDelete, isNumberHelper = false)

                                        // 3. Clear out all local layout state maps completely
                                        roomMappings.remove(roomName)
                                        roomTargetStates.remove(roomName)
                                        mockRoomSchedules.remove(roomName)
                                        roomScheduleEnabledStates.remove(roomName)

                                        // 4. Use the context we captured above to write to SharedPreferences cleanly!
                                        val prefsToEdit = currentContext.getSharedPreferences("ha_config_prefs", android.content.Context.MODE_PRIVATE)
                                        val editor = prefsToEdit.edit()
                                        editor.remove("cfg_${roomName}_temp")
                                        editor.remove("cfg_${roomName}_hum")
                                        editor.putStringSet("climate_room_keys", roomMappings.keys.toSet())
                                        editor.apply()

                                        showDeleteConfirmation = false
                                        onDrillRoom(null) // Fire out back to main directory matrix
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "CONFIRM",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
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
                                        if (sensoryPickerTargetMode == "TEMP") {
                                            roomMappings[selectedRoomName] = Pair(sensorId, currentPair.second)
                                        } else {
                                            roomMappings[selectedRoomName] = Pair(currentPair.first, sensorId)
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

@Composable
fun SecurityControlTab(
    currentBgColor: Color,
    currentTextColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    textMuted: Color,
    triggerInterfaceFeedback: () -> Unit
) {
    var mockArmedState by remember { mutableStateOf(true) }
    var typedPinCode by remember { mutableStateOf("") }
    var securityDisplayMessage by remember { mutableStateOf("ENTER AUTHORIZATION PIN") }

    Column(
        modifier = Modifier.fillMaxSize().background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("CENTRAL PERIMETER DEFENSE MATRIX (MOCK)", color = if (mockArmedState) Color.Red else Color(0xFFFFB300), fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.fillMaxWidth().height(48.dp).background(if (mockArmedState) Color.Red.copy(alpha = 0.08f) else Color(0xFFFFB300).copy(alpha = 0.06f), RoundedCornerShape(8.dp)).border(1.dp, if (mockArmedState) Color.Red.copy(alpha = 0.4f) else Color(0xFFFFB300).copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Text(text = if (mockArmedState) "PERIMETER INFRASTRUCTURE: ARMED // SECURE" else "PERIMETER INFRASTRUCTURE: DISARMED // VULNERABLE", color = if (mockArmedState) Color.Red else Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Row(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp)).padding(vertical = 12.dp, horizontal = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = securityDisplayMessage, color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(text = if (typedPinCode.isEmpty()) "----" else "• ".repeat(typedPinCode.length), color = if (mockArmedState) Color.Red else neonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        val padDigits = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("CLR", "0", "ENT"))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            padDigits.forEach { rowKeylist ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    rowKeylist.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .weight(1f).height(44.dp).background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp)).border(1.dp, currentTextColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clickable {
                                    triggerInterfaceFeedback()
                                    when (digit) {
                                        "CLR" -> { typedPinCode = ""; securityDisplayMessage = "ENTER AUTHORIZATION PIN" }
                                        "ENT" -> {
                                            if (typedPinCode == "1234") { mockArmedState = !mockArmedState; typedPinCode = ""; securityDisplayMessage = "ACCESS GRANTED" }
                                            else { typedPinCode = ""; securityDisplayMessage = "INVALID SIGNATURE PACKET" }
                                        }
                                        else -> { if (typedPinCode.length < 4) typedPinCode += digit }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = digit, color = if (digit == "ENT") neonGreen else if (digit == "CLR") Color.Red else currentTextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}