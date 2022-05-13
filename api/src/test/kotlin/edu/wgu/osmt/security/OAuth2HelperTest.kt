package edu.wgu.osmt.security

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired

internal class OAuth2HelperTest {



    @BeforeEach
    fun setUp() {
    }

    @Test
    fun hasRole() {
        assertThat(OAuth2Helper.hasRole("kblah"), `is`(true));
    }

    @Test
    fun isArchiveRelated() {
    }
}