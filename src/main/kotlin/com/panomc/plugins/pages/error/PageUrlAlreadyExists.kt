package com.panomc.plugins.pages.error

import com.panomc.platform.model.Error

class PageUrlAlreadyExists(
    statusMessage: String = "",
    extras: Map<String, Any?> = mapOf()
) : Error(422, statusMessage, extras)
