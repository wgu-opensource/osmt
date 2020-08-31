package edu.wgu.osmt.db


data class ListFieldUpdate<T>(val add: List<T>? = emptyList(), val remove: List<T>? = emptyList())