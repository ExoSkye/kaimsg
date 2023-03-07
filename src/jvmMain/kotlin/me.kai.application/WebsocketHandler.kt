package me.kai.application

import io.ktor.websocket.*

object WebsocketHandler {
    var sessions: MutableList<WebSocketSession> = mutableListOf()

    suspend fun sendToAll(message: String) {
        for (session in sessions) {
            session.send(message)
        }
    }

    fun removeClient(session: WebSocketSession) {
        if (session in sessions) {
            sessions.remove(session)
        }
    }

    fun addClient(session: WebSocketSession) {
        if (session !in sessions) {
            sessions.add(session)
        }
    }
}