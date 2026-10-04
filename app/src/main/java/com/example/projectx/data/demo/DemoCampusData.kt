package com.projectx.app.data.demo

import com.projectx.app.model.*
import com.projectx.app.model.career.*
import com.projectx.app.model.hostel.*
import com.projectx.app.model.lms.*

object DemoCampusData {

    const val DEMO_STUDENT_UID = "demo_student_uid"

    private const val BASE_TIME = 1789900800000L // Sep 15, 2026
    private const val ONE_DAY = 86400000L

    val demoCourses = listOf(
        Course(
            courseCode = "CS201",
            courseName = "Data Structures & Algorithms",
            credits = 4,
            facultyName = "Dr. Sarah Jenkins",
            department = "Computer Science",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        ),
        Course(
            courseCode = "CS202",
            courseName = "Database Management Systems",
            credits = 4,
            facultyName = "Prof. David Miller",
            department = "Computer Science",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        ),
        Course(
            courseCode = "CS203",
            courseName = "Web Technologies & Frameworks",
            credits = 3,
            facultyName = "Dr. Emily Carter",
            department = "Software Engineering",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        ),
        Course(
            courseCode = "MA201",
            courseName = "Discrete Mathematics",
            credits = 3,
            facultyName = "Prof. Robert Vance",
            department = "Data Science",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        )
    )

    val demoAttendance = listOf(
        AttendanceSubject("CS201", "Data Structures & Algorithms", 28, 32),
        AttendanceSubject("CS202", "Database Management Systems", 18, 28),
        AttendanceSubject("CS203", "Web Technologies & Frameworks", 22, 30),
        AttendanceSubject("MA201", "Discrete Mathematics", 24, 30)
    )

    val demoTimetable = listOf(
        TimetableSlot(
            id = "ts1",
            courseName = "Data Structures & Algorithms",
            courseCode = "CS201",
            section = "Sec-A",
            timeSlot = "09:25 AM - 10:25 AM",
            durationMins = 60,
            facultyName = "Dr. Sarah Jenkins",
            roomCode = "seat_c304",
            type = "Lecture"
        ),
        TimetableSlot(
            id = "ts2",
            courseName = "Database Management Systems Lab",
            courseCode = "CS202L",
            section = "Sec-A1",
            timeSlot = "10:30 AM - 12:30 PM",
            durationMins = 120,
            facultyName = "Prof. David Miller",
            roomCode = "seat_lab102",
            type = "Lab"
        ),
        TimetableSlot(
            id = "ts3",
            courseName = "Web Technologies & Frameworks",
            courseCode = "CS203",
            section = "Sec-A",
            timeSlot = "02:00 PM - 03:00 PM",
            durationMins = 60,
            facultyName = "Dr. Emily Carter",
            roomCode = "seat_c305",
            type = "Lecture"
        )
    )

    val demoMaterials = mapOf(
        "CS201" to listOf(
            CourseMaterial(
                materialId = "mat_cs201_1",
                courseCode = "CS201",
                title = "Module 1: AVL Trees & Red-Black Trees",
                description = "Self-balancing binary search trees lecture slides & implementation guide.",
                fileUrl = "",
                fileType = "PDF",
                fileSizeBytes = 2516582L, // 2.4 MB
                uploadedByUid = "fac_jenkins",
                uploadedByName = "Dr. Sarah Jenkins",
                createdAt = BASE_TIME - ONE_DAY * 3
            ),
            CourseMaterial(
                materialId = "mat_cs201_2",
                courseCode = "CS201",
                title = "Module 2: Graph Algorithms Source Code",
                description = "Starter code for BFS, DFS, Dijkstra, and Kruskal algorithms in Kotlin.",
                fileUrl = "",
                fileType = "ZIP",
                fileSizeBytes = 5347737L, // 5.1 MB
                uploadedByUid = "fac_jenkins",
                uploadedByName = "Dr. Sarah Jenkins",
                createdAt = BASE_TIME - ONE_DAY * 7
            )
        ),
        "CS202" to listOf(
            CourseMaterial(
                materialId = "mat_cs202_1",
                courseCode = "CS202",
                title = "Relational Algebra & Normalization Rules",
                description = "1NF, 2NF, 3NF, and BCNF conversion examples with solutions.",
                fileUrl = "",
                fileType = "PDF",
                fileSizeBytes = 1887436L, // 1.8 MB
                uploadedByUid = "fac_miller",
                uploadedByName = "Prof. David Miller",
                createdAt = BASE_TIME - ONE_DAY * 4
            )
        )
    )

