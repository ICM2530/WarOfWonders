package com.example.warofwonders.ui.shared.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun shouldShowPermissionRationale(context: Context, permission: String): Boolean {
    val activity = context as? Activity ?: return false
    return shouldShowRequestPermissionRationale(activity, permission)
}