package com.valentinConTilde.data.DTO

import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: String,
    val message: String
)
