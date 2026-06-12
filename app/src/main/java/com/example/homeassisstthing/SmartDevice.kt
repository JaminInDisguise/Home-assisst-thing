package com.example.homeassisstthing

import java.io.Serializable

// Using Serializable bypasses the need for the Gradle plugin entirely
data class SmartDevice(
    val entityId: String,
    val friendlyName: String,
    val state: String,
    val domain: String,
    val brightness: Float = 50f,
    val isExpanded: Boolean = false
) : Serializable