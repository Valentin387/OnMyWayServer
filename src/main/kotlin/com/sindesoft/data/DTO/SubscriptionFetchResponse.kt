package com.sindesoft.data.DTO

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionFetchResponse(
    val subscriptionId: String,
    val givenName: String,
    val familyName: String,
    val assignedCode: String,
)
