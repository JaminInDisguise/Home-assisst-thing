package com.example.homeassisstthing



import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        modifier = Modifier
            .fillMaxSize()
            .background(currentBgColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "CENTRAL PERIMETER DEFENSE MATRIX (MOCK)",
            color = if (mockArmedState) Color.Red else Color(0xFFFFB300),
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(if (mockArmedState) Color.Red.copy(alpha = 0.08f) else Color(0xFFFFB300).copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                .border(1.dp, if (mockArmedState) Color.Red.copy(alpha = 0.4f) else Color(0xFFFFB300).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (mockArmedState) "PERIMETER INFRASTRUCTURE: ARMED // SECURE" else "PERIMETER INFRASTRUCTURE: DISARMED // VULNERABLE",
                color = if (mockArmedState) Color.Red else Color(0xFFFFB300),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(vertical = 12.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = securityDisplayMessage, color = textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Text(
                text = if (typedPinCode.isEmpty()) "----" else "• ".repeat(typedPinCode.length),
                color = if (mockArmedState) Color.Red else neonCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        val padDigits = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("CLR", "0", "ENT")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            padDigits.forEach { rowKeylist ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowKeylist.forEach { digit ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(6.dp))
                                .border(1.dp, currentTextColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .clickable {
                                    triggerInterfaceFeedback()
                                    when (digit) {
                                        "CLR" -> {
                                            typedPinCode = ""
                                            securityDisplayMessage = "ENTER AUTHORIZATION PIN"
                                        }
                                        "ENT" -> {
                                            if (typedPinCode == "1234") {
                                                mockArmedState = !mockArmedState
                                                typedPinCode = ""
                                                securityDisplayMessage = "ACCESS GRANTED"
                                            } else {
                                                typedPinCode = ""
                                                securityDisplayMessage = "INVALID SIGNATURE PACKET"
                                            }
                                        }
                                        else -> {
                                            if (typedPinCode.length < 4) typedPinCode += digit
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = digit,
                                color = if (digit == "ENT") neonGreen else if (digit == "CLR") Color.Red else currentTextColor,
                                fontSize = 14.sp,
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