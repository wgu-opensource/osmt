package edu.wgu.osmt.security

import edu.wgu.osmt.db.PublishStatus.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContext

internal class AuthHelperTest {
    val securityContext: SecurityContext = mockk()
    lateinit var helper: AuthHelper

    @BeforeEach
    fun setUp() {
        helper = AuthHelper(securityContext)
    }

    @Test
    fun hasRole_emptyStr() {
        every { securityContext.authentication.authorities.toString() } returns ""

        assertFalse(helper.hasRole("ROLE_NGP_Osmt_Admin"))
        assertTrue(helper.hasRole(""))
    }

    @Test
    fun hasRole() {
        every { securityContext.authentication.authorities.toString() } returns "[ROLE_NGP_Staff, ROLE_NGP_Osmt_Admin]"
        assertFalse(helper.hasRole("blah"))
        assertTrue(helper.hasRole("ROLE_NGP_Osmt_Admin"))
    }

    @Test
    fun hasPublishStatus() {
        assertFalse(helper.hasPublishStatus(Draft, listOf(Deleted, Published)))
    }

    @Test
    fun isArchiveRelated_Archived() {
        assertTrue(helper.isArchiveRelated(Archived))
    }

    @Test
    fun isArchiveRelated_UnArchived() {
        assertTrue(helper.isArchiveRelated(Unarchived))
    }

    @Test
    fun isArchiveRelated_Draft() {
        assertFalse(helper.isArchiveRelated(Draft))
    }
    @Test
    fun isArchiveRelated_null() {
        assertFalse(helper.isArchiveRelated(null))
    }
}