package com.sindesoft.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class Subscription(
    @SerialName("_id") // Use instead of @BsonId
    @Serializable(with = ObjectIdSerializer::class)
    val id: ObjectId? = null, // Default to null, letting MongoDB generate the value
    val userId: String? = "", //Your mongo id
    val channelId: String? = "", //The id of the person you are subscribed to, in this project, the people are the channels themselves
    val timestamp: String = System.currentTimeMillis().toString(), //timestamp in milliseconds
    val currentNotificationStatus: SubscriptionNotificationStatus = SubscriptionNotificationStatus.UNKNOWN

)