    val demoAssignments = mapOf(
        "CS201" to listOf(
            Assignment(
                assignmentId = "asgn_cs201_1",
                courseCode = "CS201",
                title = "Assignment 1: AVL Tree Balance Rotation Engine",
                description = "Implement left, right, left-right, and right-left balance rotations for an AVL tree in Kotlin.",
                maxPoints = 100,
                dueDateTimestamp = BASE_TIME + ONE_DAY * 3,
                attachmentUrl = null,
                publishedByUid = "fac_jenkins",
                createdAt = BASE_TIME - ONE_DAY * 2
            ),
            Assignment(
                assignmentId = "asgn_cs201_2",
                courseCode = "CS201",
                title = "Lab Task 2: Graph Traversal & Shortest Path",
                description = "Implement Breadth-First Search and Dijkstra's algorithm for weighted directed graphs.",
                maxPoints = 50,
                dueDateTimestamp = BASE_TIME - ONE_DAY * 2,
                attachmentUrl = null,
                publishedByUid = "fac_jenkins",
                createdAt = BASE_TIME - ONE_DAY * 10
            )
        ),
        "CS202" to listOf(
            Assignment(
                assignmentId = "asgn_cs202_1",
                courseCode = "CS202",
                title = "Assignment 1: Complex SQL Queries & Indexing",
                description = "Write SQL queries involving multi-table JOINs, subqueries, GROUP BY aggregations, and B-Tree indexes.",
                maxPoints = 100,
                dueDateTimestamp = BASE_TIME + ONE_DAY * 5,
                attachmentUrl = null,
                publishedByUid = "fac_miller",
                createdAt = BASE_TIME - ONE_DAY * 1
            )
        )
    )

    val demoSubmissions = mapOf(
        "sub_asgn_cs201_2_demo_student_uid" to AssignmentSubmission(
            submissionId = "sub_asgn_cs201_2_demo_student_uid",
            assignmentId = "asgn_cs201_2",
            studentUid = DEMO_STUDENT_UID,
            courseCode = "CS201",
            responseText = "Submitted full Dijkstra implementation with unit tests covering graph cycles.",
            storagePath = "courses/CS201/submissions/asgn_cs201_2/$DEMO_STUDENT_UID/dijkstra_impl.zip",
            submittedAtTimestamp = BASE_TIME - ONE_DAY * 3,
            isLate = false,
            status = SubmissionStatus.GRADED,
            pointsEarned = 46,
            facultyFeedback = "Great work! Dijkstra implementation is efficient and handles edge cases well. Keep up the good work!",
            gradedByUid = "fac_jenkins",
            gradedAtTimestamp = BASE_TIME - ONE_DAY * 1
        )
    )

    val demoCourseAnnouncements = mapOf(
        "CS201" to listOf(
            CourseAnnouncement(
                announcementId = "ca_cs201_1",
                courseCode = "CS201",
                title = "Mid-Term Lab Practical Schedule",
                content = "The mid-term practical evaluation for Data Structures will be conducted during regular lab hours in Room seat_c304.",
                authorName = "Dr. Sarah Jenkins",
                authorUid = "fac_jenkins",
                priority = AnnouncementPriority.URGENT,
                createdAt = BASE_TIME - ONE_DAY * 1
            )
        ),
        "CS202" to listOf(
            CourseAnnouncement(
                announcementId = "ca_cs202_1",
                courseCode = "CS202",
                title = "Assignment 1 Submission Extended",
                content = "Deadline for SQL queries assignment has been extended by 48 hours to allow all students time for testing.",
                authorName = "Prof. David Miller",
                authorUid = "fac_miller",
                priority = AnnouncementPriority.NORMAL,
                createdAt = BASE_TIME - ONE_DAY * 2
            )
        )
    )

