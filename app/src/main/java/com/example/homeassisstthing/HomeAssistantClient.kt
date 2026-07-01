package com.example.homeassisstthing

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class HomeAssistantClient(
    private val serverUrl: String,
    val accessToken: String,
    private val onMessageReceived: (String) -> Unit
) {
    // This extracts just the IP/Host and Port from your WebSocket string
    val httpHostAddress: String by lazy {
        serverUrl
            .replace("wss://", "")
            .replace("ws://", "")
            .substringBefore("/api")
    }

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val messageIdCounter = AtomicInteger(1)
    private var isDisconnectingIntentionally = false


    var onScheduleUpdated: ((slug: String, rawStateString: String?) -> Unit)? = null

    fun connect() {
        isDisconnectingIntentionally = false
        messageIdCounter.set(1)
        val request = Request.Builder().url(serverUrl).build()

        Log.d("HA_CLIENT", "Attempting connection to $serverUrl")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Pass raw text immediately downwards without freezing the caller layer
                onMessageReceived(text)

                // === ADD THE LIVE MATRIX SYNC INTERCEPTOR HERE ===
                try {
                    val json = org.json.JSONObject(text)
                    if (json.optString("type") == "event") {
                        val eventData = json.optJSONObject("event")?.optJSONObject("data")
                        val entityId = eventData?.optString("entity_id") ?: ""

                        // Intercept schedule mutations streaming from Home Assistant
                        if (entityId.startsWith("input_text.") && entityId.endsWith("_schedule")) {
                            val newStateObj = eventData?.optJSONObject("new_state")
                            val rawMatrixString = newStateObj?.optString("state") ?: ""
                            val extractedSlug = entityId.removePrefix("input_text.").removeSuffix("_schedule")

                            if (rawMatrixString.isNotEmpty() && rawMatrixString != "unknown" && rawMatrixString != "unavailable") {

                                onScheduleUpdated?.invoke(extractedSlug, rawMatrixString)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HA_CLIENT", "Error processing live multi-device schedule sync", e)
                }

                // Route authentication lifecycle states cleanly
                if (text.contains("\"auth_required\"")) {
                    sendAuth()
                }
                if (text.contains("\"auth_ok\"")) {
                    Log.i("HA_CLIENT", "Auth Successful. Subscribing to event matrices...")
                    subscribeToEvents(webSocket)
                    requestInitialStates(webSocket)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("HA_CLIENT", "Socket closed clean. Code: $code | Reason: $reason")
                triggerAutoReconnectIfNeeded()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val errorBody = try { response?.body?.string() } catch(e: Exception) { null }
                Log.e("HA_CLIENT", "Connection failure detected: ${t.message}. Body: $errorBody")
                triggerAutoReconnectIfNeeded()
            }
        })
    }

    private fun triggerAutoReconnectIfNeeded() {
        if (!isDisconnectingIntentionally) {
            Log.w("HA_CLIENT", "Unexpected disconnection. Retrying linkage in 3 seconds...")
            Handler(Looper.getMainLooper()).postDelayed({
                connect()
            }, 3000)
        }
    }

    private fun sendAuth() {
        webSocket?.send("""{"type": "auth", "access_token": "$accessToken"}""")
    }

    private fun subscribeToEvents(webSocket: WebSocket) {
        webSocket.send("""{"id": ${messageIdCounter.getAndIncrement()}, "type": "subscribe_events", "event_type": "state_changed"}""")
    }

    private fun requestInitialStates(webSocket: WebSocket) {
        webSocket.send("""{"id": ${messageIdCounter.getAndIncrement()}, "type": "get_states"}""")
    }

    fun toggleLight(entityId: String, turnOn: Boolean) {
        val service = if (turnOn) "turn_on" else "turn_off"
        val payload = """{"id": ${messageIdCounter.getAndIncrement()}, "type": "call_service", "domain": "light", "service": "$service", "service_data": {"entity_id": "$entityId"}}"""

        Log.d("HA_CLIENT", "Sending payload -> $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            Log.e("HA_CLIENT", "Failed to send packet - socket may be dead. Triggering re-verification...")
            triggerAutoReconnectIfNeeded()
        }
    }

    fun setLightBrightness(entityId: String, brightnessPercent: Float) {
        // Convert 1-100% slider value to Home Assistant's native 0-255 range
        val haBrightness = ((brightnessPercent / 100f) * 255).toInt().coerceIn(0, 255)

        // This builds the payload safely using your atomic message ID counter
        val payload = """
            {
                "id": ${messageIdCounter.getAndIncrement()},
                "type": "call_service",
                "domain": "light",
                "service": "turn_on",
                "service_data": {
                    "entity_id": "$entityId",
                    "brightness": $haBrightness
                }
            }
        """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending brightness payload -> $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            Log.e("HA_CLIENT", "Failed to send brightness packet - socket may be dead.")
            triggerAutoReconnectIfNeeded()
        }
    }

    fun setLightColorTemp(entityId: String, mireds: Int) {
        val payload = """
            {
                "id": ${messageIdCounter.getAndIncrement()},
                "type": "call_service",
                "domain": "light",
                "service": "turn_on",
                "service_data": {
                    "entity_id": "$entityId",
                    "color_temp": $mireds
                }
            }
        """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending color temp payload -> $payload")
        webSocket?.send(payload)
    }

    fun setLightRgbColor(entityId: String, r: Int, g: Int, b: Int) {
        val payload = """
            {
                "id": ${messageIdCounter.getAndIncrement()},
                "type": "call_service",
                "domain": "light",
                "service": "turn_on",
                "service_data": {
                    "entity_id": "$entityId",
                    "rgb_color": [$r, $g, $b]
                }
            }
        """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending RGB payload -> $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            Log.e("HA_CLIENT", "Failed to send RGB packet - socket may be dead.")
            triggerAutoReconnectIfNeeded()
        }
    }

    fun startSleepTimer(entityId: String, minutes: Int) {
        // This utilizes Home Assistant's built-in script engine to fire a delayed shutdown service call
        val payload = """
            {
                "id": ${messageIdCounter.getAndIncrement()},
                "type": "call_service",
                "domain": "script",
                "service": "turn_on",
                "service_data": {
                    "variables": {
                        "target_entity": "$entityId",
                        "delay_minutes": $minutes
                    }
                }
            }
        """.trimIndent().replace("\n", "").replace(" ", "")

        // Note: For an immediate local solution without a pre-configured script on HA,
        // you can also trigger a standard 'homeassistant.turn_off' command via an app-side coroutine timer!
        Log.d("HA_CLIENT", "Sending sleep timer payload -> $payload")
        webSocket?.send(payload)
    }

    fun sendCustomJson(jsonString: String) {
        Log.d("HA_CLIENT", "Sending custom payload -> $jsonString")
        val success = webSocket?.send(jsonString) ?: false
        if (!success) {
            Log.e("HA_CLIENT", "Failed to send custom payload - socket may be dead.")
            triggerAutoReconnectIfNeeded()
        }
    }

    fun setClimateTemperature(entityId: String, targetTemp: Float) {
        val payload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "call_service",
            "domain": "climate",
            "service": "set_temperature",
            "service_data": {
                "entity_id": "$entityId",
                "temperature": $targetTemp
            }
        }
    """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending Target Temp -> $payload")
        webSocket?.send(payload)
    }

    fun setInputNumberHelperValue(entityId: String, value: Float) {
        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "call_service",
        "domain": "input_number",
        "service": "set_value",
        "service_data": {
            "entity_id": "$entityId",
            "value": $value
        }
    }
    """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending Helper Target Temp -> $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            triggerAutoReconnectIfNeeded()
        }
    }

    fun renameHelperEntity(oldSlug: String, newSlug: String, newDisplayName: String, isNumberHelper: Boolean) {
        val domain = if (isNumberHelper) "input_number" else "input_text"
        val suffix = if (isNumberHelper) "target" else "schedule"

        // This payload tells HA's entity registry to change both the visible name AND the entity_id
        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "config/entity_registry/update",
        "entity_id": "$domain.${oldSlug}_$suffix",
        "name": "${newDisplayName} ${if(isNumberHelper) "Target" else "Schedule"}",
        "new_entity_id": "$domain.${newSlug}_$suffix"
    }
    """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry rename: $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            triggerAutoReconnectIfNeeded()
        }
    }

    fun deleteHelperEntity(slug: String, isNumberHelper: Boolean) {
        val domain = if (isNumberHelper) "input_number" else "input_text"
        val suffix = if (isNumberHelper) "target" else "schedule"

        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "config/entity_registry/remove",
        "entity_id": "$domain.${slug}_$suffix"
    }
    """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry removal: $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
            triggerAutoReconnectIfNeeded()
        }
    }

    fun setClimateHvacMode(entityId: String, hvacMode: String) {
        val payload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "call_service",
            "domain": "climate",
            "service": "set_hvac_mode",
            "service_data": {
                "entity_id": "$entityId",
                "hvac_mode": "${hvacMode.lowercase()}"
            }
        }
    """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending HVAC Mode -> $payload")
        webSocket?.send(payload)
    }

    fun updateRoomScheduleMatrix(entityId: String, slots: List<ClimateScheduleSlot>, isEngineEnabled: Boolean) {
        try {
            // 1. Build an ultra-compact lightweight string format: "time,temp,heating,day;time,temp,heating,day"
            // We drop the long 36-character ID completely because the app generates clean IDs on load!
            val compactString = slots.joinToString(separator = ";") { slot ->
                val heatingBit = if (slot.isHeatingOn) "1" else "0"
                "${slot.time},${slot.targetTemp},$heatingBit,${slot.dayTarget}"
            }

            // 2. Construct the native Home Assistant WebSocket command structure with our tiny string
            val wsPayload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "call_service",
            "domain": "input_text",
            "service": "set_value",
            "service_data": {
                "entity_id": "$entityId",
                "value": ${org.json.JSONObject.quote(compactString)}
            }
        }
        """.trimIndent().replace("\n", "")

            Log.d("HA_CLIENT", "Sending Compact Schedule Matrix -> $wsPayload")

            // 3. Fire it off safely over the socket connection
            val success = webSocket?.send(wsPayload) ?: false
            if (!success) {
                Log.e("HA_CLIENT", "Failed to send schedule packet - socket dead.")
                triggerAutoReconnectIfNeeded()
            }
        } catch (e: Exception) {
            Log.e("HA_CLIENT", "Error generating compact schedule payload", e)
        }
    }

    fun createHelperEntities(zoneName: String) {
        try {
            val cleanSlug = zoneName.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }

            // 1. WebSocket payload for the input_number helper
            val numberPayload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "input_number/create",
            "name": "$zoneName Target",
            "min": 5.0,
            "max": 30.0,
            "step": 0.5,
            "mode": "box",
            "unit_of_measurement": "°C",
            "icon": "mdi:thermometer"
        }
        """.trimIndent().replace("\n", "")

            // 2. WebSocket payload for the input_text helper
            val textPayload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "input_text/create",
            "name": "$zoneName Schedule",
            "min": 0,
            "max": 255,
            "mode": "text",
            "icon": "mdi:calendar-clock"
        }
        """.trimIndent().replace("\n", "")

            // Send them both instantly over the active WebSocket channel
            webSocket?.send(numberPayload)
            webSocket?.send(textPayload)
            Log.d("HA_CLIENT", "Sent helper creation WebSocket requests for $zoneName")
        } catch (e: Exception) {
            Log.e("HA_CLIENT", "Error sending helper creation payloads", e)
        }
    }


    fun parseMatrixStringToSlots(rawString: String): List<ClimateScheduleSlot>? {
        val cleanString = rawString.trim()

        // 1. GUARD: If it's uninitialized on HA, return null (NOT an empty list)
        if (cleanString.isBlank() ||
            cleanString.equals("unknown", ignoreCase = true) ||
            cleanString.equals("unavailable", ignoreCase = true)) {
            return null
        }

        val slotsList = mutableListOf<ClimateScheduleSlot>()
        // 2. Explicit Empty: If the user deliberately cleared the schedule, return a valid empty list
        if (cleanString == "[]") return slotsList

        try {
            val cleanInput = cleanString.removePrefix("[").removeSuffix("]")
            val segments = cleanInput.split("],[", "], [")

            for (segment in segments) {
                val parts = segment.replace("[", "").replace("]", "").split("|")
                if (parts.size >= 4) {
                    slotsList.add(
                        ClimateScheduleSlot(
                            id = java.util.UUID.randomUUID().toString(),
                            time = parts[0].trim(),
                            dayTarget = parts[1].trim(),
                            isHeatingOn = parts[2].trim().equals("ON", ignoreCase = true),
                            targetTemp = parts[3].trim().toFloatOrNull() ?: 20.0f
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("DECODER", "Error rebuilding matrix data payload", e)
        }
        return slotsList
    }

    fun disconnect() {
        isDisconnectingIntentionally = true
        webSocket?.close(1000, "User disconnected intentionally")
        webSocket = null
    }
}