package br.com.codecacto.locadora

import platform.Foundation.NSBundle

actual fun getAppVersion(): AppVersionInfo {
    val bundle = NSBundle.mainBundle
    val versionName = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
    val versionCode = (bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)?.toIntOrNull() ?: 1
    return AppVersionInfo(
        versionName = versionName,
        versionCode = versionCode
    )
}
