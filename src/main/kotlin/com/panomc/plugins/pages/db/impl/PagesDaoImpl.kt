package com.panomc.plugins.pages.db.impl

import com.panomc.platform.annotation.Dao
import com.panomc.plugins.pages.db.dao.PagesDao
import com.panomc.plugins.pages.db.model.Page
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.mysqlclient.MySQLClient
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope

@Dao
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class PagesDaoImpl : PagesDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `title` MEDIUMTEXT NOT NULL,
                              `url` VARCHAR(255) NOT NULL,
                              `htmlContent` LONGTEXT,
                              `active` TINYINT(1) NOT NULL DEFAULT 1,
                              `loginRequired` TINYINT(1) NOT NULL DEFAULT 0,
                              `permissionNode` VARCHAR(255),
                              `resetLayout` TINYINT(1) NOT NULL DEFAULT 0,
                              `showBreadcrumb` TINYINT(1) NOT NULL DEFAULT 1,
                              `registerToThemeNav` TINYINT(1) NOT NULL DEFAULT 1,
                              `createdAt` BIGINT(20) NOT NULL,
                              `updatedAt` BIGINT(20) NOT NULL,
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `url` (`url`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Custom pages table.';
                        """
            )
            .execute()
            .coAwait()
    }

    override suspend fun add(page: Page, sqlClient: SqlClient): Long {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`title`, `url`, `htmlContent`, `active`, `loginRequired`, `permissionNode`, `resetLayout`, `showBreadcrumb`, `registerToThemeNav`, `createdAt`, `updatedAt`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    page.title,
                    page.url,
                    page.htmlContent,
                    page.active,
                    page.loginRequired,
                    page.permissionNode,
                    page.resetLayout,
                    page.showBreadcrumb,
                    page.registerToThemeNav,
                    page.createdAt,
                    page.updatedAt
                )
            )
            .coAwait()

        return rows.property(MySQLClient.LAST_INSERTED_ID)
    }

    override suspend fun update(page: Page, sqlClient: SqlClient) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `title` = ?, `url` = ?, `htmlContent` = ?, `active` = ?, `loginRequired` = ?, `permissionNode` = ?, `resetLayout` = ?, `showBreadcrumb` = ?, `registerToThemeNav` = ?, `updatedAt` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    page.title,
                    page.url,
                    page.htmlContent,
                    page.active,
                    page.loginRequired,
                    page.permissionNode,
                    page.resetLayout,
                    page.showBreadcrumb,
                    page.registerToThemeNav,
                    page.updatedAt,
                    page.id
                )
            )
            .coAwait()
    }

    override suspend fun getAllByStatus(page: Long, active: Boolean?, sqlClient: SqlClient): List<Page> {
        val offset = (page - 1) * 10
        val query = StringBuilder("SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE 1=1")
        val params = Tuple.tuple()

        if (active != null) {
            query.append(" AND `active` = ?")
            params.addBoolean(active)
        }

        query.append(" ORDER BY `id` DESC LIMIT 10 OFFSET ?")
        params.addLong(offset)

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query.toString())
            .execute(params)
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun count(active: Boolean?, sqlClient: SqlClient): Long {
        val query = StringBuilder("SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` WHERE 1=1")
        val params = Tuple.tuple()

        if (active != null) {
            query.append(" AND `active` = ?")
            params.addBoolean(active)
        }

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query.toString())
            .execute(params)
            .coAwait()

        if (rows.size() == 0) return 0L
        return rows.toList()[0].getLong(0)
    }

    override suspend fun getAllActive(sqlClient: SqlClient): List<Page> {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `active` = ? ORDER BY `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(true))
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun getById(id: Long, sqlClient: SqlClient): Page? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun getByUrl(url: String, sqlClient: SqlClient): Page? {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` WHERE `url` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(url))
            .coAwait()

        return rows.toEntities().getOrNull(0)
    }

    override suspend fun deleteById(id: Long, sqlClient: SqlClient) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()
    }

    override suspend fun getAll(sqlClient: SqlClient): List<Page> {
        val query = "SELECT ${fields.toTableQuery()} FROM `${getTablePrefix() + tableName}` ORDER BY `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .query(query)
            .execute()
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun uninstall(sqlClient: SqlClient) {
        sqlClient
            .query("DROP TABLE IF EXISTS `${getTablePrefix() + tableName}`")
            .execute()
            .coAwait()
    }
}
