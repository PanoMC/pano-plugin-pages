package com.panomc.plugins.pages.sitemap

import com.panomc.platform.api.SitemapEntry
import com.panomc.platform.api.SitemapProvider
import com.panomc.plugins.pages.db.dao.PagesDao
import io.vertx.sqlclient.SqlClient
import org.springframework.stereotype.Component

/** The active custom pages that anyone may open, for `GET /api/v1/sitemap`. */
@Component
class PagesSitemapProvider(private val pagesDao: PagesDao) : SitemapProvider {
    override suspend fun entries(sqlClient: SqlClient): List<SitemapEntry> =
        pagesDao.getAllActive(sqlClient)
            .filter { !it.loginRequired && it.permissionNode.isNullOrBlank() }
            .map { page -> SitemapEntry("pano-plugin-pages:page", mapOf("url" to page.url), page.updatedAt) }
}
