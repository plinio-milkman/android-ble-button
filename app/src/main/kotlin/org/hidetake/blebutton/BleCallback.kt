package org.hidetake.blebutton

import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.util.Log
import org.hidetake.blebutton.BleEvent.*

class BleCallback(context: Context, bleStateManager: BleStateManager) : BluetoothGattCallback() {

    val bleStateManager = bleStateManager
    val context = context
    var start : Int = 0

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Log.d("BleCallback", "onConnectionStateChange: newState=$newState")
        when (newState) {
            BluetoothProfile.STATE_CONNECTING -> bleStateManager.receiveBleEvent(BLE_CONNECTING)
            BluetoothProfile.STATE_CONNECTED -> bleStateManager.receiveBleEvent(BLE_CONNECTED)
            BluetoothProfile.STATE_DISCONNECTING -> bleStateManager.receiveBleEvent(BLE_DISCONNECTING)
            BluetoothProfile.STATE_DISCONNECTED -> bleStateManager.receiveBleEvent(BLE_DISCONNECTED)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        Log.d("BleCallback", "onServicesDiscovered: $status")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_FAILURE)
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d("BleCallback", "----onCharacteristicRead: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_FAILURE)
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d("BleCallback", "++++onCharacteristicWrite: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_FAILURE)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
//        Log.d("BleCallback", "****onCharacteristicChanged: ${characteristic?.uuid}")
        val i = Intent()
        if (start == 0) {
            Log.d("BleCallback", "onCharacteristicChanged: START ${characteristic?.uuid}")
            i.action = "com.symbol.datawedge.api.ACTION"
            i.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "START_SCANNING")
            start++
        } else {
            Log.d("BleCallback", "onCharacteristicChanged: STOP ${characteristic?.uuid}")
            i.action = "com.symbol.datawedge.api.ACTION"
            i.putExtra("com.symbol.datawedge.api.SOFT_SCAN_TRIGGER", "STOP_SCANNING")
            start--
        }
        context.sendBroadcast(i)
        bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED)
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        Log.d("BleCallback", "onDescriptorRead: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_FAILURE)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        Log.d("BleCallback", "onDescriptorWrite: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
        }
    }
}
