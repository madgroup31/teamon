package com.teamon.app

import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri

class MessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
                val channel = notification.channelId
                val tag = notification.tag
                val image = notification.imageUrl
                if(tag != profileViewModel.userId)
                    when(channel) {
                        HISTORY -> {
                            sendNotification(
                                channel = channel,
                                title = notification.title,
                                message = notification.body,
                            )
                        }
                        MESSAGES -> {

                            sendNotification(
                                channel = channel,
                                title = notification.title,
                                message = notification.body,
                            )
                        }
                        else -> {}
                    }
            }
        }

    private fun sendNotification(channel: String, title: String?, message: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_action_name)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Math.random().toInt(), notificationBuilder.build())
    }

    companion object {
        const val HISTORY = "history"
        const val MESSAGES = "messages"


            fun subscribe(topic: String) {
                Firebase.messaging.subscribeToTopic(topic).addOnCompleteListener {
                    if(!it.isSuccessful) {
                        Log.e("messaging", "Subscription to topic \"${topic}\" failed.")
                    }
                    else{
                        Log.d("messaging", "Subscription to topic \"${topic}\" successful.")
                    }
                }
            }

        fun initialize(context: Context, activity: Activity) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show an explanation to the user
                    AlertDialog.Builder(context)
                        .setTitle("Notification Permission Needed")
                        .setMessage("This app needs the Notification permission to notify you about important updates.")
                        .setPositiveButton("OK") { dialog, _ ->
                            // Request the permission again
                            ActivityCompat.requestPermissions(activity,
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                1)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            // Handle the user's refusal
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(activity,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    var channel = NotificationChannel(
                        MessagingService.HISTORY,
                        "Task Modifications",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "This channel allows to receive notifications about task modifications."
                    }
                    val notificationManager: NotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)

                    channel = NotificationChannel(
                        MessagingService.MESSAGES,
                        "Chat Messages",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "This channel allows to receive notifications when new chat messages are received."
                    }
                    notificationManager.createNotificationChannel(channel)
                }
            }
        }

    }

}