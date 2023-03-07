package me.kai.application.plugins

import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import me.kai.common.Message

class MessagesService(private val database: Database) {
    object MessagesTable : LongIdTable() {
        val content = varchar("content", length = 1000)
        val timePosted = timestamp("timePosted")
        val userID = ulong("userID")
    }

    class MessageRecord(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<MessageRecord>(MessagesTable)
        var content by MessagesTable.content
        var timePosted by MessagesTable.timePosted
        var userID by MessagesTable.userID
    }

    init {
        transaction(database) {
            SchemaUtils.create(MessagesTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(message: Message) = dbQuery {
        MessageRecord.new {
            content = message.content
            timePosted = message.timePosted.toJavaInstant()
            userID = message.userID

        }
    }

    suspend fun read(id: Long): Message? {
        return dbQuery {
            val record = MessageRecord.findById(id)
            return@dbQuery record.run {
                this?.let {
                    Message(
                        it.content,
                        Instant.fromEpochMilliseconds(it.timePosted.toEpochMilli()),
                        it.userID
                    )
                }
            }
        }
    }

    suspend fun update(id: Long, message: Message) {
        dbQuery {
            MessageRecord.findById(id)?.run {
                this.content = message.content
                this.timePosted = message.timePosted.toJavaInstant()
                this.userID = message.userID
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            MessageRecord.findById(id)?.delete()
        }
    }

    suspend fun getRange(start: Long, end: Long): List<Message> = dbQuery {
        return@dbQuery MessageRecord.find { MessagesTable.id greaterEq start and (MessagesTable.id lessEq end) }.toList().map {
            Message(it.content, Instant.fromEpochMilliseconds(it.timePosted.toEpochMilli()), it.userID)
        }
    }

    suspend fun getLast(number: Int): List<Message> = dbQuery {
        return@dbQuery MessageRecord.all().reversed().take(number).toList().map {
            Message(it.content, Instant.fromEpochMilliseconds(it.timePosted.toEpochMilli()), it.userID)
        }
    }
}