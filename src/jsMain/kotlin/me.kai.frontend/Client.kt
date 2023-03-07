package me.kai.frontend

import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.dom.clear
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.onClickFunction
import me.kai.common.Message
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import kotlin.random.Random
import kotlin.random.nextULong

private val scope = MainScope()

suspend fun renderMessages() {
    val messages = getMessages(10)
    val messagesNode = (document.getElementById("messages") as HTMLDivElement)

    messagesNode.clear()

    messagesNode.append {
        for (message in messages) {
            div {
                classes = setOf("message")
                p {
                    classes = setOf("message-author")
                    +message.userID.toString()
                }

                p {
                    classes = setOf("message-content")
                    +message.content
                }
            }
        }
    }

}

var userID = Random.nextInt()

suspend fun main() = scope.launch {
    setupUpdates {
        renderMessages()
    }

    document.body!!.append.div {
        input {
            type = InputType.text
            value = "Message"
            id = "msginput"
        }

        button {
            onClickFunction = {
                scope.launch {
                    sendMessage(Message(
                        (document.getElementById("msginput") as HTMLInputElement).value,
                        Clock.System.now(), userID.toULong())).apply {
                        if (!this) {
                            console.log("failed to send message")
                        }
                    }
                }
            }
            +"Submit"
        }

        div {
            id = "messages"
        }
    }
}