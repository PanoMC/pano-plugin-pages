package com.panomc.plugins.pages.util

import com.panomc.platform.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

/**
 * Rich text written in the panel is shown to every visitor (admins included) as HTML. Admins may keep any HTML; for
 * everyone else it is cleaned on save down to formatting (text, headings, lists, links, images, tables, inline
 * styles), so a panel role that may only edit content cannot plant scripts that run in an admin's session.
 */
object SafeHtml {
    private val safelist: Safelist = Safelist.relaxed()
        .addTags("span", "div", "s", "u", "mark", "hr", "del", "ins", "figure", "figcaption")
        .addAttributes(":all", "class", "style")
        .addAttributes("a", "target", "rel")
        .preserveRelativeLinks(true)

    private val outputSettings = Document.OutputSettings().prettyPrint(false)

    fun clean(html: String): String = Jsoup.clean(html, "http://localhost/", safelist, outputSettings)

    /** [html] unchanged when the caller is an admin, else cleaned with [clean]. */
    suspend fun forCaller(html: String?, authProvider: AuthProvider, context: RoutingContext): String? {
        if (html.isNullOrEmpty()) return html
        val userId = authProvider.getUserIdFromRoutingContext(context)
        return if (authProvider.isUserAdmin(userId)) html else clean(html)
    }
}
