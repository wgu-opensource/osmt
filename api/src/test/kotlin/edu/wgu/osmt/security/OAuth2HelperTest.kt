package edu.wgu.osmt.security

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.security.core.context.SecurityContextHolder

internal class OAuth2HelperTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun hasRole() {
        mockkObject(OAuth2Helper) // applies mocking to an Object


//        every { OAuth2Helper.getSecurityContext() } returns "[ROLE_NGP_Staff, ROLE_NGP_Osmt_Admin]"

        every { OAuth2Helper.getRoles() } returns "[ROLE_NGP_Staff, ROLE_NGP_Osmt_Admin]"
        every { OAuth2Helper.hasRole(any()) } answers { callOriginal() }

        assertFalse(OAuth2Helper.hasRole("blah"))
        assertTrue(OAuth2Helper.hasRole("ROLE_NGP_Osmt_Admin"))
    }

    @Test
    fun isArchiveRelated() {
    }
}