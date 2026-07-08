package edu.wgu.osmt.security

import edu.wgu.osmt.SpringTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@ActiveProfiles("single-auth")
internal class AuthLogoutControllerTest : SpringTest() {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `logout returns 200 and invalidates session`() {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isOk)
    }
}