    val demoUniversityAnnouncements = listOf(
        UniversityAnnouncement(
            id = "ua_1",
            title = "Mid-Term Examination Guidelines & Schedule",
            content = "The official date sheet for Semester 5 examinations has been released. All students are requested to download hall tickets from the student portal.",
            authorName = "Academic Administration",
            authorUid = "admin_office",
            targetDepartment = "ALL",
            priority = AnnouncementPriority.URGENT,
            createdAt = BASE_TIME - ONE_DAY * 1
        ),
        UniversityAnnouncement(
            id = "ua_2",
            title = "Central Library Extended Hours During Exams",
            content = "The Central Library will remain open 24/7 starting next Monday to support exam preparation.",
            authorName = "Central Library Services",
            authorUid = "admin_library",
            targetDepartment = "ALL",
            priority = AnnouncementPriority.NORMAL,
            createdAt = BASE_TIME - ONE_DAY * 3
        ),
        UniversityAnnouncement(
            id = "ua_3",
            title = "Annual Innovation & Tech Summit 2026",
            content = "Registrations are now open for the campus Hackathon & AI Project Exhibition.",
            authorName = "Student Affairs & Innovation Cell",
            authorUid = "admin_sa",
            targetDepartment = "Computer Science",
            priority = AnnouncementPriority.NORMAL,
            createdAt = BASE_TIME - ONE_DAY * 5
        )
    )

    val demoFaculty = listOf(
        Teacher(
            id = "fac_jenkins",
            name = "Dr. Sarah Jenkins",
            title = "Professor",
            department = "Computer Science",
            deskNumber = "seat_c304",
            timings = "Mon, Wed, Fri: 11:00 AM - 01:00 PM",
            email = "sarah.jenkins@projectx.demo",
            institution = "Demo University",
            institutionDomain = "projectx.demo",
            status = TeacherStatus.AT_DESK,
            isAvailableForAppointments = true,
            bio = "Specializes in Data Structures, Algorithm Optimization, and Computational Complexity."
        ),
        Teacher(
            id = "fac_miller",
            name = "Prof. David Miller",
            title = "Associate Professor",
            department = "Computer Science",
            deskNumber = "seat_lab102",
            timings = "Tue, Thu: 02:00 PM - 04:00 PM",
            email = "david.miller@projectx.demo",
            institution = "Demo University",
            institutionDomain = "projectx.demo",
            status = TeacherStatus.IN_CLASS,
            isAvailableForAppointments = true,
            bio = "Specializes in Database Management Systems, Distributed SQL, and Cloud Storage."
        ),
        Teacher(
            id = "fac_carter",
            name = "Dr. Emily Carter",
            title = "Assistant Professor",
            department = "Software Engineering",
            deskNumber = "seat_c305",
            timings = "Mon-Fri: 03:00 PM - 05:00 PM",
            email = "emily.carter@projectx.demo",
            institution = "Demo University",
            institutionDomain = "projectx.demo",
            status = TeacherStatus.BUSY,
            isAvailableForAppointments = true,
            bio = "Specializes in Web Architecture, Microservices, and Full-Stack Frameworks."
        ),
        Teacher(
            id = "fac_vance",
            name = "Prof. Robert Vance",
            title = "Professor",
            department = "Data Science",
            deskNumber = "Desk #401",
            timings = "Wed, Fri: 10:00 AM - 12:00 PM",
            email = "robert.vance@projectx.demo",
            institution = "Demo University",
            institutionDomain = "projectx.demo",
            status = TeacherStatus.AWAY,
            isAvailableForAppointments = false,
            bio = "Specializes in Discrete Mathematics, Graph Theory, and Combinatorics."
        )
    )

