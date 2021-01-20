package edu.wgu.osmt

fun String?.nullIfEmpty(): String? {
    return if (this == ""){
        null
    } else this
}

fun <T> List<T>.nullIfEmpty(): List<T>? {
    return if (this.isEmpty()) null else this
}
