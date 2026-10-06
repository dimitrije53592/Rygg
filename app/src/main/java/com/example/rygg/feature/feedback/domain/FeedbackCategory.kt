package com.example.rygg.feature.feedback.domain

import androidx.annotation.StringRes
import com.example.rygg.R

// The enum name travels to Firestore and is validated by firestore.rules — keep the two in sync.
enum class FeedbackCategory(@StringRes val labelRes: Int) {
    BUG(R.string.feedback_category_bug),
    IDEA(R.string.feedback_category_idea),
    OTHER(R.string.feedback_category_other)
}
