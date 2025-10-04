package com.android.blebutton

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_CONNECT_PERMISSION
import java.util.Locale
import java.util.UUID

@Suppress("DEPRECATION")
class BleContext(context: Context, address: String) : LogCalls {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bluetoothDevice = bluetoothAdapter.getRemoteDevice(address.uppercase(Locale.getDefault()))
    private val bleStateManager = BleStateManager(context, bluetoothDevice.address)

    private var bluetoothGatt: BluetoothGatt? = null

    val bleDeviceAddress: String
        get() = bluetoothDevice.address

    val bleState: BleState
        get() = bleStateManager.currentState

    enum class BLEUUID(uuidString: String) {
        BUTTON_SERVICE("0000ffe0-0000-1000-8000-00805f9b34fb"),
        BUTTON_STATE("0000ffe1-0000-1000-8000-00805f9b34fb"),
        CLIENT_CHARACTERISTIC_CONFIG("00002902-0000-1000-8000-00805f9b34fb")
        ;

        val uuid: UUID = UUID.fromString(uuidString)
    }

    init {
        bleStateManager.on(BleState.CONNECTING) {
            printLog(TAG, "Start on connecting")

            context.checkPermission(
                permission = BLUETOOTH_CONNECT_PERMISSION,
                granted = {
                    bluetoothGatt =
                        kotlin.runCatching { bluetoothDevice.connectGatt(context, false, BleCallback(context, bleStateManager)) }.getOrNull()
                    bluetoothGatt?.connect()
                    printLog(TAG, "BluetoothGatt#connect() finished")
                },
                denied = {
                    Toast.makeText(context.applicationContext, "You must enable Bluetooth permissions and restart the App", Toast.LENGTH_SHORT).show()
                    return@checkPermission
                })
        }

        bleStateManager.addTransition(from = BleState.INIT, to = BleState.CONNECTED, event = BleEvent.BLE_CONNECTED)
        bleStateManager.addTransition(from = BleState.CONNECTING, to = BleState.CONNECTED, event = BleEvent.BLE_CONNECTED)

        bleStateManager.on(BleState.CONNECTED) {
            bluetoothGatt?.discoverServices()
        }

        bleStateManager.addTransition(from = BleState.CONNECTED, to = BleState.SERVICE_DISCOVERED, event = BleEvent.BLE_SERVICE_DISCOVER_SUCCESS)
        bleStateManager.addTransition(from = BleState.CONNECTED, to = BleState.CONNECTED, event = BleEvent.BLE_SERVICE_DISCOVER_FAILURE)

        bleStateManager.addTransition(from = BleState.SERVICE_DISCOVERED, to = BleState.SENDING_BUTTON_DESCRIPTOR)

        bleStateManager.on(BleState.SENDING_BUTTON_DESCRIPTOR) {
            val characteristic = bluetoothGatt?.getService(BLEUUID.BUTTON_SERVICE.uuid)?.getCharacteristic(BLEUUID.BUTTON_STATE.uuid)
            bluetoothGatt?.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic?.getDescriptor(BLEUUID.CLIENT_CHARACTERISTIC_CONFIG.uuid)
            val descValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (descriptor != null) {
                    bluetoothGatt?.writeDescriptor(descriptor, descValue)
                }
            } else {
                descriptor?.value = descValue
                bluetoothGatt?.writeDescriptor(descriptor)
            }
        }

        bleStateManager.addTransition(
            from = BleState.SENDING_BUTTON_DESCRIPTOR,
            to = BleState.SENT_BUTTON_DESCRIPTOR,
            event = BleEvent.BLE_DESCRIPTOR_WRITE_SUCCESS
        )

        bleStateManager.addTransition(
            from = BleState.SENDING_BUTTON_DESCRIPTOR,
            to = BleState.SENDING_BUTTON_DESCRIPTOR,
            event = BleEvent.BLE_DESCRIPTOR_WRITE_FAILURE
        )

        bleStateManager.addTransition(from = BleState.SENT_BUTTON_DESCRIPTOR, to = BleState.READY)
        bleStateManager.addTransition(from = BleState.READY, to = BleState.IDLE)

        bleStateManager.addTransition(from = BleState.IDLE, to = BleState.BUTTON_PRESSED, event = BleEvent.BLE_CHARACTERISTIC_CHANGED_IN_START)

        bleStateManager.addTransition(from = BleState.BUTTON_PRESSED, to = BleState.BUTTON_RELEASED, event = BleEvent.BLE_CHARACTERISTIC_CHANGED_IN_STOP)
        bleStateManager.addTransition(from = BleState.BUTTON_RELEASED, to = BleState.IDLE)

        BleState.entries.toTypedArray().subtract(setOf(BleState.CLOSED)).forEach { notClosedState ->
            bleStateManager.addTransition(from = notClosedState, to = BleState.CONNECTING, event = BleEvent.BLE_DISCONNECTED)
        }
    }

    fun connect() {
        printLog(TAG, "connect()")
        bleStateManager.transitIf(current = BleState.INIT, to = BleState.CONNECTING)
    }

    fun close(context: Context) {
        printLog(TAG, "close()")
        bleStateManager.close()
        context.checkPermission(
            permission = BLUETOOTH_CONNECT_PERMISSION,
            granted = {
                bluetoothGatt?.disconnect()
                bluetoothGatt?.close()
            },
            denied = {
                Toast.makeText(context.applicationContext, "You must enable Bluetooth permissions and restart the App", Toast.LENGTH_SHORT).show()
                return@checkPermission
            })
    }

    companion object {
        const val TAG = "BleContext"
    }
}