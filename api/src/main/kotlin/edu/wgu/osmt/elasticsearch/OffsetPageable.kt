package edu.wgu.osmt.elasticsearch

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class OffsetPageable(offset: Int, limit: Int, private val sort: Sort?) : Pageable {
    val offset: Int
    val limit: Int

    init {
        if (limit < 1){
            throw IllegalArgumentException("Limit must not be less than one!")
        }
        if (offset < 0){
            throw IllegalArgumentException("Offset must not be less than zero!")
        }
        this.offset = offset
        this.limit = limit
    }

    override fun getPageNumber(): Int {
        return offset / limit
    }

    override fun hasPrevious(): Boolean {
        return offset >= limit
    }

    override fun getSort(): Sort {
        return sort ?: Sort.unsorted()
    }

    override fun next(): Pageable {
        return OffsetPageable(pageSize, offset + pageSize, getSort())
    }

    fun previous(): Pageable {
        return if (hasPrevious()) OffsetPageable(pageSize, offset - pageSize, getSort()) else this
    }

    override fun getPageSize(): Int {
        return limit
    }

    override fun getOffset(): Long {
        return offset.toLong()
    }

    override fun first(): Pageable {
        return OffsetPageable(pageSize, 0, getSort())
    }

    override fun withPage(pageNumber: Int): Pageable {
        return OffsetPageable(pageSize, pageNumber * pageSize, getSort())
    }

    override fun previousOrFirst(): Pageable {
        return if (hasPrevious()) previous() else first()
    }
}
