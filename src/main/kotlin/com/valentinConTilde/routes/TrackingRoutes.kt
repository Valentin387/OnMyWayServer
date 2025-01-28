package com.valentinConTilde.routes

import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Indexes.ascending
import com.valentinConTilde.data.DTO.StatusResponse
import com.valentinConTilde.data.database.Database.database
import com.valentinConTilde.data.models.User
import com.valentinConTilde.data.models.UserLocation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.mongodb.client.model.Indexes.descending
import com.valentinConTilde.data.DTO.UserLocationInMap
import com.valentinConTilde.data.models.Subscription
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun Route.trackingRouting(){

    val usersCollection = database.getCollection<User>("users")
    val userLocationCollection = database.getCollection<UserLocation>("userLocations")
    val subscriptionCollection = database.getCollection<Subscription>("subscriptions")


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

        get("my_subscriptions_latest_locations"){
            try{
                // Extract the userId from the query parameters
                val userId = call.request.queryParameters["userId"]

                // Check if request.mongoId is a valid 24-character hexadecimal string
                if (userId!!.isEmpty() || userId.length != 24 || !userId.matches(Regex("^[a-fA-F0-9]{24}$"))) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        StatusResponse(
                            "error",
                            "Invalid Mongo ID format"
                        )
                    )
                    return@get
                }

                if (userId.isEmpty()){
                    call.respond(
                        HttpStatusCode.BadRequest,
                        StatusResponse(
                            "error",
                            "Missing or invalid userId"
                        )
                    )
                    return@get
                }

                //validate if the user exists in the users collection
                val userExists = usersCollection.find(eq("_id", ObjectId(userId))).toList().isNotEmpty()
                if(!userExists){
                    call.respond(
                        HttpStatusCode.NotFound,
                        StatusResponse(
                            "error",
                            "User not found"
                        )
                    )
                    return@get
                }

                // Fetch subscriptions by userId
                val subscriptions = subscriptionCollection.find(eq("userId", userId)).toList()

                if(subscriptions.isEmpty()){
                    call.respond(
                        HttpStatusCode.OK,
                        StatusResponse(
                            "error",
                            "No subscriptions found"
                        )
                    )
                    return@get
                }

                // Initialize a list to store the mapped locations
                val locationList = mutableListOf<UserLocationInMap>()

                // Fetch the latest location for each subscription
                for (subscription in subscriptions) {
                    val channelId = subscription.channelId

                    // Fetch the latest location for the userId in the userLocations collection
                    val latestLocation = userLocationCollection
                        .find(eq("userId", channelId))
                        .sort(descending("date")) // Sort by date to get the latest location
                        .firstOrNull()

                    // Fetch the respective User from UserCollection
                    val user = usersCollection.
                    find(eq("_id", ObjectId(channelId)))
                        .firstOrNull()

                    if (user != null && latestLocation != null) {
                        // Map UserLocation to UserLocationInMap
                        val userLocationInMap = UserLocationInMap(
                            userId = latestLocation.userId,
                            givenName = user.givenName, // Assuming subscription has this info
                            familyName = user.familyName, // Assuming subscription has this info
                            latitude = latestLocation.latitude,
                            longitude = latestLocation.longitude,
                            date = latestLocation.date,
                            dateServer = latestLocation.dateServer,
                            speed = latestLocation.speed,
                            persistence = latestLocation.persistence,
                            batteryPercentage = latestLocation.batteryPercentage,
                            applicationVersion = latestLocation.applicationVersion,
                            locationAccuracy = latestLocation.locationAccuracy
                        )

                        // Add to the list
                        locationList.add(userLocationInMap)
                    }
                }

                // Respond with the list of locations
                call.respond(
                    HttpStatusCode.OK,
                    locationList
                )

            }catch (e: IllegalArgumentException) {
                // Handle invalid ObjectId format
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
            }catch(e: Exception){
                call.respond(
                    HttpStatusCode.InternalServerError,
                    StatusResponse(
                        "error",
                        "An error occurred while retrieving subscription latest locations"
                    )
                )
            }
        }

        get("user_location_history") {
            try {
                val userId = call.request.queryParameters["userId"]
                val startDateStr = call.request.queryParameters["startDate"]
                val endDateStr = call.request.queryParameters["endDate"]

                // Validate MongoDB ObjectId format
                if (userId.isNullOrBlank() || userId.length != 24 || !userId.matches(Regex("^[a-fA-F0-9]{24}$"))) {
                    call.respond(HttpStatusCode.BadRequest, StatusResponse("error", "Invalid Mongo ID format"))
                    return@get
                }

                if (startDateStr.isNullOrBlank() || endDateStr.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, StatusResponse("error", "Missing startDate or endDate"))
                    return@get
                }

                // Convert date strings to Unix timestamps
                val formatter = DateTimeFormatter.ofPattern("yyyy-M-d H:mm")
                val startDate = LocalDateTime.parse(startDateStr, formatter).toInstant(ZoneOffset.UTC).toEpochMilli().toString()
                val endDate = LocalDateTime.parse(endDateStr, formatter).toInstant(ZoneOffset.UTC).toEpochMilli().toString()

                if (startDate > endDate) {
                    call.respond(HttpStatusCode.BadRequest, StatusResponse("error", "Invalid date range"))
                    return@get
                }

                //application.environment.log.info("\n\nstartDate: $startDate\n")
                //application.environment.log.info("\n\nendDate: $endDate\n")

                // Query MongoDB for user locations within the date range
                val userLocations = userLocationCollection
                    .find(
                        and(
                            eq("userId", userId),
                            gte("date", startDate),
                            lte("date", endDate)
                        )
                    )
                    .sort(ascending("date")) // Ensure results are sorted chronologically
                    .toList()

                if (userLocations.isEmpty()) {
                    call.respond(HttpStatusCode.OK, emptyList<UserLocationInMap>())
                    return@get
                }

                // Fetch user details
                val user = usersCollection.find(eq("_id", ObjectId(userId))).firstOrNull()
                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, StatusResponse("error", "User not found"))
                    return@get
                }

                // Convert UserLocation to UserLocationInMap
                val locationList = userLocations.map { location ->
                    UserLocationInMap(
                        userId = location.userId,
                        givenName = user.givenName,
                        familyName = user.familyName,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = location.date,
                        dateServer = location.dateServer,
                        speed = location.speed,
                        persistence = location.persistence,
                        batteryPercentage = location.batteryPercentage,
                        applicationVersion = location.applicationVersion,
                        locationAccuracy = location.locationAccuracy
                    )
                }

                call.respond(HttpStatusCode.OK, locationList)

            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID format")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, StatusResponse("error", "Error retrieving location history"))
            }
        }


    }
}