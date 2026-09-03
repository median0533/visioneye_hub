package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NewUserNotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "channel_new_user_registration"
    private const val CHANNEL_NAME = "New User Registrations"
    private const val CHANNEL_DESC = "Notifications when a new user registers and requires account activation"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNewUserNotification(
        context: Context,
        userId: String,
        displayName: String,
        email: String
    ) {
        try {
            // Check permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.")
                    return
                }
            }

            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("SELECTED_USER_ID", userId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                userId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val label = if (displayName.isNotBlank()) displayName else if (email.isNotBlank()) email else "Customer"
            val title = "New User Added"
            val contentText = "$label registered. Tap to activate account."

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$label has registered on vISIONeYe.\nPlease open the Admin app to review and activate this account.")
                        .setSummaryText("Account Activation Required")
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = (userId.hashCode() and 0x7FFFFFFF)
            notificationManager.notify(notificationId, builder.build())
            Log.i(TAG, "Successfully posted new user notification for: $label (id: $userId)")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying new user notification: ${e.message}", e)
        }
    }
}
