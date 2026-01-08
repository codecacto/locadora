package br.com.codecacto.locadora

actual fun getAppVersion(): AppVersionInfo = AppVersionInfo(
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE
)
