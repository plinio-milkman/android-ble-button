package com.android.blebutton

import android.util.Log

interface LogCalls {
    fun printLog(tag: String, msg: String, level: Int = Log.DEBUG) {
        if (DBG) {
            when (level) {
                Log.VERBOSE -> Log.v(tag, msg)
                Log.DEBUG -> Log.d(tag, msg)
                Log.INFO -> Log.i(tag, msg)
                Log.WARN -> Log.w(tag, msg)
                Log.ERROR -> Log.e(tag, msg)
            }

        }
    }

    companion object {
        val DBG
            get() = BuildConfig.DEBUG
    }
}
