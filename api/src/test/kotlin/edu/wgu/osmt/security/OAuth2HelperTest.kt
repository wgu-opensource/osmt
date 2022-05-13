package edu.wgu.osmt.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class OAuth2HelperTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun hasRole_emptyStr() {
        mockkObject(OAuth2Helper) // applies mocking to an Object
        mockRole("")
        every { OAuth2Helper.hasRole(any()) } answers { callOriginal() }

        assertFalse(OAuth2Helper.hasRole("ROLE_NGP_Osmt_Admin"))
        assertTrue(OAuth2Helper.hasRole(""))
    }

    @Test
    fun hasRole() {
        mockkObject(OAuth2Helper) // applies mocking to an Object
        mockRole("[ROLE_NGP_Staff, ROLE_NGP_Osmt_Admin]")
        every { OAuth2Helper.hasRole(any()) } answers { callOriginal() }

        assertFalse(OAuth2Helper.hasRole("blah"))
        assertTrue(OAuth2Helper.hasRole("ROLE_NGP_Osmt_Admin"))
    }

    private fun mockRole(roles: String) {
        every { OAuth2Helper.getSecurityContext() } returns mockk {
            every { authentication.authorities.toString() } returns roles
        }
    }

    @Test
    fun isArchiveRelated() {
    }
}