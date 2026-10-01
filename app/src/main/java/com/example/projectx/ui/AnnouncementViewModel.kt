package com.projectx.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.AnnouncementRepository
import com.projectx.app.model.UniversityAnnouncement
import com.projectx.app.model.lms.AnnouncementPriority
import com.projectx.app.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnnouncementViewModel(
    private val repository: AnnouncementRepository = AnnouncementRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _announcementsState = MutableStateFlow<Resource<List<UniversityAnnouncement>>>(Resource.Loading)
    val announcementsState: StateFlow<Resource<List<UniversityAnnouncement>>> = _announcementsState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadAnnouncements()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            _announcementsState.value = Resource.Loading
            if (BuildConfig.DEBUG) {
                val currentUid = firebaseAuth.currentUser?.uid ?: DemoCampusData.DEMO_STUDENT_UID
                if (currentUid == DemoCampusData.DEMO_STUDENT_UID) {
                    _announcementsState.value = Resource.Success(DemoCampusData.demoUniversityAnnouncements)
                    return@launch
                }
            }
            repository.getAnnouncements()
                .onSuccess { list ->
                    _announcementsState.value = if (list.isEmpty()) {
                        Resource.Empty
                    } else {
                        Resource.Success(list)
                    }
                }
                .onFailure { error ->
                    _announcementsState.value = Resource.Error(error.localizedMessage ?: "Failed to load announcements.")
                }
        }
    }

    fun createAnnouncement(
        title: String,
        content: String,
        targetDepartment: String,
        priority: AnnouncementPriority
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            repository.createAnnouncement(title, content, targetDepartment, priority)
                .onSuccess {
                    _actionState.value = Resource.Success(Unit)
                    loadAnnouncements()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(error.localizedMessage ?: "Failed to publish announcement.")
                }
        }
    }

    fun updateAnnouncement(
        id: String,
        title: String,
        content: String,
        targetDepartment: String,
        priority: AnnouncementPriority
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            repository.updateAnnouncement(id, title, content, targetDepartment, priority)
                .onSuccess {
                    _actionState.value = Resource.Success(Unit)
                    loadAnnouncements()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(error.localizedMessage ?: "Failed to update announcement.")
                }
        }
    }

    fun deleteAnnouncement(id: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            repository.deleteAnnouncement(id)
                .onSuccess {
                    _actionState.value = Resource.Success(Unit)
                    loadAnnouncements()
                }
                .onFailure { error ->
                    _actionState.value = Resource.Error(error.localizedMessage ?: "Failed to delete announcement.")
                }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Success(Unit)
    }
}
