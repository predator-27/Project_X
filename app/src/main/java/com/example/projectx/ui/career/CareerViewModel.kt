package com.projectx.app.ui.career

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projectx.app.BuildConfig
import com.projectx.app.data.auth.AuthRepository
import com.projectx.app.data.demo.DemoCampusData
import com.projectx.app.data.firestore.CareerRepository
import com.projectx.app.data.storage.CareerStorageRepository
import com.projectx.app.model.career.*
import com.projectx.app.ui.auth.AuthSessionState
import com.projectx.app.util.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CareerViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val careerRepository: CareerRepository = CareerRepository(),
    private val storageRepository: CareerStorageRepository = CareerStorageRepository(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _certificationsState = MutableStateFlow<Resource<List<Certification>>>(Resource.Loading)
    val certificationsState: StateFlow<Resource<List<Certification>>> = _certificationsState.asStateFlow()

    private val _projectsState = MutableStateFlow<Resource<List<ProjectItem>>>(Resource.Loading)
    val projectsState: StateFlow<Resource<List<ProjectItem>>> = _projectsState.asStateFlow()

    private val _skillsState = MutableStateFlow<Resource<List<SkillItem>>>(Resource.Loading)
    val skillsState: StateFlow<Resource<List<SkillItem>>> = _skillsState.asStateFlow()

    private val _achievementsState = MutableStateFlow<Resource<List<Achievement>>>(Resource.Loading)
    val achievementsState: StateFlow<Resource<List<Achievement>>> = _achievementsState.asStateFlow()

    private val _portfolioFilesState = MutableStateFlow<Resource<List<PortfolioFile>>>(Resource.Loading)
    val portfolioFilesState: StateFlow<Resource<List<PortfolioFile>>> = _portfolioFilesState.asStateFlow()

    private val _cvProfileState = MutableStateFlow<Resource<CvProfile?>>(Resource.Loading)
    val cvProfileState: StateFlow<Resource<CvProfile?>> = _cvProfileState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    init {
        loadAllCareerData()
    }

    private fun isDemoSession(): Boolean {
        val sessionUid = (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid
        return BuildConfig.DEBUG && sessionUid == DemoCampusData.DEMO_STUDENT_UID
    }

    private fun getNextDemoCertId(): String {
        val maxIndex = DemoCampusData.demoCertifications
            .mapNotNull { it.certificationId.removePrefix("cert_demo_").toIntOrNull() }
            .maxOrNull() ?: 0
        return "cert_demo_${maxIndex + 1}"
    }

    private fun getNextDemoProjId(): String {
        val maxIndex = DemoCampusData.demoProjects
            .mapNotNull { it.projectId.removePrefix("proj_demo_").toIntOrNull() }
            .maxOrNull() ?: 0
        return "proj_demo_${maxIndex + 1}"
    }

    private fun getNextDemoSkillId(): String {
        val maxIndex = DemoCampusData.demoSkills
            .mapNotNull { it.skillId.removePrefix("skill_demo_").toIntOrNull() }
            .maxOrNull() ?: 0
        return "skill_demo_${maxIndex + 1}"
    }

    private fun getNextDemoAchievementId(): String {
        val maxIndex = DemoCampusData.demoAchievements
            .mapNotNull { it.achievementId.removePrefix("achieve_demo_").toIntOrNull() }
            .maxOrNull() ?: 0
        return "achieve_demo_${maxIndex + 1}"
    }

    private fun getNextDemoVaultId(): String {
        val maxIndex = DemoCampusData.demoPortfolioFiles
            .mapNotNull { it.fileId.removePrefix("vault_demo_").toIntOrNull() }
            .maxOrNull() ?: 0
        return "vault_demo_${maxIndex + 1}"
    }

    fun loadAllCareerData() {
        val currentUid = getCurrentUid()
        if (currentUid.isBlank()) return

        loadCertifications(currentUid)
        loadProjects(currentUid)
        loadSkills(currentUid)
        loadAchievements(currentUid)
        loadPortfolioFiles(currentUid)
        loadCvProfile(currentUid)
    }

    private fun getCurrentUid(): String {
        return (authRepository.sessionState.value as? AuthSessionState.Authenticated)?.user?.uid
            ?: firebaseAuth.currentUser?.uid
            ?: ""
    }

    // --- CERTIFICATIONS ---
    fun loadCertifications(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _certificationsState.value = Resource.Loading
            if (isDemoSession()) {
                _certificationsState.value = Resource.Success(DemoCampusData.demoCertifications.toList())
                return@launch
            }
            careerRepository.getCertifications(uid).fold(
                onSuccess = { list -> _certificationsState.value = Resource.Success(list) },
                onFailure = { error -> _certificationsState.value = Resource.Error(error.message ?: "Failed to load certifications.") }
            )
        }
    }

    fun addCertification(certification: Certification) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val newCert = certification.copy(
                    certificationId = getNextDemoCertId(),
                    ownerUid = DemoCampusData.DEMO_STUDENT_UID,
                    createdAt = 1789900800000L,
                    updatedAt = 1789900800000L
                )
                DemoCampusData.demoCertifications.add(0, newCert)
                _actionState.value = Resource.Success(Unit)
                loadCertifications(uid)
                return@launch
            }
            careerRepository.createCertification(certification).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadCertifications(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save certification.") }
            )
        }
    }

    fun deleteCertification(certId: String) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoCertifications.removeAll { it.certificationId == certId }
                _actionState.value = Resource.Success(Unit)
                loadCertifications(uid)
                return@launch
            }
            careerRepository.deleteCertification(certId).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadCertifications(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to delete certification.") }
            )
        }
    }

    // --- PROJECTS ---
    fun loadProjects(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _projectsState.value = Resource.Loading
            if (isDemoSession()) {
                _projectsState.value = Resource.Success(DemoCampusData.demoProjects.toList())
                return@launch
            }
            careerRepository.getProjects(uid).fold(
                onSuccess = { list -> _projectsState.value = Resource.Success(list) },
                onFailure = { error -> _projectsState.value = Resource.Error(error.message ?: "Failed to load projects.") }
            )
        }
    }

    fun addProject(project: ProjectItem) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val newProj = project.copy(
                    projectId = getNextDemoProjId(),
                    ownerUid = DemoCampusData.DEMO_STUDENT_UID,
                    createdAt = 1789900800000L,
                    updatedAt = 1789900800000L
                )
                DemoCampusData.demoProjects.add(0, newProj)
                _actionState.value = Resource.Success(Unit)
                loadProjects(uid)
                return@launch
            }
            careerRepository.createProject(project).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadProjects(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save project.") }
            )
        }
    }

    fun deleteProject(projectId: String) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoProjects.removeAll { it.projectId == projectId }
                _actionState.value = Resource.Success(Unit)
                loadProjects(uid)
                return@launch
            }
            careerRepository.deleteProject(projectId).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadProjects(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to delete project.") }
            )
        }
    }

    // --- SKILLS ---
    fun loadSkills(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _skillsState.value = Resource.Loading
            if (isDemoSession()) {
                _skillsState.value = Resource.Success(DemoCampusData.demoSkills.toList())
                return@launch
            }
            careerRepository.getSkills(uid).fold(
                onSuccess = { list -> _skillsState.value = Resource.Success(list) },
                onFailure = { error -> _skillsState.value = Resource.Error(error.message ?: "Failed to load skills.") }
            )
        }
    }

    fun addSkill(skill: SkillItem) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val newSkill = skill.copy(
                    skillId = getNextDemoSkillId(),
                    ownerUid = DemoCampusData.DEMO_STUDENT_UID,
                    createdAt = 1789900800000L
                )
                DemoCampusData.demoSkills.add(0, newSkill)
                _actionState.value = Resource.Success(Unit)
                loadSkills(uid)
                return@launch
            }
            careerRepository.createSkill(skill).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadSkills(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save skill.") }
            )
        }
    }

    fun deleteSkill(skillId: String) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoSkills.removeAll { it.skillId == skillId }
                _actionState.value = Resource.Success(Unit)
                loadSkills(uid)
                return@launch
            }
            careerRepository.deleteSkill(skillId).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadSkills(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to delete skill.") }
            )
        }
    }

    // --- ACHIEVEMENTS ---
    fun loadAchievements(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _achievementsState.value = Resource.Loading
            if (isDemoSession()) {
                _achievementsState.value = Resource.Success(DemoCampusData.demoAchievements.toList())
                return@launch
            }
            careerRepository.getAchievements(uid).fold(
                onSuccess = { list -> _achievementsState.value = Resource.Success(list) },
                onFailure = { error -> _achievementsState.value = Resource.Error(error.message ?: "Failed to load achievements.") }
            )
        }
    }

    fun addAchievement(achievement: Achievement) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val newAch = achievement.copy(
                    achievementId = getNextDemoAchievementId(),
                    ownerUid = DemoCampusData.DEMO_STUDENT_UID,
                    createdAt = 1789900800000L
                )
                DemoCampusData.demoAchievements.add(0, newAch)
                _actionState.value = Resource.Success(Unit)
                loadAchievements(uid)
                return@launch
            }
            careerRepository.createAchievement(achievement).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadAchievements(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save achievement.") }
            )
        }
    }

    fun deleteAchievement(achievementId: String) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoAchievements.removeAll { it.achievementId == achievementId }
                _actionState.value = Resource.Success(Unit)
                loadAchievements(uid)
                return@launch
            }
            careerRepository.deleteAchievement(achievementId).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadAchievements(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to delete achievement.") }
            )
        }
    }

    // --- PORTFOLIO VAULT ---
    fun loadPortfolioFiles(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _portfolioFilesState.value = Resource.Loading
            if (isDemoSession()) {
                _portfolioFilesState.value = Resource.Success(DemoCampusData.demoPortfolioFiles.toList())
                return@launch
            }
            careerRepository.getPortfolioFiles(uid).fold(
                onSuccess = { list -> _portfolioFilesState.value = Resource.Success(list) },
                onFailure = { error -> _portfolioFilesState.value = Resource.Error(error.message ?: "Failed to load portfolio vault.") }
            )
        }
    }

    fun uploadVaultFile(
        context: Context,
        selectedUri: Uri,
        category: String
    ) {
        val uid = getCurrentUid()
        if (uid.isBlank()) return

        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                val demoFile = PortfolioFile(
                    fileId = getNextDemoVaultId(),
                    ownerUid = DemoCampusData.DEMO_STUDENT_UID,
                    fileName = selectedUri.lastPathSegment ?: "demo_document.pdf",
                    fileType = "PDF",
                    category = category,
                    storagePath = "career/demo_student_uid/vault/demo_document.pdf",
                    fileSizeBytes = 1048576L,
                    uploadedAt = 1789900800000L
                )
                DemoCampusData.demoPortfolioFiles.add(0, demoFile)
                _actionState.value = Resource.Success(Unit)
                loadPortfolioFiles(uid)
                return@launch
            }

            try {
                val contentResolver = context.contentResolver
                var fileName = "document"
                var sizeBytes = 0L

                contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: "document"
                        if (sizeIdx != -1) sizeBytes = cursor.getLong(sizeIdx)
                    }
                }

                val ext = fileName.substringAfterLast('.', "pdf")
                val mime = contentResolver.getType(selectedUri) ?: "application/pdf"
                val fileBytes = contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }

                if (fileBytes == null || fileBytes.isEmpty()) {
                    _actionState.value = Resource.Error("Selected file is empty.")
                    return@launch
                }

                val uploadResult = storageRepository.uploadCareerFile(
                    uid = uid,
                    subPath = "vault",
                    fileName = fileName,
                    extension = ext,
                    contentType = mime,
                    fileBytes = fileBytes
                )

                if (uploadResult.isFailure) {
                    _actionState.value = Resource.Error(uploadResult.exceptionOrNull()?.message ?: "Upload failed.")
                    return@launch
                }

                val path = uploadResult.getOrThrow()
                val metadata = PortfolioFile(
                    ownerUid = uid,
                    fileName = fileName,
                    fileType = ext.uppercase(),
                    category = category,
                    storagePath = path,
                    fileSizeBytes = sizeBytes
                )

                careerRepository.createPortfolioFileMetadata(metadata).fold(
                    onSuccess = {
                        _actionState.value = Resource.Success(Unit)
                        loadPortfolioFiles(uid)
                    },
                    onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save file metadata.") }
                )
            } catch (e: Exception) {
                _actionState.value = Resource.Error("File processing failed: ${e.message}")
            }
        }
    }

    fun deletePortfolioFile(fileId: String, storagePath: String) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoPortfolioFiles.removeAll { it.fileId == fileId }
                _actionState.value = Resource.Success(Unit)
                loadPortfolioFiles(uid)
                return@launch
            }

            storageRepository.deleteCareerFile(storagePath)
            careerRepository.deletePortfolioFileMetadata(fileId).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadPortfolioFiles(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to delete file.") }
            )
        }
    }

    // --- CV PROFILE ---
    fun loadCvProfile(uid: String = getCurrentUid()) {
        viewModelScope.launch {
            _cvProfileState.value = Resource.Loading
            if (isDemoSession()) {
                _cvProfileState.value = Resource.Success(DemoCampusData.demoCvProfile)
                return@launch
            }
            careerRepository.getCvProfile(uid).fold(
                onSuccess = { cv -> _cvProfileState.value = Resource.Success(cv) },
                onFailure = { error -> _cvProfileState.value = Resource.Error(error.message ?: "Failed to load CV profile.") }
            )
        }
    }

    fun saveCvProfile(cvProfile: CvProfile) {
        val uid = getCurrentUid()
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            if (isDemoSession()) {
                DemoCampusData.demoCvProfile = cvProfile
                _cvProfileState.value = Resource.Success(cvProfile)
                _actionState.value = Resource.Success(Unit)
                return@launch
            }
            careerRepository.saveCvProfile(cvProfile).fold(
                onSuccess = {
                    _actionState.value = Resource.Success(Unit)
                    loadCvProfile(uid)
                },
                onFailure = { error -> _actionState.value = Resource.Error(error.message ?: "Failed to save CV profile.") }
            )
        }
    }

    fun resetActionState() {
        _actionState.value = Resource.Empty
    }
}
