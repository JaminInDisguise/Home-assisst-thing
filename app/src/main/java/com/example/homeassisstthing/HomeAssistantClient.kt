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

                        // Matches 'input_text.' followed by anything, then '_schedule', optionally followed by '_number' at the end
                        // Examples: input_text.kitchen_schedule, input_text.living_room_schedule_2
                        val scheduleRegex = Regex("^input_text\\.(.+)_schedule(?:_\\d+)?$")
                        val matchResult = scheduleRegex.find(entityId)

                        if (matchResult != null) {
                            // Group 1 extracts just the room name slug (e.g., "kitchen" or "living_room")
                            val extractedSlug = matchResult.groupValues[1]

                            val newStateObj = eventData?.optJSONObject("new_state")
                            val rawMatrixString = newStateObj?.optString("state") ?: ""

                            if (rawMatrixString.isNotEmpty() && rawMatrixString != "unknown" && rawMatrixString != "unavailable") {
                                Log.d("HA_DISCOVERY", "Live sync caught schedule for slug: $extractedSlug (Full ID: $entityId)")
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

    fun renameHelperEntity(oldSlug: String, newSlug: String, newDisplayName: String, suffix: String) {
        val domain = if (suffix == "target") "input_number" else "input_text"

        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "config/entity_registry/update",
        "entity_id": "$domain.${oldSlug}_$suffix",
        "name": "${newDisplayName} ${suffix.replaceFirstChar { it.uppercase() }}",
        "new_entity_id": "$domain.${newSlug}_$suffix"
    }
    """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry rename: $payload")
        webSocket?.send(payload)
        
        // Force refresh
        Handler(Looper.getMainLooper()).postDelayed({
            webSocket?.let { requestInitialStates(it) }
        }, 1000)
    }

    fun deleteHelperEntity(slug: String, suffix: String) {
        val domain = if (suffix == "target") "input_number" else "input_text"

        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "config/entity_registry/remove",
        "entity_id": "$domain.${slug}_$suffix"
    }
    """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry removal: $payload")
        webSocket?.send(payload)
        
        // Force refresh
        Handler(Looper.getMainLooper()).postDelayed({
            webSocket?.let { requestInitialStates(it) }
        }, 1000)
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
            // 1. WebSocket payload for the input_number helper (Target Temp)
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

            // 2. WebSocket payload for the input_text helper (Schedule)
            val schedulePayload = """
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

            // 3. WebSocket payload for the input_text helper (Sensors Mapping)
            val sensorsPayload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "input_text/create",
            "name": "$zoneName Sensors",
            "min": 0,
            "max": 255,
            "mode": "text",
            "icon": "mdi:leak"
        }
        """.trimIndent().replace("\n", "")

            webSocket?.send(numberPayload)
            webSocket?.send(schedulePayload)
            webSocket?.send(sensorsPayload)
            
            // 4. Force a state refresh so the new entities appear in deviceList immediately
            Handler(Looper.getMainLooper()).postDelayed({
                webSocket?.let { requestInitialStates(it) }
            }, 1500)

            Log.d("HA_CLIENT", "Sent creation requests for $zoneName helpers")
        } catch (e: Exception) {
            Log.e("HA_CLIENT", "Error sending helper creation payloads", e)
        }
    }

    fun updateRoomSensors(roomSlug: String, tempId: String, humId: String) {
        val payload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "call_service",
            "domain": "input_text",
            "service": "set_value",
            "service_data": {
                "entity_id": "input_text.${roomSlug}_sensors",
                "value": "$tempId|$humId"
            }
        }
        """.trimIndent().replace("\n", "")
        webSocket?.send(payload)
    }

    fun disconnect() {
        isDisconnectingIntentionally = true
        webSocket?.close(1000, "User disconnected intentionally")
        webSocket = null
    }
}
