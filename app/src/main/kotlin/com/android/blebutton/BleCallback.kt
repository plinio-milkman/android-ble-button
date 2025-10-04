package com.android.blebutton

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Looper
import android.widget.Toast
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_CHANGED_IN_START
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_CHANGED_IN_STOP
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_READ_FAILURE
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_READ_SUCCESS
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_WRITE_FAILURE
import com.android.blebutton.BleEvent.BLE_CHARACTERISTIC_WRITE_SUCCESS
import com.android.blebutton.BleEvent.BLE_CONNECTED
import com.android.blebutton.BleEvent.BLE_CONNECTING
import com.android.blebutton.BleEvent.BLE_DESCRIPTOR_READ_FAILURE
import com.android.blebutton.BleEvent.BLE_DESCRIPTOR_READ_SUCCESS
import com.android.blebutton.BleEvent.BLE_DESCRIPTOR_WRITE_SUCCESS
import com.android.blebutton.BleEvent.BLE_DISCONNECTED
import com.android.blebutton.BleEvent.BLE_DISCONNECTING
import com.android.blebutton.BleEvent.BLE_SERVICE_DISCOVER_FAILURE
import com.android.blebutton.BleEvent.BLE_SERVICE_DISCOVER_SUCCESS

class BleCallback(val context: Context, private val bleStateManager: BleStateManager) : BluetoothGattCallback(), LogCalls {
    private var start: Int = 0
    private var toast: Toast? = null

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        printLog(TAG, "onConnectionStateChange: newState=$newState")
        when (newState) {
            BluetoothProfile.STATE_CONNECTING -> bleStateManager.receiveBleEvent(BLE_CONNECTING)
            BluetoothProfile.STATE_CONNECTED -> bleStateManager.receiveBleEvent(BLE_CONNECTED)
            BluetoothProfile.STATE_DISCONNECTING -> bleStateManager.receiveBleEvent(BLE_DISCONNECTING)
            BluetoothProfile.STATE_DISCONNECTED -> bleStateManager.receiveBleEvent(BLE_DISCONNECTED)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        printLog(TAG, "onServicesDiscovered: $status")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_SERVICE_DISCOVER_FAILURE)
        }
    }

    // triggered in Android version < 33
    @Deprecated("Deprecated in Java")
    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        printLog(TAG, "onCharacteristicRead: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_FAILURE)
        }
    }

    // triggered in Android version >= 33
    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        printLog(TAG, "onCharacteristicRead: $status: ${characteristic.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_READ_FAILURE)
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        printLog(TAG, "onCharacteristicWrite: $status: ${characteristic?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_WRITE_FAILURE)
        }
    }

    // triggered in Android version < 33
    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {

        if (start == 0) {
            printLog(TAG, "onCharacteristicChanged: START ${characteristic?.uuid}")
            showToast(text = "BLE Button clicked")
            start++
            bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED_IN_START)
        } else {
            printLog(TAG, "onCharacteristicChanged: STOP ${characteristic?.uuid}")
            showToast(text = "BLE Button unclicked")
            start--
            bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED_IN_STOP)
        }
    }

    // triggered in Android version >= 33
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {

        if (start == 0) {
            printLog(TAG, "onCharacteristicChanged: START ${characteristic.uuid}")
            showToast(text = "BLE Button clicked")
            start++
            bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED_IN_START)
        } else {
            printLog(TAG, "onCharacteristicChanged: STOP ${characteristic.uuid}")
            showToast(text = "BLE Button unclicked")
            start--
            bleStateManager.receiveBleEvent(BLE_CHARACTERISTIC_CHANGED_IN_STOP)
        }
    }

    // triggered in Android version < 33
    @Deprecated("Deprecated in Java")
    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        printLog(TAG, "onDescriptorRead: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_FAILURE)
        }
    }

    // triggered in Android version >= 33
    override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
        printLog(TAG, "onDescriptorRead: $status: ${descriptor.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_READ_FAILURE)
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        printLog(TAG, "onDescriptorWrite: $status: ${descriptor?.uuid}")
        when (status) {
            BluetoothGatt.GATT_SUCCESS -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
            else -> bleStateManager.receiveBleEvent(BLE_DESCRIPTOR_WRITE_SUCCESS)
        }
    }

    private fun showToast(text: String) {
        val mainHandler = android.os.Handler(Looper.getMainLooper())
        mainHandler.post {
            toast?.cancel()
            toast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT)
            toast?.show()
        }
    }

    companion object {
        const val TAG = "BleCallback"
    }
}
