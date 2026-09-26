package com.example.projectx.data.firestore

import com.example.projectx.model.Course
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CourseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val coursesCollection = firestore.collection("courses")

    suspend fun getStudentCourses(departmentFilter: String? = null): Result<List<Course>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = coursesCollection.get().await()
            if (querySnapshot.isEmpty) {
                return@withContext Result.success(emptyList())
            }

            val courses = querySnapshot.documents.mapNotNull { doc ->
                try {
                    Course(
                        courseCode = doc.getString("courseCode") ?: doc.id,
                        courseName = doc.getString("courseName") ?: "",
                        credits = doc.getLong("credits")?.toInt() ?: 3,
                        facultyName = doc.getString("facultyName") ?: "",
                        department = doc.getString("department") ?: "",
                        schoolName = doc.getString("schoolName") ?: "",
                        semester = doc.getLong("semester")?.toInt() ?: 1,
                        section = doc.getString("section") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            val filtered = if (!departmentFilter.isNullOrBlank()) {
                courses.filter { it.department.equals(departmentFilter, ignoreCase = true) }
            } else {
                courses
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load courses from Firestore: ${e.localizedMessage}"))
        }
    }
}
