# ==============================================================================
# ProGuard / R8 Configuration Rules
# Application: VisionEye Admin Hub (com.mindron.visioneye_adminapp)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. General & Line Number Preservation (For Stack Traces & Crash Reporting)
# ------------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Keep parcelables and serializable members
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ------------------------------------------------------------------------------
# 2. Application Core Components
# ------------------------------------------------------------------------------
-keep public class com.example.AdminApplication { *; }
-keep public class com.example.MainActivity { *; }
-keep public class com.example.service.NewUserMonitoringService { *; }
-keep public class com.example.service.BootReceiver { *; }

# ------------------------------------------------------------------------------
# 3. Data Models & Firebase Firestore Serialization
# Firestore uses reflection to serialize and deserialize documents into data classes.
# ------------------------------------------------------------------------------
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** {
    <fields>;
    <methods>;
}

# Keep Firestore annotations and annotated fields/methods
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.IgnoreExtraProperties <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
}

# Preserve no-argument constructors needed by Firestore deserialization
-keepclassmembers class * {
    public <init>();
}

# ------------------------------------------------------------------------------
# 4. Firebase SDKs (Auth, Firestore, AppCheck)
# ------------------------------------------------------------------------------
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# ------------------------------------------------------------------------------
# 5. AndroidX WorkManager
# Workers are instantiated dynamically by reflection using this exact constructor
# ------------------------------------------------------------------------------
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.example.util.NewUserCheckWorker { *; }

# ------------------------------------------------------------------------------
# 6. Kotlin Coroutines & Flow
# ------------------------------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# 7. Jetpack Compose
# ------------------------------------------------------------------------------
-keepclassmembers class androidx.compose.** {
    public <methods>;
}

# ------------------------------------------------------------------------------
# 8. Room Database (if used)
# ------------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 9. Retrofit, OkHttp, Moshi
# ------------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*
