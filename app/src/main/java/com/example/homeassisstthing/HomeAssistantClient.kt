package com.example.homeassisstthing

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class HomeAssistantClient(
    private val serverUrl: String,
    val accessToken: String,
    private val onMessageReceived: (String) -> Unit
) {
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
    var onStatusChanged: ((String) -> Unit)? = null

    fun connect() {
        isDisconnectingIntentionally = false
        messageIdCounter.set(1)
        val request = Request.Builder().url(serverUrl).build()

        Log.d("HA_CLIENT", "Attempting connection to $serverUrl")
        onStatusChanged?.invoke("Connecting...")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived(text)

                try {
                    val json = org.json.JSONObject(text)
                    if (json.optString("type") == "event") {
                        val eventData = json.optJSONObject("event")?.optJSONObject("data")
                        val entityId = eventData?.optString("entity_id") ?: ""

                        val scheduleRegex = Regex("^input_text\\.(.+)_schedule(?:_\\d+)?$")
                        val matchResult = scheduleRegex.find(entityId)

                        if (matchResult != null) {
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

                if (text.contains("\"auth_required\"")) {
                    sendAuth()
                }
                if (text.contains("\"auth_ok\"")) {
                    Log.i("HA_CLIENT", "Auth Successful. Subscribing to event matrices...")
                    onStatusChanged?.invoke("Connected")
                    subscribeToEvents(webSocket)
                    requestInitialStates(webSocket)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("HA_CLIENT", "Socket closed clean. Code: $code | Reason: $reason")
                onStatusChanged?.invoke("Disconnected")
                triggerAutoReconnectIfNeeded()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val errorBody = try { response?.body?.string() } catch (e: Exception) { null }
                Log.e("HA_CLIENT", "Connection failure detected: ${t.message}. Body: $errorBody")
                onStatusChanged?.invoke("Failed: ${t.message ?: "Unknown Error"}")
                triggerAutoReconnectIfNeeded()
            }
        })
    }

    private fun triggerAutoReconnectIfNeeded() {
        if (!isDisconnectingIntentionally) {
            Log.w("HA_CLIENT", "Unexpected disconnection. Retrying linkage in 3 seconds...")
            onStatusChanged?.invoke("Reconnecting...")
            Handler(Looper.getMainLooper()).postDelayed({
                connect()
            }, 3000)
        }
    }

    private fun sendAuth() {
        webSocket?.send("""{"type": "auth", "access_token": "$accessToken"}""")
    }

    private fun subscribeToEvents(webSocket: WebSocket) {
        webSocket.send("""
        {
            "id": ${messageIdCounter.getAndIncrement()}, 
            "type": "subscribe_events", 
            "event_type": "state_changed"
        }
        """.trimIndent().replace("\n", ""))

        webSocket.send("""
        {
            "id": ${messageIdCounter.getAndIncrement()}, 
            "type": "subscribe_events", 
            "event_type": "entity_registry_updated"
        }
        """.trimIndent().replace("\n", ""))
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
            triggerAutoReconnectIfNeeded()
        }
    }

    fun setLightBrightness(entityId: String, brightnessPercent: Float) {
        val haBrightness = ((brightnessPercent / 100f) * 255).toInt().coerceIn(0, 255)
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
        """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Sending brightness payload -> $payload")
        val success = webSocket?.send(payload) ?: false
        if (!success) {
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
            triggerAutoReconnectIfNeeded()
        }
    }

    fun startSleepTimer(entityId: String, minutes: Int) {
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

        Log.d("HA_CLIENT", "Sending sleep timer payload -> $payload")
        webSocket?.send(payload)
    }

    fun sendCustomJson(jsonString: String) {
        Log.d("HA_CLIENT", "Sending custom payload -> $jsonString")
        val success = webSocket?.send(jsonString) ?: false
        if (!success) {
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

    suspend fun renameHelperEntity(fullOldEntityId: String, newSlug: String, newDisplayName: String, suffix: String) {
        val domain = if (suffix == "target") "input_number" else "input_text"

        val payload = """
        {
            "id": ${messageIdCounter.getAndIncrement()},
            "type": "config/entity_registry/update",
            "entity_id": "$fullOldEntityId",
            "name": "${newDisplayName} ${suffix.replaceFirstChar { it.uppercase() }}",
            "new_entity_id": "$domain.${newSlug}_$suffix"
        }
        """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry rename: $payload")
        webSocket?.send(payload)
        delay(250)
    }

    fun triggerStateRefresh() {
        webSocket?.let { ws ->
            requestInitialStates(ws)
        }
    }

    suspend fun deleteHelperEntity(exactEntityId: String) {
        val payloadId = messageIdCounter.incrementAndGet()

        val payload = """
        {
            "id": $payloadId,
            "type": "config/entity_registry/remove",
            "entity_id": "$exactEntityId"
        }
        """.trimIndent().replace("\n", "")

        Log.d("HA_CLIENT", "Requesting HA registry removal [MsgID: $payloadId]: $payload")

        val sentSuccessfully = webSocket?.send(payload) ?: false
        if (!sentSuccessfully) {
            Log.e("HA_CLIENT", "Failed to transmit removal payload for $exactEntityId (WebSocket is null or closed)")
        }

        delay(250)
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
            val compactString = slots.joinToString(separator = ";") { slot ->
                val heatingBit = if (slot.isHeatingOn) "1" else "0"
                "${slot.time},${slot.targetTemp},$heatingBit,${slot.dayTarget}"
            }

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

    suspend fun createGenericThermostat(
        roomSlug: String,
        tempSensorEntityId: String,
        switchEntityId: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                val httpBaseUrl = serverUrl
                    .replace("wss://", "https://")
                    .replace("ws://", "http://")
                    .substringBefore("/api")

                val endpoint = "$httpBaseUrl/api/config/climate/config/${roomSlug}_climate"

                val jsonPayload = org.json.JSONObject().apply {
                    put("name", "${roomSlug.replace('_', ' ').uppercase()} HEATING")
                    put("heater", switchEntityId)
                    put("target_sensor", tempSensorEntityId)
                    put("min_temp", 7.0)
                    put("max_temp", 28.0)
                    put("ac_mode", false)
                    put("cold_tolerance", 0.3)
                    put("hot_tolerance", 0.3)
                }

                val body = RequestBody.create(
                    "application/json; charset=utf-8".toMediaType(),
                    jsonPayload.toString()
                )

                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.i("HA_CLIENT", "Successfully provisioned generic thermostat: ${roomSlug}_climate")
                        triggerStateRefresh()
                    } else {
                        Log.e("HA_CLIENT", "Failed to provision thermostat. Code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("HA_CLIENT", "Exception provisioning generic thermostat", e)
            }
        }
    }

    fun callService(domain: String, service: String, entityId: String, serviceData: Map<String, Any> = emptyMap()) {
        val payloadMap = mutableMapOf<String, Any>(
            "id" to messageIdCounter.getAndIncrement(),
            "type" to "call_service",
            "domain" to domain,
            "service" to service,
            "target" to mapOf("entity_id" to entityId)
        )
        if (serviceData.isNotEmpty()) {
            payloadMap["service_data"] = serviceData
        }

        val jsonPayload = org.json.JSONObject(payloadMap).toString()
        webSocket?.send(jsonPayload)
    }

    fun setPresetMode(entityId: String, presetMode: String) {
        val payload = """
    {
        "id": ${messageIdCounter.getAndIncrement()},
        "type": "call_service",
        "domain": "climate",
        "service": "set_preset_mode",
        "service_data": {
            "entity_id": "$entityId",
            "preset_mode": "${presetMode.lowercase()}"
        }
    }
    """.trimIndent().replace("\n", "").replace(" ", "")

        Log.d("HA_CLIENT", "Sending Preset Mode -> $payload")
        webSocket?.send(payload)
    }

    fun disconnect() {
        isDisconnectingIntentionally = true
        webSocket?.close(1000, "User disconnected intentionally")
        webSocket = null
    }
}