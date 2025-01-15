package com.valentinConTilde.data.DTO

import com.valentinConTilde.data.models.User
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    val status: String,
    val user: User
)
