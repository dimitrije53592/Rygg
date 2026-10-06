package com.example.rygg.feature.feedback.data

import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.feedback.domain.FeedbackCategory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

// One-way drop box: submissions land in the write-only /feedback collection (see firestore.rules)
// and are read in the Firebase console. Offline-first, like route sync — the document write rides
// Firestore's offline queue rather than being awaited, so a report filed on a mountain still
// arrives once the phone reconnects.
class FeedbackRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val contextProvider: FeedbackContextProvider
) {
    suspend fun submit(category: FeedbackCategory, message: String): Outcome<Unit> = outcomeCatching {
        // The only step that genuinely needs the network, and so the only real failure mode.
        val uid = authRepository.ensureAnyUid()
        val document = buildMap<String, Any> {
            put("uid", uid)
            put("isGuest", !contextProvider.isSignedIn())
            put("category", category.name)
            put("message", message.trim())
            put("createdAt", FieldValue.serverTimestamp())
            putAll(contextProvider.collect())
        }

        // Deliberately not awaited: with persistent cache the task only completes on server ack,
        // which never happens offline. An enqueued write is durable across process death.
        firestore.collection(COLLECTION).add(document)
        Unit
    }

    companion object {
        private const val COLLECTION = "feedback"

        // Mirrors the message length firestore.rules accepts. The write is not awaited, so a
        // rule rejection would be invisible to the user — the cap has to hold before sending.
        const val MAX_MESSAGE_LENGTH = 2000
    }
}
