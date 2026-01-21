package com.panomc.plugins.pages.db.model

import com.panomc.platform.db.DBEntity

open class Page(
    val id: Long = -1,
    val title: String = "",
    val url: String = "",
    val htmlContent: String = "",
    val active: Boolean = true,
    val loginRequired: Boolean = false,
    val permissionNode: String? = null,
    val resetLayout: Boolean = false,
    val showBreadcrumb: Boolean = true,
    val registerToThemeNav: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) : DBEntity()
