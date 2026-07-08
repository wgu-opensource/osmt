package edu.wgu.osmt.credentialengine

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Provides Credential Engine finder URLs for skills and collections.
 * Returns null when CE sync is not configured (CtidGenerator is null).
 */
@Component
class CredentialEngineUrlProvider
    @Autowired
    constructor(
        @Autowired(required = false) private val ctidGenerator: CtidGenerator?,
        @Value("\${credential-engine.registry-url:https://sandbox.credentialengine.org}")
        private val registryUrl: String,
    ) {
        private val registryBase = registryUrl.trimEnd('/')

        fun skillFinderUrl(uuid: String): String? =
            ctidGenerator?.let {
                "$registryBase/finder/competency/${it.generate(uuid)}"
            }

        fun collectionFinderUrl(uuid: String): String? =
            ctidGenerator?.let {
                "$registryBase/finder/collection/${it.generate(uuid)}"
            }
    }
