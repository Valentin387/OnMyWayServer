package com.valentinConTilde.routes

import com.valentinConTilde.data.DTO.StatusResponse
import com.valentinConTilde.data.database.Database.database
import com.valentinConTilde.data.models.User
import com.valentinConTilde.data.models.UserLocation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.trackingRouting(){

    val usersCollection = database.getCollection<User>("users")
    val userLocationCollection = database.getCollection<UserLocation>("userLocations")

    route("/tracking"){

        post("new_userLocation") {
            try{
                //receive an object of type NewSubscriptionRequest
                val request = call.receive<UserLocation>()

                // Check if persistence is true
                if(request.persistence){
                    // Add the server's current date to the UserLocation object
                    val updatedRequest = request.copy(dateServer = System.currentTimeMillis().toString())

                    // Insert the UserLocation object into the database
                    userLocationCollection.insertOne(updatedRequest)
                    call.respond(
                        HttpStatusCode.OK,
                        StatusResponse(
                            "success",
                            "User location saved successfully"
                        )
                    )
                }else{
                    // If persistence is false, respond with a success message but don't save
                    call.respond(
                        HttpStatusCode.OK,
                        StatusResponse(
                            "ignored",
                            "User location was not saved (persistence is false)"
                        )
                    )
                }

            }catch(e: Exception){
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    StatusResponse(
                        "error",
                        "An error occurred while uploading the user location"
                    )
                )
            }
        }
    }

}