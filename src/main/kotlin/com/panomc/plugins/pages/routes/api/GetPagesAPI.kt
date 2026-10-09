package com.panomc.plugins.pages.routes.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import com.panomc.platform.schema.dsl.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import com.panomc.platform.schema.EndpointDoc
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class GetPagesAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : Api() {
    override val paths = listOf(Path("/pages", RouteType.GET))

    override val doc = EndpointDoc(
        summary = "The active custom pages.",
        tag = "pages",
        response = objectSchema()
            .requiredProperty(
                "items",
                arraySchema().items(
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
                )
            )
    )

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository).build()

    override suspend fun handle(context: RoutingContext): Result {
        val sqlClient = databaseManager.getSqlClient()
        val pages = pagesDao.getAllActive(sqlClient)

        return Successful(mapOf("items" to pages))
    }
}
