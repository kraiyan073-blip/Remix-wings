package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String, // e.g. "contact_1", "group_1", "wings_assistant"
    val name: String,
    val avatarUrl: String?,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val contactStatus: String = "Available",
    val isMuted: Boolean = false,
    val phoneNumber: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val senderName: String,
    val senderId: String, // "me" or contact id
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "read", // "sent", "delivered", "read"
    val isAi: Boolean = false,
    val mediaType: String? = null, // "text", "image", "voice"
    val mediaUrl: String? = null
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val userAvatar: String?,
    val caption: String,
    val timestamp: Long = System.currentTimeMillis(),
    val backgroundColorHex: String = "#FF2C5364",
    val viewed: Boolean = false
)

data class CallHistory(
    val id: String,
    val name: String,
    val avatarUrl: String?,
    val timestamp: Long,
    val isIncoming: Boolean,
    val isVideo: Boolean,
    val missed: Boolean
)
