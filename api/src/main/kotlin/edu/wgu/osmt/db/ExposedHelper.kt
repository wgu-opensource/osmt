package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.vendors.currentDialect

/*
* TODO copy/paste to get around private visibility
*/
fun SchemaUtils.addMissingColumnsStatementsPublic(vararg tables: Table): List<String> {
    with(TransactionManager.current()) {
        val statements = ArrayList<String>()
        if (tables.isEmpty())
            return statements

        val existingTableColumns = currentDialect.tableColumns(*tables)

        for (table in tables) {
            //create columns
            val thisTableExistingColumns = existingTableColumns[table].orEmpty()
            val missingTableColumns =
                table.columns.filterNot { c -> thisTableExistingColumns.any { it.name.equals(c.name, true) } }
            missingTableColumns.flatMapTo(statements) { it.ddl }

            if (db.supportsAlterTableWithAddColumn) {
                // create indexes with new columns
                for (index in table.indices) {
                    if (index.columns.any { missingTableColumns.contains(it) }) {
                        statements.addAll(createIndex(index))
                    }
                }

                // sync nullability of existing columns
                val incorrectNullabilityColumns = table.columns.filter { c ->
                    thisTableExistingColumns.any {
                        c.name.equals(
                            it.name,
                            true
                        ) && it.nullable != c.columnType.nullable
                    }
                }
                incorrectNullabilityColumns.flatMapTo(statements) { it.modifyStatement() }
            }
        }

        if (db.supportsAlterTableWithAddColumn) {
            val existingColumnConstraint = db.dialect.columnConstraints(*tables)

            for (table in tables) {
                for (column in table.columns) {
                    val foreignKey = column.foreignKey
                    if (foreignKey != null) {
                        val existingConstraint = existingColumnConstraint[table to column]?.firstOrNull()
                        if (existingConstraint == null) {
                            statements.addAll(createFKey(column))
                        } else if (existingConstraint.target.table != foreignKey.target.table
                            || foreignKey.deleteRule != existingConstraint.deleteRule
                            || foreignKey.updateRule != existingConstraint.updateRule
                        ) {
                            statements.addAll(existingConstraint.dropStatement())
                            statements.addAll(createFKey(column))
                        }
                    }
                }
            }
        }

        return statements
    }
}
