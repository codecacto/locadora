package br.com.codecacto.locadora

data class AppVersionInfo(
    val versionName: String,
    val versionCode: Int
) {
    val displayVersion: String
        get() = "Versão $versionName ($versionCode)"
}

expect fun getAppVersion(): AppVersionInfo
