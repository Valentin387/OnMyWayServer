package com.sindesoft

import com.sindesoft.data.database.Database
import com.sindesoft.plugins.configureRouting
import com.sindesoft.plugins.configureSerialization
import com.sindesoft.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets.Plugin.install
import kotlin.time.Duration


fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT").toInt(),
        host = "127.0.0.1",
        module = Application::module
    ).start(wait = true)


}

fun Application.module() {
    configureSerialization()
    configureSockets()
    configureRouting()

    environment.monitor.subscribe(ApplicationStopped){ application ->
        application.environment.log.info("Server is stopped")
        Database.mongoClient.close()
        application.environment.log.info("Closed Connection to the database.")
    }
}
