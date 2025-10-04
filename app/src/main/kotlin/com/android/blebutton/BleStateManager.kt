package com.android.blebutton

import android.content.BroadcastReceiver
import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class BleStateManager(context: Context, private val bleDeviceAddress: String) : LogCalls {

    private val broadcastManager = LocalBroadcastManager.getInstance(context.applicationContext)
    private val broadcastReceivers = arrayListOf<BroadcastReceiver>()

    private var _currentState: BleState = BleState.INIT
    val currentState: BleState
        get() = _currentState

    fun receiveBleEvent(event: BleEvent) {
        val intent = BleEvent.intent(bleDeviceAddress, currentState, event)
        printLog(TAG, "Received event $event and broadcasting ${intent.action}")
        broadcastManager.sendBroadcast(intent)
    }

    fun addTransition(from: BleState, to: BleState, event: BleEvent) {
        val receiver = LambdaBroadcastReceiver { _, _ -> transit(to) }
        broadcastManager.registerReceiver(receiver, BleEvent.intentFilter(bleDeviceAddress, from, event))
        broadcastReceivers.add(receiver)
    }

    fun addTransition(from: BleState, to: BleState, delay: Long = 0) {
        val receiver = LambdaBroadcastReceiver { _, _ ->
            runBlocking {
                delay(delay)
                transit(to)
            }
        }
        broadcastManager.registerReceiver(receiver, BleState.intentFilter(from))
        broadcastReceivers.add(receiver)
    }

    fun on(state: BleState, handler: () -> Unit) {
        val receiver = LambdaBroadcastReceiver { _, _ -> handler() }
        broadcastManager.registerReceiver(receiver, BleState.intentFilter(state))
        broadcastReceivers.add(receiver)
    }

    fun transitIf(current: BleState, to: BleState) {
        if (currentState == current) {
            transit(to)
        }
    }

    private fun transit(to: BleState) {
        printLog(TAG, "Transit: $currentState -> $to")
        _currentState = to
        broadcastManager.sendBroadcast(BleState.intent(to))
    }

    fun close() {
        printLog(TAG, "Close: unregister ${broadcastReceivers.size} broadcast receivers")
        broadcastReceivers.forEach { receiver ->
            broadcastManager.unregisterReceiver(receiver)
        }
        transit(BleState.CLOSED)
    }

    companion object {
        const val TAG = "BleStateManager"
    }
}
