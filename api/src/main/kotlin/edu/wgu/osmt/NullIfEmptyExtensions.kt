package edu.wgu.osmt

fun String?.nullIfEmpty(): String? =
    if (this == "") {
        null
    } else {
        this
    }

fun <T> List<T>.nullIfEmpty(): List<T>? = if (this.isEmpty()) null else this
