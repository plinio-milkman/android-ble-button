package com.android.blebutton

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_COARSE_LOCATION_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_CONNECT_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_FINE_LOCATION_PERMISSION
import com.android.blebutton.BtPermissions.Companion.BLUETOOTH_SCAN_PERMISSION

class DeviceActivity : AppCompatActivity(), LogCalls {

    private var paused = false

    val serviceConnection = BleServiceConnection()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(applicationContext, "POST_NOTIFICATIONS granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "POST_NOTIFICATIONS permission not granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        printLog(TAG, "onCreate()")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.device_activity_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.device_activity_menu_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        printLog(TAG, "onStart()")
        super.onStart()

        askNotificationPermission()

        checkPermission(permission = BLUETOOTH_COARSE_LOCATION_PERMISSION, requestCode = 1, granted = {
            checkPermission(permission = BLUETOOTH_FINE_LOCATION_PERMISSION, requestCode = 2, granted = {
                checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                    checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = { startBleService() })
                })
            })
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(PERMISSION_GRANTED) for ACCESS_COARSE_LOCATION")
                    checkPermission(permission = BLUETOOTH_FINE_LOCATION_PERMISSION, requestCode = 2, granted = {
                        checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                            checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = { startBleService() })
                        })
                    })
                }

                else -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for ACCESS_COARSE_LOCATION")
                    Toast.makeText(applicationContext, "You must enable Access coarse location permission", Toast.LENGTH_SHORT).show()
                }
            }

            2 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(PERMISSION_GRANTED) for ACCESS_FINE_LOCATION")
                    checkPermission(permission = BLUETOOTH_SCAN_PERMISSION, requestCode = 3, granted = {
                        checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = { startBleService() })
                    })
                }

                else -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for ACCESS_FINE_LOCATION")
                    Toast.makeText(applicationContext, "You must enable Access coarse location permission", Toast.LENGTH_SHORT).show()
                }
            }

            3 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    checkPermission(permission = BLUETOOTH_CONNECT_PERMISSION, requestCode = 4, granted = { startBleService() })
                }

                else -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for BLUETOOTH_SCAN")
                    Toast.makeText(applicationContext, "You must enable Bluetooth permission", Toast.LENGTH_SHORT).show()
                }
            }

            4 -> when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> {
                    startBleService()
                }

                else -> {
                    printLog(ScanDevicesActivity.TAG, "onRequestPermissionsResult(not PERMISSION_GRANTED) for BLUETOOTH_CONNECT")
                    Toast.makeText(applicationContext, "You must enable Bluetooth Connect permission", Toast.LENGTH_SHORT).show()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    printLog(TAG, "POST_NOTIFICATIONS permission already agreed.")
                }

                else -> {
                    printLog(TAG, "Ask POST_NOTIFICATIONS permission.")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun startBleService() {
        val serviceIntent = Intent(applicationContext, BleService::class.java)
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.registerReceiver(onConnectionStateChanged, BleState.intentFilter(BleState.CONNECTING, BleState.IDLE))
    }

    private val onConnectionStateChanged = LambdaBroadcastReceiver { _, _ ->
        updateFragmentState()
    }

    override fun onPause() {
        printLog(TAG, "onPause()")
        super.onPause()
        paused = true
    }

    override fun onResume() {
        printLog(TAG, "onResume()")
        super.onResume()
        paused = false

        updateFragmentState()
    }

    override fun onStop() {
        printLog(TAG, "onStop()")

        val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        broadcastManager.unregisterReceiver(onConnectionStateChanged)

        if (isFinishing) {
            val serviceIntent = Intent(applicationContext, BleService::class.java)
            unbindService(serviceConnection)
            stopService(serviceIntent)
        }

        super.onStop()
    }

    private fun updateFragmentState() {
        if (!paused) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.layout_container,
                    when (serviceConnection.service?.bleState) {
                        BleState.IDLE -> ConnectedFragment()
                        else -> ConnectingFragment()
                    }
                )
                .commit()
        }
    }

    companion object {
        const val TAG = "DeviceActivity"
    }
}
