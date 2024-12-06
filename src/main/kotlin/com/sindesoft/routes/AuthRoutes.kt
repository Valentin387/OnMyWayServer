package com.sindesoft.routes

import com.sindesoft.data.database.Database
import com.sindesoft.data.models.User
import com.sindesoft.utils.decodeIdToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.authRouting() {

    val usersCollection = Database.database.getCollection<User>("users")

    post("/signin"){
        val idToken = call.receive<Map<String, String>>()["idToken"] ?: return@post call.respondText(
            "Missing idToken",
            status = HttpStatusCode.BadRequest
        )

        val payload = decodeIdToken(idToken)
        if (payload != null) {
            call.respond(HttpStatusCode.OK, payload)
        } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid ID Token format")
        }

    }
}