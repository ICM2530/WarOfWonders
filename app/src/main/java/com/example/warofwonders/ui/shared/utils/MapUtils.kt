package com.example.warofwonders.ui.shared.utils

import android.content.Context
import android.graphics.Bitmap.createBitmap
import com.google.android.gms.maps.model.BitmapDescriptor

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId)!!
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}
