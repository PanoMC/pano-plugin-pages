package com.panomc.plugins.pages

import com.panomc.platform.api.PanoPlugin
import com.panomc.platform.api.PluginDatabaseManager
import com.panomc.platform.setup.SetupManager

class PagesPlugin : PanoPlugin() {
    private val pluginDatabaseManager by lazy {
        applicationContext.getBean(PluginDatabaseManager::class.java)
    }

    private val setupManager by lazy {
        applicationContext.getBean(SetupManager::class.java)
    }

    private var isInitialized = false

    override suspend fun onStart() {
        logger.info("Starting...")

        startPlugin()
    }

    suspend fun startPlugin() {
        if (isInitialized) return
        isInitialized = true

        if (!setupManager.isSetupDone()) {
            logger.info("Setup is not finished, waiting for setup completion...")
            return
        }

        pluginDatabaseManager.initialize(this)
    }

    override suspend fun onEnable() {
        logger.info("Enabled!")

        startPlugin()
    }

    override suspend fun onUninstall() {
        logger.info("Uninstalling...")
        pluginDatabaseManager.uninstall(this)
    }
}
