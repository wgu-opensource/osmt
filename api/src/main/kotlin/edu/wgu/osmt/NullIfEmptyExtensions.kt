package edu.wgu.osmt

fun CharSequence?.nullIfEmpty(): CharSequence? {
    return if (this == ""){
        null
    } else this
}

fun <T> List<T>.nullIfEmpty(): List<T>? {
    return if (this.isEmpty()) null else this
}
