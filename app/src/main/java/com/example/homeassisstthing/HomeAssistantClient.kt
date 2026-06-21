package com.example.homeassisstthing

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class HomeAssistantClient(
    private val serverUrl: String,
    private val accessToken: String,
    private val onMessageReceived: (String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val messageIdCounter = AtomicInteger(1)
    private var isDisconnectingIntentionally = false

    fun connect() {
        isDisconnectingIntentionally = false
        messageIdCounter.set(1)
        val request = Request.Builder().url(serverUrl).build()

        Log.d("HA_CLIENT", "Attempting connection to $serverUrl")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Pass raw text immediately downwards without freezing the caller layer
                onMessageReceived(text)

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

    fun disconnect() {
        isDisconnectingIntentionally = true
        webSocket?.close(1000, "User disconnected intentionally")
        webSocket = null
    }
}