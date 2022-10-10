package com.veroanggra.experimentalgeofence.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun hasPermission(context: Context, permission: MutableList<String>): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission[0]
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    permission[1]
                ) == PackageManager.PERMISSION_GRANTED

    fun getDeniedPermission(context: Context, permission: MutableList<String>): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission[0]
        ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    permission[1]
                ) != PackageManager.PERMISSION_GRANTED

}