package com.valentinConTilde

import com.valentinConTilde.data.database.Database
import com.valentinConTilde.plugins.configureRouting
import com.valentinConTilde.plugins.configureSerialization
import com.valentinConTilde.plugins.configureSockets
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*


fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("PORT").toInt(),
        host = "0.0.0.0",
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
