package me.kai.frontend

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import me.kai.common.Message

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getMessages(number: Int): List<Message> {
    return jsonClient.get("/messages/$number").body()
}

