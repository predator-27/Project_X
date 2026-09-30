package com.projectx.app.util

object AuthValidation {

    const val BENNETT_DOMAIN_SUFFIX = "bennett.edu.in"

    /**
     * Checks if email ends with 'bennett.edu.in' and contains an '@' symbol.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim().lowercase()
        return trimmed.contains("@") && trimmed.endsWith(BENNETT_DOMAIN_SUFFIX)
    }

    /**
     * Validates password criteria:
     * - Minimum 8 characters
     * - At least 1 uppercase letter (A-Z)
     * - At least 1 number (0-9)
     * - At least 1 special character (!, @, #, $, %, etc.)
     */
    fun isValidPassword(password: String): Boolean {
        val hasMinLength = password.length >= 8
        val hasCapital = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasMinLength && hasCapital && hasDigit && hasSpecial
    }

    /**
     * Returns a human-readable error message if email validation fails, or null if valid.
     */
    fun getEmailError(email: String): String? {
        val trimmed = email.trim().lowercase()
        if (trimmed.isBlank()) {
            return "Email is required."
        }
        if (!trimmed.contains("@") || !trimmed.endsWith(BENNETT_DOMAIN_SUFFIX)) {
            return "Email must end with @bennett.edu.in"
        }
        return null
    }

    /**
     * Returns a human-readable error message if password validation fails, or null if valid.
     */
    fun getPasswordError(password: String): String? {
        if (password.isBlank()) {
            return "Password is required."
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters long."
        }
        if (!password.any { it.isUpperCase() }) {
            return "Password must contain at least 1 capital letter (A-Z)."
        }
        if (!password.any { it.isDigit() }) {
            return "Password must contain at least 1 number (0-9)."
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            return "Password must contain at least 1 special character (!, @, #, $, etc.)."
        }
        return null
    }
}
