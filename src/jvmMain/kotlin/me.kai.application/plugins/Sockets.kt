package me.kai.application.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import me.kai.application.WebsocketHandler

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false

    }

    routing {
        webSocket("/ws") { // websocketSession
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {

                        when (frame.readText().trim()) {
                            "ping" -> {
                                outgoing.send(Frame.Text("pong ${Clock.System.now()}"))
                            }

                            "register" -> {
                                WebsocketHandler.addClient(this)
                            }

                            "close" -> {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Endpoint requested close"))

                                WebsocketHandler.removeClient(this)
                            }

                            else -> {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Not a valid command?"))

                                WebsocketHandler.removeClient(this)
                            }
                        }
                    }
                }
            } finally {
                // Remove on close
                WebsocketHandler.removeClient(this)
            }
        }
    }
}
