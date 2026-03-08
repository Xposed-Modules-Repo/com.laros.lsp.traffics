package com.laros.lsp.traffics.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.laros.lsp.traffics.config.ConfigStore
import com.laros.lsp.traffics.core.SwitchRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val config = ConfigStore(context).load()
        if (!config.enabled) return
        val appContext = context.applicationContext
        val pending = goAsync()
        scope.launch(Dispatchers.IO) {
            try {
                RunModeController.apply(
                    context = appContext,
                    config = config,
                    source = "boot_completed"
                ) {
                    SwitchRunner(appContext).runOnce(it)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
    }
}
