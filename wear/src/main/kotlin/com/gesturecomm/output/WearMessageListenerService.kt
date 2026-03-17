package com.gesturecomm.output

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * Receives optional acknowledgements back from the phone.
 * Not required for basic operation — here for extensibility.
 */
class WearMessageListenerService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == PhoneMessenger.PATH_GESTURE_ACK) {
            Log.d("WearListener", "Phone TTS ack received")
        }
    }
}
