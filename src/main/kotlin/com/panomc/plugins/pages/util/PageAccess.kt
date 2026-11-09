package com.panomc.plugins.pages.util

import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PermissionManager
import com.panomc.plugins.pages.db.model.Page
import io.vertx.ext.web.RoutingContext

/**
 * Who may read a custom page's content: a page marked "login required" only a logged-in user, a page with a permission
 * node only a user holding that node (or an admin). Others still get the page's details (so the site can send them to
 * the login page), but never its content.
 */
class PageAccess(
    private val authProvider: AuthProvider,
    private val permissionManager: PermissionManager,
    private val context: RoutingContext
) {
    private var resolved = false
    private var userId: Long? = null
    private var isAdmin = false

    private suspend fun resolve() {
        if (resolved) return
        resolved = true

        val loggedIn = try {
            authProvider.isLoggedIn(context)
        } catch (_: Throwable) {
            false
        }
        if (!loggedIn) return

        val id = authProvider.getUserIdFromRoutingContext(context)
        userId = id
        isAdmin = authProvider.isUserAdmin(id)
    }

    suspend fun canView(page: Page): Boolean {
        val node = page.permissionNode
        if (!page.loginRequired && node.isNullOrBlank()) return true

        resolve()
        val id = userId ?: return false

        if (node.isNullOrBlank() || isAdmin) return true

        return permissionManager.hasPermissionNode(id, node)
    }

    /** [page] as is when the caller may read it, else the same page without its content. */
    suspend fun visible(page: Page): Page = if (canView(page)) page else withoutContent(page)

    private fun withoutContent(page: Page) = Page(
        id = page.id,
        title = page.title,
        linkName = page.linkName,
        url = page.url,
        htmlContent = "",
        active = page.active,
        loginRequired = page.loginRequired,
        permissionNode = page.permissionNode,
        resetLayout = page.resetLayout,
        showBreadcrumb = page.showBreadcrumb,
        target = page.target,
        registerToThemeNav = page.registerToThemeNav,
        createdAt = page.createdAt,
        updatedAt = page.updatedAt
    )
}
