package com.valentinConTilde.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Route.socketRouting(){

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

    }

}