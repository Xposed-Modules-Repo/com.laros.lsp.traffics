package com.laros.lsp.traffics.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.laros.lsp.traffics.model.AppConfig

object RunModeController {
    fun apply(
        context: Context,
        config: AppConfig,
        source: String,
        triggerRunOnce: ((String) -> Unit)? = null
    ) {
        if (!config.enabled) return
        val appContext = context.applicationContext
        if (config.powerSaveMode) {
            PowerSaveScheduler.schedule(appContext)
            triggerRunOnce?.invoke(source)
            stopForegroundService(appContext)
            return
        }

        PowerSaveScheduler.cancel(appContext)
        ContextCompat.startForegroundService(appContext, Intent(appContext, AutoSwitchService::class.java))
    }

    fun disable(context: Context) {
        val appContext = context.applicationContext
        stopForegroundService(appContext)
        PowerSaveScheduler.cancel(appContext)
    }

    private fun stopForegroundService(context: Context) {
        context.stopService(Intent(context, AutoSwitchService::class.java))
    }
}

