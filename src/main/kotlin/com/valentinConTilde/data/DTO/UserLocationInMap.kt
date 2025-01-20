package com.valentinConTilde.data.DTO

import java.util.*

import kotlinx.serialization.Serializable

@Serializable
data class UserLocationInMap(
    val userId: String? = null,
    val givenName: String,
    val familyName: String,
    val latitude: String,
    val longitude: String,
    val date: String,
    val dateServer: String,
    val speed: String,
    val persistence: Boolean,
    val batteryPercentage: Float? = null,
    val applicationVersion: String,
    val locationAccuracy: String
)
