package com.projectx.app.ui.feedback

import androidx.lifecycle.ViewModel
import com.projectx.app.model.FeedbackCategory
import com.projectx.app.model.FeedbackItem
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory feedback store. No Firestore write — brief says: "Keep submissions in a
 * ViewModel with StateFlow + Resource, in memory only."
 */
class FeedbackViewModel : ViewModel() {

    private val _submissions = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val submissions: StateFlow<List<FeedbackItem>> = _submissions.asStateFlow()

    private val _lastSubmission = MutableStateFlow<Resource<FeedbackItem>>(Resource.Empty)
    val lastSubmission: StateFlow<Resource<FeedbackItem>> = _lastSubmission.asStateFlow()

    fun submit(
        category: FeedbackCategory,
        rating: Int,
        comment: String,
        anonymous: Boolean,
        currentDisplayName: String?,
    ): Boolean {
        val validRating = rating in 1..5
        val trimmed = comment.trim()
        if (!validRating || trimmed.length < 10) {
            _lastSubmission.value = Resource.Error("Please pick a rating and write at least 10 characters.")
            return false
        }
        val item = FeedbackItem(
            id = "fb_${System.currentTimeMillis()}",
            category = category,
            rating = validRating.let { rating },
            comment = trimmed,
            submittedByName = if (anonymous) null else (currentDisplayName?.ifBlank { null } ?: "You"),
            createdAt = System.currentTimeMillis(),
        )
        _submissions.value = listOf(item) + _submissions.value
        _lastSubmission.value = Resource.Success(item)
        return true
    }

    fun clearLastSubmission() {
        _lastSubmission.value = Resource.Empty
    }
}
