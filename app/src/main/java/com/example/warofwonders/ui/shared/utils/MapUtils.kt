package com.example.warofwonders.ui.shared.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.location.Location
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.BitmapDescriptor

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, maxDp: Float): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val maxPx = (maxDp * density).toInt()

    val vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId)
        ?: return com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker()

    var intrinsicWidth = vectorDrawable.intrinsicWidth
    var intrinsicHeight = vectorDrawable.intrinsicHeight
    if (intrinsicWidth <= 0) intrinsicWidth = (maxOf(1f, context.resources.displayMetrics.density * 24f)).toInt()
    if (intrinsicHeight <= 0) intrinsicHeight = (maxOf(1f, context.resources.displayMetrics.density * 24f)).toInt()
    val scale = minOf(maxPx / intrinsicWidth.toFloat(), maxPx / intrinsicHeight.toFloat())

    val width = (intrinsicWidth * scale).toInt()
    val height = (intrinsicHeight * scale).toInt()

    vectorDrawable.setBounds(0, 0, width, height)
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

fun distanceBetween(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Double {
    val results = FloatArray(1)
    Location.distanceBetween(
        latitude1,
        longitude1,
        latitude2,
        longitude2,
        results
    )
    return results[0].toDouble()
}
