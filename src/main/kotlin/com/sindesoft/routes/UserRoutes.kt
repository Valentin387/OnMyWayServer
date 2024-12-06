package com.sindesoft.routes

import com.sindesoft.data.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val users = mutableListOf(
    User("1", "<EMAIL>", "John", "Doe"),
    User("2", "<EMAIL>", "Carlos", "Wayne"),
)

fun Route.userRouting() {
    route("/user") {
        get{
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
            val user = users.find { it.googleId == id } ?: return@get call.respondText(
                "Not found user with id: $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(user)
        }

        post{
            val user = call.receive<User>()
            users.add(user)
            call.respondText("User created correctly", status = HttpStatusCode.Created)
        }

        delete("{id?}"){
            val id = call.parameters["id"]?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (users.removeIf { it.googleId == id }) {
                call.respondText("User deleted correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found user with id: $id", status = HttpStatusCode.NotFound)
            }
        }

    }
}