package com.android.blebutton

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_COARSE_LOCATION_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_CONNECT_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_FINE_LOCATION_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_SCAN_PERMISSION

class ScanDevicesActivity : AppCompatActivity(), LogCalls {

    private val bleScanner = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            printLog(TAG, "onScanFailed(): $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION,
                granted = { printLog(TAG, "onScanResult(): ${result?.device?.address} - ${result?.device?.name}") },
                denied = {
                    Toast.makeText(applicationContext, "You must enable Bluetooth permissions", Toast.LENGTH_SHORT).show()
                    return@checkPermission
                })
        }
    }

    private val bluetoothLeScanner: BluetoothLeScanner
        get() {
            val bluetoothManager = applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            return bluetoothAdapter.bluetoothLeScanner
        }

//    class ListDevicesAdapter(context: Context?, resource: Int) : ArrayAdapter<String>(context!!, resource) {
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        printLog(TAG, "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_devices)

//        val listDevices: ListView = findViewById(R.id.list_devices)
//        listDevices.adapter
//        listDevices.adapter = ListDevicesAdapter(
//            this,
//            resource = TODO(),
//        )
    }

    override fun onStart() {
        printLog(TAG, "onStart()")
        super.onStart()

        checkPermission(permission = BLUETOOTH_COARSE_LOCATION_PERMISSION, requestCode = 1, granted = {
            checkPermission(permission = BLUETOOTH_FINE_LOCATION_PERMISSION, requestCode = 2, granted = {
                checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                    checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = {
                        bluetoothLeScanner.startScan(bleScanner)
                    })
                })
            })
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    printLog(TAG, "onRequestPermissionsResult(PERMISSION_GRANTED) for ACCESS_COARSE_LOCATION")
                    checkPermission(permission = BLUETOOTH_FINE_LOCATION_PERMISSION, requestCode = 2, granted = {
                        checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                            checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = {
                                bluetoothLeScanner.startScan(bleScanner)
                            })
                        })
                    })
                }

                else -> {
                    printLog(TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for ACCESS_COARSE_LOCATION")
                    Toast.makeText(applicationContext, "You must enable Access coarse location permission", Toast.LENGTH_SHORT).show()
                }
            }

            2 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    printLog(TAG, "onRequestPermissionsResult(PERMISSION_GRANTED) for ACCESS_FINE_LOCATION")
                    checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                        checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = {
                            bluetoothLeScanner.startScan(bleScanner)
                        })
                    })
                }

                else -> {
                    printLog(TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for ACCESS_FINE_LOCATION")
                    Toast.makeText(applicationContext, "You must enable Access coarse location permission", Toast.LENGTH_SHORT).show()
                }
            }

            3 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = {
                        bluetoothLeScanner.startScan(bleScanner)
                    })
                }

                else -> {
                    printLog(TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for BLUETOOTH_SCAN")
                    Toast.makeText(applicationContext, "You must enable Bluetooth permission", Toast.LENGTH_SHORT).show()
                }
            }

            4 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    bluetoothLeScanner.startScan(bleScanner)
                }

                else -> {
                    printLog(TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for BLUETOOTH_CONNECT")
                    Toast.makeText(applicationContext, "You must enable Bluetooth Connect permission", Toast.LENGTH_SHORT).show()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        printLog(TAG, "onStop()")
        super.onStop()
        checkPermission(
            permission = BLUETOOTH_COARSE_LOCATION_PERMISSION,
            granted = { bluetoothLeScanner.stopScan(bleScanner) },
            denied = { return@checkPermission })
    }

    companion object {
        const val TAG = "ScanDevicesActivity"
    }
}
