package edu.wgu.osmt.io.common

interface TabularResource<S: TabColumn<T>, T> {

    /**
     * Defines the columns of this table in their desired order.
     */
    fun columnTranslations(data: List<T>): Array<S>

}

interface TabColumn<T>
