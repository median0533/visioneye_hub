package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.NewUserNotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Persistent Background Service that continuously listens to Firestore 'users' collection
 * in real-time. Fires immediate notifications whenever a new customer registers,
 * even when the app is minimized or closed.
 */
class NewUserMonitoringService : Service() {

    private val tag = "NewUserMonitorService"
    private var listenerRegistration: ListenerRegistration? = null
    private var isRunning = false

    companion object {
        const val MONITOR_CHANNEL_ID = "visioneye_service_channel"
        const val MONITOR_NOTIFICATION_ID = 9001

        fun start(context: Context) {
            try {
                val intent = Intent(context, NewUserMonitoringService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.i("NewUserMonitorService", "Requested NewUserMonitoringService start")
            } catch (e: Exception) {
                Log.e("NewUserMonitorService", "Failed to start NewUserMonitoringService: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, NewUserMonitoringService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e("NewUserMonitorService", "Failed to stop service: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "NewUserMonitoringService onCreate")
        createServiceNotificationChannel()
        startAsForeground()
        startFirestoreRealtimeListener()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "NewUserMonitoringService onStartCommand")
        startAsForeground()
        if (listenerRegistration == null) {
            startFirestoreRealtimeListener()
        }
        return START_STICKY
    }

    private fun createServiceNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MONITOR_CHANNEL_ID,
                "vISIONeYe Admin Real-Time Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Keeps real-time Firestore sync active for immediate new registration alerts"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startAsForeground() {
        try {
            val openAppIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, MONITOR_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle("vISIONeYe Live Monitor")
                .setContentText("Active • Listening for new customer registrations")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(MONITOR_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(tag, "startForeground error: ${e.message}")
        }
    }

    private fun startFirestoreRealtimeListener() {
        try {
            var app = runCatching { FirebaseApp.getInstance() }.getOrNull()
            if (app == null) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("visioneye-a04dd")
                    .setApiKey("AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw")
                    .setApplicationId("1:189246600899:android:9f13bb9d10f53b398d9476")
                    .setStorageBucket("visioneye-a04dd.firebasestorage.app")
                    .setGcmSenderId("189246600899")
                    .build()
                app = runCatching { FirebaseApp.initializeApp(this, options) }.getOrNull()
            }

            if (app == null) {
                Log.w(tag, "FirebaseApp could not be obtained for background listener")
                return
            }

            val prefs = getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
            val collectionName = prefs.getString("collection_name", "users") ?: "users"

            val db = FirebaseFirestore.getInstance(app)
            listenerRegistration?.remove()

            listenerRegistration = db.collection(collectionName)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(tag, "Firestore background listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val notifiedUsers = prefs.getStringSet("notified_user_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
                        var hasNewNotification = false

                        // Check document changes for real-time immediate alerts
                        for (dc in snapshot.documentChanges) {
                            if (dc.type == DocumentChange.Type.ADDED || dc.type == DocumentChange.Type.MODIFIED) {
                                val doc = dc.document
                                val userId = doc.id
                                val data = doc.data ?: emptyMap<String, Any>()
                                val status = data["status"]?.toString()?.lowercase()?.trim() ?: "active"

                                // Check if unnotified or pending
                                if (!notifiedUsers.contains(userId)) {
                                    val name = (data["customerName"] ?: data["name"] ?: data["displayName"] ?: data["businessName"] ?: "").toString().trim()
                                    val email = (data["email"] ?: "").toString().trim()

                                    Log.i(tag, "New customer detected in background: $userId ($name). Triggering immediate notification!")
                                    NewUserNotificationHelper.showNewUserNotification(
                                        context = applicationContext,
                                        userId = userId,
                                        displayName = name,
                                        email = email
                                    )
                                    notifiedUsers.add(userId)
                                    hasNewNotification = true
                                }
                            }
                        }

                        if (hasNewNotification) {
                            prefs.edit().putStringSet("notified_user_ids", notifiedUsers).apply()
                        }
                    }
                }

            Log.i(tag, "Successfully attached real-time Firestore listener to collection '$collectionName'")
        } catch (e: Exception) {
            Log.e(tag, "Error starting background Firestore listener: ${e.message}", e)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(tag, "App task removed from Recents. Re-ensuring NewUserMonitoringService is active.")
        // Restart service immediately
        val restartIntent = Intent(applicationContext, NewUserMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(restartIntent)
        } else {
            applicationContext.startService(restartIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(tag, "NewUserMonitoringService onDestroy. Re-launching...")
        listenerRegistration?.remove()
        listenerRegistration = null
        // Re-launch on destroy so listener is never dead
        start(applicationContext)
    }
}
