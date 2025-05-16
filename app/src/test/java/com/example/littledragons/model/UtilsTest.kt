package com.example.littledragons.model

import org.junit.Assert
import org.junit.Test

class UtilsTest {
    @Test
    fun splitFirstAndLastName() {
        Assert.assertEquals("First" to "Last", splitFirstAndLastName("First Last"))
        Assert.assertNull(splitFirstAndLastName("FirstLast"))
    }

    @Test
    fun isValidPassword() {
        Assert.assertTrue(isValidPassword("Abcdef1+"))
        Assert.assertFalse(isValidPassword("123456"))
        Assert.assertFalse(isValidPassword("Aa1+"))
        Assert.assertFalse(isValidPassword("Aa1+".repeat(10)))
    }

    @Test
    fun isValidSchoolClassName() {
        Assert.assertFalse(isValidSchoolClassName("5"))
        Assert.assertTrue(isValidSchoolClassName("5А"))
        Assert.assertFalse(isValidSchoolClassName("АА"))
    }
}