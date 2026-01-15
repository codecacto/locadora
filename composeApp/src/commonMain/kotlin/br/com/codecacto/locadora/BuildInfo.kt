package br.com.codecacto.locadora

/**
 * Informações de build específicas por plataforma.
 */
expect object BuildInfo {
    /**
     * Indica se o app está em modo debug.
     */
    val isDebug: Boolean
}
