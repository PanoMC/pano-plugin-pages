package com.panomc.plugins.pages.db.dao

import com.panomc.platform.db.Dao
import com.panomc.plugins.pages.db.model.Page
import io.vertx.sqlclient.SqlClient

abstract class PagesDao : Dao<Page>(Page::class.java) {
    abstract suspend fun add(page: Page, sqlClient: SqlClient): Long

    abstract suspend fun update(page: Page, sqlClient: SqlClient)

    abstract suspend fun getAllByStatus(page: Long, active: Boolean?, sqlClient: SqlClient): List<Page>

    abstract suspend fun count(active: Boolean?, sqlClient: SqlClient): Long

    abstract suspend fun deleteById(id: Long, sqlClient: SqlClient)

    abstract suspend fun getById(id: Long, sqlClient: SqlClient): Page?

    abstract suspend fun getByUrl(url: String, sqlClient: SqlClient): Page?

    abstract suspend fun getAllActive(sqlClient: SqlClient): List<Page>

    abstract suspend fun getAll(sqlClient: SqlClient): List<Page>
}
