package com.panomc.plugins.pages.event

import com.panomc.platform.api.event.SetupEventListener
import com.panomc.plugins.pages.PagesPlugin
import org.pf4j.PluginState
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SetupEventHandler(private val plugin: PagesPlugin) : SetupEventListener {
    @EventListener
    override suspend fun onSetupFinished() {
        if (plugin.pluginState == PluginState.STARTED) {
            plugin.startPlugin()
        }
    }
}
