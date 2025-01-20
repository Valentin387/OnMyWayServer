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
import com.valentinConTilde.data.DTO.UserLocationInMap
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

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

            //Verify if the user exists
            // Verify that exists a user with the _id equal to request.mongoId
            val existingUser = usersCollection.find(
                eq("_id", ObjectId(userId))
            ).toList().isNotEmpty()

            if (!existingUser){
                // The mongo id is invalid
                call.respond(
                    HttpStatusCode.BadRequest,
                    StatusResponse(
                        "error",
                        "Invalid mongo Id"
                    )
                )
                return@webSocket
            }

            if(userId.isNullOrEmpty()){
                application.log.info("closing session for userId: $userId")
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
                        send(Frame.Text("Received message from $userId: $receiveText!"))
                        //log activeConnections
                        //application.log.info(activeConnections.toString())
                }
            }catch (e: Exception){
                application.log.error("WebSocket error for user: $userId: ${e.message}", e)
            }finally{
                application.log.info("closing session for user: $userId")
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

                application.log.info("\n\nSubscribers: $subscribers")
                val user = usersCollection.find(eq("_id", ObjectId(updatedRequest.userId))).firstOrNull()

                // Broadcast to active connections of subscribers
                for (subscriber in subscribers) {
                    val userLocationInMap = UserLocationInMap(
                        userId = updatedRequest.userId,
                        givenName = user?.givenName?:"N/A",
                        familyName = user?.familyName?:"N/A",
                        latitude = updatedRequest.latitude,
                        longitude = updatedRequest.longitude,
                        date = updatedRequest.date,
                        dateServer = updatedRequest.dateServer,
                        speed = updatedRequest.speed,
                        persistence = updatedRequest.persistence,
                        batteryPercentage = updatedRequest.batteryPercentage,
                        applicationVersion = updatedRequest.applicationVersion,
                        locationAccuracy =updatedRequest.locationAccuracy
                    )

                    activeConnections[subscriber.userId]?.let { session ->
                        session.send(userLocationInMap.toString())
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
