package com.example.projectx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectx.data.firestore.LostFoundRepository
import com.example.projectx.model.LostItem
import com.example.projectx.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LostFoundViewModel(
    private val repository: LostFoundRepository = LostFoundRepository()
) : ViewModel() {

    private val _itemsState = MutableStateFlow<Resource<List<LostItem>>>(Resource.Loading)
    val itemsState: StateFlow<Resource<List<LostItem>>> = _itemsState.asStateFlow()

    private val _myClaimsState = MutableStateFlow<Resource<List<LostItem>>>(Resource.Loading)
    val myClaimsState: StateFlow<Resource<List<LostItem>>> = _myClaimsState.asStateFlow()

    private val _myReportsState = MutableStateFlow<Resource<List<LostItem>>>(Resource.Loading)
    val myReportsState: StateFlow<Resource<List<LostItem>>> = _myReportsState.asStateFlow()

    private val _staffQueueState = MutableStateFlow<Resource<List<LostItem>>>(Resource.Loading)
    val staffQueueState: StateFlow<Resource<List<LostItem>>> = _staffQueueState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadBrowseableItems()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadBrowseableItems() {
        viewModelScope.launch {
            _itemsState.value = Resource.Loading
            try {
                val list = repository.getBrowseableItems()
                _itemsState.value = if (list.isEmpty()) {
                    Resource.Empty
                } else {
                    Resource.Success(list)
                }
            } catch (e: Exception) {
                _itemsState.value = Resource.Error(e.localizedMessage ?: "Failed to load Lost & Found items.")
            }
        }
    }

    fun loadMyClaims(userUid: String) {
        if (userUid.isBlank()) {
            _myClaimsState.value = Resource.Empty
            return
        }
        viewModelScope.launch {
            _myClaimsState.value = Resource.Loading
            try {
                val list = repository.getMyClaims(userUid)
                _myClaimsState.value = if (list.isEmpty()) {
                    Resource.Empty
                } else {
                    Resource.Success(list)
                }
            } catch (e: Exception) {
                _myClaimsState.value = Resource.Error(e.localizedMessage ?: "Failed to load your claims.")
            }
        }
    }

    fun loadMyReports(userUid: String) {
        if (userUid.isBlank()) {
            _myReportsState.value = Resource.Empty
            return
        }
        viewModelScope.launch {
            _myReportsState.value = Resource.Loading
            try {
                val list = repository.getMyReports(userUid)
                _myReportsState.value = if (list.isEmpty()) {
                    Resource.Empty
                } else {
                    Resource.Success(list)
                }
            } catch (e: Exception) {
                _myReportsState.value = Resource.Error(e.localizedMessage ?: "Failed to load your reported items.")
            }
        }
    }

    fun loadStaffQueue() {
        viewModelScope.launch {
            _staffQueueState.value = Resource.Loading
            try {
                val list = repository.getStaffQueue()
                _staffQueueState.value = if (list.isEmpty()) {
                    Resource.Empty
                } else {
                    Resource.Success(list)
                }
            } catch (e: Exception) {
                _staffQueueState.value = Resource.Error(e.localizedMessage ?: "Failed to load staff queue.")
            }
        }
    }

    fun reportFoundItem(
        title: String,
        description: String,
        locationFound: String,
        imageUrl: String?,
        reporterUid: String
    ) {
        if (title.isBlank() || description.isBlank() || locationFound.isBlank() || reporterUid.isBlank()) {
            _actionState.value = Resource.Error("Please fill in all required fields.")
            return
        }

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.reportFoundItem(
                    title = title.trim(),
                    description = description.trim(),
                    locationFound = locationFound.trim(),
                    imageUrl = imageUrl?.ifBlank { null },
                    reporterUid = reporterUid
                )
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyReports(reporterUid)
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to report item.")
            }
        }
    }

    fun submitClaim(
        itemId: String,
        claimantUid: String,
        claimNotes: String,
        claimantPhone: String
    ) {
        if (itemId.isBlank() || claimantUid.isBlank() || claimNotes.isBlank() || claimantPhone.isBlank()) {
            _actionState.value = Resource.Error("Please provide claim notes and contact phone number.")
            return
        }

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.submitClaim(
                    itemId = itemId,
                    claimantUid = claimantUid,
                    claimNotes = claimNotes.trim(),
                    claimantPhone = claimantPhone.trim()
                )
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyClaims(claimantUid)
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to submit claim.")
            }
        }
    }

    fun rejectClaim(
        itemId: String,
        staffUid: String,
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.rejectClaim(itemId, staffUid, staffNotes?.ifBlank { null })
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to reject claim.")
            }
        }
    }

    fun verifyClaim(
        itemId: String,
        staffUid: String,
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.verifyClaim(itemId, staffUid, staffNotes?.ifBlank { null })
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to verify claim.")
            }
        }
    }

    fun completeHandover(
        itemId: String,
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.completeHandover(itemId, staffNotes?.ifBlank { null })
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to complete handover.")
            }
        }
    }

    fun cancelOwnReport(itemId: String, reporterUid: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            try {
                repository.cancelOwnReport(itemId)
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyReports(reporterUid)
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to cancel report.")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Success(Unit)
    }
}
