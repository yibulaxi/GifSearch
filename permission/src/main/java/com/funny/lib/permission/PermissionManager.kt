package com.funny.lib.permission

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import com.funny.lib.common.app.App
import com.yanzhenjie.permission.AndPermission
import java.util.*

object PermissionManager {
    fun request(listener: PermissionListener, vararg permissions: String) {
        AndPermission.with(App.context)
            .runtime()
            .permission(permissions)
            .onGranted {
                listener.onGranted(it)
            }
            .onDenied {
                if (AndPermission.hasAlwaysDeniedPermission(App.context, it)) {
                    listener.alwaysDenied(it)
                } else {
                    listener.onDenied(it)
                }
            }
            .start()
    }

//    fun request(
//        activity: Activity?,
//        listener: PermissionListener,
//        vararg permissions: String
//    ) {
//        FastPermission.request(activity, listener, *permissions)
//    }
//
//    fun request(fragment: Fragment, listener: PermissionListener, vararg permissions: String) {
//        FastPermission.request(fragment, listener, *permissions)
//    }

    fun hasPermissions(vararg permissions: String): Boolean {
        return hasPermission(App.context, *permissions)
    }

    fun alwaysDenyPermissions(activity: Activity, vararg permissions: String): Boolean =
        AndPermission.hasAlwaysDeniedPermission(activity, *permissions)

//    fun jumpPermissionSetting(
//        activity: Activity?,
//        requestCode: Int,
//        cancelListener: DialogInterface.OnClickListener
//    ) {
//        FastPermission.openPermissionManually(activity, requestCode, cancelListener)
//    }

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        return hasPermission(context, Arrays.asList(*permissions))
    }

    fun hasPermission(context: Context, permissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        for (permission in permissions) {
            var result = ContextCompat.checkSelfPermission(context, permission)
            if (result == PackageManager.PERMISSION_DENIED) {
                return false
            }

            val op = AppOpsManagerCompat.permissionToOp(permission)
            if (TextUtils.isEmpty(op)) {
                continue
            }
            result = AppOpsManagerCompat.noteProxyOp(context, op!!, context.packageName)
            if (result != AppOpsManagerCompat.MODE_ALLOWED) {
                return false
            }
        }
        return true
    }

    fun hasAlwaysDeniedPermission(activity: Activity, vararg deniedPermissions: String): Boolean {
        return hasAlwaysDeniedPermission(activity, Arrays.asList(*deniedPermissions))
    }

    fun hasAlwaysDeniedPermission(activity: Activity, deniedPermissions: List<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }

        if (deniedPermissions.size == 0) {
            return false
        }

        for (permission in deniedPermissions) {
            val rationale = activity.shouldShowRequestPermissionRationale(permission)
            if (!rationale) {
                return true
            }
        }
        return false
    }

    fun hasAlwaysDeniedPermission(activity: Activity, deniedPermissions: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        val rationale = activity.shouldShowRequestPermissionRationale(deniedPermissions)
        return !rationale
    }

    fun openPermissionManually(activity: Activity, requestCode: Int, cancelListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.permission_permission_need_some_permission)
        builder.setTitle(R.string.permission_permission_warm_tips)
        builder.setCancelable(false)
        builder.setPositiveButton(R.string.permission_permission_go) { dialog, which ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, requestCode)
        }
        builder.setNegativeButton(R.string.permission_permission_cancel, cancelListener)
        builder.show()
    }

    fun requestPermission(activity: Activity, permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
    }

}