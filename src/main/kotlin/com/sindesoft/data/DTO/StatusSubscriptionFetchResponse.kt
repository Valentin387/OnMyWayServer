package com.sindesoft.data.DTO

import kotlinx.serialization.Serializable

@Serializable
data class StatusSubscriptionFetchResponse(
    val status: String,
    val subscriptions: List<SubscriptionFetchResponse>
)
