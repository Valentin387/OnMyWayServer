package com.sindesoft.routes

import com.mongodb.client.model.Filters.eq
import com.sindesoft.data.database.Database
import com.sindesoft.data.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList


fun Route.userRouting() {

    val usersCollection = Database.database.getCollection<User>("users")

    route("/user") {
        get{
            //fetch all the users
            val users = usersCollection.find().toList()
            if (users.isNotEmpty()) {
                call.respond(users)
            }else{
                call.respondText("There are no users",status = HttpStatusCode.OK)
            }
        }

        get("{id?}"){
            val id = call.parameters["id"]?: return@get call.respondText(
                "Not valid Id",
                status = HttpStatusCode.BadRequest
            )

            val existingUser = usersCollection.find(
                eq("id", id)
            ).firstOrNull()

            existingUser ?: return@get call.respondText(
                "Not found user with id: $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(existingUser)
        }

        post{
            val user = call.receive<User>()
            usersCollection.insertOne(user)
            call.respondText("User created correctly", status = HttpStatusCode.Created)
        }

        delete("{id?}"){
            val id = call.parameters["id"]?: return@delete call.respond(HttpStatusCode.BadRequest)

            val existingUser = usersCollection.find(
                eq("id", id)
            ).firstOrNull()

            if (existingUser != null) {
                usersCollection.deleteOne(eq("id", id))
                call.respondText("User deleted correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found user with id: $id", status = HttpStatusCode.NotFound)
            }
        }

    }
}