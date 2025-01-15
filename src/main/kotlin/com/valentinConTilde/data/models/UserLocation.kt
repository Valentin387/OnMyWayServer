package com.valentinConTilde.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserLocation(
    val userId: String? = null, // Nullable with default null
    val latitude: String = "", // Default empty string to avoid missing field errors
    val longitude: String = "",
    val date: String = "",
    val dateServer: String = "",
    val speed: String = "",
    val persistence: Boolean = false, // Default false for boolean
    val batteryPercentage: Float? = null, // Nullable with default null
    val applicationVersion: String = "",
    val locationAccuracy: String = ""
)