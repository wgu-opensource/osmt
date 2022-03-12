package edu.wgu.osmt.searchhub

import com.ninjasquad.springmockk.MockkBean
import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.PaginationDefaults
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.GeneralApiException
import edu.wgu.osmt.api.model.ApiAdvancedSearch
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.mockdata.MockData
import edu.wgu.osmt.searchhub.SearchHubController
import edu.wgu.osmt.searchhub.client.apis.LibrariesApi
import edu.wgu.osmt.searchhub.client.apis.SearchingApi
import edu.wgu.osmt.searchhub.client.apis.SharingApi
import edu.wgu.osmt.searchhub.client.infrastructure.ApiResponse
import edu.wgu.osmt.searchhub.client.infrastructure.ServerError
import edu.wgu.osmt.searchhub.client.infrastructure.Success
import edu.wgu.osmt.searchhub.client.models.*
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.util.UriComponentsBuilder

@ExtendWith(SpringExtension::class)
@TestPropertySource( properties = [
    "app.searchhub.enabled=true",
    "app.searchhub.baseUrl=http://searchhub.test",
    "app.searchhub.accessToken=token1"
])
internal class SearchHubControllerTest @Autowired constructor(): SpringTest(), BaseDockerizedTest {

    @MockkBean
    private lateinit var librariesApi: LibrariesApi

    @MockkBean
    private lateinit var sharingApi: SharingApi

    @MockkBean
    private lateinit var searchingApi: SearchingApi

    @Autowired
    lateinit var searchHubController: SearchHubController

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `test get libraries`(){
        // Arrange
        val expected: List<LibrarySummary> = mockData.getSearchHubLibrarySummaries()

        val searchHubResponse = expected.map { l -> l }

        every { librariesApi.getLibraries(PaginationDefaults.size, 0) } returns searchHubResponse

        // Act
        val response = searchHubController.getLibraries(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            size = PaginationDefaults.size,
            from = 0,
            user = nullJwt
        )

        // Assert
        assertThat(response.body).isEqualTo(expected)
    }

    @Test
    fun `test get libraries - search hub client error`() {
        // Arrange
        every { librariesApi.getLibraries(PaginationDefaults.size, 0) } throws Error()

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.getLibraries(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Dependency Error: librariesApi")
    }

    @Test
    fun `test search collections`() {
        // Arrange
        val totalCount = 75
        val collectionSummaries = mockData.getSearchHubCollectionSummaries()
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)
        val expected = createCollectionSearchResults(collectionSummaries)

        val searchHubResponse = Success(
            data = collectionSummaries,
            statusCode = 200,
            headers = mapOf("X-Total-Count" to listOf(totalCount.toString()))
        ) as ApiResponse<List<CollectionSummary>?>

        every {
            searchingApi.searchCollectionsWithHttpInfo(any(), PaginationDefaults.size, any())
        } returns searchHubResponse

        // Act
        val response = searchHubController.searchCollections(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            size = PaginationDefaults.size,
            from = 0,
            request = request,
            user = nullJwt
        )

        // Assert
        assertThat(response.headers["X-Total-Count"]?.get(0)).isEqualTo(totalCount.toString())
        assertThat(response.body).containsAll(expected)
    }

