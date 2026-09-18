package com.example.projectx.model

data class AttendanceSubject(
    val courseCode: String,
    val courseName: String,
    val attendedClasses: Int,
    val totalClasses: Int
) {
    val percentage: Float
        get() = if (totalClasses == 0) 0f else (attendedClasses.toFloat() / totalClasses) * 100f

    val classesNeededFor75: Int
        get() {
            if (percentage >= 75f) return 0
            // (attended + x) / (total + x) >= 0.75  =>  x >= 3 * total - 4 * attended
            val needed = (3 * totalClasses - 4 * attendedClasses)
            return if (needed < 0) 0 else needed
        }

    val classesBunkable: Int
        get() {
            if (percentage < 75f) return 0
            // attended / (total + x) >= 0.75 => x <= (4 * attended - 3 * total) / 3
            val bunkable = (4 * attendedClasses - 3 * totalClasses) / 3
            return if (bunkable < 0) 0 else bunkable
        }
}

data class TimetableSlot(
    val id: String,
    val courseName: String,
    val courseCode: String,
    val section: String,
    val timeSlot: String,
    val durationMins: Int,
    val facultyName: String,
    val roomCode: String,
    val type: String // Lecture, Lab, Tutorial
)

data class CampusMessage(
    val id: String,
    val senderName: String,
    val subject: String,
    val preview: String,
    val timestamp: String,
    val isUnread: Boolean = false
)

object SampleCampusData {
    val sampleAttendance = listOf(
        AttendanceSubject("CS201", "Data Structures & Algorithms", 28, 32), // 87.5%
        AttendanceSubject("CS202", "Database Management Systems", 18, 28), // 64.2% (ALERT)
        AttendanceSubject("CS203", "Web Technologies & Frameworks", 22, 30), // 78.5%
        AttendanceSubject("MA201", "Discrete Mathematics", 21, 30)          // 70.0% (WARNING)
    )

    val sampleTimetable = listOf(
        TimetableSlot(
            id = "slot1",
            courseName = "Data Structures & Algorithms",
            courseCode = "CS201",
            section = "Sec-A",
            timeSlot = "09:25 AM - 10:25 AM",
            durationMins = 60,
            facultyName = "Dr. Sarah Jenkins",
            roomCode = "010-N-CC",
            type = "Lecture"
        ),
        TimetableSlot(
            id = "slot2",
            courseName = "Database Management Systems Lab",
            courseCode = "CS202L",
            section = "Sec-A1",
            timeSlot = "10:30 AM - 12:30 PM",
            durationMins = 120,
            facultyName = "Prof. David Miller",
            roomCode = "Lab-304",
            type = "Lab"
        ),
        TimetableSlot(
            id = "slot3",
            courseName = "Web Technologies",
            courseCode = "CS203",
            section = "Sec-A",
            timeSlot = "02:00 PM - 03:00 PM",
            durationMins = 60,
            facultyName = "Dr. Emily Carter",
            roomCode = "102-S-Block",
            type = "Lecture"
        )
    )

    val sampleMessages = listOf(
        CampusMessage(
            id = "msg1",
            senderName = "Academic Office",
            subject = "Mid-Term Examination Date Sheet Published",
            preview = "The mid-term examination schedule for Semester 3 BCA has been published. Please check your hall tickets.",
            timestamp = "17-Sep-2026, 09:15 AM",
            isUnread = true
        ),
        CampusMessage(
            id = "msg2",
            senderName = "Dr. Sarah Jenkins",
            subject = "Project Architecture Review Session",
            preview = "All project groups are requested to bring their initial class diagrams tomorrow during office hours.",
            timestamp = "16-Sep-2026, 04:30 PM",
            isUnread = false
        )
    )
}
