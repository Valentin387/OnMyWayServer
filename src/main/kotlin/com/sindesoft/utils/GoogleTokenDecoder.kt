package com.sindesoft.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun decodeIdToken(idToken: String): Map<String, String> {
    // Split the token into its parts (header, payload, and signature)
    val parts = idToken.split(".")

    if (parts.size != 3) {
        throw IllegalArgumentException("Invalid token structure. Expected 3 parts, but got ${parts.size}")
    }

    // Decode the payload (second part of the token)
    val payload = parts[1]

    // Ensure padding is correct
    val paddedPayload = if (payload.length % 4 != 0) {
        payload + "=".repeat(4 - payload.length % 4) // Add padding if necessary
    } else {
        payload
    }

    // Decode using URL-safe Base64
    val decodedBytes = Base64.decode(paddedPayload)
    val jsonString = String(decodedBytes)
    val jsonObject = Json.parseToJsonElement(jsonString).jsonObject

    // Convert JsonObject to a map for easier handling
    return jsonObject.mapValues { (_, value) ->
        value.toString().removeSurrounding("\"") // Remove quotes from string values
    }
}