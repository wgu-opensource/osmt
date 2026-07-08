package edu.wgu.osmt.credentialengine

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID

/**
 * Generates Credential Engine CTIDs deterministically using UUIDv5 (RFC 4122).
 * CTIDs are scoped by orgCtid so different deployments produce different IDs.
 */
class CtidGenerator(
    orgCtid: String,
) {
    private val namespaceUuid: UUID = uuidv5(OSMT_NAMESPACE, orgCtid)

    fun generate(entityUuid: String): String = "$CTID_PREFIX${uuidv5(namespaceUuid, entityUuid)}"

    companion object {
        private const val CTID_PREFIX = "ce-"
        private val OSMT_NAMESPACE: UUID =
            uuidv5(UUID(0, 0), "osmt.credentialengine.ctid")

        internal fun uuidv5(
            namespace: UUID,
            name: String,
        ): UUID {
            val sha1 = MessageDigest.getInstance("SHA-1")
            sha1.update(namespaceBytes(namespace))
            sha1.update(name.toByteArray(Charsets.UTF_8))
            val hash = sha1.digest().copyOf(16)
            hash[6] = (hash[6].toInt() and 0x0F or 0x50).toByte() // version 5
            hash[8] = (hash[8].toInt() and 0x3F or 0x80).toByte() // variant
            val buf = ByteBuffer.wrap(hash)
            return UUID(buf.getLong(), buf.getLong())
        }

        private fun namespaceBytes(uuid: UUID): ByteArray {
            val buf = ByteBuffer.allocate(16)
            buf.putLong(uuid.mostSignificantBits)
            buf.putLong(uuid.leastSignificantBits)
            return buf.array()
        }
    }
}
