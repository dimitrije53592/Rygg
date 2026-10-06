package com.example.rygg.feature.feedback.data

import com.example.rygg.core.common.Outcome
import com.example.rygg.core.common.outcomeCatching
import com.example.rygg.feature.auth.data.AuthRepository
import com.example.rygg.feature.feedback.domain.FeedbackCategory
import com.example.rygg.feature.feedback.domain.FeedbackDelivery
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

// One-way drop box: submissions land in the write-only /feedback collection (see firestore.rules)
// and are read in the Firebase console.
class FeedbackRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val contextProvider: FeedbackContextProvider
) {
    suspend fun submit(category: FeedbackCategory, message: String): Outcome<FeedbackDelivery> =
        outcomeCatching {
            val uid = authRepository.ensureAnyUid()
            val document = buildMap<String, Any> {
                put("uid", uid)
                put("isGuest", !contextProvider.isSignedIn())
                put("category", category.name)
                put("message", message.trim())
                put("createdAt", FieldValue.serverTimestamp())
                putAll(contextProvider.collect())
            }

            // Wait for the server's acknowledgement, but only briefly. Awaiting outright would hang
            // forever offline (with a persistent cache the task completes only on server ack), while
            // not awaiting at all would swallow a rules rejection and lose the report in silence.
            // A timeout is the honest middle: confirmed, or queued and durable across process death.
            val acknowledged = withTimeoutOrNull(ACK_TIMEOUT_MS) {
                firestore.collection(COLLECTION).add(document).await()
            }

            if (acknowledged != null) FeedbackDelivery.CONFIRMED else FeedbackDelivery.QUEUED
        }

    companion object {
        private const val COLLECTION = "feedback"
        private const val ACK_TIMEOUT_MS = 5_000L

        // Mirrors the message length firestore.rules accepts, so a report is never rejected for
        // length after the user was told it sent.
        const val MAX_MESSAGE_LENGTH = 2000
    }
}
