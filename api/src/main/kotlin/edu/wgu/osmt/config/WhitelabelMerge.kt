package edu.wgu.osmt.config

object WhitelabelMerge {
    fun mergeBrandingLayers(
        staticConfig: Map<String, Any>,
        jsonOverlay: Map<String, Any>,
        envOverrides: Map<String, String>,
    ): Map<String, Any> {
        val fromEnv: Map<String, Any> = envOverrides.mapValues { it.value as Any }
        return staticConfig + jsonOverlay + fromEnv
    }
}
