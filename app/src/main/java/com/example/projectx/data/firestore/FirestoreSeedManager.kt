package com.projectx.app.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.projectx.app.data.demo.DemoCampusData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSeedManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun seedFirestoreDatabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            var seededCount = 0

            // 1. Seed Faculty Profiles (/faculty_profiles)
            DemoCampusData.demoFaculty.forEach { teacher ->
                val teacherMap = mapOf(
                    "id" to teacher.id,
                    "name" to teacher.name,
                    "title" to teacher.title,
                    "department" to teacher.department,
                    "deskNumber" to teacher.deskNumber,
                    "timings" to teacher.timings,
                    "email" to teacher.email,
                    "institution" to teacher.institution,
                    "institutionDomain" to teacher.institutionDomain,
                    "status" to teacher.status.name,
                    "isAvailableForAppointments" to teacher.isAvailableForAppointments,
                    "bio" to teacher.bio
                )
                firestore.collection("faculty_profiles").document(teacher.id).set(teacherMap, SetOptions.merge()).await()
                seededCount++
            }

            // 2. Seed Courses (/courses)
            DemoCampusData.demoCourses.forEach { course ->
                val courseMap = mapOf(
                    "courseCode" to course.courseCode,
                    "courseName" to course.courseName,
                    "credits" to course.credits,
                    "facultyName" to course.facultyName,
                    "department" to course.department,
                    "schoolName" to course.schoolName,
                    "semester" to course.semester,
                    "section" to course.section
                )
                firestore.collection("courses").document(course.courseCode).set(courseMap, SetOptions.merge()).await()
                seededCount++
            }

            // 3. Seed Timetable Slots (/timetables)
            DemoCampusData.demoTimetable.forEach { slot ->
                val slotMap = mapOf(
                    "id" to slot.id,
                    "courseName" to slot.courseName,
                    "courseCode" to slot.courseCode,
                    "section" to slot.section,
                    "timeSlot" to slot.timeSlot,
                    "durationMins" to slot.durationMins,
                    "facultyName" to slot.facultyName,
                    "roomCode" to slot.roomCode,
                    "type" to slot.type
                )
                firestore.collection("timetables").document(slot.id).set(slotMap, SetOptions.merge()).await()
                seededCount++
            }

            // 4. Seed University Announcements (/announcements)
            DemoCampusData.demoUniversityAnnouncements.forEach { notice ->
                val noticeMap = mapOf(
                    "id" to notice.id,
                    "title" to notice.title,
                    "content" to notice.content,
                    "authorName" to notice.authorName,
                    "authorUid" to notice.authorUid,
                    "targetDepartment" to notice.targetDepartment,
                    "priority" to notice.priority.name,
                    "createdAt" to notice.createdAt
                )
                firestore.collection("announcements").document(notice.id).set(noticeMap, SetOptions.merge()).await()
                seededCount++
            }

            // 5. Seed Lost & Found (/lost_found)
            DemoCampusData.demoLostItems.forEach { item ->
                val itemMap = mutableMapOf<String, Any?>(
                    "itemId" to item.itemId,
                    "title" to item.title,
                    "description" to item.description,
                    "locationFound" to item.locationFound,
                    "imageUrl" to item.imageUrl,
                    "reporterUid" to item.reporterUid,
                    "status" to item.status.name,
                    "createdAt" to item.createdAt
                )
                item.claimantUid?.let { itemMap["claimantUid"] = it }
                item.claimNotes?.let { itemMap["claimNotes"] = it }
                item.claimantPhone?.let { itemMap["claimantPhone"] = it }
                item.claimTimestamp?.let { itemMap["claimTimestamp"] = it }
                item.staffNotes?.let { itemMap["staffNotes"] = it }

                firestore.collection("lost_found").document(item.itemId).set(itemMap, SetOptions.merge()).await()
                seededCount++
            }

            Result.success("Firestore development seed completed successfully! $seededCount documents written.")
        } catch (e: Exception) {
            Result.failure(Exception("Firestore seed failed: ${e.localizedMessage}"))
        }
    }
}
