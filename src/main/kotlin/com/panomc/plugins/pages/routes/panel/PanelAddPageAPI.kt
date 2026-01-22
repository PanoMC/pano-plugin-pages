package com.panomc.plugins.pages.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import com.panomc.plugins.pages.db.model.Page
import com.panomc.plugins.pages.error.PageUrlAlreadyExists
import com.panomc.plugins.pages.log.CreatedPageLog
import com.panomc.plugins.pages.permission.ManagePagesPermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelAddPageAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/pages", RouteType.POST))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> = BodyHandler.create()

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .body(
                Bodies.json(
                    objectSchema()
                        .requiredProperty("title", stringSchema())
                        .optionalProperty("linkName", stringSchema())
                        .requiredProperty("url", stringSchema())
                        .requiredProperty("htmlContent", stringSchema())
                        .requiredProperty("active", booleanSchema())
                        .requiredProperty("loginRequired", booleanSchema())
                        .optionalProperty("permissionNode", stringSchema())
                        .requiredProperty("resetLayout", booleanSchema())
                        .requiredProperty("showBreadcrumb", booleanSchema())
                        .requiredProperty("target", stringSchema())
                        .requiredProperty("registerToThemeNav", booleanSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManagePagesPermission(), context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject
        val sqlClient = databaseManager.getSqlClient()

        val url = data.getString("url")
        if (pagesDao.getByUrl(url, sqlClient) != null) {
            throw PageUrlAlreadyExists()
        }

        val title = data.getString("title")
        val linkName = data.getString("linkName")?.takeIf { it.isNotBlank() } ?: title

        val page = Page(
            title = title,
            linkName = linkName,
            url = url,
            htmlContent = data.getString("htmlContent"),
            active = data.getBoolean("active"),
            loginRequired = data.getBoolean("loginRequired"),
            permissionNode = data.getString("permissionNode"),
            resetLayout = data.getBoolean("resetLayout"),
            showBreadcrumb = data.getBoolean("showBreadcrumb"),
            target = data.getString("target"),
            registerToThemeNav = data.getBoolean("registerToThemeNav")
        )

        val id = pagesDao.add(page, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            CreatedPageLog(userId, username, plugin.pluginId, title),
            sqlClient
        )

        return Successful(mapOf("id" to id))
    }
}
