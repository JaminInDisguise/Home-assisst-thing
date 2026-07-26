package com.example.homeassisstthing

import java.io.Serializable

data class SmartDevice(
    val entityId: String,
    val friendlyName: String,
    val state: String,
    val domain: String,
    val brightness: Float = 50f,
    val isExpanded: Boolean = false,
    val isColorCapable: Boolean = false,
    val currentTemperature: Float = 0f,
    val targetTemperature: Float = 0f,
    val attributes: Map<String, Any> = emptyMap()
) : Serializable