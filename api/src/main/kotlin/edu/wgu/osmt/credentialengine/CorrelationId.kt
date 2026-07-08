package edu.wgu.osmt.credentialengine

import java.security.SecureRandom

private const val CHARS = "abcdefghijklmnopqrstuvwxyz0123456789"
private const val LENGTH = 10

/** Generates an alphanumeric correlation ID for log hunting. */
fun generateCorrelationId(): String {
    val rng = SecureRandom()
    return (1..LENGTH).map { CHARS[rng.nextInt(CHARS.length)] }.joinToString("")
}
