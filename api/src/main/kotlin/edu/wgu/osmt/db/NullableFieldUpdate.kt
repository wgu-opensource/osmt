package edu.wgu.osmt.db

/**
 * This data class is used when creating update objects with nullable fields
 * allowing us to
 *  A) not include the field in an update
 *  B) update the nullable field to a value
 *  C) update the nullable field back to null
 *
 * where the original field is of type :T?,
 * the update object field type would be :NullableFieldUpdate<T>?
 * TODO WIP, needs example and tests
 */
data class NullableFieldUpdate<T>(val t: T?)
