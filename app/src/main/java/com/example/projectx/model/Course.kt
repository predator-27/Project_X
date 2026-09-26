package com.example.projectx.model

import androidx.compose.runtime.Immutable

@Immutable
data class Course(
    val courseCode: String = "",
    val courseName: String = "",
    val credits: Int = 3,
    val facultyName: String = "",
    val department: String = "",
    val schoolName: String = "",
    val semester: Int = 1,
    val section: String = ""
)
