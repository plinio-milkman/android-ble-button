package com.android.blebutton

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LambdaBroadcastReceiver(val receive: (context: Context?, intent: Intent?) -> Unit) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) = receive(context, intent)

}
