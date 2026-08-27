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
import com.example.opengluco.wear.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

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
                            .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(12f)).setColor(argb(0xFF81D4FA.toInt())).build())
                            .build()
                    )
                    .addContent(Spacer.Builder().setHeight(dp(4f)).build())
                    .addContent(
                        Row.Builder()
                            .addContent(
                                Text.Builder()
                                    .setText("140")
                                    .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(36f)).setColor(argb(0xFF00E676.toInt())).build())
                                    .build()
                            )
                            .addContent(
                                Text.Builder()
                                    .setText(" →")
                                    .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(24f)).build())
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("mg/dL • En rango")
                            .setFontStyle(LayoutElementBuilders.FontStyle.Builder().setSize(sp(11f)).setColor(argb(0xFFB0BEC5.toInt())).build())
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
