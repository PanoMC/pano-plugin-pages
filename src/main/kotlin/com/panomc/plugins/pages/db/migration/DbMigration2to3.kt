package com.panomc.plugins.pages.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient

@Migration
class DbMigration2to3 : DatabaseMigration(
    2,
    3,
    "Add linkName column to pages table"
) {
    override val handlers: List<suspend (SqlClient) -> Unit> = listOf(
        addLinkNameColumn()
    )

    private fun addLinkNameColumn(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            val query = "ALTER TABLE `${getTablePrefix()}page` ADD COLUMN `linkName` MEDIUMTEXT AFTER `title`"
            sqlClient.query(query).execute().coAwait()
        }
}
