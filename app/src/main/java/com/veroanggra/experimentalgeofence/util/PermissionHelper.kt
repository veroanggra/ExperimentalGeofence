package com.veroanggra.experimentalgeofence.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.veroanggra.experimentalgeofence.MainActivity

object PermissionHelper {
    @SuppressLint("ObsoleteSdkInt")
    fun getNeedGrantPermissions(permissions: Array<String>, context: Context): List<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()) {
            return emptyList()
        }

        val results = ArrayList<String>()
        for (permission in permissions) {
            if (!hasPermission(context, permission)) {
                results.add(permission)
            }
        }
        return results
    }

    fun checkPermission(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int,
        grantBlock: (() -> Unit)? = null
    ) {
        val needPermission = getNeedGrantPermissions(permissions, activity)
        if (needPermission.isEmpty()) {
            grantBlock?.invoke()
            return
        }
    }

    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}