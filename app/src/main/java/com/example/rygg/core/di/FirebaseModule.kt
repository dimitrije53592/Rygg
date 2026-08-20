package com.example.rygg.core.di

import com.example.rygg.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    // The Android emulator reaches the host machine's loopback (where the Firebase Emulator Suite
    // runs) via 10.0.2.2. Ports mirror firebase.json.
    private const val EMULATOR_HOST = "10.0.2.2"
    private const val AUTH_EMULATOR_PORT = 9099
    private const val FIRESTORE_EMULATOR_PORT = 8080
    private const val STORAGE_EMULATOR_PORT = 9199

    // Debug builds with USE_FIREBASE_EMULATOR=true talk to the local suite instead of prod.
    private val useEmulator: Boolean
        get() = BuildConfig.DEBUG && BuildConfig.USE_FIREBASE_EMULATOR

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance().apply {
        if (useEmulator) useEmulator(EMULATOR_HOST, AUTH_EMULATOR_PORT)
    }

    // Offline persistence lets metadata writes queue while offline and flush on reconnect,
    // which is what makes the "metadata eager" sync policy work without custom plumbing.
    // useEmulator must be called before the first Firestore use (and before settings).
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().apply {
        if (useEmulator) useEmulator(EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {})
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance().apply {
        if (useEmulator) useEmulator(EMULATOR_HOST, STORAGE_EMULATOR_PORT)
    }
}
