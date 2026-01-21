package com.panomc.plugins.pages.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.PageNotFound
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import com.panomc.plugins.pages.permission.ManagePagesPermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import kotlin.math.ceil

@Endpoint
class PanelGetPagesAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/pages", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return ValidationHandlerBuilder.create(schemaRepository)
            .queryParameter(optionalParam("page", numberSchema()))
            .build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManagePagesPermission(), context)

        val parameters = getParameters(context)
        val page = parameters.queryParameter("page")?.long ?: 1L

        val sqlClient = databaseManager.getSqlClient()
        val pages = pagesDao.getAllByStatus(page, null, sqlClient)
        val count = pagesDao.count(null, sqlClient)

        var totalPageNum = ceil(count.toDouble() / 10).toLong()
        if (totalPageNum == 0L) totalPageNum = 1L

        if (totalPageNum < page) {
            throw PageNotFound()
        }

        return Successful(
            mapOf(
                "pages" to pages,
                "totalPage" to totalPageNum,
                "pageCount" to count
            )
        )
    }
}
