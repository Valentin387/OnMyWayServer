package com.valentinConTilde.routes

import com.valentinConTilde.data.DTO.StatusResponse
import com.valentinConTilde.data.database.Database.database
import com.valentinConTilde.data.models.Subscription
import com.valentinConTilde.data.models.User
import com.valentinConTilde.data.models.UserLocation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.mongodb.client.model.Filters.eq
import kotlinx.coroutines.flow.toList

fun Route.socketRouting(){

    val activeConnections = mutableMapOf<String, WebSocketSession>()

    val usersCollection = database.getCollection<User>("users")
    val userLocationCollection = database.getCollection<UserLocation>("userLocations")
    val subscriptionCollection = database.getCollection<Subscription>("subscriptions")

    route("/socket") {
        // Handle a WebSocket session
        webSocket("/echo") {
            send("Please enter your name")
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                if (receivedText.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                } else {
                    send(Frame.Text("Hi, $receivedText!"))
                }
            }
        }

        webSocket("/infinitePing"){
            try {
                // Start a coroutine to send pings periodically
                val pingJob = launch {
                    while (true) {
                        delay(3_000) // 10 seconds delay
                        send("ping")
                    }
                }

                // Handle incoming messages
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    if (receivedText.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        break
                    } else {
                        send("Received: $receivedText")
                    }
                }

                // Cancel the ping job if the loop ends
                pingJob.cancelAndJoin()
            } catch (e: Exception) {
                application.log.error("WebSocket error: ${e.message}", e)
            }
        }

        webSocket("/tracking_updates"){
            val userId = call.request.queryParameters["userId"]
            if(userId.isNullOrEmpty()){
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Unauthorized"))
                return@webSocket
            }

            // Add user to active connections
            activeConnections[userId] = this

            try {
                for(frame in incoming){
                    frame as? Frame.Text ?: continue
                    val receiveText = frame.readText()
                    application.log.info("Received message from $userId: $receiveText")
                }
            }catch (e: Exception){
                application.log.error("WebSocket error for user: $userId: ${e.message}", e)
            }finally{
                activeConnections.remove(userId)
            }
        }

        post("new_userLocation") {
            try{
                //receive an object of type NewSubscriptionRequest
                val request = call.receive<UserLocation>()

                // Add server's current date
                val updatedRequest = request.copy(dateServer = System.currentTimeMillis().toString())

                // Check if persistence is true
                if(request.persistence){

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

                // Fetch subscribers of the user
                val subscribers = subscriptionCollection.find(
                    eq("channelId", request.userId)
                ).toList()

                // Broadcast to active connections of subscribers
                for (subscriber in subscribers) {
                    activeConnections[subscriber.userId]?.let { session ->
                        session.send(updatedRequest.toString())
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    StatusResponse(
                        "success",
                        "User location processed successfully"
                    )
                )


            }catch(e: Exception){
                application.log.error("Error processing new_userLocation: ${e.message}", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    StatusResponse(
                        "error",
                        "An error occurred while processing the user location"
                    )
                )
            }
        }

    }

}
