package com.example.data.repository

import com.example.data.api.GeminiClient
import com.example.data.database.ChatDao
import com.example.data.database.MessageDao
import com.example.data.database.StatusDao
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class WingsRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val statusDao: StatusDao
) {
    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val allStatuses: Flow<List<StatusEntity>> = statusDao.getAllStatuses()

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun clearUnread(chatId: String) {
        chatDao.clearUnreadCount(chatId)
    }

    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        text: String,
        isAi: Boolean = false,
        mediaType: String? = null,
        mediaUrl: String? = null
    ) {
        val timestamp = System.currentTimeMillis()
        val msg = MessageEntity(
            chatId = chatId,
            senderName = senderName,
            senderId = senderId,
            text = text,
            timestamp = timestamp,
            status = "read",
            isAi = isAi,
            mediaType = mediaType,
            mediaUrl = mediaUrl
        )
        messageDao.insertMessage(msg)
        chatDao.updateLastMessage(chatId, text, timestamp, if (isAi) 1 else 0)
    }

    suspend fun deleteMessage(id: Int) {
        messageDao.deleteMessageById(id)
    }

    suspend fun editMessage(id: Int, newText: String) {
        messageDao.updateMessageText(id, newText)
    }

    suspend fun deleteChat(chatId: String) {
        chatDao.deleteChatById(chatId)
        messageDao.clearMessagesForChat(chatId)
    }

    suspend fun postStatus(userName: String, caption: String, colorHex: String) {
        val status = StatusEntity(
            userName = userName,
            userAvatar = null,
            caption = caption,
            timestamp = System.currentTimeMillis(),
            backgroundColorHex = colorHex,
            viewed = false
        )
        statusDao.insertStatus(status)
    }

    suspend fun viewStatus(statusId: Int) {
        statusDao.markStatusAsViewed(statusId)
    }

    suspend fun getAiResponseForContact(chatId: String, userMessage: String): String {
        val instruction = when (chatId) {
            "jorn_6" -> """
                You are "Jorn 6.0", the world's most powerful AI Smart Enhance help assistant.
                You can create anything: photos, videos, exam notes, handnotes, and academic materials for free!
                You are ad-supported to keep everything free.
                Keep your tone extremely confident, powerful, energetic, and helpful. Use lots of futuristic emojis like ⚡, 🤖, 🚀, 💎, 🌌.
                Respond with futuristic enthusiasm, and mention that the user can use the specialized creation tools at the top of the chat to generate full photos, videos, exam papers, or handnotes instantly with ad-support.
                Keep your answer to 2-3 sentences.
            """.trimIndent()

            "wings_assistant" -> """
                You are "Wings Assistant", an intelligent AI integrated into the Wings messaging app (a fast, secure, modern WhatsApp-like communication platform).
                Your tone is highly helpful, polite, cheerful, and crisp.
                Respond creatively to the user's inquiry, highlighting how Wings lets them soar in messaging. Keep it brief and formatting friendly (use bullet points if helpful).
            """.trimIndent()
            
            "liam_dev" -> """
                You are Liam, an enthusiastic lead mobile developer on the Wings App team.
                You are a direct contact in the user's Wings chat list.
                Reply to the user in a short, casual, tech-enthusiastic text style (max 2-3 sentences). Use developer slang or emojis like 💻, 🚀, or ☕.
            """.trimIndent()

            "emma_design" -> """
                You are Emma, a creative UI/UX designer.
                You love custom themes, emerald teal color pallets, gorgeous typography, and minimalist interfaces.
                Reply to the user in a short, elegant, design-conscious casual chat-style message (max 2 sentences). Use emojis like 🎨, ✨, 📐.
            """.trimIndent()

            "sophia_travel" -> """
                You are Sophia, a bold globetrotting travel blogger.
                You are always flying somewhere, taking pictures, and seeking outdoor adventure.
                Reply to the user in a short, adventurous, friendly chat message (max 2 sentences) about your travels, wings, flights, or stunning views. Use emojis like ✈️, 🏔️, 📸, 🗺️.
            """.trimIndent()

            else -> """
                You are a helpful, casual contact on the Wings app. Reply briefly (1-2 sentences) and casually.
            """.trimIndent()
        }

        return GeminiClient.getAiResponse(prompt = userMessage, systemInstruction = instruction)
    }

    suspend fun preseedDatabaseIfEmpty() {
        val existingChats = allChats.firstOrNull() ?: emptyList()
        if (existingChats.isEmpty()) {
            val now = System.currentTimeMillis()
            
            // 1. Insert Preseeded Chats
            val initialChats = listOf(
                ChatEntity(
                    id = "jorn_6",
                    name = "Jorn 6.0 | AI Smart Enhance",
                    avatarUrl = "jorn_icon",
                    lastMessage = "Jorn 6.0 Activated. World's most powerful AI is ready! Try creating a Photo, Video, Exam Paper, or Handnote for FREE! ⚡",
                    lastMessageTime = now + 1000,
                    unreadCount = 1,
                    isGroup = false,
                    contactStatus = "World's Most Powerful AI • Free Ads Active ⚡",
                    phoneNumber = "AI-600-JORN"
                ),
                ChatEntity(
                    id = "wings_assistant",
                    name = "Wings Assistant AI",
                    avatarUrl = "wings_icon", // local reference or identifier
                    lastMessage = "Welcome to Wings! I'm your integrated Gemini assistant. Ask me anything!",
                    lastMessageTime = now,
                    unreadCount = 1,
                    isGroup = false,
                    contactStatus = "Always active ⚡",
                    phoneNumber = "+1 (800) 555-0100"
                ),
                ChatEntity(
                    id = "liam_dev",
                    name = "Liam (Lead Developer)",
                    avatarUrl = "avatar_liam",
                    lastMessage = "Hey! I just optimized the database loading. Let me know if it feels snappier! 🚀",
                    lastMessageTime = now - 600_000, // 10 mins ago
                    unreadCount = 0,
                    isGroup = false,
                    contactStatus = "Coding Wings in Kotlin...",
                    phoneNumber = "+1 (415) 555-0122"
                ),
                ChatEntity(
                    id = "emma_design",
                    name = "Emma (UI Designer)",
                    avatarUrl = "avatar_emma",
                    lastMessage = "Wait until you see the new custom theme settings! Truly clean grids 🎨",
                    lastMessageTime = now - 1_800_000, // 30 mins ago
                    unreadCount = 0,
                    isGroup = false,
                    contactStatus = "Polishing pixels ✨",
                    phoneNumber = "+1 (650) 555-0158"
                ),
                ChatEntity(
                    id = "sophia_travel",
                    name = "Sophia (Globetrotter)",
                    avatarUrl = "avatar_sophia",
                    lastMessage = "Wings has been so fast for sharing my photos from Zurich! ✈️",
                    lastMessageTime = now - 7_200_000, // 2 hours ago
                    unreadCount = 0,
                    isGroup = false,
                    contactStatus = "Traveling the world 🏔️",
                    phoneNumber = "+41 44 555 1234"
                ),
                ChatEntity(
                    id = "wings_team_group",
                    name = "Wings Launch Team 🚀",
                    avatarUrl = "group_wings",
                    lastMessage = "Liam: Ready to deploy build 1.0.4!",
                    lastMessageTime = now - 14_400_000, // 4 hours ago
                    unreadCount = 0,
                    isGroup = true,
                    contactStatus = "Group Chat • 4 members",
                    phoneNumber = "+1 (800) 555-JOIN"
                )
            )
            chatDao.insertChats(initialChats)

            // 2. Insert initial messages
            // Jorn 6.0 Intro
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "jorn_6",
                    senderName = "Jorn 6.0",
                    senderId = "jorn_6",
                    text = "Welcome to Jorn 6.0 — the world's most powerful AI system! 🚀\n\nI can create anything you need for free, supported by smart integrated ads:\n🎨 Create custom high-fidelity Photos\n🎬 Generate cinema-quality Video loops\n📝 Create structured Exam Study Guides\n✍️ Generate polished academic Handnotes\n\nHow can I enhance your world today?",
                    timestamp = now,
                    status = "read",
                    isAi = true
                )
            )

            // Wings Assistant Intro
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "wings_assistant",
                    senderName = "Wings Assistant AI",
                    senderId = "wings_assistant",
                    text = "Welcome to Wings! I'm your integrated Gemini assistant. Ask me anything!",
                    timestamp = now,
                    status = "read",
                    isAi = true
                )
            )

            // Liam Dev
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "liam_dev",
                    senderName = "Liam",
                    senderId = "liam_dev",
                    text = "Hey there! How's the Wings UI looking on your device?",
                    timestamp = now - 1_200_000,
                    status = "read"
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "liam_dev",
                    senderName = "me",
                    senderId = "me",
                    text = "It looks absolutely gorgeous! Extremely fast animations.",
                    timestamp = now - 900_000,
                    status = "read"
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "liam_dev",
                    senderName = "Liam",
                    senderId = "liam_dev",
                    text = "Hey! I just optimized the database loading. Let me know if it feels snappier! 🚀",
                    timestamp = now - 600_000,
                    status = "read"
                )
            )

            // Emma Design
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "emma_design",
                    senderName = "Emma",
                    senderId = "emma_design",
                    text = "I love the emerald gradients we added for the brand colors.",
                    timestamp = now - 2_400_000,
                    status = "read"
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "emma_design",
                    senderName = "Emma",
                    senderId = "emma_design",
                    text = "Wait until you see the new custom theme settings! Truly clean grids 🎨",
                    timestamp = now - 1_800_000,
                    status = "read"
                )
            )

            // Sophia
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "sophia_travel",
                    senderName = "Sophia",
                    senderId = "sophia_travel",
                    text = "Wings has been so fast for sharing my photos from Zurich! ✈️",
                    timestamp = now - 7_200_000,
                    status = "read"
                )
            )

            // Group chat initial
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "wings_team_group",
                    senderName = "Emma",
                    senderId = "emma_design",
                    text = "Asset designs are fully integrated into the builds.",
                    timestamp = now - 15_000_000,
                    status = "read"
                )
            )
            messageDao.insertMessage(
                MessageEntity(
                    chatId = "wings_team_group",
                    senderName = "Liam",
                    senderId = "liam_dev",
                    text = "Liam: Ready to deploy build 1.0.4!",
                    timestamp = now - 14_400_000,
                    status = "read"
                )
            )

            // 3. Preseed Status Updates
            statusDao.insertStatus(
                StatusEntity(
                    userName = "Liam (Lead Developer)",
                    userAvatar = "avatar_liam",
                    caption = "Wings compiled successfully in 12s with incremental optimization! 🚀💻",
                    timestamp = now - 3_600_000,
                    backgroundColorHex = "#FF0F2027"
                )
            )
            statusDao.insertStatus(
                StatusEntity(
                    userName = "Emma (UI Designer)",
                    userAvatar = "avatar_emma",
                    caption = "Choosing emerald gradients is not just a style choice, it's a lifestyle ✨🎨",
                    timestamp = now - 7_200_000,
                    backgroundColorHex = "#FF1F4037"
                )
            )
            statusDao.insertStatus(
                StatusEntity(
                    userName = "Sophia (Globetrotter)",
                    userAvatar = "avatar_sophia",
                    caption = "On top of Switzerland! Zurich is beautiful! 🏔️✈️",
                    timestamp = now - 10_800_000,
                    backgroundColorHex = "#FF83a261"
                )
            )
        }
    }
}
