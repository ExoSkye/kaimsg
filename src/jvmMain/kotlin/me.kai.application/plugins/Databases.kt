package me.kai.application.plugins

import org.jetbrains.exposed.sql.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.kai.application.WebsocketHandler
import me.kai.common.Message

fun Application.configureDatabases() {
    val database = Database.connect(
        url = "jdbc:h2:mem:messages;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )

    val messagesService = MessagesService(database)

    routing {
        post("/message") {
            val message = call.receive<Message>()
            messagesService.create(message)
            WebsocketHandler.sendToAll("update")
            call.respond(HttpStatusCode.OK)
        }

        get("/messages/{number}") {
            val num = call.parameters["number"]?.toInt() ?: call.respond(HttpStatusCode.BadRequest, "Invalid Request")

            if (num is Int) {
                val messages = messagesService.getLast(num)
                call.respond(HttpStatusCode.OK, messages)
            }
        }

        get("/message/{number}") {
            val num = call.parameters["number"]?.toLong() ?: call.respond(HttpStatusCode.BadRequest, "Invalid Request")

            if (num is Long) {
                val message = messagesService.read(num) ?: call.respond(HttpStatusCode.NotFound)

                if (message is Message) {
                    call.respond(HttpStatusCode.OK, message)
                }
            }
        }

        put("/message/{id}") {
            val id = call.parameters["id"]?.toLong() ?: call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            if (id is Long) {
                val message = call.receive<Message>()
                messagesService.update(id, message)
                WebsocketHandler.sendToAll("update")
                call.respond(HttpStatusCode.OK)
            }
        }

        delete("/message/{id}") {
            val id = call.parameters["id"]?.toLong() ?: call.respond(HttpStatusCode.BadRequest, "Invalid ID")
            if (id is Long) {
                messagesService.delete(id)
                WebsocketHandler.sendToAll("update")
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
