package com.panomc.plugins.pages.error

import com.panomc.platform.model.Error

class PageUrlAlreadyExists(
    statusMessage: String = "",
    extras: Map<String, Any?> = mapOf()
) : Error("PAGE_URL_ALREADY_EXISTS", 422, statusMessage, extras)
