package com.valentinConTilde.data.DTO

import kotlinx.serialization.Serializable

@Serializable
data class NewSubscriptionRequest(
    val mongoId: String,
    val assignedCode: String
)
