package com.example.opengluco.wear.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.Layout
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.TimelineBuilders.TimelineEntry
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.opengluco.core.data.GlucoseUnit
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.wear.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GlucoseTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val deviceParams = requestParams.deviceConfiguration
        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion("1")
            .setTileTimeline(
                Timeline.Builder()
                    .addTimelineEntry(
                        TimelineEntry.Builder()
                            .setLayout(
                                Layout.Builder()
                                    .setRoot(buildTileLayout(this, deviceParams))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        val resources = ResourceBuilders.Resources.Builder().setVersion("1").build()
        return Futures.immediateFuture(resources)
    }

    private fun buildTileLayout(context: Context, deviceParams: DeviceParameters): LayoutElementBuilders.LayoutElement {
        val prefs = UserPreferencesRepository(context)
        val (settings, history) = runBlocking(Dispatchers.IO) {
            try {
                val s = prefs.userSettingsFlow.first()
                val h = prefs.getHistoricalReadingsList(1, patientId = s.selectedPatientId)
                s to h
            } catch (_: Exception) {
                UserSettings() to emptyList()
            }
        }
        val last = history.lastOrNull()

        val isMmol = settings.unit == GlucoseUnit.MMOL
        val displayVal = last?.getFormattedValue(isMmol) ?: "--"
        val trendSymbol = last?.trendSymbol ?: "→"
        val mgdl = last?.numericValue ?: 0.0

        val (statusText, statusColorArgb) = when {
            mgdl <= 55 -> "Urgente bajo" to 0xFFEF4444.toInt()
            mgdl < settings.lowThreshold -> "Bajo" to 0xFFF87171.toInt()
            mgdl > 250 -> "Muy alto" to 0xFFFB923C.toInt()
            mgdl > settings.highThreshold -> "Alto" to 0xFFFBBF24.toInt()
            mgdl > 0 -> "En rango" to 0xFF4ADE80.toInt()
            else -> "Sin datos" to 0xFF94A3B8.toInt()
        }

        val clickAction = Clickable.Builder()
            .setOnClick(
                ActionBuilders.LaunchAction.Builder()
                    .setAndroidActivity(
                        ActionBuilders.AndroidActivity.Builder()
                            .setPackageName(context.packageName)
                            .setClassName(MainActivity::class.java.name)
                            .build()
                    )
                    .build()
            )
            .build()

        return Box.Builder()
            .setWidth(expand())
            .setHeight(expand())
            .setModifiers(Modifiers.Builder().setClickable(clickAction).build())
            .addContent(
                Column.Builder()
                    .addContent(
                        Text.Builder()
                            .setText("OpenGluco")
                            .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(12f)).setColor(argb(0xFF38BDF8.toInt())).build())
                            .build()
                    )
                    .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                    .addContent(
                        Row.Builder()
                            .addContent(
                                Text.Builder()
                                    .setText(displayVal)
                                    .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(34f)).setColor(argb(statusColorArgb)).build())
                                    .build()
                            )
                            .addContent(
                                Text.Builder()
                                    .setText(" $trendSymbol")
                                    .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(22f)).setColor(argb(statusColorArgb)).build())
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("${settings.unit.label} • $statusText")
                            .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(11f)).setColor(argb(0xFF94A3B8.toInt())).build())
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
