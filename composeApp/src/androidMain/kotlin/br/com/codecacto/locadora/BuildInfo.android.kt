package br.com.codecacto.locadora

actual object BuildInfo {
    actual val isDebug: Boolean
        get() = BuildConfig.DEBUG
}
