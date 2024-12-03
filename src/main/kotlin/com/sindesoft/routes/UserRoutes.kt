package com.sindesoft.routes

import com.sindesoft.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val users = mutableListOf(
    User(1, "John", 30, "<EMAIL>"),
    User(2, "William", 25, "<EMAIL>"),
)

fun Route.userRouting() {
    route("/user") {
        get{
            if (users.isNotEmpty()) {
                call.respond(users)
            }else{
                call.respondText("No hay usuarios",status = HttpStatusCode.OK)
            }
        }

        get("{id?}"){
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Id no valido",
                status = HttpStatusCode.BadRequest
            )
            val user = users.find { it.id == id } ?: return@get call.respondText(
                "No se encontro el usuario con el id $id",
                status = HttpStatusCode.NotFound
            )
            call.respond(user)
        }

        post{
            val user = call.receive<User>()
            users.add(user)
            call.respondText("Usuatio creado correctamente", status = HttpStatusCode.Created)
        }

        delete("{id?}"){
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (users.removeIf { it.id == id }) {
                call.respondText("Usuario eliminado correctamente", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("No se encontro el usuario con el id $id", status = HttpStatusCode.NotFound)
            }
        }

    }
}