    @Test
    fun `test search collections - search hub client error`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)

        every {
            searchingApi.searchCollectionsWithHttpInfo(any(), any(), any())
        } throws Error()

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Dependency Error: searchingApi")
    }

    @Test
    fun `test search collections - search hub server error`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)

        val searchHubResponse = ServerError<List<CollectionSummary>>(
            statusCode = 500,
            headers = mapOf()
        ) as ApiResponse<List<CollectionSummary>?>

        every {
            searchingApi.searchCollectionsWithHttpInfo(any(), any(), any())
        } returns searchHubResponse

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Dependency Error: searchingApi")
    }

    @Test
    fun `test search collections - empty search`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries, null)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(error.message).isEqualTo("Request did not contain search")
    }

    @Test
    fun `test search skills`() {
        // Arrange
        val totalCount = 75
        val skillSummaries = mockData.getSearchHubSkillSummaries()
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)
        val expected = createSkillSearchResults(skillSummaries)

        val searchHubResponse = Success(
            data = skillSummaries,
            statusCode = 200,
            headers = mapOf("X-Total-Count" to listOf(totalCount.toString()))
        ) as ApiResponse<List<SkillSummary>?>

        every {
            searchingApi.searchSkillsWithHttpInfo(any(), PaginationDefaults.size, any())
        } returns searchHubResponse

        // Act
        val response = searchHubController.searchSkills(
            uriComponentsBuilder = UriComponentsBuilder.newInstance(),
            size = PaginationDefaults.size,
            from = 0,
            request = request,
            user = nullJwt
        )

        // Assert
        assertThat(response.headers["X-Total-Count"]?.get(0)).isEqualTo(totalCount.toString())
        assertThat(response.body).containsAll(expected)
    }

    @Test
    fun `test search skills - search hub client error`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)

        val searchHubResponse = ServerError<List<SkillSummary>>(
            statusCode = 500,
            headers = mapOf()
        ) as ApiResponse<List<SkillSummary>?>

        every {
            searchingApi.searchSkillsWithHttpInfo(any(), any(), any())
        } returns searchHubResponse

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Dependency Error: searchingApi")
    }

    @Test
    fun `test search skills - search hub server error`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries)

        every {
            searchingApi.searchSkillsWithHttpInfo(any(), any(), any())
        } throws Error()

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Dependency Error: searchingApi")
    }

    @Test
    fun `test search skills - empty search`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = createDefaultSearchRequest(librarySummaries, null)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(error.message).isEqualTo("Request did not contain search")
    }

    @Test
    fun `test submit collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }
        val responseStatus = Result.Status.success
        val responseMessage = "message"

        val searchHubResponse = Result(
            status = responseStatus,
            message = responseMessage
        )

        every { sharingApi.submitCollections(any()) } returns searchHubResponse

        // Act
        val response = searchHubController.submitCollections(collectionUris)

        // Assert
        assertThat(response.status).isEqualTo(responseStatus)
        assertThat(response.message).isEqualTo(responseMessage)
    }

    @Test
    fun `test submit collections - search hub client error`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }
        val errorMsg = "error"

        every { sharingApi.submitCollections(any()) } throws Error(errorMsg)

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo(errorMsg)
    }

    @Test
    fun `test remove collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }
        val responseStatus = Result.Status.success
        val responseMessage = "message"

        val searchHubResponse = Result(
            status = responseStatus,
            message = responseMessage
        )

        every { sharingApi.removeCollections(any()) } returns searchHubResponse

        // Act
        val response = searchHubController.removeCollections(collectionUris)

        // Assert
        assertThat(response.status).isEqualTo(responseStatus)
        assertThat(response.message).isEqualTo(responseMessage)
    }

    @Test
    fun `test remove collections - search hub client error`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }
        val errorMsg = "error"

        every { sharingApi.removeCollections(any()) } throws Error(errorMsg)

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo(errorMsg)
    }

    @Test
    fun `test submit skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillUris = skills.map { s -> s.canonicalUrl("http://test.test") }
        val responseStatus = Result.Status.success
        val responseMessage = "message"

        val searchHubResponse = Result(
            status = responseStatus,
            message = responseMessage
        )

        every { sharingApi.submitSkills(any()) } returns searchHubResponse

        // Act
        val response = searchHubController.submitSkills(skillUris)

        // Assert
        assertThat(response.status).isEqualTo(responseStatus)
        assertThat(response.message).isEqualTo(responseMessage)
    }

    @Test
    fun `test submit skills - search hub client error`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val collectionUris = skills.map { s -> s.canonicalUrl("http://test.test") }
        val errorMsg = "error"

        every { sharingApi.submitSkills(any()) } throws Error(errorMsg)

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitSkills(collectionUris)
        }

        assertThat(error.message).isEqualTo(errorMsg)
    }

    @Test
    fun `test remove skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillUris = skills.map { s -> s.canonicalUrl("http://test.test") }
        val responseStatus = Result.Status.success
        val responseMessage = "message"

        val searchHubResponse = Result(
            status = responseStatus,
            message = responseMessage
        )

        every { sharingApi.removeSkills(any()) } returns searchHubResponse

        // Act
        val response = searchHubController.removeSkills(skillUris)

        // Assert
        assertThat(response.status).isEqualTo(responseStatus)
        assertThat(response.message).isEqualTo(responseMessage)
    }

    @Test
    fun `test remove skills - search hub client error`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val collectionUris = skills.map { s -> s.canonicalUrl("http://test.test") }
        val errorMsg = "error"

        every { sharingApi.removeSkills(any()) } throws Error(errorMsg)

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeSkills(collectionUris)
        }

        assertThat(error.message).isEqualTo(errorMsg)
    }

    companion object {

        fun createDefaultSearchRequest(
            librarySummaries: List<LibrarySummary>?,
            search: ApiSearch? = ApiSearch(
                advanced = ApiAdvancedSearch()
            )
        ): SearchHubController.Companion.ApiSearchRequest {
            return SearchHubController.Companion.ApiSearchRequest(
                libraries = librarySummaries?.mapNotNull { l -> l.uuid },
                search = search
            )
        }

        fun createCollectionSearchResults(
            collectionSummaries: List<CollectionSummary>
        ): List<SearchHubController.Companion.ApiCollectionSearchResult> {
            return collectionSummaries.map { c ->
                SearchHubController.Companion.ApiCollectionSearchResult(
                    SearchHubController.Companion.convertToApiCollectionSummary(c)
                )
            }
        }

        fun createSkillSearchResults(
            skillSummaries: List<SkillSummary>,
            setIsSimilarToLocalSkill: Boolean = false
        ): List<SearchHubController.Companion.ApiSkillSearchResult> {
            return skillSummaries.map { s ->
                SearchHubController.Companion.ApiSkillSearchResult(
                    SearchHubController.Companion.convertToApiSkillSummary(s),
                    isSimilarToLocalSkill = setIsSimilarToLocalSkill
                )
            }
        }
    }
}