    val demoAppointments = mutableListOf(
        Appointment(
            id = "appt_demo_1",
            teacherId = "fac_jenkins",
            teacherName = "Dr. Sarah Jenkins",
            studentName = "Demo Student",
            studentEmail = "demo.student@projectx.demo",
            date = "2026-09-22",
            timeSlot = "11:00 AM - 11:30 AM",
            purpose = "Guidance on AVL Tree Rotation & Project Architecture",
            status = AppointmentStatus.CONFIRMED
        ),
        Appointment(
            id = "appt_demo_2",
            teacherId = "fac_miller",
            teacherName = "Prof. David Miller",
            studentName = "Demo Student",
            studentEmail = "demo.student@projectx.demo",
            date = "2026-09-25",
            timeSlot = "02:30 PM - 03:00 PM",
            purpose = "DBMS Query Index Optimization Review",
            status = AppointmentStatus.PENDING
        ),
        Appointment(
            id = "appt_demo_3",
            teacherId = "fac_carter",
            teacherName = "Dr. Emily Carter",
            studentName = "Demo Student",
            studentEmail = "demo.student@projectx.demo",
            date = "2026-09-15",
            timeSlot = "03:00 PM - 03:30 PM",
            purpose = "Web Architecture Feedback",
            status = AppointmentStatus.COMPLETED
        )
    )

    val demoLostItems = mutableListOf(
        LostItem(
            itemId = "lost_demo_1",
            title = "Black Dell Laptop Charger (65W)",
            description = "Left near power socket in Room seat_c304 during morning lecture.",
            locationFound = "Block N1 Room 102",
            imageUrl = null,
            reporterUid = "student_reporter_1",
            status = LostItemStatus.REPORTED,
            createdAt = BASE_TIME - ONE_DAY * 1
        ),
        LostItem(
            itemId = "lost_demo_2",
            title = "Blue Stainless Steel Water Bottle",
            description = "Milton insulated bottle found on cafeteria table.",
            locationFound = "Student Center Cafeteria",
            imageUrl = null,
            reporterUid = "staff_reporter_1",
            status = LostItemStatus.CLAIM_SUBMITTED,
            createdAt = BASE_TIME - ONE_DAY * 3,
            claimantUid = DEMO_STUDENT_UID,
            claimNotes = "Has my name sticker on bottom of bottle.",
            claimantPhone = "+91 9876543210",
            claimTimestamp = BASE_TIME - ONE_DAY * 2
        ),
        LostItem(
            itemId = "lost_demo_3",
            title = "Scientific Calculator Casio FX-991EX",
            description = "Casio scientific calculator with solar panel.",
            locationFound = "Central Library 2nd Floor",
            imageUrl = null,
            reporterUid = "staff_reporter_3",
            status = LostItemStatus.VERIFIED,
            createdAt = BASE_TIME - ONE_DAY * 5,
            claimantUid = DEMO_STUDENT_UID,
            claimNotes = "Name written on back lid.",
            claimantPhone = "+91 9876543210",
            claimTimestamp = BASE_TIME - ONE_DAY * 4,
            verifiedByUid = "staff_1",
            verificationTimestamp = BASE_TIME - ONE_DAY * 1,
            staffNotes = "Verified ID card match at desk."
        ),
        LostItem(
            itemId = "lost_demo_4",
            title = "Red Noise Smartwatch",
            description = "Red silicone strap smartwatch found near sports complex.",
            locationFound = "Sports Complex Court 2",
            imageUrl = null,
            reporterUid = "staff_reporter_4",
            status = LostItemStatus.HANDOVER_COMPLETE,
            createdAt = BASE_TIME - ONE_DAY * 7,
            claimantUid = "other_student_uid",
            claimNotes = "Handed over to owner.",
            claimantPhone = "+91 9876543211",
            claimTimestamp = BASE_TIME - ONE_DAY * 6,
            handoverTimestamp = BASE_TIME - ONE_DAY * 2
        )
    )

