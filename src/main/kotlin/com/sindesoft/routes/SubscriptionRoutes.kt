package com.sindesoft.routes

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.sindesoft.data.DTO.NewSubscriptionRequest
import com.mongodb.client.model.Filters.eq
import com.sindesoft.data.DTO.SubscriptionFetchResponse
import com.sindesoft.data.database.Database.database
import com.sindesoft.data.models.Subscription
import com.sindesoft.data.models.SubscriptionNotificationStatus
import com.sindesoft.data.models.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId

fun Route.subscriptionRouting() {

    val subscriptionCollection = database.getCollection<Subscription>("subscriptions")
    val usersCollection = database.getCollection<User>("users")

    route("/subscription") {
        //create new subscription
        post("new_subscription") {
            try{
                //receive an object of type NewSubscriptionRequest
                val request = call.receive<NewSubscriptionRequest>()

                //Fetch the user (channel) ID based on assignedCode
                val channelUser = usersCollection.find(
                    eq("assignedCode", request.assignedCode)
                ).firstOrNull()

                if (channelUser == null){
                    // The assigned code is invalid
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("status" to "error", "message" to "Invalid assignedCode")
                    )
                    return@post
                }

                //check if a subscription already exists
                val existingSubscription = subscriptionCollection.find(
                    and(
                        eq("userId", request.mongoId),
                        eq("channelId", channelUser.id)
                    )
                ).firstOrNull()

                if (existingSubscription != null){
                    // That very same subscription already exists
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("status" to "error", "message" to "You are already subscribed to this channel")
                    )
                    return@post
                }

                // create a new subscription
                val newSubscription = Subscription(
                    userId = request.mongoId,
                    channelId = channelUser.id.toString(),
                    timestamp = System.currentTimeMillis().toString(), //timestamp in milliseconds
                    currentNotificationStatus = SubscriptionNotificationStatus.UNKNOWN
                )

                subscriptionCollection.insertOne(newSubscription)

                // Respond with success
                call.respond(
                    HttpStatusCode.Created,
                    mapOf("status" to "success", "message" to "Subscription created successfully")
                )


            }catch(e: Exception){
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("status" to "error", "message" to "An error occurred while creating the subscription")
                )
            }
        }

        // Fetch subscribed channels by userId. It returns a user's subscriptions
        get("channels"){
            try{
                // Extract the userId from the query parameters
                val userId = call.request.queryParameters["userId"]

                if (userId.isNullOrEmpty()){
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("status" to "error", "message" to "Missing or invalid userId")
                    )
                    return@get
                }

                //validate if the user exists in the users collection
                val userExists = usersCollection.find(eq("_id", ObjectId(userId))).toList().isNotEmpty()
                if(!userExists){
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("status" to "error", "message" to "User not found")
                    )
                    return@get
                }

                // Fetch subscriptions by userId
                val subscriptions = subscriptionCollection.find(eq("userId", userId)).toList()

                if(subscriptions.isEmpty()){
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("status" to "error", "message" to "No subscriptions found", "channels" to emptyList<User>())
                    )
                    return@get
                }

                //Fetch the users associated with channelIds
                val channelIds = subscriptions.mapNotNull {it.channelId }.map { ObjectId(it)}
                val channelUsers = usersCollection.find(
                    Filters.`in`("_id", channelIds)
                ).toList()

                // Map the results to SubscriptionFetchResponse
                val responseList = subscriptions.mapNotNull { subscription ->
                    val channelUser = channelUsers.find { it.id.toString() == subscription.channelId }
                    channelUser?.let {
                        SubscriptionFetchResponse(
                            subscriptionId = subscription.id?: "",
                            givenName = it.givenName,
                            familyName = it.familyName,
                            assignedCode = it.assignedCode
                        )
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("status" to "success", "subscriptions" to responseList)
                )

            }catch(e: Exception){
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("status" to "error", "message" to "An error occurred while fetching subscriptions")
                )
            }

        }

        delete("delete/{id}"){
            try{
                val subscriptionId = call.parameters["id"]

                // Validate the provided subscription ID
                if(subscriptionId.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest, "Missing or invalid subscription ID")
                    return@delete
                }
                // Attempt to delete the subscription
                val deleteResult = subscriptionCollection.deleteOne(
                    eq("_id", ObjectId(subscriptionId))
                )

                if (deleteResult.deletedCount == 0L){
                    // No subscription was deleted
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("status" to "error", "message" to "Subscription not found")

                    )
                }else{
                    // Successfully deleted
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("status" to "error", "message" to "Subscription deleted successfully")
                    )
                }

            }catch (e: Exception) {
                // Handle general errors
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred while deleting the subscription")
            }
        }

        get("fetch_subscribers/{id}"){
            try{
                val userId = call.parameters["id"]

                //validate user ID
                if(userId.isNullOrEmpty()){
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("status" to "error", "message" to "Missing or invalid user ID")
                    )
                    return@get
                }

                // Check if the user exists
                val userExists = usersCollection.find(eq("_id", userId)).toList().isNotEmpty()
                if (!userExists){
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("status" to "error", "message" to "User not found")
                    )
                    return@get
                }

                // Find all subscriptions where the given user is the channel
                val subscriptions = subscriptionCollection.find(eq("channelId", userId)).toList()

                if(subscriptions.isEmpty()){
                    call.respond(
                        HttpStatusCode.OK,
                        emptyList<SubscriptionFetchResponse>()
                    )
                    return@get
                }

                // Map subscriptions to the response format
                val responses = subscriptions.mapNotNull { subscription ->
                    val subscriber = usersCollection.find(
                        eq("_id", ObjectId(subscription.userId))
                    ).firstOrNull()

                    subscriber?.let {
                        SubscriptionFetchResponse(
                            subscriptionId = subscription.id?: "",
                            givenName = it.givenName,
                            familyName = it.familyName,
                            assignedCode = it.assignedCode
                        )
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    responses
                )

            }catch (e: IllegalArgumentException) {
                // Handle invalid ObjectId format
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
            } catch (e: Exception) {
                // Handle general errors
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "An error occurred while fetching subscribers")
            }
        }

    }

}