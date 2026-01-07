package br.com.codecacto.locadora.features.auth.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val providerId: String = "password" // "password" or "google.com"
)
