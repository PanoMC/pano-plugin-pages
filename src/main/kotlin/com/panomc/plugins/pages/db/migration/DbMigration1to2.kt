package com.panomc.plugins.pages.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.SqlClient

@Migration
class DbMigration1to2: DatabaseMigration(
    1,
    2,
    "Add target columns to pages table"
) {
    override val handlers: List<suspend (SqlClient) -> Unit> = listOf(
        addTargetColumn()
    )

    private fun addTargetColumn(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            val query = "ALTER TABLE `${getTablePrefix()}page` ADD COLUMN `target` VARCHAR(20) NOT NULL DEFAULT '_self' AFTER `showBreadcrumb`"
            sqlClient.query(query).execute().coAwait()
        }
}
