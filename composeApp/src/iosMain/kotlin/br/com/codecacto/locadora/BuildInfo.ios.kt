package br.com.codecacto.locadora

import platform.Foundation.NSBundle

actual object BuildInfo {
    actual val isDebug: Boolean
        get() {
            // Em iOS, verificamos pelo nome do bundle ou configuração
            val bundleId = NSBundle.mainBundle.bundleIdentifier ?: ""
            return bundleId.contains("debug", ignoreCase = true)
        }
}
