package com.panomc.plugins.pages.routes.panel

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotFound
import com.panomc.platform.model.*
import com.panomc.plugins.pages.PagesPlugin
import com.panomc.plugins.pages.db.dao.PagesDao
import com.panomc.plugins.pages.db.model.Page
import com.panomc.plugins.pages.error.PageUrlAlreadyExists
import com.panomc.plugins.pages.log.UpdatedPageLog
import com.panomc.plugins.pages.permission.ManagePagesPermission
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelUpdatePageAPI(
    private val plugin: PagesPlugin,
    private val pagesDao: PagesDao
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/pages/:id", RouteType.PUT))

    private val authProvider: AuthProvider by lazy {
        plugin.applicationContext.getBean(AuthProvider::class.java)
    }

    private val databaseManager: DatabaseManager by lazy {
        plugin.applicationContext.getBean(DatabaseManager::class.java)
    }

    override fun bodyHandler(): Handler<RoutingContext> = BodyHandler.create()

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler =
        ValidationHandlerBuilder.create(schemaRepository)
            .pathParameter(param("id", numberSchema()))
            .body(
                json(
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
        val id = parameters.pathParameter("id").long
        val data = parameters.body().jsonObject
        val sqlClient = databaseManager.getSqlClient()

        val existingPage = pagesDao.getById(id, sqlClient) ?: throw NotFound()

        val url = data.getString("url")
        val pageWithUrl = pagesDao.getByUrl(url, sqlClient)
        if (pageWithUrl != null && pageWithUrl.id != id) {
            throw PageUrlAlreadyExists()
        }

        val title = data.getString("title")
        val linkName = data.getString("linkName")?.takeIf { it.isNotBlank() } ?: title

        val updatedPage = Page(
            id = id,
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
            registerToThemeNav = data.getBoolean("registerToThemeNav"),
            createdAt = existingPage.createdAt,
            updatedAt = System.currentTimeMillis()
        )

        pagesDao.update(updatedPage, sqlClient)

        val changes = io.vertx.core.json.JsonObject()
        if (existingPage.title != title) changes.put("title", title)
        if (existingPage.linkName != linkName) changes.put("linkName", linkName)
        if (existingPage.url != url) changes.put("url", url)
        if (existingPage.htmlContent != data.getString("htmlContent")) changes.put("htmlContent", data.getString("htmlContent"))
        if (existingPage.active != data.getBoolean("active")) changes.put("active", data.getBoolean("active"))
        if (existingPage.loginRequired != data.getBoolean("loginRequired")) changes.put("loginRequired", data.getBoolean("loginRequired"))
        if (existingPage.permissionNode != data.getString("permissionNode")) changes.put("permissionNode", data.getString("permissionNode"))
        if (existingPage.resetLayout != data.getBoolean("resetLayout")) changes.put("resetLayout", data.getBoolean("resetLayout"))
        if (existingPage.showBreadcrumb != data.getBoolean("showBreadcrumb")) changes.put("showBreadcrumb", data.getBoolean("showBreadcrumb"))
        if (existingPage.target != data.getString("target")) changes.put("target", data.getString("target"))
        if (existingPage.registerToThemeNav != data.getBoolean("registerToThemeNav")) changes.put("registerToThemeNav", data.getBoolean("registerToThemeNav"))

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            UpdatedPageLog(userId, username, plugin.pluginId, title, if (changes.isEmpty) null else changes),
            sqlClient
        )

        return Successful()
    }
}
