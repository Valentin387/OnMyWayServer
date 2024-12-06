package com.sindesoft.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun decodeIdToken(idToken: String): String {
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
    return String(decodedBytes)
}