package com.teamon.app.utils.classes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.teamon.app.MainActivity
import com.teamon.app.R
import com.teamon.app.utils.classes.Chat
import com.teamon.app.utils.classes.Message



data class MessaggioFalso(
    @DocumentId
    val id: String,
    val content: String,
    val senderId: String,
    val receiverId: String
)

class FirestoreMessageListener(private val context: Context)
{
    private val firestore = FirebaseFirestore.getInstance()
    private var messageListener: ListenerRegistration? = null

    /*
    fun startListeningForMessagesUnread(userId: String) {
        messageListener = firestore.collection("messages")
            .whereArrayContains("unread", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firebase", "Error fetching messages", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("Firebase", "Fetched ${snapshots.size()} documents")
                    for (doc in snapshots.documentChanges) {
                        if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            Log.d("Firebase", "New unread message: ${doc.document.id}")
                            val message = doc.document.toObject(Message::class.java)
                            sendNotification(context, message.content)
                        }
                    }
                } else {
                    Log.d("Firebase", "No documents fetched")
                }
            }
    }
     */


    fun startListeningForMessagesUnread(userId: String) {
        messageListener = firestore.collection("messages")
            .whereArrayContains("unread", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firebase", "Error fetching messages", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("Firebase", "Fetched ${snapshots.size()} documents")


                    for (doc in snapshots.documentChanges)
                    {
                        if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED)
                        {
                            Log.d("Firebase", "New unread message: ${doc.document.id}")
                            val message = doc.document.toObject(Message::class.java)

                            firestore.collection("users")
                                .document(message.senderId)
                                .addSnapshotListener { result, e ->
                                    if (e != null) {
                                        Log.e("Firebase", "Error fetching users", error)
                                        return@addSnapshotListener
                                    }

                                    if(result != null)
                                    {
                                        val user = result.toObject(User::class.java)
                                        val chatId= message.chatId

                                        if(user!=null)
                                        {
                                            val userSend=  "${user.name} ${user.surname}"

                                            firestore
                                                .collection("chats")
                                                .document(chatId)
                                                .addSnapshotListener { resultChat, errorChat ->
                                                    if (errorChat != null) {
                                                        Log.e("Firebase", "Error fetching chats", error)
                                                        return@addSnapshotListener
                                                    }

                                                    if(resultChat != null)
                                                    {
                                                        val chat= resultChat.toObject(Chat::class.java)
                                                        if(chat!=null)
                                                        {
                                                            val teamId= chat.teamId
                                                            val personalChat= chat.personal
                                                            firestore
                                                                .collection("teams")
                                                                .document(teamId)
                                                                .addSnapshotListener { resultTeam, errorTeam ->
                                                                    if (errorTeam != null) {
                                                                        Log.e("Firebase", "Error fetching team", error)
                                                                        return@addSnapshotListener
                                                                    }

                                                                    if(resultTeam != null)
                                                                    {
                                                                        val team= resultTeam.toObject(Team::class.java)
                                                                        if(team!=null)
                                                                        {
                                                                            val teamGroup= if(!personalChat) "${team.name} (GROUP CHAT)"
                                                                                             else "${team.name}"
                                                                            sendNotification(context, userSend, teamGroup ,message.content)
                                                                        }
                                                                    }

                                                                }
                                                        }

                                                    }

                                                }
                                        }

                                    }
                                }

                        }
                    }
                } else {
                    Log.d("Firebase", "No messages fetched")
                }
            }
    }



    fun stopListeningForMessages() {
        messageListener?.remove()
    }

    private fun sendNotification(context: Context, userSend: String, teamName: String, message: String) {
        val channelId = "message_channel"
        val channelName = "Message Notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for message notifications"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        //BOLD TEAM NAME
        val spannableTeamName = SpannableString(teamName).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        val spannableMessage = SpannableString("\n$message")

        val finalText = TextUtils.concat(spannableTeamName, spannableMessage)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(userSend)
            .setContentText(finalText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(0, builder.build())
        }
    }
}