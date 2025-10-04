package com.android.blebutton

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.android.blebutton.BtPermissions.Companion.POST_NOTIFICATIONS_PERMISSION

class BleService : Service(), LogCalls {

    private var bleContext: BleContext? = null

    val bleDeviceAddress: String?
        get() = bleContext?.bleDeviceAddress

    val bleState: BleState?
        get() = bleContext?.bleState

    override fun onCreate() {
        printLog(TAG, "onCreate()")

        createForegroundServiceNotificationChannel()

        val foregroundNotification = Notification.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.bluetooth_notification)
            .setContentTitle("BLE Button Service ON")
            .setContentText("Connected to the BLE Device.")
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotification)

        createBleButtonNotificationChannel()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(onPreferenceChanged)

        bleContext =
            preferences.getString(MyPreference.DEVICE_ADDRESS.name.lowercase(), "00:00:00:00:00:00")?.let { BleContext(applicationContext, it) }

        val broadcastManager = LocalBroadcastManager.getInstance(this)
        broadcastManager.registerReceiver(onBleButtonPressed, BleState.intentFilter(BleState.BUTTON_PRESSED))

        bleContext!!.connect()
    }

    override fun onBind(intent: Intent): IBinder {
        printLog(TAG, "onBind()")
        return BleServiceConnection.BleServiceBinder(this)
    }

    private fun createBleButtonNotificationChannel() {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(BUTTON_CHANNEL_ID) == null) {
            val channel = NotificationChannel(BUTTON_CHANNEL_ID, "BLE Button pressed notification", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundServiceNotificationChannel() {
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            val channel = NotificationChannel(FOREGROUND_CHANNEL_ID, "BLE Service Status", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val onBleButtonPressed = LambdaBroadcastReceiver { _, _ ->
        printLog(TAG, "onBleButtonPressed()")

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (notificationSoundUri != null) {
                MediaPlayer().apply {
                    setDataSource(applicationContext, notificationSoundUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setOnPreparedListener { it.start() }
                    setOnCompletionListener { it.release() }
                    setOnErrorListener { _, _, _ -> true }
                    prepareAsync()
                }
            }
        } catch (e: Exception) {
            printLog(TAG, "Exception on MediaPlayer: ${e.message}")
        }

        val notification = NotificationCompat.Builder(this, BUTTON_CHANNEL_ID)
            .setSmallIcon(R.drawable.bluetooth_notification)
            .setColor(this.getColor(R.color.colorPrimary))
            .setContentTitle("BLE button is pressed")
            .setContentText(bleContext?.bleDeviceAddress)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.checkPermission(
                POST_NOTIFICATIONS_PERMISSION,
                granted = { notificationManager.notify(BUTTON_NOTIFICATION_ID, notification) },
                denied = {
                    printLog(TAG, "Not agreed POST_NOTIFICATIONS")
                    return@checkPermission
                }
            )
        } else {
            notificationManager.notify(BUTTON_NOTIFICATION_ID, notification)
        }
    }

    private val onPreferenceChanged = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "device_address" -> {
                printLog(TAG, "onPreferenceChanged(device_address)")
                val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                val broadcastManager = LocalBroadcastManager.getInstance(this)
                broadcastManager.unregisterReceiver(onBleButtonPressed)
                bleContext?.close(this)

                bleContext = preferences.getString(MyPreference.DEVICE_ADDRESS.name.lowercase(), "00:00:00:00:00:00")
                    ?.let { BleContext(applicationContext, it) }
                broadcastManager.registerReceiver(onBleButtonPressed, BleState.intentFilter(BleState.BUTTON_PRESSED))
                bleContext!!.connect()
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        printLog(TAG, "onUnbind()")
        return true
    }

    override fun onRebind(intent: Intent?) {
        printLog(TAG, "onRebind()")
    }

    override fun onDestroy() {
        printLog(TAG, "onDestroy()")

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChanged)

        val broadcastManager = LocalBroadcastManager.getInstance(this)
        broadcastManager.unregisterReceiver(onBleButtonPressed)

        bleContext?.close(this)
        bleContext = null
    }

    companion object {
        const val TAG = "BleService"
        const val BUTTON_CHANNEL_ID = "ble_device_channel"
        const val BUTTON_NOTIFICATION_ID = 1
        const val FOREGROUND_CHANNEL_ID = "ble_service_channel"
        const val FOREGROUND_NOTIFICATION_ID = 2
    }
}
