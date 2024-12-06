package com.sindesoft

import com.sindesoft.data.database.Database
import com.sindesoft.plugins.configureRouting
import com.sindesoft.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        host = "127.0.0.1",
        module = Application::module
    ).start(wait = true)


}

fun Application.module() {
    configureSerialization()
    configureRouting()

    environment.monitor.subscribe(ApplicationStopped){
        Database.mongoClient.close()
    }
}
