package edu.wgu.osmt.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WhitelabelMergeTest {
    @Test
    fun `mergeBrandingLayers applies json overlay over static`() {
        val static = mapOf<String, Any>("toolName" to "OSMT", "logoUrl" to "/a.svg")
        val json = mapOf<String, Any>("toolName" to "Custom")
        val env = emptyMap<String, String>()
        val out = WhitelabelMerge.mergeBrandingLayers(static, json, env)
        assertEquals("Custom", out["toolName"])
        assertEquals("/a.svg", out["logoUrl"])
    }

    @Test
    fun `mergeBrandingLayers env overrides json and static`() {
        val static = mapOf<String, Any>("toolName" to "OSMT")
        val json = mapOf<String, Any>("toolName" to "FromJson")
        val env = mapOf("toolName" to "FromEnv")
        val out = WhitelabelMerge.mergeBrandingLayers(static, json, env)
        assertEquals("FromEnv", out["toolName"])
    }
}
