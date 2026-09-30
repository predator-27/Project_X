package com.projectx.app

import com.projectx.app.util.AuthValidation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun email_must_end_with_bennett_edu_in() {
        assertTrue(AuthValidation.isValidEmail("student@bennett.edu.in"))
        assertTrue(AuthValidation.isValidEmail("FACULTY.NAME@BENNETT.EDU.IN"))

        assertFalse(AuthValidation.isValidEmail("student@gmail.com"))
        assertFalse(AuthValidation.isValidEmail("bennett.edu.in"))
        assertFalse(AuthValidation.isValidEmail(""))
    }

    @Test
    fun password_must_contain_capital_number_and_special_char() {
        assertTrue(AuthValidation.isValidPassword("Pass1234!"))
        assertTrue(AuthValidation.isValidPassword("Bennett#2025"))
        assertTrue(AuthValidation.isValidPassword("P@ssw0rd99"))

        assertFalse(AuthValidation.isValidPassword("Pass12345"))
        assertNotNull(AuthValidation.getPasswordError("Pass12345"))

        assertFalse(AuthValidation.isValidPassword("pass1234!"))
        assertNotNull(AuthValidation.getPasswordError("pass1234!"))

        assertFalse(AuthValidation.isValidPassword("Password!"))
        assertNotNull(AuthValidation.getPasswordError("Password!"))

        assertFalse(AuthValidation.isValidPassword("Pas1!"))
        assertNotNull(AuthValidation.getPasswordError("Pas1!"))
    }
}
