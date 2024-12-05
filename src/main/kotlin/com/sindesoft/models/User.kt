package com.sindesoft.models

import kotlinx.serialization.Serializable


@Serializable
data class User(
    val googleId: String,
    val name: String,
    val age: Int,
    val email: String
)
