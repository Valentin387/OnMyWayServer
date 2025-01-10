package com.sindesoft.data.DTO

data class SubscriptionFetchResponse(
    val subscriptionId: String,
    val givenName: String,
    val familyName: String,
    val assignedCode: String,
)
