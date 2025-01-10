package com.sindesoft.data.models

import kotlinx.serialization.SerialName

data class Subscription(
    @SerialName("_id")
    val id: String? = "",
    val userId: String? = "", //Your mongo id
    val channelId: String? = "", //The id of the person you are subscribed to, in this project, the people are the channels themselves
    val timestamp: String = System.currentTimeMillis().toString(), //timestamp in milliseconds
    val currentNotificationStatus: SubscriptionNotificationStatus = SubscriptionNotificationStatus.UNKNOWN

)
