package com.projectx.app.data.firestore

import com.projectx.app.model.career.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CareerRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // --- CERTIFICATIONS ---
    suspend fun getCertifications(uid: String): Result<List<Certification>> {
        if (uid.isBlank()) return Result.success(emptyList())
        return try {
            val snapshot = firestore.collection("career_certifications")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val list = snapshot.documents.map { it.toCertificationStrict() }
                .sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCertification(certification: Certification): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated. Must be logged in to save certifications."))

        if (certification.name.isBlank() || certification.issuingOrganization.isBlank()) {
            return Result.failure(IllegalArgumentException("Certification name and issuing organization are required."))
        }

        return try {
            val docRef = firestore.collection("career_certifications").document()
            val data = mapOf(
                "certificationId" to docRef.id,
                "ownerUid" to currentUid,
                "name" to certification.name.trim(),
                "issuingOrganization" to certification.issuingOrganization.trim(),
                "issueDate" to certification.issueDate.trim(),
                "expiryDate" to certification.expiryDate?.trim()?.ifBlank { null },
                "credentialId" to certification.credentialId?.trim()?.ifBlank { null },
                "credentialUrl" to certification.credentialUrl?.trim()?.ifBlank { null },
                "category" to certification.category.ifBlank { "General" },
                "skills" to certification.skills,
                "description" to certification.description.trim(),
                "certificateStoragePath" to certification.certificateStoragePath?.trim()?.ifBlank { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCertification(certification: Certification): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        if (certification.certificationId.isBlank() || certification.ownerUid != currentUid) {
            return Result.failure(IllegalStateException("Unauthorized modification of certification."))
        }

        return try {
            val updates = mapOf(
                "name" to certification.name.trim(),
                "issuingOrganization" to certification.issuingOrganization.trim(),
                "issueDate" to certification.issueDate.trim(),
                "expiryDate" to certification.expiryDate?.trim()?.ifBlank { null },
                "credentialId" to certification.credentialId?.trim()?.ifBlank { null },
                "credentialUrl" to certification.credentialUrl?.trim()?.ifBlank { null },
                "category" to certification.category.ifBlank { "General" },
                "skills" to certification.skills,
                "description" to certification.description.trim(),
                "certificateStoragePath" to certification.certificateStoragePath?.trim()?.ifBlank { null },
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection("career_certifications").document(certification.certificationId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCertification(certificationId: String): Result<Unit> {
        if (certificationId.isBlank()) return Result.failure(IllegalArgumentException("ID required."))
        return try {
            firestore.collection("career_certifications").document(certificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PROJECTS ---
    suspend fun getProjects(uid: String): Result<List<ProjectItem>> {
        if (uid.isBlank()) return Result.success(emptyList())
        return try {
            val snapshot = firestore.collection("career_projects")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val list = snapshot.documents.map { it.toProjectItemStrict() }
                .sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProject(project: ProjectItem): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        if (project.projectName.isBlank() || project.description.isBlank()) {
            return Result.failure(IllegalArgumentException("Project name and description are required."))
        }

        return try {
            val docRef = firestore.collection("career_projects").document()
            val data = mapOf(
                "projectId" to docRef.id,
                "ownerUid" to currentUid,
                "projectName" to project.projectName.trim(),
                "description" to project.description.trim(),
                "technologies" to project.technologies,
                "skills" to project.skills,
                "projectUrl" to project.projectUrl?.trim()?.ifBlank { null },
                "githubUrl" to project.githubUrl?.trim()?.ifBlank { null },
                "startDate" to project.startDate?.trim()?.ifBlank { null },
                "endDate" to project.endDate?.trim()?.ifBlank { null },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        if (projectId.isBlank()) return Result.failure(IllegalArgumentException("ID required."))
        return try {
            firestore.collection("career_projects").document(projectId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- SKILLS ---
    suspend fun getSkills(uid: String): Result<List<SkillItem>> {
        if (uid.isBlank()) return Result.success(emptyList())
        return try {
            val snapshot = firestore.collection("career_skills")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val list = snapshot.documents.map { it.toSkillItemStrict() }
                .sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSkill(skill: SkillItem): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        if (skill.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Skill name cannot be blank."))
        }

        return try {
            val docRef = firestore.collection("career_skills").document()
            val data = mapOf(
                "skillId" to docRef.id,
                "ownerUid" to currentUid,
                "name" to skill.name.trim(),
                "category" to skill.category.ifBlank { "General" },
                "proficiency" to (skill.proficiency ?: "Intermediate"),
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSkill(skillId: String): Result<Unit> {
        if (skillId.isBlank()) return Result.failure(IllegalArgumentException("ID required."))
        return try {
            firestore.collection("career_skills").document(skillId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ACHIEVEMENTS ---
    suspend fun getAchievements(uid: String): Result<List<Achievement>> {
        if (uid.isBlank()) return Result.success(emptyList())
        return try {
            val snapshot = firestore.collection("career_achievements")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val list = snapshot.documents.map { it.toAchievementStrict() }
                .sortedByDescending { it.createdAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAchievement(achievement: Achievement): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        if (achievement.title.isBlank() || achievement.description.isBlank()) {
            return Result.failure(IllegalArgumentException("Title and description are required."))
        }

        return try {
            val docRef = firestore.collection("career_achievements").document()
            val data = mapOf(
                "achievementId" to docRef.id,
                "ownerUid" to currentUid,
                "title" to achievement.title.trim(),
                "description" to achievement.description.trim(),
                "date" to achievement.date.trim(),
                "issuingOrganization" to achievement.issuingOrganization.trim(),
                "documentStoragePath" to achievement.documentStoragePath?.trim()?.ifBlank { null },
                "createdAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAchievement(achievementId: String): Result<Unit> {
        if (achievementId.isBlank()) return Result.failure(IllegalArgumentException("ID required."))
        return try {
            firestore.collection("career_achievements").document(achievementId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PORTFOLIO VAULT ---
    suspend fun getPortfolioFiles(uid: String): Result<List<PortfolioFile>> {
        if (uid.isBlank()) return Result.success(emptyList())
        return try {
            val snapshot = firestore.collection("career_portfolio_vault")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val list = snapshot.documents.map { it.toPortfolioFileStrict() }
                .sortedByDescending { it.uploadedAt }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPortfolioFileMetadata(file: PortfolioFile): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        if (file.fileName.isBlank() || file.storagePath.isBlank()) {
            return Result.failure(IllegalArgumentException("File name and storage path are required."))
        }

        return try {
            val docRef = firestore.collection("career_portfolio_vault").document()
            val data = mapOf(
                "fileId" to docRef.id,
                "ownerUid" to currentUid,
                "fileName" to file.fileName.trim(),
                "fileType" to file.fileType.ifBlank { "PDF" },
                "category" to file.category.ifBlank { "General" },
                "storagePath" to file.storagePath.trim(),
                "fileSizeBytes" to file.fileSizeBytes,
                "uploadedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePortfolioFileMetadata(fileId: String): Result<Unit> {
        if (fileId.isBlank()) return Result.failure(IllegalArgumentException("ID required."))
        return try {
            firestore.collection("career_portfolio_vault").document(fileId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CV PROFILES ---
    suspend fun getCvProfile(uid: String): Result<CvProfile?> {
        if (uid.isBlank()) return Result.success(null)
        return try {
            val snapshot = firestore.collection("career_cv_profiles")
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val firstDoc = snapshot.documents.firstOrNull()
            if (firstDoc == null) {
                Result.success(null)
            } else {
                Result.success(firstDoc.toCvProfileStrict())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCvProfile(cvProfile: CvProfile): Result<Unit> {
        val currentUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Unauthenticated."))

        // Strict Owner Reference Verification: Fetch owned items and FAIL if any requested ID is unowned or fetch fails
        val certsResult = getCertifications(currentUid)
        val projectsResult = getProjects(currentUid)
        val skillsResult = getSkills(currentUid)
        val achievementsResult = getAchievements(currentUid)

        if (certsResult.isFailure) return Result.failure(certsResult.exceptionOrNull()!!)
        if (projectsResult.isFailure) return Result.failure(projectsResult.exceptionOrNull()!!)
        if (skillsResult.isFailure) return Result.failure(skillsResult.exceptionOrNull()!!)
        if (achievementsResult.isFailure) return Result.failure(achievementsResult.exceptionOrNull()!!)

        val ownedCertIds = certsResult.getOrThrow().map { it.certificationId }.toSet()
        val ownedProjIds = projectsResult.getOrThrow().map { it.projectId }.toSet()
        val ownedSkillIds = skillsResult.getOrThrow().map { it.skillId }.toSet()
        val ownedAchieveIds = achievementsResult.getOrThrow().map { it.achievementId }.toSet()

        val unownedCerts = cvProfile.selectedCertificationIds.filter { !ownedCertIds.contains(it) }
        val unownedProjs = cvProfile.selectedProjectIds.filter { !ownedProjIds.contains(it) }
        val unownedSkills = cvProfile.selectedSkillIds.filter { !ownedSkillIds.contains(it) }
        val unownedAchieves = cvProfile.selectedAchievementIds.filter { !ownedAchieveIds.contains(it) }

        if (unownedCerts.isNotEmpty() || unownedProjs.isNotEmpty() || unownedSkills.isNotEmpty() || unownedAchieves.isNotEmpty()) {
            return Result.failure(IllegalArgumentException("CV selection contains references to career items that do not belong to the current user."))
        }

        return try {
            val docId = if (cvProfile.cvId.isNotBlank()) cvProfile.cvId else firestore.collection("career_cv_profiles").document().id
            val docRef = firestore.collection("career_cv_profiles").document(docId)

            val data = mapOf(
                "cvId" to docId,
                "ownerUid" to currentUid,
                "title" to cvProfile.title.ifBlank { "Master CV" },
                "selectedCertificationIds" to cvProfile.selectedCertificationIds,
                "selectedProjectIds" to cvProfile.selectedProjectIds,
                "selectedSkillIds" to cvProfile.selectedSkillIds,
                "selectedAchievementIds" to cvProfile.selectedAchievementIds,
                "personalSummary" to cvProfile.personalSummary.trim(),
                "targetRole" to cvProfile.targetRole.trim().ifBlank { "Software Engineer" },
                "selectedTemplate" to cvProfile.selectedTemplate.ifBlank { "Classic" },
                "updatedAt" to FieldValue.serverTimestamp()
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- STRICT DOCUMENT MAPPERS (No fabricated timestamps or fallbacks) ---
    private fun DocumentSnapshot.toCertificationStrict(): Certification {
        if (!exists()) throw IllegalStateException("Document /career_certifications/$id does not exist")
        return Certification(
            certificationId = getString("certificationId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            name = getString("name") ?: throw IllegalStateException("Missing required field 'name'"),
            issuingOrganization = getString("issuingOrganization") ?: throw IllegalStateException("Missing required field 'issuingOrganization'"),
            issueDate = getString("issueDate") ?: "",
            expiryDate = getString("expiryDate"),
            credentialId = getString("credentialId"),
            credentialUrl = getString("credentialUrl"),
            category = getString("category") ?: "General",
            skills = (get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            description = getString("description") ?: "",
            certificateStoragePath = getString("certificateStoragePath"),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'createdAt'"),
            updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'updatedAt'")
        )
    }

    private fun DocumentSnapshot.toProjectItemStrict(): ProjectItem {
        if (!exists()) throw IllegalStateException("Document /career_projects/$id does not exist")
        return ProjectItem(
            projectId = getString("projectId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            projectName = getString("projectName") ?: throw IllegalStateException("Missing required field 'projectName'"),
            description = getString("description") ?: throw IllegalStateException("Missing required field 'description'"),
            technologies = (get("technologies") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            skills = (get("skills") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            projectUrl = getString("projectUrl"),
            githubUrl = getString("githubUrl"),
            startDate = getString("startDate"),
            endDate = getString("endDate"),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'createdAt'"),
            updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'updatedAt'")
        )
    }

    private fun DocumentSnapshot.toSkillItemStrict(): SkillItem {
        if (!exists()) throw IllegalStateException("Document /career_skills/$id does not exist")
        return SkillItem(
            skillId = getString("skillId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            name = getString("name") ?: throw IllegalStateException("Missing required field 'name'"),
            category = getString("category") ?: "General",
            proficiency = getString("proficiency") ?: "Intermediate",
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'createdAt'")
        )
    }

    private fun DocumentSnapshot.toAchievementStrict(): Achievement {
        if (!exists()) throw IllegalStateException("Document /career_achievements/$id does not exist")
        return Achievement(
            achievementId = getString("achievementId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            title = getString("title") ?: throw IllegalStateException("Missing required field 'title'"),
            description = getString("description") ?: throw IllegalStateException("Missing required field 'description'"),
            date = getString("date") ?: "",
            issuingOrganization = getString("issuingOrganization") ?: "",
            documentStoragePath = getString("documentStoragePath"),
            createdAt = getTimestamp("createdAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'createdAt'")
        )
    }

    private fun DocumentSnapshot.toPortfolioFileStrict(): PortfolioFile {
        if (!exists()) throw IllegalStateException("Document /career_portfolio_vault/$id does not exist")
        return PortfolioFile(
            fileId = getString("fileId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            fileName = getString("fileName") ?: throw IllegalStateException("Missing required field 'fileName'"),
            fileType = getString("fileType") ?: "PDF",
            category = getString("category") ?: "General",
            storagePath = getString("storagePath") ?: throw IllegalStateException("Missing required field 'storagePath'"),
            fileSizeBytes = getLong("fileSizeBytes") ?: 0L,
            uploadedAt = getTimestamp("uploadedAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'uploadedAt'")
        )
    }

    private fun DocumentSnapshot.toCvProfileStrict(): CvProfile {
        if (!exists()) throw IllegalStateException("Document /career_cv_profiles/$id does not exist")
        return CvProfile(
            cvId = getString("cvId") ?: id,
            ownerUid = getString("ownerUid") ?: throw IllegalStateException("Missing required field 'ownerUid'"),
            title = getString("title") ?: "Master CV",
            selectedCertificationIds = (get("selectedCertificationIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            selectedProjectIds = (get("selectedProjectIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            selectedSkillIds = (get("selectedSkillIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            selectedAchievementIds = (get("selectedAchievementIds") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
            personalSummary = getString("personalSummary") ?: "",
            targetRole = getString("targetRole") ?: "Software Engineer",
            selectedTemplate = getString("selectedTemplate") ?: "Classic",
            updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: throw IllegalStateException("Missing required timestamp 'updatedAt'")
        )
    }
}