@ExtendWith(SpringExtension::class)
@TestPropertySource( properties = [
    "app.searchhub.enabled=false",
    "app.searchhub.baseUrl=",
    "app.searchhub.accessToken="
])
internal class SearchHubControllerNotEnabledTest @Autowired constructor(): SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var searchHubController: SearchHubController

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `test get libraries`(){
        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.getLibraries(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test search collections`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test search skills`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test submit collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test remove collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test submit skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }

    @Test
    fun `test remove skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not enabled")
    }
}

@ExtendWith(SpringExtension::class)
@TestPropertySource( properties = [
    "app.searchhub.enabled=true",
    "app.searchhub.baseUrl=",
    "app.searchhub.accessToken="
])
internal class SearchHubControllerUrlNotConfiguredTest @Autowired constructor(): SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var searchHubController: SearchHubController

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `test get libraries`(){
        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.getLibraries(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test search collections`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test search skills`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test submit collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test remove collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test submit skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test remove skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }
}

@ExtendWith(SpringExtension::class)
@TestPropertySource( properties = [
    "app.searchhub.enabled=true",
    "app.searchhub.baseUrl=http://searchhub.test",
    "app.searchhub.accessToken="
])
internal class SearchHubControllerTokenNotConfiguredTest @Autowired constructor(): SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var searchHubController: SearchHubController

    private lateinit var mockData : MockData
    val nullJwt : Jwt? = null

    @BeforeAll
    fun setup() {
        mockData = MockData()
    }

    @Test
    fun `test get libraries`(){
        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.getLibraries(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test search collections`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchCollections(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test search skills`() {
        // Arrange
        val librarySummaries = mockData.getSearchHubLibrarySummaries()
        val request = SearchHubControllerTest.createDefaultSearchRequest(librarySummaries)

        // Assert
        val error = assertThrows<GeneralApiException> {
            searchHubController.searchSkills(
                uriComponentsBuilder = UriComponentsBuilder.newInstance(),
                size = PaginationDefaults.size,
                from = 0,
                request = request,
                user = nullJwt
            )
        }

        assertThat(error.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test submit collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test remove collections`() {
        // Arrange
        val collections = mockData.getCollections()
        val collectionUris = collections.map { c -> c.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeCollections(collectionUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test submit skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.submitSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }

    @Test
    fun `test remove skills`() {
        // Arrange
        val skills = mockData.getRichSkillDescriptors()
        val skillsUris = skills.map { s -> s.canonicalUrl("http://test.test") }

        // Assert
        val error = assertThrows<Error> {
            searchHubController.removeSkills(skillsUris)
        }

        assertThat(error.message).isEqualTo("Search Hub is not configured correctly")
    }
}
