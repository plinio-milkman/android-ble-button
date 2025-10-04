package com.android.blebutton

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder

class BleServiceConnection : ServiceConnection, LogCalls {

    class BleServiceBinder(val service: BleService) : Binder()

    private var _service: BleService? = null

    val service: BleService?
        get() = _service

    override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        printLog(TAG, "onServiceConnected()")
        if (binder is BleServiceBinder) {
            printLog(TAG, "is BleServiceBinder")
            _service = binder.service
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        printLog(TAG, "onServiceDisconnected()")
        _service = null
    }

    companion object {
        const val TAG = "BleServiceConnection"
    }
}