    // --- DEMO CAREER & PORTFOLIO DATASET ---
    val demoCertifications = mutableListOf(
        Certification(
            certificationId = "cert_demo_1",
            ownerUid = DEMO_STUDENT_UID,
            name = "Cloud Architecture Fundamentals",
            issuingOrganization = "Demo Certification Authority",
            issueDate = "2025-06-15",
            expiryDate = "2028-06-15",
            credentialId = "DEMO-AWS-8821",
            credentialUrl = null,
            category = "Cloud Computing",
            skills = listOf("AWS", "Serverless", "System Design"),
            description = "Fundamental certification covering cloud computing architectures, IAM security, and serverless deployments.",
            createdAt = BASE_TIME - ONE_DAY * 90,
            updatedAt = BASE_TIME - ONE_DAY * 90
        ),
        Certification(
            certificationId = "cert_demo_2",
            ownerUid = DEMO_STUDENT_UID,
            name = "AI & ML Essentials",
            issuingOrganization = "Demo Certification Authority",
            issueDate = "2025-08-10",
            expiryDate = null,
            credentialId = "DEMO-AI-4091",
            credentialUrl = null,
            category = "Artificial Intelligence",
            skills = listOf("Python", "PyTorch", "Deep Learning"),
            description = "Comprehensive practical course covering neural networks, computer vision, and NLP model training.",
            createdAt = BASE_TIME - ONE_DAY * 40,
            updatedAt = BASE_TIME - ONE_DAY * 40
        )
    )

    val demoProjects = mutableListOf(
        ProjectItem(
            projectId = "proj_demo_1",
            ownerUid = DEMO_STUDENT_UID,
            projectName = "Project X Digital Campus Platform",
            description = "High-density Android student platform with offline vector map navigation, LMS assignment submission pipeline, and lost & found desk.",
            technologies = listOf("Kotlin", "Jetpack Compose", "Firebase", "Coroutines"),
            skills = listOf("Mobile Architecture", "UI Design", "Clean Architecture"),
            projectUrl = null,
            githubUrl = null,
            startDate = "2025-01-10",
            endDate = "2025-05-20",
            createdAt = BASE_TIME - ONE_DAY * 60,
            updatedAt = BASE_TIME - ONE_DAY * 10
        ),
        ProjectItem(
            projectId = "proj_demo_2",
            ownerUid = DEMO_STUDENT_UID,
            projectName = "Distributed Database Engine",
            description = "High-concurrency B-Tree indexing engine with Write-Ahead Logging (WAL) and multi-threaded transaction locking.",
            technologies = listOf("C++20", "CMake", "POSIX Threads"),
            skills = listOf("Systems Programming", "Concurrency", "Database Theory"),
            projectUrl = null,
            githubUrl = null,
            startDate = "2025-08-01",
            endDate = null,
            createdAt = BASE_TIME - ONE_DAY * 30,
            updatedAt = BASE_TIME - ONE_DAY * 5
        )
    )

    val demoSkills = mutableListOf(
        SkillItem(
            skillId = "skill_demo_1",
            ownerUid = DEMO_STUDENT_UID,
            name = "Kotlin",
            category = "Programming Languages",
            proficiency = "Advanced",
            createdAt = BASE_TIME - ONE_DAY * 120
        ),
        SkillItem(
            skillId = "skill_demo_2",
            ownerUid = DEMO_STUDENT_UID,
            name = "Jetpack Compose",
            category = "UI Frameworks",
            proficiency = "Advanced",
            createdAt = BASE_TIME - ONE_DAY * 100
        ),
        SkillItem(
            skillId = "skill_demo_3",
            ownerUid = DEMO_STUDENT_UID,
            name = "System Architecture",
            category = "Core Engineering",
            proficiency = "Intermediate",
            createdAt = BASE_TIME - ONE_DAY * 80
        )
    )

    val demoAchievements = mutableListOf(
        Achievement(
            achievementId = "achieve_demo_1",
            ownerUid = DEMO_STUDENT_UID,
            title = "Campus Innovation Hackathon Finalist",
            description = "First runner-up out of 45 teams for building an accessible campus indoor navigation assistant.",
            date = "2025-11-20",
            issuingOrganization = "Demo Student Cell",
            documentStoragePath = null,
            createdAt = BASE_TIME - ONE_DAY * 20
        )
    )

    val demoPortfolioFiles = mutableListOf(
        PortfolioFile(
            fileId = "vault_demo_1",
            ownerUid = DEMO_STUDENT_UID,
            fileName = "Academic_Transcript_Sem5.pdf",
            fileType = "PDF",
            category = "Academic Document",
            storagePath = "career/demo_student_uid/vault/vault_demo_1/Academic_Transcript_Sem5.pdf",
            fileSizeBytes = 1258291L, // 1.2 MB
            uploadedAt = BASE_TIME - ONE_DAY * 15
        )
    )

