package com.example.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NewUserCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val tag = "NewUserCheckWorker"

    override suspend fun doWork(): Result {
        return try {
            val prefs = context.getSharedPreferences("firebase_hub_prefs", Context.MODE_PRIVATE)
            val collectionName = prefs.getString("collection_name", "users") ?: "users"
            val notifiedUsersSet = prefs.getStringSet("notified_user_ids", emptySet())?.toMutableSet() ?: mutableSetOf()

            var app = runCatching { FirebaseApp.getInstance() }.getOrNull()
            if (app == null) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("visioneye-a04dd")
                    .setApiKey("AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw")
                    .setApplicationId("1:189246600899:android:9f13bb9d10f53b398d9476")
                    .setStorageBucket("visioneye-a04dd.firebasestorage.app")
                    .setGcmSenderId("189246600899")
                    .build()
                app = runCatching { FirebaseApp.initializeApp(context, options) }.getOrNull()
            }

            if (app == null) {
                Log.w(tag, "FirebaseApp could not be initialized in background worker.")
                return Result.retry()
            }

            val db = FirebaseFirestore.getInstance(app)
            val querySnapshot = db.collection(collectionName).get().await()

            for (doc in querySnapshot.documents) {
                val userId = doc.id
                val data = doc.data ?: emptyMap<String, Any>()

                val status = data["status"]?.toString()?.lowercase()?.trim() ?: "active"
                val isPending = status == "pending"

                // Check if user is pending or registered recently and hasn't been notified
                if (!notifiedUsersSet.contains(userId)) {
                    val name = (data["customerName"] ?: data["name"] ?: data["displayName"] ?: data["businessName"] ?: "").toString().trim()
                    val email = (data["email"] ?: "").toString().trim()

                    // Trigger notification
                    NewUserNotificationHelper.showNewUserNotification(
                        context = context,
                        userId = userId,
                        displayName = name,
                        email = email
                    )

                    notifiedUsersSet.add(userId)
                }
            }

            // Save updated notified IDs
            prefs.edit().putStringSet("notified_user_ids", notifiedUsersSet).apply()

            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Error checking new users in background: ${e.message}", e)
            Result.retry()
        }
    }
}
