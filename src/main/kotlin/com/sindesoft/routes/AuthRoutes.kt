package com.sindesoft.routes

import com.sindesoft.data.database.Database
import com.sindesoft.data.models.User
import com.sindesoft.utils.decodeIdToken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId


fun Route.authRouting() {

    val usersCollection = Database.database.getCollection<User>("users")

    post("/signin"){
        try {
            val idToken = call.receive<Map<String, String>>()["idToken"] ?: return@post call.respondText(
                "Missing idToken",
                status = HttpStatusCode.BadRequest
            )

            val payload = decodeIdToken(idToken)
            val email = payload["email"]  // Assume payload contains user's email as an example

            // Check if user already exists in the database
            val existingUser = usersCollection.find(
                eq("email", email)
            ).firstOrNull()

            if (existingUser != null) {
                call.respond(HttpStatusCode.OK, existingUser)
            } else {
                val newUser = User(
                    googleId = payload["sub"] ?: "",
                    email = payload["email"]!!,
                    emailVerified = payload["email_verified"]?.toBoolean() ?: false,
                    givenName = payload["given_name"] ?: "",
                    familyName = payload["family_name"] ?: "",
                    profilePicture = payload["picture"] ?: ""
                )

                usersCollection.insertOne(newUser)
                call.respond(HttpStatusCode.Created, newUser)
            }
        }catch (e: Exception){
            call.respond(
                HttpStatusCode.InternalServerError,
                "Error occurred while processing signin request")
        }

    }
}