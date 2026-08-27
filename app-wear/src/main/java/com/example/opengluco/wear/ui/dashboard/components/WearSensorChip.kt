package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.model.SensorInfo
import com.example.opengluco.wear.ui.theme.GlucoseInRange
import com.example.opengluco.wear.ui.theme.GlucoseLow
import com.example.opengluco.wear.ui.theme.WearSurface

@Composable
fun WearSensorChip(
    sensor: SensorInfo?,
    modifier: Modifier = Modifier
) {
    if (sensor == null) return

    val daysLeft = sensor.getRemainingDays()
    val text = if (daysLeft != null) {
        if (daysLeft > 0) "Sensor: $daysLeft d" else "Sensor expirado"
    } else {
        "Sensor: ${sensor.serialNumber ?: "OK"}"
    }

    val iconColor = if ((daysLeft ?: 14) <= 2) GlucoseLow else GlucoseInRange

    Button(
        onClick = { /* Detalle del sensor */ },
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sensors,
            contentDescription = "Sensor",
            tint = iconColor,
            modifier = Modifier.size(16.dp).padding(end = 4.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
