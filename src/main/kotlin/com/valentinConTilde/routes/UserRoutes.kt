package com.valentinConTilde.routes

import com.mongodb.client.model.Filters.eq
import com.valentinConTilde.data.database.Database
import com.valentinConTilde.data.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId


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

            val objectId = try{
                ObjectId(id)
            }catch(e: Exception){
                return@get call.respondText(e.localizedMessage,status = HttpStatusCode.BadRequest)
            }

            val existingUser = usersCollection.find(
                eq("_id", objectId)
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

            val objectId = try{
                ObjectId(id)
            }catch(e: Exception){
                return@delete call.respondText(e.localizedMessage,status = HttpStatusCode.BadRequest)
            }

            val existingUser = usersCollection.find(
                eq("_id", objectId)
            ).firstOrNull()

            if (existingUser != null) {
                usersCollection.deleteOne(eq("_id", objectId))
                call.respondText("User deleted correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found user with id: $id", status = HttpStatusCode.NotFound)
            }
        }

    }
}