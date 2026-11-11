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

    /** True between onStart and onStop/onDisable; the setup listener starts the plugin only then. */
    var isRunning = false
        private set

    override suspend fun onStart() {
        logger.info("Starting...")
        isRunning = true

        startPlugin()
    }

    suspend fun startPlugin() {
        if (isInitialized) return

        if (!setupManager.isSetupDone()) {
            logger.info("Setup is not finished, waiting for setup completion...")
            return
        }

        isInitialized = true

        pluginDatabaseManager.initialize(this)
    }

    override suspend fun onEnable() {
        logger.info("Enabled!")
    }

    override suspend fun onStop() {
        isRunning = false
    }

    override suspend fun onDisable() {
        isRunning = false
        isInitialized = false
    }

    override suspend fun onUninstall() {
        logger.info("Uninstalling...")
        pluginDatabaseManager.uninstall(this)
    }
}
