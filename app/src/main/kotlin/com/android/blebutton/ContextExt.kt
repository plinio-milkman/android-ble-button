package com.android.blebutton

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker

fun Context.checkPermission(
    permission: String,
    requestCode: Int? = null,
    granted: () -> Unit = {},
    denied: () -> Unit = {
        if (requestCode != null) {
            let{this as? AppCompatActivity}?.requestPermissions(arrayOf(permission), requestCode)
        }
    }
) {
    when (PermissionChecker.checkSelfPermission(this, permission)) {
        PermissionChecker.PERMISSION_GRANTED -> {
            granted()
        }

        else -> {
            denied()
        }
    }
}
