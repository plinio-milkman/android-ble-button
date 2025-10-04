package com.android.blebutton

import android.app.Application
import androidx.preference.PreferenceManager
class MyApplication : Application(), LogCalls {

    override fun onCreate() {
        super.onCreate()

        printLog(TAG, "Initialize preferences")
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
    }

    companion object {
        const val TAG = "MyApplication"
    }
}
