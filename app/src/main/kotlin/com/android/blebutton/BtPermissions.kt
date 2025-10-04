package com.android.blebutton

interface BtPermissions {
    companion object {
        const val BLUETOOTH_COARSE_LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"
        const val BLUETOOTH_FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
        const val BLUETOOTH_SCAN_PERMISSION = "android.permission.BLUETOOTH_SCAN"
        const val BLUETOOTH_CONNECT_PERMISSION = "android.permission.BLUETOOTH_CONNECT"
        const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }
}