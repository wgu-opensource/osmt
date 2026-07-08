package edu.wgu.osmt.credentialengine

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class CtidGeneratorTest {
    private val orgCtid = "ce-org-123"
    private val generator = CtidGenerator(orgCtid)

    @Test
    fun `generate returns ce- prefixed string`() {
        val ctid = generator.generate("abc-def-123")
        assertThat(ctid).startsWith("ce-")
    }

    @Test
    fun `generate is deterministic`() {
        val a = generator.generate("uuid-1")
        val b = generator.generate("uuid-1")
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun `generate produces different CTIDs for different uuids`() {
        val a = generator.generate("uuid-1")
        val b = generator.generate("uuid-2")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `different orgCtids produce different CTIDs for same uuid`() {
        val gen1 = CtidGenerator("ce-org-111")
        val gen2 = CtidGenerator("ce-org-222")
        val a = gen1.generate("same-uuid")
        val b = gen2.generate("same-uuid")
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `generate produces valid UUID format after ce- prefix`() {
        val ctid = generator.generate(UUID.randomUUID().toString())
        val uuidPart = ctid.removePrefix("ce-")
        val parsed = UUID.fromString(uuidPart)
        assertThat(parsed.version()).isEqualTo(5)
        assertThat(parsed.variant()).isEqualTo(2)
    }

    @Test
    fun `uuidv5 matches known test vector`() {
        val dns = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
        val result = CtidGenerator.uuidv5(dns, "python.org")
        assertThat(result.toString())
            .isEqualTo("886313e1-3b8a-5372-9b90-0c9aee199e5d")
    }
}
