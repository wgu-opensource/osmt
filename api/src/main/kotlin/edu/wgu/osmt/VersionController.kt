package edu.wgu.osmt

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

/**
 * Serves build and version information for deployment verification.
 *
 * Returns the contents of [version.json] verbatim. The file is generated at build time
 * by Maven (standalone) or by the Docker build (monorepo). See docs/plans/2026-02-25-version-endpoint.
 */
@RestController
class VersionController {
    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun version(): ResponseEntity<String> {
        val resource = ClassPathResource("version.json")
        return if (resource.exists()) {
            val json = resource.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
            ResponseEntity.ok().body(json)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
