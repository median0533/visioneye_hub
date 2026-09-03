package com.example

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.util.NewUserCheckWorker
import com.example.util.NewUserNotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.util.concurrent.TimeUnit

class AdminApplication : Application() {
    companion object {
        var instance: AdminApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeFirebaseSafely()
        NewUserNotificationHelper.createNotificationChannel(this)
        com.example.service.NewUserMonitoringService.start(this)
        scheduleBackgroundNewUserChecks()
    }

    private fun scheduleBackgroundNewUserChecks() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NewUserCheckWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "NewUserBackgroundCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.i("AdminApp", "Scheduled periodic background worker for new user notifications")
        } catch (e: Exception) {
            Log.e("AdminApp", "Failed to schedule background worker: ${e.message}", e)
        }
    }

    private fun initializeFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    // Try automatic initialization from google-services.json resources
                    FirebaseApp.initializeApp(this)
                    Log.i("AdminApp", "FirebaseApp initialized from google-services resources")
                } catch (e: Exception) {
                    Log.w("AdminApp", "Default init failed, initializing with explicit FirebaseOptions: ${e.message}")
                    val options = FirebaseOptions.Builder()
                        .setProjectId("visioneye-a04dd")
                        .setApiKey("AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw")
                        .setApplicationId("1:189246600899:android:9f13bb9d10f53b398d9476")
                        .setStorageBucket("visioneye-a04dd.firebasestorage.app")
                        .setGcmSenderId("189246600899")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.i("AdminApp", "FirebaseApp initialized with explicit fallback options")
                }
            } else {
                Log.i("AdminApp", "FirebaseApp was already initialized by FirebaseInitProvider")
            }
        } catch (e: Exception) {
            Log.e("AdminApp", "Error during FirebaseApp startup initialization: ${e.message}", e)
        }
    }
}
