package com.panomc.plugins.pages.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import com.panomc.plugins.pages.permission.ManagePagesPermission
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository

@Endpoint
class PanelGetPagesAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : PanelApi() {
    override val paths = listOf(Path("/pages", RouteType.GET))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return Paging.params(ValidationHandlerBuilder.create(schemaRepository)).build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManagePagesPermission(), context)

        val page = Paging.request(context)

        val sqlClient = databaseManager.getSqlClient()
        val count = pagesDao.count(null, sqlClient)

        Paging.requireInRange(page, count)

        val pages = pagesDao.getAllByStatus(page, null, sqlClient)

        return Successful(Paging.response(pages, count, page))
    }
}
