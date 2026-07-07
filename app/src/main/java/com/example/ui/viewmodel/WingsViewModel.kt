package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WingsDatabase
import com.example.data.model.CallHistory
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusEntity
import com.example.data.repository.WingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class WingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WingsRepository

    init {
        val database = WingsDatabase.getDatabase(application)
        repository = WingsRepository(
            chatDao = database.chatDao(),
            messageDao = database.messageDao(),
            statusDao = database.statusDao()
        )
        // Preseed if DB is empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.preseedDatabaseIfEmpty()
        }
    }

    // Search and Chats filtering
    val searchQuery = MutableStateFlow("")
    
    val chats: StateFlow<List<ChatEntity>> = repository.allItemsStateFlow()
    
    private fun WingsRepository.allItemsStateFlow(): StateFlow<List<ChatEntity>> {
        return allChats.combine(searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter { it.name.contains(query, ignoreCase = true) || it.lastMessage.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Status updates
    val statuses: StateFlow<List<StatusEntity>> = repository.allStatuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State
    val activeChatId = MutableStateFlow<String?>(null)
    private val _activeChat = MutableStateFlow<ChatEntity?>(null)
    val activeChat: StateFlow<ChatEntity?> = _activeChat.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private var messageCollectionJob: Job? = null
    val isAiTyping = MutableStateFlow(false)

    // User Profile settings
    val userName = MutableStateFlow("Flyer One")
    val userStatusMessage = MutableStateFlow("Available • Flying with Wings ✈️")
    val activeThemeColor = MutableStateFlow("WhatsApp Light") // "WhatsApp Light", "WhatsApp Dark", "Professional Polish", "Emerald Teal", "Sunset Orange", "Indigo Cyber"

    // New Advanced Privacy & Control States
    val readReceiptsEnabled = MutableStateFlow(true)
    val lastSeenSetting = MutableStateFlow("Everyone") // "Everyone", "My Contacts", "Nobody"
    val onlineStatusSetting = MutableStateFlow("Everyone") // "Everyone", "Same as Last Seen"
    val twoStepVerificationEnabled = MutableStateFlow(false)
    val securityLockType = MutableStateFlow("None") // "None", "Passcode", "Fingerprint", "Face Lock"
    
    // Feature interactions
    val blockedContactIds = MutableStateFlow<Set<String>>(emptySet())
    val starredMessageIds = MutableStateFlow<Set<Int>>(emptySet())
    val pinnedChatIds = MutableStateFlow<Set<String>>(emptySet())
    val archivedChatIds = MutableStateFlow<Set<String>>(emptySet())
    val mutedChatIds = MutableStateFlow<Set<String>>(emptySet())
    
    // Wallpaper Selection
    val globalWallpaperType = MutableStateFlow("Classic Wings") // "Classic Wings", "Cherry Blossom", "Deep Cyber Cosmic", "Emerald Forest", "Sunset Peach"
    val userProfilePhoto = MutableStateFlow<String?>("avatar_default")
    
    // Group Poll & Message Replied Reference
    val pollVotes = MutableStateFlow<Map<Int, Map<String, Int>>>(emptyMap()) // messageId -> (option -> count)
    val pollUserVotes = MutableStateFlow<Map<Int, String>>(emptyMap()) // messageId -> selectedOption
    val messageRepliedTo = MutableStateFlow<MessageEntity?>(null)

    // Call History and Active Call Overlay State
    private val _callHistory = MutableStateFlow<List<CallHistory>>(emptyList())
    val callHistory: StateFlow<List<CallHistory>> = _callHistory.asStateFlow()

    val activeCall = MutableStateFlow<CallHistory?>(null)
    val isCallActive = MutableStateFlow(false)
    val callDurationSeconds = MutableStateFlow(0)
    private var callTimerJob: Job? = null

    // Privacy & Security Toggles
    val screenshotPreventionEnabled = MutableStateFlow(false)
    val chatDisappearingMessages = MutableStateFlow(false) // e.g. 24h, 7d, off
    val chatLockEnabled = MutableStateFlow(false)

    // Call Privacy & Controls
    val callMuted = MutableStateFlow(false)
    val callCameraEnabled = MutableStateFlow(true)
    val callBlurBackground = MutableStateFlow(false)
    val callMembersCount = MutableStateFlow(2) // 2 means standard 1-on-1 call, 3+ means multi-member/group calling

    init {
        // Initialize call list
        val now = System.currentTimeMillis()
        _callHistory.value = listOf(
            CallHistory("1", "Liam (Lead Developer)", "avatar_liam", now - 3600 * 1000 * 2, isIncoming = true, isVideo = true, missed = true),
            CallHistory("2", "Emma (UI Designer)", "avatar_emma", now - 3600 * 1000 * 5, isIncoming = false, isVideo = false, missed = false),
            CallHistory("3", "Sophia (Globetrotter)", "avatar_sophia", now - 3600 * 1000 * 24, isIncoming = true, isVideo = true, missed = false)
        )

        // React to active chat selection
        viewModelScope.launch {
            activeChatId.collect { chatId ->
                messageCollectionJob?.cancel()
                if (chatId != null) {
                    // Update current chat details
                    val db = WingsDatabase.getDatabase(getApplication())
                    val chatObj = db.chatDao().getChatById(chatId)
                    _activeChat.value = chatObj
                    
                    // Clear unreads
                    repository.clearUnread(chatId)

                    // Collect messages flow
                    messageCollectionJob = launch {
                        repository.getMessagesForChat(chatId).collect { list ->
                            _messages.value = list
                        }
                    }
                } else {
                    _activeChat.value = null
                    _messages.value = emptyList()
                }
            }
        }
    }

    fun selectChat(chatId: String?) {
        activeChatId.value = chatId
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    fun sendJornCreation(text: String, mediaType: String? = null, mediaUrl: String? = null) {
        val chatId = activeChatId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                senderId = "jorn_6",
                senderName = "Jorn 6.0",
                text = text,
                isAi = true,
                mediaType = mediaType,
                mediaUrl = mediaUrl
            )
        }
    }

    // Send a user message and trigger dynamic AI responses based on contact persona
    fun sendMessage(text: String, mediaType: String? = null, mediaUrl: String? = null) {
        val chatId = activeChatId.value ?: return
        if (text.isBlank() && mediaUrl == null) return

        val replied = messageRepliedTo.value
        val finalTxt = if (replied != null) {
            val sender = if (replied.senderId == "me") "You" else replied.senderName
            "💬 Replying to $sender: \"${replied.text.take(30)}\"\n\n$text"
        } else {
            text
        }

        viewModelScope.launch {
            // Clear reply state immediately
            messageRepliedTo.value = null

            // 1. Send user message
            repository.sendMessage(
                chatId = chatId,
                senderId = "me",
                senderName = userName.value,
                text = finalTxt,
                isAi = false,
                mediaType = mediaType,
                mediaUrl = mediaUrl
            )

            // 2. Automated smart reply if talking to one of our contacts
            delay(800) // Small organic buffer
            isAiTyping.value = true
            
            val responseText = repository.getAiResponseForContact(chatId, text)
            
            // Simulating Typing duration
            val typingDelay = (responseText.length * 20L).coerceIn(1200L, 4000L)
            delay(typingDelay)
            
            isAiTyping.value = false
            
            val contactName = _activeChat.value?.name ?: "Wings Friend"
            repository.sendMessage(
                chatId = chatId,
                senderId = chatId,
                senderName = contactName,
                text = responseText,
                isAi = true
            )
        }
    }

    // Posting a new text-based status update (Story)
    fun addNewStatus(caption: String, bgHex: String) {
        if (caption.isBlank()) return
        viewModelScope.launch {
            repository.postStatus(
                userName = userName.value,
                caption = caption,
                colorHex = bgHex
            )
        }
    }

    fun markStatusViewed(statusId: Int) {
        viewModelScope.launch {
            repository.viewStatus(statusId)
        }
    }

    // Call Actions
    fun initiateCall(contactName: String, avatarUrl: String?, isVideo: Boolean) {
        val call = CallHistory(
            id = UUID.randomUUID().toString(),
            name = contactName,
            avatarUrl = avatarUrl,
            timestamp = System.currentTimeMillis(),
            isIncoming = false,
            isVideo = isVideo,
            missed = false
        )
        activeCall.value = call
        isCallActive.value = true
        callDurationSeconds.value = 0

        // Start Call Timer simulating real-time connection
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            // Simulate 2 seconds of ringing, then connect
            delay(2500)
            while (isCallActive.value) {
                delay(1000)
                callDurationSeconds.value += 1
            }
        }

        // Add to call history list
        val currentList = _callHistory.value.toMutableList()
        currentList.add(0, call)
        _callHistory.value = currentList
    }

    fun endCall() {
        callTimerJob?.cancel()
        isCallActive.value = false
        activeCall.value = null
        callDurationSeconds.value = 0
    }

    fun createNewMockChat(contactName: String, initialStatus: String, phoneNumber: String = "") {
        val contactId = "contact_" + UUID.randomUUID().toString().take(6)
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            val db = WingsDatabase.getDatabase(getApplication())
            val newChat = ChatEntity(
                id = contactId,
                name = contactName,
                avatarUrl = null,
                lastMessage = "Hey! Let's chat on Wings ✈️",
                lastMessageTime = now,
                unreadCount = 0,
                isGroup = false,
                contactStatus = initialStatus,
                phoneNumber = phoneNumber
            )
            db.chatDao().insertChat(newChat)
            
            // Add welcome message
            repository.sendMessage(
                chatId = contactId,
                senderId = contactId,
                senderName = contactName,
                text = "Hey there! Thanks for adding me on Wings ✈️. How is everything going?",
                isAi = true
            )
        }
    }

    fun deleteMessage(msgId: Int) {
        viewModelScope.launch {
            repository.deleteMessage(msgId)
        }
    }

    fun editMessage(msgId: Int, newText: String) {
        viewModelScope.launch {
            repository.editMessage(msgId, newText)
        }
    }

    fun toggleBlockContact(chatId: String) {
        val current = blockedContactIds.value.toMutableSet()
        if (current.contains(chatId)) {
            current.remove(chatId)
        } else {
            current.add(chatId)
        }
        blockedContactIds.value = current
    }

    fun toggleStarMessage(msgId: Int) {
        val current = starredMessageIds.value.toMutableSet()
        if (current.contains(msgId)) {
            current.remove(msgId)
        } else {
            current.add(msgId)
        }
        starredMessageIds.value = current
    }

    fun togglePinChat(chatId: String) {
        val current = pinnedChatIds.value.toMutableSet()
        if (current.contains(chatId)) {
            current.remove(chatId)
        } else {
            current.add(chatId)
        }
        pinnedChatIds.value = current
    }

    fun toggleArchiveChat(chatId: String) {
        val current = archivedChatIds.value.toMutableSet()
        if (current.contains(chatId)) {
            current.remove(chatId)
        } else {
            current.add(chatId)
        }
        archivedChatIds.value = current
    }

    fun toggleMuteChat(chatId: String) {
        val current = mutedChatIds.value.toMutableSet()
        if (current.contains(chatId)) {
            current.remove(chatId)
        } else {
            current.add(chatId)
        }
        mutedChatIds.value = current
    }

    fun voteInPoll(msgId: Int, option: String) {
        val currentVotes = pollVotes.value.toMutableMap()
        val optionsMap = currentVotes[msgId]?.toMutableMap() ?: mutableMapOf()
        
        // Remove previous vote if any
        val previousSelected = pollUserVotes.value[msgId]
        if (previousSelected != null && optionsMap.containsKey(previousSelected)) {
            optionsMap[previousSelected] = (optionsMap[previousSelected] ?: 1) - 1
        }
        
        // Add new vote
        optionsMap[option] = (optionsMap[option] ?: 0) + 1
        currentVotes[msgId] = optionsMap
        pollVotes.value = currentVotes
        
        // Track user choice
        val currentUserChoices = pollUserVotes.value.toMutableMap()
        currentUserChoices[msgId] = option
        pollUserVotes.value = currentUserChoices
    }

    fun broadcastMessage(text: String) {
        viewModelScope.launch {
            val allActiveChats = chats.value
            allActiveChats.forEach { chat ->
                if (!chat.isGroup && chat.id != "jorn_6" && chat.id != "wings_assistant") {
                    repository.sendMessage(
                        chatId = chat.id,
                        senderId = "me",
                        senderName = userName.value,
                        text = "[Broadcast] $text",
                        isAi = false
                    )
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }
}
