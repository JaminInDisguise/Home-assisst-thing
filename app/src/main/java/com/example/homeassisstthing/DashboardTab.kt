package com.example.homeassisstthing


import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun DashboardView(
    textMuted: Color,
    currentTextColor: Color,
    currentCardColor: Color,
    neonCyan: Color,
    neonGreen: Color,
    macro1Name: String,
    macro1Entities: List<String>, // Adjust type to match your entity objects
    macro2Name: String,
    macro2Entities: List<String>,
    homeEnergyUsage: String,
    totalActiveLights: Int,
    connectionStatus: String,
    currentMetricIndex: Int,
    onMacro1Click: () -> Unit,
    onMacro2Click: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QUICK ACTIONS",
            color = textMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        // --- MACRO EXECUTION LOOP PANEL ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Macro 1 Trigger Command
            Button(
                onClick = onMacro1Click,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonCyan.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.4f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = macro1Name.uppercase(),
                        color = neonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${macro1Entities.size} DEVICES",
                        color = textMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Macro 2 Trigger Command
            Button(
                onClick = onMacro2Click,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = neonGreen.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, neonGreen.copy(alpha = 0.4f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = macro2Name.uppercase(),
                        color = neonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${macro2Entities.size} DEVICES",
                        color = textMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // --- CYCLING TELEMETRY CAROUSEL CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = currentCardColor),
            border = BorderStroke(1.dp, neonCyan.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Crossfade(
                    targetState = currentMetricIndex,
                    animationSpec = tween(600),
                    label = "TelemetryStreamCrossfade"
                ) { index ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        when (index) {
                            0 -> {
                                Column {
                                    Text(
                                        text = "HOUSE METRICS // SLOT 01",
                                        color = neonCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "CURRENT GRID LOAD",
                                        color = textMuted,
                                        fontSize = 14.sp
                                    )
                                    Text(
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
                                        text = "HOUSE METRICS // SLOT 02",
                                        color = neonCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "ACTIVE LIGHT SYSTEMS",
                                        color = textMuted,
                                        fontSize = 14.sp
                                    )
                                    Text(
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
                                        text = "HOUSE METRICS // SLOT 03",
                                        color = neonCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "SERVER TELEMETRY LINK",
                                        color = textMuted,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = connectionStatus,
                                        color = neonGreen,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        // Carousel Pager Dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            repeat(3) { dotIndex ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = if (currentMetricIndex == dotIndex) neonCyan else textMuted.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(50.dp)
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