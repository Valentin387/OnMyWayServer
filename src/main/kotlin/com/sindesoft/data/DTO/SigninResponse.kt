package com.sindesoft.data.DTO

import com.sindesoft.data.models.User
import kotlinx.serialization.Serializable

@Serializable
data class SignInResponse(
    val status: String,
    val user: User
)
