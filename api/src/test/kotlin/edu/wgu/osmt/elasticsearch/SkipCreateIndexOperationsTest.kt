package edu.wgu.osmt.elasticsearch

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.data.elasticsearch.core.IndexOperations
import org.springframework.data.elasticsearch.core.document.Document

class SkipCreateIndexOperationsTest {
    private val delegate = mock(IndexOperations::class.java)
    private val wrapper = SkipCreateIndexOperations(delegate)

    @Test
    fun `create returns true without calling delegate`() {
        assertTrue(wrapper.create())
        verifyNoInteractions(delegate)
    }

    @Test
    fun `create with settings is a no-op`() {
        assertTrue(wrapper.create(mutableMapOf()))
        verifyNoInteractions(delegate)
    }

    @Test
    fun `create with settings and mapping is a no-op`() {
        assertTrue(
            wrapper.create(mutableMapOf(), Document.create()),
        )
        verifyNoInteractions(delegate)
    }

    @Test
    fun `createWithMapping is a no-op`() {
        assertTrue(wrapper.createWithMapping())
        verifyNoInteractions(delegate)
    }

    @Test
    fun `delete is a no-op`() {
        assertTrue(wrapper.delete())
        verifyNoInteractions(delegate)
    }

    @Test
    fun `exists delegates to real operations`() {
        `when`(delegate.exists()).thenReturn(true)
        assertTrue(wrapper.exists())
        verify(delegate).exists()
    }

    @Test
    fun `getMapping delegates to real operations`() {
        val mapping = mapOf("properties" to emptyMap<String, Any>())
        `when`(delegate.mapping).thenReturn(mapping)

        wrapper.mapping
        verify(delegate).mapping
    }
}
