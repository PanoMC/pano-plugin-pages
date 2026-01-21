package com.panomc.plugins.pages.log

import com.panomc.platform.db.model.PluginActivityLog
import io.vertx.core.json.JsonObject

class UpdatedPageLog(
    userId: Long,
    username: String,
    pluginId: String,
    title: String,
    changes: JsonObject? = null
) : PluginActivityLog(
    userId = userId,
    pluginId = pluginId,
    details = JsonObject().put("title", title).put("username", username).apply {
        if (changes != null) {
            put("changes", changes)
        }
    }
)
