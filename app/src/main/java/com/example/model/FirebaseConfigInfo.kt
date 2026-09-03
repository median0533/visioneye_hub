package com.example.model

object FirebaseConfigInfo {
    const val PROJECT_ID = "visioneye-a04dd"
    const val PROJECT_NUMBER = "189246600899"
    const val STORAGE_BUCKET = "visioneye-a04dd.firebasestorage.app"
    
    // Admin App (Current app)
    const val ADMIN_APP_ID = "1:189246600899:android:9f13bb9d10f53b398d9476"
    const val ADMIN_APP_NICKNAME = "vISIONeYe_Hub"
    const val ADMIN_PACKAGE_NAME = "com.mindron.visioneye_adminapp"
    
    // Client App (ZewarCam app)
    const val CLIENT_APP_ID = "1:189246600899:android:ef1296994a41a2ef8d9476"
    const val CLIENT_APP_NICKNAME = "vISIONeYe"
    const val CLIENT_PACKAGE_NAME = "com.mindron.zewarcam"
    const val CLIENT_CERT_HASH = "9897d8e8e48f65531ac669fcec3152ac30f71e9e"

    const val API_KEY = "AIzaSyA_LHu-abZKQUmS48aeoQXnwpxBT1YFyWw"

    // User's current rule
    const val CURRENT_USER_RULE = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow create, read, update: if request.auth != null && request.auth.uid == userId;
    }
  }
}"""

    // Option 1: Allows Admin App to list users while keeping client create/update restricted to owner
    const val RECOMMENDED_ADMIN_RULE = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Allow Admin App to list and view all registered users
      allow read: if true;
      // Regular users can only create & update their own profile
      allow create, update: if request.auth != null && request.auth.uid == userId;
    }
  }
}"""

    // Option 2: Allow specific Admin email (trusuremedian@gmail.com) full access
    const val ADMIN_EMAIL_RULE = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      // Regular users can only create, read, update their own record
      allow create, read, update: if request.auth != null && request.auth.uid == userId;
      // Admin has full read and write access
      allow read, write: if request.auth != null && (
        request.auth.token.email == 'trusuremedian@gmail.com' ||
        request.auth.token.admin == true
      );
    }
  }
}"""

    // Option 3: Full open rules for development/admin hub
    const val OPEN_FIRESTORE_RULES = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}"""

    const val AUTH_FIRESTORE_RULES = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}"""

    const val COLLECTION_RULES = """rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if true;
    }
  }
}"""
}
