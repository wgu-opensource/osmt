package edu.wgu.osmt.security

import edu.wgu.osmt.RoutePaths
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SecurityConfigHelperTest {
    @Test
    fun `test buildAllVersions creates correct paths for all API versions`() {
        val endpoint = RoutePaths.SKILL_DETAIL
        val expectedPaths =
            arrayOf(
                RoutePaths.API + RoutePaths.API_V3 + endpoint,
                RoutePaths.API + RoutePaths.API_V2 + endpoint,
                RoutePaths.API + RoutePaths.UNVERSIONED + endpoint,
            )

        val result = SecurityConfigHelper.buildAllVersions(endpoint)

        assertArrayEquals(expectedPaths, result)
    }

    @Test
    fun `test buildAllVersions returns exactly three paths for each endpoint`() {
        val endpoints =
            listOf(
                RoutePaths.SKILL_DETAIL,
                RoutePaths.COLLECTION_DETAIL,
                RoutePaths.SKILLS_LIST,
                RoutePaths.COLLECTIONS_LIST,
            )

        endpoints.forEach { endpoint ->
            val result = SecurityConfigHelper.buildAllVersions(endpoint)
            assertEquals(3, result.size, "buildAllVersions should return exactly 3 paths for endpoint: $endpoint")

            // Verify each path contains the correct API version prefix
            assertTrue(result[0].contains(RoutePaths.API_V3), "First path should be v3: ${result[0]}")
            assertTrue(result[1].contains(RoutePaths.API_V2), "Second path should be v2: ${result[1]}")
            assertTrue(result[2].contains(RoutePaths.UNVERSIONED), "Third path should be unversioned: ${result[2]}")
        }
    }

    @Test
    fun `test buildAllVersions preserves endpoint structure`() {
        val testEndpoint = "/test/{id}/action"
        val result = SecurityConfigHelper.buildAllVersions(testEndpoint)

        assertEquals(3, result.size)
        assertTrue(result[0].endsWith(testEndpoint), "Endpoint structure should be preserved in v3 path")
        assertTrue(result[1].endsWith(testEndpoint), "Endpoint structure should be preserved in v2 path")
        assertTrue(result[2].endsWith(testEndpoint), "Endpoint structure should be preserved in unversioned path")
    }
}
