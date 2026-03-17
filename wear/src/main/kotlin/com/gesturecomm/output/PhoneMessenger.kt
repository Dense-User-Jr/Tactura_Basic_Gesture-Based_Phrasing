package com.gesturecomm.output

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

/**
 * Sends the recognised phrase to the paired phone over the Wearable Data Layer.
 * The phone companion app receives it and speaks it aloud via Android TTS.
 */
object PhoneMessenger {

    private const val TAG      = "PhoneMessenger"
    const val PATH_SPEAK       = "/speak"
    const val PATH_GESTURE_ACK = "/gesture_ack"

    suspend fun sendPhrase(context: Context, phrase: String) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected phone nodes found — TTS skipped")
                return
            }
            val bytes = phrase.toByteArray(Charsets.UTF_8)
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, PATH_SPEAK, bytes)
                    .await()
                Log.d(TAG, "Sent phrase to ${node.displayName}: $phrase")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send phrase to phone", e)
        }
    }
}