    var demoCvProfile = CvProfile(
        cvId = "cv_demo_1",
        ownerUid = DEMO_STUDENT_UID,
        title = "Master Engineering CV",
        selectedCertificationIds = listOf("cert_demo_1", "cert_demo_2"),
        selectedProjectIds = listOf("proj_demo_1", "proj_demo_2"),
        selectedSkillIds = listOf("skill_demo_1", "skill_demo_2", "skill_demo_3"),
        selectedAchievementIds = listOf("achieve_demo_1"),
        personalSummary = "Passionate Computer Science student specialized in Android development, Kotlin, and system architecture. Proven track record in building offline-capable mobile applications.",
        targetRole = "Software Engineer / Android Architect",
        selectedTemplate = "Classic",
        updatedAt = BASE_TIME - ONE_DAY * 2
    )

    // --- DEMO HOSTELLER DATASET ---
    val demoDiningMeals = listOf(
        // Date 1: 2026-10-04
        DiningMeal(
            mealId = "meal_1004_bf",
            date = "2026-10-04",
            mealType = "Breakfast",
            timeRange = "07:30 AM - 09:30 AM",
            menuItems = listOf("Masala Dosa", "Sambar & Coconut Chutney", "Boiled Eggs", "Tea / Coffee", "Fresh Banana"),
            calories = "450 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261004-BF-7718"
        ),
        DiningMeal(
            mealId = "meal_1004_lu",
            date = "2026-10-04",
            mealType = "Lunch",
            timeRange = "12:30 PM - 02:30 PM",
            menuItems = listOf("Paneer Butter Masala", "Dal Makhani", "Jeera Rice", "Butter Roti", "Salad & Green Curd"),
            calories = "680 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261004-LU-9924"
        ),
        DiningMeal(
            mealId = "meal_1004_sn",
            date = "2026-10-04",
            mealType = "Snacks",
            timeRange = "05:00 PM - 06:00 PM",
            menuItems = listOf("Samosa & Mint Chutney", "Assorted Biscuits", "Hot Masala Tea"),
            calories = "320 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261004-SN-1033"
        ),
        DiningMeal(
            mealId = "meal_1004_dn",
            date = "2026-10-04",
            mealType = "Dinner",
            timeRange = "07:30 PM - 09:30 PM",
            menuItems = listOf("Kadhai Chicken / Shahi Paneer", "Yellow Dal Tadka", "Steamed Basmati Rice", "Phulka", "Gulab Jamun"),
            calories = "720 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261004-DN-4401"
        ),

        // Date 2: 2026-10-05
        DiningMeal(
            mealId = "meal_1005_bf",
            date = "2026-10-05",
            mealType = "Breakfast",
            timeRange = "07:30 AM - 09:30 AM",
            menuItems = listOf("Aloo Paratha", "Butter & Curd", "Sprouted Moong", "Tea / Coffee", "Apple"),
            calories = "480 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261005-BF-8812"
        ),
        DiningMeal(
            mealId = "meal_1005_lu",
            date = "2026-10-05",
            mealType = "Lunch",
            timeRange = "12:30 PM - 02:30 PM",
            menuItems = listOf("Rajma Masala", "Aloo Gobi", "Steamed Rice", "Tandoori Roti", "Boondi Raita"),
            calories = "650 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261005-LU-3341"
        ),
        DiningMeal(
            mealId = "meal_1005_sn",
            date = "2026-10-05",
            mealType = "Snacks",
            timeRange = "05:00 PM - 06:00 PM",
            menuItems = listOf("Veg Bread Pakora", "Sweet Milk Tea", "Mathri"),
            calories = "310 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261005-SN-5590"
        ),
        DiningMeal(
            mealId = "meal_1005_dn",
            date = "2026-10-05",
            mealType = "Dinner",
            timeRange = "07:30 PM - 09:30 PM",
            menuItems = listOf("Butter Chicken / Malai Kofta", "Mix Veg", "Jeera Rice", "Naan / Phulka", "Rasgulla"),
            calories = "750 kcal",
            location = "Central Mess — Ground Floor",
            qrToken = "BENNETT-QR-20261005-DN-6682"
        )
    )

