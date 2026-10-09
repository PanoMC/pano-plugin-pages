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
import com.panomc.platform.schema.dsl.Parameters.param
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetPageByUrlAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : Api() {
    override val paths = listOf(Path("/pages/url", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "One active custom page by its url.",
        tag = "pages",
        response = objectSchema()
            .requiredProperty(
                "page",
                objectSchema()
                .requiredProperty("id", intSchema())
                .requiredProperty("title", stringSchema())
                .requiredProperty("url", stringSchema())
                .requiredProperty("htmlContent", stringSchema())
                .requiredProperty("active", booleanSchema())
                .optionalProperty("linkName", stringSchema().nullable())
                .optionalProperty("loginRequired", booleanSchema())
                .optionalProperty("permissionNode", stringSchema().nullable())
                .optionalProperty("resetLayout", booleanSchema())
                .optionalProperty("showBreadcrumb", booleanSchema())
                .optionalProperty("target", stringSchema())
                .optionalProperty("registerToThemeNav", booleanSchema())
                .optionalProperty("createdAt", intSchema())
                .optionalProperty("updatedAt", intSchema())
            ),
        errors = listOf(BadRequest::class, NotFound::class)
    )

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
