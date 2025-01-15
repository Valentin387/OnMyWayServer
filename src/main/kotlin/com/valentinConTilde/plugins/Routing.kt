package com.valentinConTilde.plugins

import com.valentinConTilde.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        //Example:
        get("/") {
            call.respondText("Hello World!")
        }
        //routes.UserRoutes
        userRouting()

        //routes.AuthRoutes
        authRouting()

        //routes.WebSocketRoutes
        socketRouting()

        //routes.SubscriptionRoutes
        subscriptionRouting()

        //routes.TrackingRoutes
        trackingRouting()
    }
}
