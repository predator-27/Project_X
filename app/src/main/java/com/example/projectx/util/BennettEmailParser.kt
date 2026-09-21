package com.example.projectx.util

data class BennettEmailParseResult(
    val isBennettEmail: Boolean,
    val suggestedAdmissionYear: Int? = null,
    val suggestedProgramCode: String? = null,
    val suggestedEnrollmentNumber: String? = null
)

/**
 * Utility for providing OPTIONAL, NON-AUTHORITATIVE suggestions for student profile prefilling.
 *
 * This parser is NOT used for authentication or authorization. All suggested fields must be
 * presented as optional recommendations that the user can review and edit.
 */
object BennettEmailParser {

    private const val BENNETT_DOMAIN = "bennett.edu.in"

    // Strict conservative regex for student pattern: e.g. S25CSEU1823@bennett.edu.in
    // Group 1: 2-digit admission year (15-35)
    // Group 2: Program alpha code (2-6 letters)
    // Group 3: Sequence number (3-6 digits)
    private val CONFIDENT_STUDENT_REGEX = Regex("^[a-zA-Z](1[5-9]|2[0-9]|3[0-5])([a-zA-Z]{2,6})(\\d{3,6})@bennett\\.edu\\.in$", RegexOption.IGNORE_CASE)

    fun parse(email: String?): BennettEmailParseResult {
        if (email.isNullOrBlank()) {
            return BennettEmailParseResult(isBennettEmail = false)
        }

        val trimmedEmail = email.trim().lowercase()

        // 1. Domain Check
        if (!trimmedEmail.endsWith("@$BENNETT_DOMAIN")) {
            return BennettEmailParseResult(isBennettEmail = false)
        }

        // 2. Conservative Pattern Match
        val match = CONFIDENT_STUDENT_REGEX.matchEntire(trimmedEmail)
        if (match != null && match.groupValues.size >= 4) {
            val yearDigits = match.groupValues[1].toIntOrNull()
            val programCode = match.groupValues[2].uppercase()

            if (yearDigits != null) {
                val suggestedYear = 2000 + yearDigits
                val fullEnrollment = trimmedEmail.substringBefore("@").uppercase()

                return BennettEmailParseResult(
                    isBennettEmail = true,
                    suggestedAdmissionYear = suggestedYear,
                    suggestedProgramCode = programCode,
                    suggestedEnrollmentNumber = fullEnrollment
                )
            }
        }

        // 3. Valid Bennett Email, but unconfident or non-standard format (Faculty, Staff, Admin)
        return BennettEmailParseResult(
            isBennettEmail = true,
            suggestedAdmissionYear = null,
            suggestedProgramCode = null,
            suggestedEnrollmentNumber = null
        )
    }
}
