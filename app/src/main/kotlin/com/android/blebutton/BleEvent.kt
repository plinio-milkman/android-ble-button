package com.android.blebutton

import android.content.Intent
import android.content.IntentFilter

enum class BleEvent {
    BLE_CONNECTING,
    BLE_CONNECTED,
    BLE_DISCONNECTING,
    BLE_DISCONNECTED,
    BLE_SERVICE_DISCOVER_SUCCESS,
    BLE_SERVICE_DISCOVER_FAILURE,
    BLE_CHARACTERISTIC_READ_SUCCESS,
    BLE_CHARACTERISTIC_READ_FAILURE,
    BLE_CHARACTERISTIC_WRITE_SUCCESS,
    BLE_CHARACTERISTIC_WRITE_FAILURE,
    BLE_CHARACTERISTIC_CHANGED_IN_START,
    BLE_CHARACTERISTIC_CHANGED_IN_STOP,
    BLE_DESCRIPTOR_READ_SUCCESS,
    BLE_DESCRIPTOR_READ_FAILURE,
    BLE_DESCRIPTOR_WRITE_SUCCESS,
    BLE_DESCRIPTOR_WRITE_FAILURE,
    ;

    companion object {
        fun intent(bleDeviceAddress: String, bleState: BleState, bleEvent: BleEvent) =
                Intent("${bleDeviceAddress}__${bleState}__$bleEvent")

        fun intentFilter(bleDeviceAddress: String, bleState: BleState, bleEvent: BleEvent) =
                IntentFilter("${bleDeviceAddress}__${bleState}__$bleEvent")

    }
}
