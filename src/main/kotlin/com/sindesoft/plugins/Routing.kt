package com.sindesoft.plugins

import com.sindesoft.routes.authRouting
import com.sindesoft.routes.socketRouting
import com.sindesoft.routes.subscriptionRouting
import com.sindesoft.routes.userRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
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
    }
}
