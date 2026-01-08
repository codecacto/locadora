package br.com.codecacto.locadora

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun openUrl(url: String)