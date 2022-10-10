package com.veroanggra.experimentalgeofence.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

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
        requestPermissions(activity, needPermission.first(), requestCode, true)
    }

    fun checkPermission(
        fragment: Fragment,
        permissions: Array<String>,
        isCancel: Boolean,
        requestCode: Int,
        showPermissionDialog: ((permission: String, isCancel: Boolean) -> Unit)? = null,
        onGrantDenied: (() -> Unit)? = null,
        onGrantAllowed: (() -> Unit)? = null
    ) {
        permissions.find { !hasPermission(fragment.requireActivity(), it) }?.let {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    fragment.requireActivity(),
                    it
            )) {
                showPermissionDialog?.invoke(it, isCancel)
                onGrantDenied?.invoke()
            } else {
                fragment.requestPermissions(permissions, requestCode)
            }
            return
        } ?: onGrantAllowed?.invoke()
    }

    fun requestPermissions(
        activity: Activity,
        permission: String,
        requestCode: Int,
        isFinishActivityRational: Boolean = false
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            startAppSettingActivity(activity, isFinishActivityRational)
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
        }
    }

    fun startAppSettingActivity(activity: Activity, finishActivityRational: Boolean) {
        try {
            activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                    Uri.parse("package:${activity.packageName}")
                )
            )
            if (finishActivityRational) activity.finish()
        } catch (e: Throwable) {
            activity.startActivity(Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS))
        }
    }

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }


}