    // --- DEMO FACULTY SCHEDULE & ASSIGNED CLASSES ---
    val demoFacultySchedule = listOf(
        TimetableSlot(
            id = "fs_1",
            courseName = "Data Structures & Algorithms",
            courseCode = "CS201",
            section = "B.Tech CSE • Sem 5 • Sec-A",
            timeSlot = "09:25 AM - 10:25 AM",
            durationMins = 60,
            facultyName = "Dr. Sarah Jenkins",
            roomCode = "seat_c304",
            type = "Lecture"
        ),
        TimetableSlot(
            id = "fs_2",
            courseName = "Database Management Systems Lab",
            courseCode = "CS202L",
            section = "B.Tech CSE • Sem 5 • Sec-A1",
            timeSlot = "10:30 AM - 12:30 PM",
            durationMins = 120,
            facultyName = "Prof. David Miller",
            roomCode = "seat_lab102",
            type = "Lab"
        ),
        TimetableSlot(
            id = "fs_3",
            courseName = "Web Technologies & Frameworks",
            courseCode = "CS203",
            section = "B.Tech SE • Sem 5 • Sec-A",
            timeSlot = "02:00 PM - 03:00 PM",
            durationMins = 60,
            facultyName = "Dr. Emily Carter",
            roomCode = "seat_c305",
            type = "Lecture"
        )
    )

    val demoFacultyClasses = listOf(
        Course(
            courseCode = "CS201",
            courseName = "Data Structures & Algorithms",
            credits = 4,
            facultyName = "Dr. Sarah Jenkins",
            department = "Computer Science",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        ),
        Course(
            courseCode = "CS202",
            courseName = "Database Management Systems",
            credits = 4,
            facultyName = "Prof. David Miller",
            department = "Computer Science",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        ),
        Course(
            courseCode = "CS203",
            courseName = "Web Technologies & Frameworks",
            credits = 3,
            facultyName = "Dr. Emily Carter",
            department = "Software Engineering",
            schoolName = "Demo University",
            semester = 5,
            section = "Sec-A"
        )
    )

    var demoRoomPartnerRequest = RoomPartnerRequest(
        requestId = "room_demo_1",
        studentUid = DEMO_STUDENT_UID,
        partnerRollNo = "E26CSEU0099",
        partnerName = "Alex Rivera",
        roomType = "Triple Sharing",
        status = "REQUEST_SENT",
        submittedAt = BASE_TIME - ONE_DAY * 5
    )

    val demoLeavePasses = mutableListOf(
        HostelLeavePass(
            leaveId = "leave_demo_1",
            studentUid = DEMO_STUDENT_UID,
            leaveType = "Home Leave",
            startDate = "2026-09-20",
            endDate = "2026-09-23",
            reason = "Family function & festival holiday at hometown",
            status = "APPROVED",
            approvedBy = "Demo Warden Dr. K. Sharma",
            appliedAt = BASE_TIME - ONE_DAY * 10
        ),
        HostelLeavePass(
            leaveId = "leave_demo_2",
            studentUid = DEMO_STUDENT_UID,
            leaveType = "Local Gate Pass",
            startDate = "2026-09-18",
            endDate = "2026-09-18",
            reason = "Medical checkup & prescription at City Health Center",
            status = "APPROVED",
            approvedBy = "Demo Assistant Warden R. Verma",
            appliedAt = BASE_TIME - ONE_DAY * 4
        ),
        HostelLeavePass(
            leaveId = "leave_demo_3",
            studentUid = DEMO_STUDENT_UID,
            leaveType = "Emergency Leave",
            startDate = "2026-09-10",
            endDate = "2026-09-12",
            reason = "Personal urgent errand",
            status = "REJECTED",
            approvedBy = "Warden Office",
            appliedAt = BASE_TIME - ONE_DAY * 15
        )
    )
}
