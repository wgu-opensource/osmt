package edu.wgu.osmt.db

interface HasInsert<T : DatabaseData<T>> {
    val table: TableWithMappers<T>
}

interface DslCrudRepository<T : DatabaseData<T>, in UpdateObjectType : UpdateObject<T>> : HasInsert<T>


