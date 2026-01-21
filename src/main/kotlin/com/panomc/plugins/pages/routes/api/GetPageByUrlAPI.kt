package com.panomc.plugins.pages.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.BadRequest
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.stringSchema

@Endpoint
class GetPageByUrlAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : Api() {
    override val paths = listOf(Path("/api/pages/url", RouteType.GET))

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler {
        return ValidationHandlerBuilder.create(schemaRepository)
            .queryParameter(param("url", stringSchema()))
            .build()
    }

    override suspend fun handle(context: RoutingContext): Result {
        val url = context.queryParams().get("url") ?: throw BadRequest()
        val sqlClient = databaseManager.getSqlClient()
        val page = pagesDao.getByUrl(url, sqlClient)

        if (page == null || !page.active) {
            throw NotFound()
        }

        return Successful(mapOf("page" to page))
    }
}
