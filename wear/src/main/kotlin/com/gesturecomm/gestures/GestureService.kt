package com.gesturecomm.gestures

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.gesturecomm.output.WatchTts
import com.gesturecomm.output.PhoneMessenger
import kotlinx.coroutines.*

/**
 * Foreground service that owns the SensorManager lifecycle.
 * Runs even when the watch screen is off.
 *
 * Broadcasts detected gestures via:
 *   1. Local broadcast → UI (MainActivity updates the display)
 *   2. PhoneMessenger  → paired phone speaks the phrase via TTS
 */
class GestureService : Service(), SensorEventListener {

    companion object {
        const val ACTION_GESTURE = "com.gesturecomm.GESTURE_DETECTED"
        const val EXTRA_GESTURE  = "gesture_name"
        const val EXTRA_PHRASE   = "phrase"
        const val CHANNEL_ID     = "gesture_service"

        fun start(context: Context) {
            val intent = Intent(context, GestureService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, GestureService::class.java))
        }
    }

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var gyroSensor:  Sensor? = null
    private lateinit var vibrator: Vibrator
    private val classifier = GestureClassifier()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor   = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor    = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        vibrator      = getSystemService(VIBRATOR_SERVICE) as Vibrator
        WatchTts.init(this)

        // Register at SENSOR_DELAY_GAME (~50 Hz) — good balance of responsiveness and battery
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyroSensor?.let  { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        WatchTts.shutdown()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── SensorEventListener ────────────────────────────────────────────────────

    override fun onSensorChanged(event: SensorEvent) {
        val ts = System.currentTimeMillis()
        val gesture = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> classifier.onAccel(event.values[0], event.values[1], event.values[2], ts)
            Sensor.TYPE_GYROSCOPE     -> classifier.onGyro (event.values[0], event.values[1], event.values[2], ts)
            else -> null
        } ?: return

        onGestureDetected(gesture)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    // ── Gesture handling ───────────────────────────────────────────────────────

    private fun onGestureDetected(gesture: Gesture) {
        // 1. Haptic feedback on the watch (double pulse = confirmation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 80, 60, 80), -1)
        }

        // 2. Notify the UI
        val uiIntent = Intent(ACTION_GESTURE).apply {
            putExtra(EXTRA_GESTURE, gesture.name)
            putExtra(EXTRA_PHRASE, gesture.phrase)
            `package` = packageName
        }
        sendBroadcast(uiIntent)

        // 3. Speak the phrase directly on the watch
        WatchTts.speak(gesture.phrase)

        // 4. Also attempt phone TTS if a phone is connected (bonus, non-blocking)
        scope.launch {
            PhoneMessenger.sendPhrase(applicationContext, gesture.phrase)
        }
    }

    // ── Notification ───────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Gesture Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Running gesture recognition in background" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GestureComm active")
            .setContentText("Listening for gestures…")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
}
