package com.example.opengluco.wear.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.Text
import com.example.opengluco.core.model.ConnectionItem
import com.example.opengluco.wear.ui.theme.ClinicalArcticCyan
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder
import com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard
import com.example.opengluco.wear.ui.theme.ClinicalTextPrimary

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ClinicalSurfaceCard)
            .border(1.dp, ClinicalSurfaceBorder, RoundedCornerShape(14.dp))
            .clickable { onSwitchPatient() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Paciente",
                tint = ClinicalArcticCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = labelText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClinicalTextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
