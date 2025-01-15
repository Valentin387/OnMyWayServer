package com.valentinConTilde.data.DTO

import com.valentinConTilde.data.models.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class SubscriptionFetchResponse(
    @Serializable(with = ObjectIdSerializer::class)
    val subscriptionId: ObjectId? = null,
    val givenName: String,
    val familyName: String,
    val assignedCode: String,
)
