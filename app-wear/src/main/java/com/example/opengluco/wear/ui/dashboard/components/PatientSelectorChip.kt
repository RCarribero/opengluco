package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.wear.ui.theme.WearPrimary
import com.example.opengluco.wear.ui.theme.WearSurface

@Composable
fun PatientSelectorChip(
    selectedPatient: ConnectionItem?,
    totalPatients: Int,
    onSwitchPatient: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedPatient == null) return

    val labelText = if (totalPatients > 1) {
        "${selectedPatient.fullName} ($totalPatients)"
    } else {
        selectedPatient.fullName
    }

    Button(
        onClick = onSwitchPatient,
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Paciente",
            tint = WearPrimary,
            modifier = Modifier.size(16.dp).padding(end = 4.dp)
        )
        Text(
            text = labelText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
