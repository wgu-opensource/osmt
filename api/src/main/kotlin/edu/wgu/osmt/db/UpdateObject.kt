package edu.wgu.osmt.db

interface UpdateObject<T> {
    val id: Long?
}

interface HasPublishStatus {
    val publishStatus: PublishStatus?
}
