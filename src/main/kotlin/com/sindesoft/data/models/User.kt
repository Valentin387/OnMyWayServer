package com.sindesoft.data.models

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val googleId: String,
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val age: Int? = null
)
