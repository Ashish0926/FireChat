package com.example.firechat.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.firechat.R
import com.example.firechat.activities.ChatActivity
import com.example.firechat.models.User
import com.example.firechat.utilities.Constants.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MessagingService: FirebaseMessagingService() {
    override fun onNewToken(@NonNull token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Token: $token")
    }

    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val user = User(
            remoteMessage.data[Constants.KEY_NAME]!!,
            null,
            null,
            remoteMessage.data[Constants.KEY_FCM_TOKEN],
            remoteMessage.data[Constants.KEY_USER_ID]!!
        )
        val notificationId = Random().nextInt()
        val channelId = "chat_message"

        val intent = Intent(this, ChatActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Constants.KEY_USER, user)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setContentTitle(user.name)
        builder.setContentText(remoteMessage.data[Constants.KEY_MESSAGE])
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(
            remoteMessage.data[Constants.KEY_MESSAGE]
        ))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Chat Message" as CharSequence
            val channelDescription = "This notification channel is used for chat message notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, builder.build())

        // Log.d("FCM", "Message: ${remoteMessage.notification?.body}")
    }
}