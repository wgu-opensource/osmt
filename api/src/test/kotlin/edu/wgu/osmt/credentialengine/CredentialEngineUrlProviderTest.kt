package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CredentialEngineUrlProviderTest {
    private val registryUrl = "https://sandbox.credentialengine.org"
    private val orgCtid = "ce-org-123"

    @Test
    fun `skillFinderUrl returns null when CtidGenerator is null`() {
        val provider =
            CredentialEngineUrlProvider(
                ctidGenerator = null,
                registryUrl = registryUrl,
            )
        assertThat(provider.skillFinderUrl("uuid-123")).isNull()
    }

    @Test
    fun `collectionFinderUrl returns null when CtidGenerator is null`() {
        val provider =
            CredentialEngineUrlProvider(
                ctidGenerator = null,
                registryUrl = registryUrl,
            )
        assertThat(provider.collectionFinderUrl("uuid-123")).isNull()
    }

    @Test
    fun `skillFinderUrl returns correct URL when configured`() {
        val ctidGenerator = CtidGenerator(orgCtid)
        val provider =
            CredentialEngineUrlProvider(
                ctidGenerator = ctidGenerator,
                registryUrl = registryUrl,
            )
        val uuid = "abc-def-456"
        val ctid = ctidGenerator.generate(uuid)
        val url = provider.skillFinderUrl(uuid)
        assertThat(url).isEqualTo("$registryUrl/finder/competency/$ctid")
    }

    @Test
    fun `collectionFinderUrl returns correct URL when configured`() {
        val ctidGenerator = CtidGenerator(orgCtid)
        val provider =
            CredentialEngineUrlProvider(
                ctidGenerator = ctidGenerator,
                registryUrl = registryUrl,
            )
        val uuid = "xyz-789-ghi"
        val ctid = ctidGenerator.generate(uuid)
        val url = provider.collectionFinderUrl(uuid)
        assertThat(url).isEqualTo("$registryUrl/finder/collection/$ctid")
    }

    @Test
    fun `skillFinderUrl trims trailing slash from registryUrl`() {
        val ctidGenerator = CtidGenerator(orgCtid)
        val provider =
            CredentialEngineUrlProvider(
                ctidGenerator = ctidGenerator,
                registryUrl = "$registryUrl/",
            )
        val url = provider.skillFinderUrl("uuid-1")
        assertThat(url).startsWith(registryUrl)
        assertThat(url).doesNotContain("finder//")
    }
}
