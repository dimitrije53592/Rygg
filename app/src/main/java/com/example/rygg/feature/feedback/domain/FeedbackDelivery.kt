package com.example.rygg.feature.feedback.domain

// Whether the server acknowledged the submission, or it is still sitting in Firestore's offline
// queue. Both mean the report is safe; only the wording the user sees differs.
enum class FeedbackDelivery {
    CONFIRMED,
    QUEUED
}
