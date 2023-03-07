package me.kai.frontend

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import me.kai.common.Message

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

val wsClient = HttpClient {
    install(WebSockets)
}

private val scope = MainScope()

@OptIn(DelicateCoroutinesApi::class)
suspend fun setupUpdates(block: suspend () -> Unit) {
    scope.launch {
        wsClient.webSocket("/ws") {
            this.send(Frame.Text("register"))

            while (true) {
                val msg = incoming.receive() as? Frame.Text
                if (msg != null) {
                    if (msg.readText() == "update") {
                        block()
                    }
                }
            }
        }
    }
}

suspend fun getMessages(number: Int): List<Message> {
    return jsonClient.get("/messages/$number").body()
}

suspend fun sendMessage(msg: Message): Boolean {
    jsonClient.post("/message") {
        setBody(msg)
        contentType(ContentType.Application.Json)
    }.apply {
        return this.status == HttpStatusCode.OK
    }
}

