package com.laros.lsp.traffics.core

import java.util.concurrent.TimeUnit

object RootShell {
    private val suBins = listOf(
        "su",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/adb/ksu/bin/su",
        "/data/adb/ap/bin/su",
        "/debug_ramdisk/su"
    )

    fun runAsRoot(command: String, timeoutMs: Long = 8_000L): String {
        val traces = mutableListOf<String>()
        for (suBin in suBins) {
            val output = runSingleSu(suBin, command, timeoutMs)
            traces += "$suBin=>$output"
            if (output.startsWith("ok:")) return output
        }
        return "err: ${traces.joinToString(" | ")}"
    }

    fun hasRootAccess(timeoutMs: Long = 2_000L): Boolean {
        return runAsRoot("id", timeoutMs).startsWith("ok:")
    }

    fun readGlobalDataSlot(): Int? {
        val output = runAsRoot("settings get global multi_sim_data_call")
        if (!output.startsWith("ok:")) return null
        val value = output.removePrefix("ok:").trim()
        return value.toIntOrNull()?.takeIf { it in 0..1 }
    }

    private fun runSingleSu(suBin: String, command: String, timeoutMs: Long): String {
        return runCatching {
            val process = ProcessBuilder(suBin, "-c", command)
                .redirectErrorStream(true)
                .start()
            val finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            val out = process.inputStream.bufferedReader().readText().trim()
            if (!finished) {
                process.destroyForcibly()
                return "err: timeout"
            }
            val code = process.exitValue()
            if (code == 0) "ok: $out" else "err($code): $out"
        }.getOrElse { "err: ${it.javaClass.simpleName}: ${it.message}" }
    }
}

