package com.projectx.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.LostFoundRepository
import com.projectx.app.model.LostItem
import com.projectx.app.model.LostItemStatus
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LostFoundViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
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

    private val knownDemoUids = setOf(
        DemoCampusData.DEMO_STUDENT_UID,
        "demo_faculty_uid",
        "demo_staff_uid",
        "demo_collegeadmin_uid",
        "demo_superadmin_uid"
    )

    private fun isDemoSession(uid: String? = null): Boolean {
        val sessionUid = (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid
        return BuildConfig.DEBUG && (sessionUid in knownDemoUids || uid in knownDemoUids)
    }

    private fun getActiveUserUid(): String? {
        return (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadBrowseableItems() {
        viewModelScope.launch {
            _itemsState.value = Resource.Loading
            if (isDemoSession()) {
                val demoList = DemoCampusData.demoLostItems.filter {
                    it.status == LostItemStatus.REPORTED || it.status == LostItemStatus.CLAIM_SUBMITTED
                }
                _itemsState.value = Resource.Success(demoList)
                return@launch
            }
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
            if (isDemoSession(userUid)) {
                val claims = DemoCampusData.demoLostItems.filter { it.claimantUid == userUid }
                _myClaimsState.value = Resource.Success(claims)
                return@launch
            }
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
            if (isDemoSession(userUid)) {
                val reports = DemoCampusData.demoLostItems.filter { it.reporterUid == userUid }
                _myReportsState.value = Resource.Success(reports)
                return@launch
            }
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
            if (isDemoSession(reporterUid)) {
                val newLost = LostItem(
                    itemId = "lost_demo_${System.currentTimeMillis()}",
                    title = title.trim(),
                    description = description.trim(),
                    locationFound = locationFound.trim(),
                    imageUrl = imageUrl?.ifBlank { null },
                    reporterUid = reporterUid,
                    status = LostItemStatus.REPORTED,
                    createdAt = System.currentTimeMillis()
                )
                DemoCampusData.demoLostItems.add(0, newLost)
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyReports(reporterUid)
                return@launch
            }
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
            if (isDemoSession(claimantUid)) {
                val index = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (index != -1) {
                    DemoCampusData.demoLostItems[index] = DemoCampusData.demoLostItems[index].copy(
                        claimantUid = claimantUid,
                        claimNotes = claimNotes.trim(),
                        claimantPhone = claimantPhone.trim(),
                        claimTimestamp = System.currentTimeMillis(),
                        status = LostItemStatus.CLAIM_SUBMITTED
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyClaims(claimantUid)
                return@launch
            }
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

    fun isTransitionAllowed(currentStatus: LostItemStatus, newStatus: LostItemStatus): Boolean {
        if (currentStatus == newStatus) return true
        return when (currentStatus) {
            LostItemStatus.REPORTED -> newStatus == LostItemStatus.CANCELLED
            LostItemStatus.CLAIM_SUBMITTED -> newStatus == LostItemStatus.VERIFIED || newStatus == LostItemStatus.CANCELLED
            LostItemStatus.VERIFIED -> newStatus == LostItemStatus.HANDOVER_COMPLETE || newStatus == LostItemStatus.CANCELLED
            LostItemStatus.HANDOVER_COMPLETE -> false
            LostItemStatus.CANCELLED -> false
        }
    }

    fun rejectClaim(
        itemId: String,
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val staffUid = getActiveUserUid()
            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        status = LostItemStatus.REPORTED,
                        reviewedByUid = staffUid,
                        reviewTimestamp = System.currentTimeMillis(),
                        staffNotes = staffNotes?.ifBlank { DemoCampusData.demoLostItems[idx].staffNotes }
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }
            val activeUid = staffUid ?: run {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }
            try {
                repository.rejectClaim(itemId, activeUid, staffNotes?.ifBlank { null })
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
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val staffUid = getActiveUserUid()
            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        status = LostItemStatus.VERIFIED,
                        verifiedByUid = staffUid,
                        verificationTimestamp = System.currentTimeMillis(),
                        staffNotes = staffNotes?.ifBlank { DemoCampusData.demoLostItems[idx].staffNotes }
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }
            val activeUid = staffUid ?: run {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }
            try {
                repository.verifyClaim(itemId, activeUid, staffNotes?.ifBlank { null })
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to verify claim.")
            }
        }
    }

    fun updateItemStatus(
        itemId: String,
        newStatus: LostItemStatus
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val currentItem = if (isDemoSession()) {
                DemoCampusData.demoLostItems.find { it.itemId == itemId }
            } else {
                (_staffQueueState.value as? Resource.Success)?.data?.find { it.itemId == itemId }
            }

            if (currentItem != null && !isTransitionAllowed(currentItem.status, newStatus)) {
                _actionState.value = Resource.Error("Invalid status transition from ${currentItem.status.label} to ${newStatus.label}.")
                return@launch
            }

            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    val isHandover = newStatus == LostItemStatus.HANDOVER_COMPLETE
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        status = newStatus,
                        handoverTimestamp = if (isHandover) System.currentTimeMillis() else DemoCampusData.demoLostItems[idx].handoverTimestamp
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }

            if (getActiveUserUid() == null) {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }

            try {
                repository.updateItemStatus(itemId, newStatus)
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to update item status.")
            }
        }
    }

    fun completeHandover(
        itemId: String,
        staffNotes: String?
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        status = LostItemStatus.HANDOVER_COMPLETE,
                        handoverTimestamp = System.currentTimeMillis(),
                        staffNotes = staffNotes?.ifBlank { DemoCampusData.demoLostItems[idx].staffNotes }
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }

            if (getActiveUserUid() == null) {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }

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

    fun editItemListing(
        itemId: String,
        title: String,
        locationFound: String,
        description: String
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        title = title.trim(),
                        locationFound = locationFound.trim(),
                        description = description.trim()
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }

            if (getActiveUserUid() == null) {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }

            try {
                repository.updateItemListing(itemId, title.trim(), locationFound.trim(), description.trim())
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to update item listing.")
            }
        }
    }

    fun addStaffNote(
        itemId: String,
        note: String
    ) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val idx = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (idx != -1) {
                    DemoCampusData.demoLostItems[idx] = DemoCampusData.demoLostItems[idx].copy(
                        staffNotes = note.trim()
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
                return@launch
            }

            if (getActiveUserUid() == null) {
                _actionState.value = Resource.Error("Authentication required to perform staff action.")
                return@launch
            }

            try {
                repository.addStaffNote(itemId, note.trim())
                _actionState.value = Resource.Success(Unit)
                loadStaffQueue()
                loadBrowseableItems()
            } catch (e: Exception) {
                _actionState.value = Resource.Error(e.localizedMessage ?: "Failed to add staff note.")
            }
        }
    }

    fun cancelOwnReport(itemId: String, reporterUid: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession(reporterUid)) {
                val index = DemoCampusData.demoLostItems.indexOfFirst { it.itemId == itemId }
                if (index != -1) {
                    DemoCampusData.demoLostItems[index] = DemoCampusData.demoLostItems[index].copy(
                        status = LostItemStatus.CANCELLED
                    )
                }
                _actionState.value = Resource.Success(Unit)
                loadBrowseableItems()
                loadMyReports(reporterUid)
                return@launch
            }
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
