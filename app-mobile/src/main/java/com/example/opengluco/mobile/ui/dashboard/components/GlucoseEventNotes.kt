package com.example.opengluco.mobile.ui.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.opengluco.core.model.GlucoseEventMarker
import com.example.opengluco.core.model.GlucoseEventType
import com.example.opengluco.mobile.ui.theme.ClinicalTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GlucoseEventNotesMenuButton(
    markerCount: Int,
    onAddNote: () -> Unit,
    onViewNotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ClinicalTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.EventNote,
                contentDescription = if (markerCount > 0) "Notas y eventos, $markerCount guardadas" else "Notas y eventos",
                tint = if (markerCount > 0) colors.mint else colors.textSecondary,
                modifier = Modifier.size(21.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(264.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("NOTAS DE GLUCOSA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.8.sp)
                    Text(
                        text = if (markerCount == 1) "1 marca guardada" else "$markerCount marcas guardadas",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Añadir nota", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text("Al punto seleccionado", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = colors.mint) },
                    onClick = {
                        expanded = false
                        onAddNote()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = colors.surfaceBorder)
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Ver todas las notas", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                            Text(
                                text = if (markerCount == 0) "Aún no hay notas" else "Buscar y filtrar tus marcas",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = colors.mint) },
                    onClick = {
                        expanded = false
                        onViewNotes()
                    }
                )
            }
        }
    }
}

@Composable
fun GlucoseEventEditorDialog(
    measurementTime: String,
    glucoseValue: String,
    canSave: Boolean,
    selectedType: GlucoseEventType,
    note: String,
    onTypeSelected: (GlucoseEventType) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = ClinicalTheme.colors
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).heightIn(max = 660.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceCard,
            border = BorderStroke(1.dp, colors.surfaceBorder),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DialogHeader(
                    title = "Añadir nota",
                    subtitle = "Anota un evento relacionado con este punto del gráfico.",
                    onClose = onDismiss
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.surfaceBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = colors.mint)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Punto seleccionado", fontSize = 11.sp, color = colors.textMuted)
                            Text(
                                "$measurementTime · $glucoseValue",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tipo de evento", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlucoseEventType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { onTypeSelected(type) },
                                label = { Text(type.label, maxLines = 1) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = eventIcon(type),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { onNoteChanged(it.take(250)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nota opcional") },
                    placeholder = { Text("Añade un detalle que quieras recordar") },
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Puedes dejarla vacía")
                            Text("${note.length}/250")
                        }
                    },
                    maxLines = 4
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("La nota se guarda cifrada en este dispositivo.", fontSize = 10.5.sp, color = colors.textMuted)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.mint,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar nota", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun GlucoseEventListDialog(
    markers: List<GlucoseEventMarker>,
    onAddNote: () -> Unit,
    onDelete: (GlucoseEventMarker) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = ClinicalTheme.colors
    var searchText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<GlucoseEventType?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val filteredMarkers = remember(markers, searchText, selectedType) {
        val query = searchText.trim()
        markers.asSequence()
            .filter { selectedType == null || it.type == selectedType }
            .filter { marker ->
                query.isBlank() || listOf(
                    marker.type.label,
                    marker.note.orEmpty(),
                    dateFormatter.format(Date(marker.timestampMs)),
                    timeFormatter.format(Date(marker.timestampMs))
                ).any { it.contains(query, ignoreCase = true) }
            }
            .sortedByDescending { it.timestampMs }
            .toList()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.96f).heightIn(max = 760.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceCard,
            border = BorderStroke(1.dp, colors.surfaceBorder),
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 700.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DialogHeader(
                    title = "Notas y eventos",
                    subtitle = if (markers.isEmpty()) "Tus anotaciones aparecerán aquí." else "${markers.size} marcas guardadas para este paciente.",
                    onClose = onDismiss
                )

                if (markers.isNotEmpty()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Buscar en notas") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (searchText.isNotEmpty()) {
                            { IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda") } }
                        } else null
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { selectedType = null },
                            label = { Text("Todas (${markers.size})") }
                        )
                        GlucoseEventType.entries.forEach { type ->
                            val count = markers.count { it.type == type }
                            if (count > 0) {
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = if (selectedType == type) null else type },
                                    label = { Text("${type.label} ($count)", maxLines = 1) }
                                )
                            }
                        }
                    }
                }

                if (filteredMarkers.isEmpty()) {
                    EmptyNotesState(
                        hasFilters = markers.isNotEmpty(),
                        onAddNote = onAddNote
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        filteredMarkers.forEach { marker ->
                            EventNoteCard(marker = marker, onDelete = { onDelete(marker) })
                        }
                    }
                    Button(
                        onClick = onAddNote,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.mint,
                            contentColor = if (colors.isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Añadir nota al punto seleccionado")
                    }
                }
            }
        }
    }
}

@Composable
private fun EventNoteCard(marker: GlucoseEventMarker, onDelete: () -> Unit) {
    val colors = ClinicalTheme.colors
    val markerColor = eventColor(marker.type)
    val dateTime = remember(marker.timestampMs) {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(marker.timestampMs))
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(marker.timestampMs))
        "$date · $time"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceOrb),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(1.dp, colors.surfaceBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 11.dp, bottom = 11.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(markerColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(eventIcon(marker.type), contentDescription = null, tint = markerColor, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).padding(top = 1.dp)) {
                Text(marker.type.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                Text(dateTime, fontSize = 10.5.sp, color = colors.textMuted)
                Spacer(Modifier.height(5.dp))
                Text(
                    text = marker.note?.takeIf { it.isNotBlank() } ?: "Sin nota añadida",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (marker.note.isNullOrBlank()) colors.textMuted else colors.textSecondary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar ${marker.type.label.lowercase(Locale.getDefault())}", tint = colors.lowCoral)
            }
        }
    }
}

@Composable
private fun EmptyNotesState(hasFilters: Boolean, onAddNote: () -> Unit) {
    val colors = ClinicalTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(34.dp))
        Text(
            text = if (hasFilters) "No hay notas que coincidan" else "Todavía no hay notas",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
        Text(
            text = if (hasFilters) "Prueba otra búsqueda o categoría." else "Guarda un evento asociado a la lectura seleccionada.",
            fontSize = 11.sp,
            color = colors.textSecondary
        )
        if (!hasFilters) {
            TextButton(onClick = onAddNote) { Text("Añadir la primera nota") }
        }
    }
}

@Composable
private fun DialogHeader(title: String, subtitle: String, onClose: () -> Unit) {
    val colors = ClinicalTheme.colors
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.AutoMirrored.Filled.EventNote, contentDescription = null, tint = colors.mint, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(subtitle, fontSize = 11.sp, lineHeight = 15.sp, color = colors.textSecondary)
        }
        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = colors.textSecondary)
        }
    }
}

private fun eventIcon(type: GlucoseEventType): ImageVector = when (type) {
    GlucoseEventType.MEAL -> Icons.Default.Restaurant
    GlucoseEventType.ACTIVITY -> Icons.AutoMirrored.Filled.DirectionsRun
    GlucoseEventType.ILLNESS -> Icons.Default.MedicalServices
    GlucoseEventType.SENSOR_CHANGE -> Icons.Default.Sensors
}

@Composable
private fun eventColor(type: GlucoseEventType): Color {
    val colors = ClinicalTheme.colors
    return when (type) {
        GlucoseEventType.MEAL -> colors.arcticCyan
        GlucoseEventType.ACTIVITY -> colors.mint
        GlucoseEventType.ILLNESS -> colors.highAmber
        GlucoseEventType.SENSOR_CHANGE -> colors.textSecondary
    }
}
