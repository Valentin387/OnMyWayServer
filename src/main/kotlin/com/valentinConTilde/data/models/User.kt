package com.valentinConTilde.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId


@Serializable
data class User(
    @SerialName("_id") // Use instead of @BsonId
    @Serializable(with = ObjectIdSerializer::class)
    val id: ObjectId? = null,
    val googleId: String, // From `sub`
    val assignedCode: String = "",
    val email: String,
    val emailVerified: Boolean,
    val givenName: String,
    val familyName: String,
    val profilePicture: String? = null,
    val geofenceRadius: Int,
    val firstSignupTimestamp: String
)
