package edu.wgu.osmt.security

import edu.wgu.osmt.db.PublishStatus.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder

internal class AuthHelperServiceTest {
    lateinit var helper: AuthHelperService

    @BeforeEach
    fun setUp() {
        mockkStatic(SecurityContextHolder::class)
        helper = AuthHelperService()
    }

    @Test
    fun hasRole_emptyStr() {
        every { SecurityContextHolder.getContext() } returns mockk {
            every { authentication.authorities.toString() } returns ""
        }
        assertFalse(helper.hasRole("ROLE_NGP_Osmt_Admin"))
        assertTrue(helper.hasRole(""))
    }

    @Test
    fun hasRole() {
        every { SecurityContextHolder.getContext() } returns mockk {
            every { authentication.authorities.toString() } returns "[ROLE_NGP_Staff, ROLE_NGP_Osmt_Admin]"
        }
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
