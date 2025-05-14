package com.neu.classmate.model

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // You can send this token to Firestore if needed
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d("FCM", "Title: ${it.title}")
            Log.d("FCM", "Body: ${it.body}")
        }
    }
}