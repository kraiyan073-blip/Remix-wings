package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CallHistory
import com.example.data.model.ChatEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusEntity
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WingsViewModel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

class MainActivity : ComponentActivity() {
    private val viewModel: WingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WingsApp(viewModel)
            }
        }
    }
}

// --- Dynamic Color Theme Definitions ---
data class WingsThemeColors(
    val primary: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val onBackground: Color,
    val onSurface: Color,
    val activeGlow: Color,
    val accent: Color,
    val bubbleMe: Color,
    val bubbleOther: Color
)

fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return if (parts.size >= 2) {
        (parts[0].take(1) + parts[1].take(1)).uppercase()
    } else {
        name.take(2).uppercase()
    }
}

fun getAvatarColors(name: String, themeColors: WingsThemeColors): Pair<Color, Color> {
    if (themeColors.background != Color.White) {
        // Dark theme: use default container and primary tint
        return Pair(themeColors.primaryContainer, themeColors.primary)
    }
    // Professional Polish light theme: use beautiful pastel shades matching Tailwind specs
    val hash = name.hashCode()
    return when (Math.abs(hash) % 4) {
        0 -> Pair(Color(0xFFDBEAFE), Color(0xFF0066FF)) // blue-100, blue-700
        1 -> Pair(Color(0xFFF3E8FF), Color(0xFF9333EA)) // purple-100, purple-700
        2 -> Pair(Color(0xFFD1FAE5), Color(0xFF047857)) // emerald-100, emerald-700
        else -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C)) // red-100, red-700
    }
}

@Composable
fun getThemeColors(themeName: String): WingsThemeColors {
    return when (themeName) {
        "Sunset Orange" -> WingsThemeColors(
            primary = Color(0xFFFF512F),
            primaryContainer = Color(0x33FF512F),
            background = Color(0xFF150D0A),
            surface = Color(0xFF221410),
            onBackground = Color(0xFFFFF3F0),
            onSurface = Color(0xFFFFF3F0),
            activeGlow = Color(0xFFFF512F),
            accent = Color(0xFFDD2476),
            bubbleMe = Color(0xFF9E2C1A),
            bubbleOther = Color(0xFF2E1B17)
        )
        "Indigo Cyber" -> WingsThemeColors(
            primary = Color(0xFF4776E6),
            primaryContainer = Color(0x334776E6),
            background = Color(0xFF0A0B10),
            surface = Color(0xFF131520),
            onBackground = Color(0xFFECEEFF),
            onSurface = Color(0xFFECEEFF),
            activeGlow = Color(0xFF00E5FF),
            accent = Color(0xFF8E54E9),
            bubbleMe = Color(0xFF2C3E6B),
            bubbleOther = Color(0xFF1D1E2C)
        )
        "Emerald Teal" -> WingsThemeColors(
            primary = Color(0xFF00B4DB),
            primaryContainer = Color(0x2200B4DB),
            background = Color(0xFF0A121E),
            surface = Color(0xFF121F32),
            onBackground = Color(0xFFE5EFF6),
            onSurface = Color(0xFFE5EFF6),
            activeGlow = Color(0xFF00FF87),
            accent = Color(0xFF0083B0),
            bubbleMe = Color(0xFF084B61),
            bubbleOther = Color(0xFF182E47)
        )
        "WhatsApp Dark" -> WingsThemeColors(
            primary = Color(0xFF00A884),
            primaryContainer = Color(0xFF005C4B),
            background = Color(0xFF0B141A),
            surface = Color(0xFF111B21),
            onBackground = Color(0xFFE9EDEF),
            onSurface = Color(0xFF8696A0),
            activeGlow = Color(0xFF25D366),
            accent = Color(0xFF005C4B),
            bubbleMe = Color(0xFF005C4B),
            bubbleOther = Color(0xFF202C33)
        )
        "WhatsApp Light" -> WingsThemeColors(
            primary = Color(0xFF128C7E),
            primaryContainer = Color(0xFFD9FDD3),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFECEFF1),
            onBackground = Color(0xFF1F2C34),
            onSurface = Color(0xFF667781),
            activeGlow = Color(0xFF25D366),
            accent = Color(0xFF075E54),
            bubbleMe = Color(0xFFD9FDD3),
            bubbleOther = Color(0xFFFFFFFF)
        )
        else -> WingsThemeColors( // "Professional Polish" Default Light Theme
            primary = Color(0xFF0066FF),
            primaryContainer = Color(0xFFD3E3FD),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF7F9FB),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF49454F),
            activeGlow = Color(0xFF00E676),
            accent = Color(0xFF041E49),
            bubbleMe = Color(0xFFD3E3FD),
            bubbleOther = Color(0xFFF3F4F6)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WingsApp(viewModel: WingsViewModel) {
    val activeTheme by viewModel.activeThemeColor.collectAsStateWithLifecycle()
    val colors = getThemeColors(activeTheme)
    val coroutineScope = rememberCoroutineScope()

    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val statuses by viewModel.statuses.collectAsStateWithLifecycle()
    val callHistory by viewModel.callHistory.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatId.collectAsStateWithLifecycle()
    val activeChat by viewModel.activeChat.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isAiTyping by viewModel.isAiTyping.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val isCallActive by viewModel.isCallActive.collectAsStateWithLifecycle()
    val callDurationSeconds by viewModel.callDurationSeconds.collectAsStateWithLifecycle()

    val screenshotPreventionEnabled by viewModel.screenshotPreventionEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(screenshotPreventionEnabled) {
        val activity = context as? android.app.Activity
        if (activity != null) {
            if (screenshotPreventionEnabled) {
                activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    var currentTab by remember { mutableStateOf(0) } // 0: CHATS, 1: STATUS, 2: CALLS
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showCreateStatusDialog by remember { mutableStateOf(false) }
    var activeStatusViewerList by remember { mutableStateOf<List<StatusEntity>?>(null) }
    var activeStatusViewerIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        containerColor = colors.background,
        topBar = {
            if (activeChatId == null && !isCallActive && activeStatusViewerList == null) {
                Column {
                    val isWhatsAppLight = activeTheme == "WhatsApp Light"
                    val topAppBarBg = if (isWhatsAppLight) colors.accent else colors.surface
                    val topAppBarTitleColor = if (isWhatsAppLight) Color.White else colors.onBackground
                    val topAppBarIconColor = if (isWhatsAppLight) Color.White else colors.primary

                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isWhatsAppLight) Color.White.copy(alpha = 0.2f) else colors.primary)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Wings",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 22.sp,
                                    color = topAppBarTitleColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = topAppBarBg
                        ),
                        actions = {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Preseeds",
                                    tint = topAppBarIconColor
                                )
                            }
                            IconButton(onClick = { showSettingsScreen = true }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = topAppBarIconColor
                                )
                            }
                        }
                    )

                    // Mini Customised Search Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (colors.background == Color.White) Color(0xFFF3F4F6) else colors.surface)
                            .border(1.dp, colors.primary.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                            .testTag("search_bar")
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search chats or numbers...",
                                        color = Color.Gray.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.searchQuery.value = it },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = colors.onBackground,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.searchQuery.value = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Navigation Tabs
                    val tabRowBg = if (isWhatsAppLight) colors.accent else colors.surface
                    val tabIndicatorColor = if (isWhatsAppLight) Color(0xFF25D366) else colors.primary

                    TabRow(
                        selectedTabIndex = currentTab,
                        containerColor = tabRowBg,
                        contentColor = if (isWhatsAppLight) Color.White else colors.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                                color = tabIndicatorColor
                            )
                        }
                    ) {
                        val tabs = listOf(
                            "CHATS" to Icons.Default.Chat,
                            "STATUS" to Icons.Default.Camera,
                            "CALLS" to Icons.Default.Call,
                            "FEEDS" to Icons.Default.Public,
                            "AI STUDIO" to Icons.Default.AutoAwesome
                        )
                        tabs.forEachIndexed { index, (label, icon) ->
                            val isSelected = currentTab == index
                            val itemColor = if (isSelected) {
                                if (isWhatsAppLight) Color.White else colors.primary
                            } else {
                                if (isWhatsAppLight) Color.White.copy(alpha = 0.6f) else Color.Gray
                            }

                            Tab(
                                selected = isSelected,
                                onClick = { currentTab = index },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(16.dp),
                                            tint = itemColor
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = itemColor
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (activeChatId == null && !isCallActive && activeStatusViewerList == null) {
                FloatingActionButton(
                    onClick = {
                        when (currentTab) {
                            0 -> showAddContactDialog = true
                            1 -> showCreateStatusDialog = true
                            2 -> {
                                if (chats.isNotEmpty()) {
                                    val firstChat = chats.first()
                                    viewModel.initiateCall(firstChat.name, firstChat.avatarUrl, isVideo = false)
                                }
                            }
                            3 -> {
                                viewModel.userName.value = "Commander Wings"
                                viewModel.userStatusMessage.value = "Always Flying high 🪶"
                            }
                        }
                    },
                    containerColor = colors.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_action")
                ) {
                    val icon = when (currentTab) {
                        0 -> Icons.Default.AddComment
                        1 -> Icons.Default.Create
                        2 -> Icons.Default.PhoneEnabled
                        3 -> Icons.Default.AutoAwesome
                        else -> Icons.Default.Bolt
                    }
                    Icon(imageVector = icon, contentDescription = "FAB Action")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Dashboard Pages
            if (activeChatId == null) {
                when (currentTab) {
                    0 -> ChatsTabScreen(chats, colors, viewModel)
                    1 -> StatusTabScreen(statuses, colors, viewModel) { list, index ->
                        activeStatusViewerList = list
                        activeStatusViewerIndex = index
                    }
                    2 -> CallsTabScreen(callHistory, colors, viewModel)
                    3 -> FeedsTabScreen(colors, viewModel)
                    4 -> AiStudioTabScreen(colors, viewModel)
                }
            }

            // Slide-in Active Chat Screen
            AnimatedVisibility(
                visible = activeChatId != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                if (activeChatId != null && activeChat != null) {
                    ActiveChatWindow(
                        chat = activeChat!!,
                        messages = messages,
                        colors = colors,
                        isTyping = isAiTyping,
                        viewModel = viewModel,
                        onBack = { viewModel.selectChat(null) }
                    )
                }
            }

            // Calling Screen Overlay
            AnimatedVisibility(
                visible = isCallActive && activeCall != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeCall != null) {
                    CallingOverlayScreen(
                        call = activeCall!!,
                        count = callDurationSeconds,
                        colors = colors,
                        viewModel = viewModel,
                        onEndCall = { viewModel.endCall() }
                    )
                }
            }

            // Status Immersive Viewer Overlay
            AnimatedVisibility(
                visible = activeStatusViewerList != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeStatusViewerList != null) {
                    StatusImmersiveViewer(
                        statusList = activeStatusViewerList!!,
                        startIndex = activeStatusViewerIndex,
                        colors = colors,
                        viewModel = viewModel,
                        onFinished = {
                            activeStatusViewerList = null
                        }
                    )
                }
            }

            // Slide-in Settings Screen Overlay
            AnimatedVisibility(
                visible = showSettingsScreen,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it })
            ) {
                SettingsOverlayScreen(
                    colors = colors,
                    viewModel = viewModel,
                    onBack = { showSettingsScreen = false }
                )
            }
        }
    }

    // New Contact Dialog
    if (showAddContactDialog) {
        AddContactDialog(
            colors = colors,
            onDismiss = { showAddContactDialog = false },
            onConfirm = { name, status, phone ->
                viewModel.createNewMockChat(name, status, phone)
                showAddContactDialog = false
            }
        )
    }

    // New Status Dialog
    if (showCreateStatusDialog) {
        CreateStatusDialog(
            colors = colors,
            onDismiss = { showCreateStatusDialog = false },
            onConfirm = { text, bgHex ->
                viewModel.addNewStatus(text, bgHex)
                showCreateStatusDialog = false
            }
        )
    }
}

// ==========================================
// --- SCREEN TABS IMPLEMENTATION ---
// ==========================================

@Composable
fun ChatsTabScreen(
    chats: List<ChatEntity>,
    colors: WingsThemeColors,
    viewModel: WingsViewModel
) {
    val pinnedChats by viewModel.pinnedChatIds.collectAsStateWithLifecycle()
    val archivedChats by viewModel.archivedChatIds.collectAsStateWithLifecycle()
    val mutedChats by viewModel.mutedChatIds.collectAsStateWithLifecycle()
    val blockedContacts by viewModel.blockedContactIds.collectAsStateWithLifecycle()

    var showArchivedOnly by remember { mutableStateOf(false) }
    var actionDialogChat by remember { mutableStateOf<ChatEntity?>(null) }

    val filteredChats = remember(chats, pinnedChats, archivedChats, showArchivedOnly) {
        val list = chats.filter { chat ->
            val isArchived = archivedChats.contains(chat.id)
            if (showArchivedOnly) isArchived else !isArchived
        }
        // Sort: Pinned chats first, then by lastMessageTime DESC
        list.sortedWith { c1, c2 ->
            val p1 = pinnedChats.contains(c1.id)
            val p2 = pinnedChats.contains(c2.id)
            when {
                p1 && !p2 -> -1
                !p1 && p2 -> 1
                else -> c2.lastMessageTime.compareTo(c1.lastMessageTime)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showArchivedOnly) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .clickable { showArchivedOnly = false }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Archived Chats", color = colors.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = colors.surface.copy(alpha = 0.5f))
        } else if (archivedChats.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showArchivedOnly = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Archive, contentDescription = "Archived", tint = colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Archived Chats", color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = archivedChats.size.toString(),
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            HorizontalDivider(color = colors.surface.copy(alpha = 0.3f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }

        if (filteredChats.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(
                        Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colors.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (showArchivedOnly) "No archived chats" else "No active wings chats",
                        color = colors.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showArchivedOnly) "Archived chats will appear here." else "Tap the chat button below to add your first smart contact!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredChats) { chat ->
                    val isPinned = pinnedChats.contains(chat.id)
                    val isMuted = mutedChats.contains(chat.id)
                    val isBlocked = blockedContacts.contains(chat.id)

                    Column {
                        // Wrap in Box to support long press
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(chat) {
                                    detectTapGestures(
                                        onLongPress = {
                                            actionDialogChat = chat
                                        },
                                        onTap = {
                                            viewModel.selectChat(chat.id)
                                        }
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("chat_item_${chat.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Styled Initial/Avatar circular badge
                                val isJorn = chat.id == "jorn_6"
                                val avatarPair = getAvatarColors(chat.name, colors)
                                val avatarBg = if (isJorn) Color.Transparent else if (chat.id == "wings_assistant") colors.primaryContainer else avatarPair.first
                                val avatarTint = if (isJorn) Color(0xFFFFD600) else if (chat.id == "wings_assistant") colors.primary else avatarPair.second

                                val rainbowBrush = Brush.sweepGradient(
                                    colors = listOf(
                                        Color(0xFFFF3D00),
                                        Color(0xFFFFD600),
                                        Color(0xFF00E5FF),
                                        Color(0xFFE040FB),
                                        Color(0xFFFF3D00)
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isJorn) {
                                                Brush.radialGradient(listOf(Color(0xFF2C1A04), Color(0xFF0D0701)))
                                            } else {
                                                Brush.linearGradient(listOf(avatarBg, avatarBg))
                                            }
                                        )
                                        .then(
                                            if (isJorn) {
                                                Modifier.border(
                                                    width = 2.5.dp,
                                                    brush = rainbowBrush,
                                                    shape = CircleShape
                                                )
                                            } else {
                                                Modifier.border(
                                                    width = if (colors.background == Color.White) 1.5.dp else 2.dp,
                                                    color = if (colors.background == Color.White) Color.White else (if (chat.id == "wings_assistant") colors.activeGlow else colors.primary.copy(alpha = 0.4f)),
                                                    shape = CircleShape
                                                )
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isJorn) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD600),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    } else if (chat.id == "wings_assistant") {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else if (chat.id == "wings_team_group") {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = avatarTint,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        Text(
                                            text = getInitials(chat.name),
                                            color = avatarTint,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }

                                    // Tiny Online Status glow
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(colors.activeGlow)
                                            .border(1.5.dp, colors.surface, CircleShape)
                                            .align(Alignment.BottomEnd)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = chat.name,
                                                color = colors.onBackground,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isPinned) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = colors.primary, modifier = Modifier.size(12.dp))
                                            }
                                            if (isMuted) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.VolumeMute, contentDescription = "Muted", tint = Color.Gray, modifier = Modifier.size(12.dp))
                                            }
                                            if (isBlocked) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Block, contentDescription = "Blocked Contact", tint = Color.Red, modifier = Modifier.size(12.dp))
                                            }
                                            if (chat.id == "jorn_6") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD600)),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "JORN 6.0",
                                                        color = Color.Black,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else if (chat.id == "wings_assistant") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "AI",
                                                        color = colors.primary,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            if (chat.phoneNumber.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = chat.phoneNumber,
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                        }

                                        val timeStr = remember(chat.lastMessageTime) {
                                            val mins = ((System.currentTimeMillis() - chat.lastMessageTime) / 60000).toInt()
                                            when {
                                                mins < 1 -> "now"
                                                mins < 60 -> "${mins}m ago"
                                                mins < 1440 -> "${mins / 60}h ago"
                                                else -> "yesterday"
                                            }
                                        }
                                        Text(
                                            text = timeStr,
                                            color = if (chat.unreadCount > 0 && colors.background == Color.White) colors.primary else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBlocked) "🚫 This contact is blocked" else chat.lastMessage,
                                            color = if (colors.background == Color.White) Color(0xFF4B5563) else Color.LightGray,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (chat.unreadCount > 0 && !isMuted) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chat.unreadCount.toString(),
                                                    color = colors.background,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = colors.surface.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 84.dp))
                    }
                }
            }
        }
    }

    // Context Action Dialog
    if (actionDialogChat != null) {
        val chat = actionDialogChat!!
        val isPinned = pinnedChats.contains(chat.id)
        val isMuted = mutedChats.contains(chat.id)
        val isArchived = archivedChats.contains(chat.id)
        val isBlocked = blockedContacts.contains(chat.id)

        AlertDialog(
            onDismissRequest = { actionDialogChat = null },
            title = { Text(text = chat.name, fontWeight = FontWeight.Bold, color = colors.onBackground) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            viewModel.togglePinChat(chat.id)
                            actionDialogChat = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isPinned) "Unpin Chat" else "Pin Chat", color = colors.onBackground)
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.toggleMuteChat(chat.id)
                            actionDialogChat = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (isMuted) Icons.Default.VolumeUp else Icons.Default.VolumeMute, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isMuted) "Unmute Chat" else "Mute Notifications", color = colors.onBackground)
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.toggleArchiveChat(chat.id)
                            actionDialogChat = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Archive, contentDescription = null, tint = colors.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isArchived) "Unarchive Chat" else "Archive Chat", color = colors.onBackground)
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.toggleBlockContact(chat.id)
                            actionDialogChat = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = if (isBlocked) "Unblock Contact" else "Block Contact", color = Color.Red)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.deleteChat(chat.id)
                            actionDialogChat = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Delete Chat", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { actionDialogChat = null }) {
                    Text("Cancel", color = colors.primary)
                }
            }
        )
    }
}

@Composable
fun StatusTabScreen(
    statuses: List<StatusEntity>,
    colors: WingsThemeColors,
    viewModel: WingsViewModel,
    onStatusClicked: (List<StatusEntity>, Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // My status cell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Let user write a text status
                    viewModel.addNewStatus("Floating through lines of code! 💻🪶", "#FF1F4037")
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text("My Status", color = colors.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tap to write a status update", color = Color.Gray, fontSize = 13.sp)
            }
        }

        Text(
            text = "Recent updates",
            color = colors.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (statuses.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No statuses available yet.", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(statuses.size) { index ->
                    val status = statuses[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStatusClicked(statuses, index)
                                viewModel.markStatusViewed(status.id)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("status_item_${status.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored gradient circle representation
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(status.backgroundColorHex)))
                                .border(
                                    width = 2.dp,
                                    color = if (status.viewed) Color.Gray else colors.activeGlow,
                                    shape = CircleShape
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getInitials(status.userName),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(status.userName, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            val relativeTime = remember(status.timestamp) {
                                val mins = ((System.currentTimeMillis() - status.timestamp) / 60000).toInt()
                                when {
                                    mins < 1 -> "Just now"
                                    mins < 60 -> "$mins minutes ago"
                                    else -> "${mins / 60} hours ago"
                                }
                            }
                            Text(relativeTime, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                    HorizontalDivider(color = colors.surface.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 84.dp))
                }
            }
        }
    }
}

@Composable
fun CallsTabScreen(
    callHistory: List<CallHistory>,
    colors: WingsThemeColors,
    viewModel: WingsViewModel
) {
    if (callHistory.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recent wings calls.", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(callHistory) { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("call_item_${call.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatarPair = getAvatarColors(call.name, colors)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(avatarPair.first),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(call.name),
                            color = avatarPair.second,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(call.name, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (call.isIncoming) Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (call.missed) Color.Red else Color.Green,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val relativeTime = remember(call.timestamp) {
                                val mins = ((System.currentTimeMillis() - call.timestamp) / 60000).toInt()
                                when {
                                    mins < 60 -> "$mins m ago"
                                    else -> "${mins / 60} h ago"
                                }
                            }
                            Text(relativeTime, color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    IconButton(onClick = {
                        viewModel.initiateCall(call.name, call.avatarUrl, call.isVideo)
                    }) {
                        Icon(
                            imageVector = if (call.isVideo) Icons.Default.VideoCall else Icons.Default.Call,
                            contentDescription = "Redial",
                            tint = colors.primary
                        )
                    }
                }
                HorizontalDivider(color = colors.surface.copy(alpha = 0.5f), thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
            }
        }
    }
}

@Composable
fun FeedsTabScreen(
    colors: WingsThemeColors,
    viewModel: WingsViewModel
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Communities, 1 = Channels
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.surface,
            contentColor = colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Communities 👥", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Channels 📢", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // New Community button
                    Button(
                        onClick = {
                            viewModel.broadcastMessage("Welcome to our brand new Community Space!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Community", color = Color.White)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Wings Developer Alliance", fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 16.sp)
                                    Text("3 subgroups • 12.4K members", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "The official community for builders, designers, and enthusiasts using the Wings Smart Protocol. Collaborate, share code, and discuss Jorn 6.0 features!",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = colors.surface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Subgroups
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = colors.activeGlow, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Announcements", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.onBackground)
                                    Text("Liam (Lead Dev): Version 6.0 update is live!", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Forum, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("General Chat", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.onBackground)
                                    Text("Sophia (Travel): Check out these scenic screens!", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Channels
            var isFollowingOfficial by remember { mutableStateOf(false) }
            var isFollowingJorn by remember { mutableStateOf(true) }
            var officialReactions by remember { mutableStateOf(1420) }
            var jornReactions by remember { mutableStateOf(8452) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Wings Official Channel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(colors.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.primary)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Wings Official 🪶", fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 15.sp)
                                        Text("1.2M followers", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }

                                TextButton(
                                    onClick = { isFollowingOfficial = !isFollowingOfficial },
                                    colors = ButtonDefaults.textButtonColors(contentColor = colors.primary)
                                ) {
                                    Text(if (isFollowingOfficial) "Following ✓" else "+ Follow")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Welcome to the Wings Channel! We are rolling out ultra-fast end-to-end encryption pipelines and beautiful per-chat wallpapers in our latest build. Tap follow to stay informed!",
                                color = colors.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive reaction
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.primaryContainer.copy(alpha = 0.4f))
                                    .clickable { officialReactions++ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(officialReactions.toString(), fontWeight = FontWeight.Bold, color = colors.primary, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    // Jorn AI feed
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFD600).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD600))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Jorn 6.0 Intelligence Feed ⚡", fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 15.sp)
                                        Text("840K followers", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }

                                TextButton(
                                    onClick = { isFollowingJorn = !isFollowingJorn },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFD600))
                                ) {
                                    Text(if (isFollowingJorn) "Following ✓" else "+ Follow", color = if (isFollowingJorn) colors.primary else Color(0xFFFFD600))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "BREAKING: Jorn 6.0 is now performing exam assistance and digital note crafting in milliseconds! Tap below to fuel our next neural training loop.",
                                color = colors.onBackground.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFFD600).copy(alpha = 0.1f))
                                    .clickable { jornReactions++ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚡", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(jornReactions.toString(), fontWeight = FontWeight.Bold, color = Color(0xFFFFD600), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsTabScreen(
    colors: WingsThemeColors,
    viewModel: WingsViewModel
) {
    val name by viewModel.userName.collectAsStateWithLifecycle()
    val status by viewModel.userStatusMessage.collectAsStateWithLifecycle()
    val activeTheme by viewModel.activeThemeColor.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val screenshotPreventionEnabled by viewModel.screenshotPreventionEnabled.collectAsStateWithLifecycle()
    val chatDisappearingMessages by viewModel.chatDisappearingMessages.collectAsStateWithLifecycle()
    val chatLockEnabled by viewModel.chatLockEnabled.collectAsStateWithLifecycle()
    val callMuted by viewModel.callMuted.collectAsStateWithLifecycle()
    val callCameraEnabled by viewModel.callCameraEnabled.collectAsStateWithLifecycle()
    val callBlurBackground by viewModel.callBlurBackground.collectAsStateWithLifecycle()
    val callMembersCount by viewModel.callMembersCount.collectAsStateWithLifecycle()

    var editingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.primaryContainer)
                            .border(2.dp, colors.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(2).uppercase(), color = colors.primary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        if (editingName) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.userName.value = nameInput
                                        editingName = false
                                    }) {
                                        Icon(Icons.Default.Check, contentDescription = "Save", tint = colors.primary)
                                    }
                                }
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = colors.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                IconButton(onClick = {
                                    nameInput = name
                                    editingName = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color.Gray)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(status, color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wings Brand Selection Themes
        Text("Customize Wings Theme", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val themes = listOf("WhatsApp Light", "WhatsApp Dark", "Professional Polish", "Emerald Teal", "Sunset Orange", "Indigo Cyber")
        themes.forEach { t ->
            val isSelected = activeTheme == t
            val themeColors = getThemeColors(t)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.activeThemeColor.value = t }
                    .background(if (isSelected) colors.surface else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(themeColors.primary, themeColors.accent)
                            )
                        )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = t,
                    color = colors.onBackground,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = colors.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Advanced Privacy & Security Section
        Text("Advanced Privacy & Security", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // SCREENSHOT PRIVACY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Screenshot Prevention", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Block screenshots inside the app for security.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = screenshotPreventionEnabled,
                        onCheckedChange = { viewModel.screenshotPreventionEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.background, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // CHAT PRIVACY & DISAPPEARING MESSAGES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Disappearing Messages", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Messages self-destruct after viewing.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = chatDisappearingMessages,
                        onCheckedChange = { viewModel.chatDisappearingMessages.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.background, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // CHAT LOCK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Chat Lock Shield", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Lock conversational threads with passcode.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = chatLockEnabled,
                        onCheckedChange = { viewModel.chatLockEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // calling mute and do member ek sath baat kar sakte hain
        Text("Call Privacy & Voice/Video Rules", color = colors.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // CALLING MUTE BY DEFAULT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MicOff, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Calling Auto-Mute", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Mute microphone automatically when joining.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = callMuted,
                        onCheckedChange = { viewModel.callMuted.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.background, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // VIDEO CALL PRIVACY / CAMERA OFF BY DEFAULT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VideocamOff, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Video Privacy Shield", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Keep video camera disabled on start.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = !callCameraEnabled,
                        onCheckedChange = { viewModel.callCameraEnabled.value = !it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.background, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // BACKGROUND BLUR PRIVACY
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BlurOn, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Background Blur Shield", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Intelligent background blur for video calls.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = callBlurBackground,
                        onCheckedChange = { viewModel.callBlurBackground.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.background, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // DO MEMBER EK SATH BAAT KAR SAKTE HAIN (MULTI-MEMBER simultaneity configuration)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Multi-Member Calling Limit", color = colors.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Allows 2 or more members to talk simultaneously.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            val isLimitSelected = callMembersCount == count
                            val text = when (count) {
                                2 -> "Dual (2 Members)"
                                3 -> "Tri (3 Members)"
                                else -> "Quad (4 Members)"
                            }
                            Button(
                                onClick = { viewModel.callMembersCount.value = count },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLimitSelected) colors.primary else colors.background,
                                    contentColor = if (isLimitSelected) colors.background else colors.onBackground
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .border(
                                        width = if (isLimitSelected) 0.dp else 1.dp,
                                        color = Color.Gray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wings Build Info", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Version: 1.0.4 - Canary Stable", color = colors.onBackground, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Local State Engine: Active (Room SQLite)", color = colors.onBackground, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("AI Engine: Server-side Gemini 3.5 Flash", color = colors.onBackground, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOverlayScreen(
    colors: WingsThemeColors,
    viewModel: WingsViewModel,
    onBack: () -> Unit
) {
    val isWhatsAppLight = viewModel.activeThemeColor.collectAsStateWithLifecycle().value == "WhatsApp Light"
    val topAppBarBg = if (isWhatsAppLight) colors.accent else colors.surface
    val topAppBarTitleColor = if (isWhatsAppLight) Color.White else colors.onBackground
    val topAppBarIconColor = if (isWhatsAppLight) Color.White else colors.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = topAppBarTitleColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = topAppBarIconColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topAppBarBg
                )
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            SettingsTabScreen(colors = colors, viewModel = viewModel)
        }
    }
}

// ==========================================
// --- CHAT WINDOW IMPLEMENTATION ---
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveChatWindow(
    chat: ChatEntity,
    messages: List<MessageEntity>,
    colors: WingsThemeColors,
    isTyping: Boolean,
    viewModel: WingsViewModel,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val activeTheme by viewModel.activeThemeColor.collectAsStateWithLifecycle()
    val activeReplyMessage by viewModel.messageRepliedTo.collectAsStateWithLifecycle()
    val starredMessages by viewModel.starredMessageIds.collectAsStateWithLifecycle()

    val isChatLocked by viewModel.chatLockEnabled.collectAsStateWithLifecycle()
    val isDisappearingMessagesOn by viewModel.chatDisappearingMessages.collectAsStateWithLifecycle()
    
    // Auto-drafting loading state
    var isDrafting by remember { mutableStateOf(false) }

    // Custom Attachment Sharing States
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var selectedShareType by remember { mutableStateOf<String?>(null) }
    var showCustomizeShareDialog by remember { mutableStateOf(false) }
    var showVoiceRecorderDialog by remember { mutableStateOf(false) }

    var selectedMessageForAction by remember { mutableStateOf<MessageEntity?>(null) }
    var showEditMessageDialog by remember { mutableStateOf<MessageEntity?>(null) }
    var showPollCreatorDialog by remember { mutableStateOf(false) }
    var showLocationShareDialog by remember { mutableStateOf(false) }
    var showContactShareDialog by remember { mutableStateOf(false) }

    // Jorn 6.0 Studio States
    var showJornCreator by remember { mutableStateOf(false) }
    var jornCreatorType by remember { mutableStateOf("Photo") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isJorn = chat.id == "jorn_6"
                    val avatarPair = getAvatarColors(chat.name, colors)
                    val avatarBg = if (isJorn) Color.Transparent else if (chat.id == "wings_assistant") colors.primaryContainer else avatarPair.first
                    val avatarTint = if (isJorn) Color(0xFFFFD600) else if (chat.id == "wings_assistant") colors.primary else avatarPair.second

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isJorn) {
                                    Brush.radialGradient(listOf(Color(0xFF2C1A04), Color(0xFF0D0701)))
                                } else {
                                    Brush.linearGradient(listOf(avatarBg, avatarBg))
                                }
                            )
                            .then(
                                if (isJorn) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        brush = Brush.sweepGradient(
                                            colors = listOf(Color(0xFFFF3D00), Color(0xFFFFD600), Color(0xFF00E5FF), Color(0xFFFF3D00))
                                        ),
                                        shape = CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isJorn) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (chat.id == "wings_assistant") {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = getInitials(chat.name),
                                color = avatarTint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(chat.name, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isChatLocked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Thread Locked",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            if (isDisappearingMessagesOn) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Disappearing Messages Active",
                                    tint = colors.activeGlow,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            if (chat.phoneNumber.isNotBlank()) {
                                Text(
                                    text = " • ${chat.phoneNumber}",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isTyping) "typing..." else chat.contactStatus,
                            color = if (isTyping) colors.activeGlow else Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primary)
                }
            },
            actions = {
                IconButton(onClick = { viewModel.initiateCall(chat.name, chat.avatarUrl, isVideo = false) }) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = colors.primary)
                }
                IconButton(onClick = { viewModel.initiateCall(chat.name, chat.avatarUrl, isVideo = true) }) {
                    Icon(Icons.Default.VideoCall, contentDescription = "Video Call", tint = colors.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface)
        )

        if (chat.id == "jorn_6") {
            JornQuickActionsBar(colors = colors) { type ->
                jornCreatorType = type
                showJornCreator = true
            }
        }

        // Messages Box with customized wings canvas background
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .drawBehind {
                    // Configure papercut aesthetic variables based on activeTheme
                    val isDark = colors.background != Color.White
                    val skyColor: Color
                    val sunColor: Color
                    val cloudColor: Color
                    val hillColor1: Color
                    val hillColor2: Color
                    val hillColor3: Color
                    val trunkColor: Color
                    val fMain: Color
                    val fAccent: Color
                    val fDark: Color
                    val birdColor: Color

                    when (activeTheme) {
                        "Sunset Orange" -> {
                            skyColor = Color(0xFF150D0A)
                            sunColor = Color(0xFFFF512F)
                            cloudColor = Color(0xFF33140C).copy(alpha = 0.5f)
                            hillColor1 = Color(0xFF2E150F)
                            hillColor2 = Color(0xFF220E0A)
                            hillColor3 = Color(0xFF150805)
                            trunkColor = Color(0xFF0A0302)
                            fMain = Color(0xFFFF512F)
                            fAccent = Color(0xFFFFD269)
                            fDark = Color(0xFF9E2C1A)
                            birdColor = Color(0xFFFFD269).copy(alpha = 0.8f)
                        }
                        "Indigo Cyber" -> {
                            skyColor = Color(0xFF0A0B10)
                            sunColor = Color(0xFF00E5FF)
                            cloudColor = Color(0xFF131520).copy(alpha = 0.4f)
                            hillColor1 = Color(0xFF1C1D30)
                            hillColor2 = Color(0xFF131422)
                            hillColor3 = Color(0xFF0B0C15)
                            trunkColor = Color(0xFF05050A)
                            fMain = Color(0xFF8E54E9)
                            fAccent = Color(0xFF00E5FF)
                            fDark = Color(0xFF4776E6)
                            birdColor = Color(0xFF00E5FF)
                        }
                        "Emerald Teal" -> {
                            skyColor = Color(0xFF0A121E)
                            sunColor = Color(0xFF00FF87)
                            cloudColor = Color(0xFF121F32).copy(alpha = 0.4f)
                            hillColor1 = Color(0xFF152A3F)
                            hillColor2 = Color(0xFF0E1E2F)
                            hillColor3 = Color(0xFF08121E)
                            trunkColor = Color(0xFF03080F)
                            fMain = Color(0xFF00B4DB)
                            fAccent = Color(0xFF00FF87)
                            fDark = Color(0xFF004E64)
                            birdColor = Color(0xFF00FF87)
                        }
                        "WhatsApp Dark" -> {
                            skyColor = Color(0xFF0B141A)
                            sunColor = Color(0xFFFFD54F)
                            cloudColor = Color(0xFF1F2C33).copy(alpha = 0.4f)
                            hillColor1 = Color(0xFF15202B)
                            hillColor2 = Color(0xFF111922)
                            hillColor3 = Color(0xFF0E141B)
                            trunkColor = Color(0xFF203247)
                            fMain = Color(0xFF00A884)
                            fAccent = Color(0xFF25D366)
                            fDark = Color(0xFF005C4B)
                            birdColor = Color(0xFFECEFF1)
                        }
                        "WhatsApp Light" -> {
                            skyColor = Color(0xFFF4F5F7)
                            sunColor = Color(0xFFFFD54F)
                            cloudColor = Color.White.copy(alpha = 0.8f)
                            hillColor1 = Color(0xFFE2E8F0)
                            hillColor2 = Color(0xFFCBD5E1)
                            hillColor3 = Color(0xFF94A3B8)
                            trunkColor = Color(0xFF1E293B)
                            fMain = Color(0xFFFBC02D)
                            fAccent = Color(0xFFFFF59D)
                            fDark = Color(0xFFE65100)
                            birdColor = Color(0xFF1C1D1F)
                        }
                        else -> { // Professional Polish, default themes
                            skyColor = if (isDark) Color(0xFF11151A) else Color(0xFFF0F4F8)
                            sunColor = colors.primary
                            cloudColor = if (isDark) Color(0xFF2F3235).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.8f)
                            hillColor1 = if (isDark) Color(0xFF1B2330) else Color(0xFFE2EAF4)
                            hillColor2 = if (isDark) Color(0xFF121B24) else Color(0xFFC7D8EB)
                            hillColor3 = if (isDark) Color(0xFF0B1118) else Color(0xFF9BBCE2)
                            trunkColor = if (isDark) Color(0xFF080C10) else Color(0xFF334E68)
                            fMain = colors.primary
                            fAccent = colors.primaryContainer
                            fDark = colors.accent
                            birdColor = if (isDark) Color.White else Color(0xFF1C1D1F)
                        }
                    }

                    // Render background sky
                    drawRect(color = skyColor)

                    // 1. Draw Sun/Moon in top-right
                    val sunRadius = 40.dp.toPx()
                    val sunCenter = Offset(size.width - 50.dp.toPx(), 80.dp.toPx())
                    drawCircle(color = sunColor, radius = sunRadius, center = sunCenter)

                    // 2. Draw soft layered paper clouds
                    drawCircle(color = cloudColor, radius = 25.dp.toPx(), center = Offset(40.dp.toPx(), 60.dp.toPx()))
                    drawCircle(color = cloudColor, radius = 38.dp.toPx(), center = Offset(70.dp.toPx(), 70.dp.toPx()))
                    drawCircle(color = cloudColor, radius = 28.dp.toPx(), center = Offset(100.dp.toPx(), 75.dp.toPx()))

                    drawCircle(color = cloudColor, radius = 20.dp.toPx(), center = Offset(size.width - 100.dp.toPx(), 180.dp.toPx()))
                    drawCircle(color = cloudColor, radius = 30.dp.toPx(), center = Offset(size.width - 75.dp.toPx(), 190.dp.toPx()))

                    // 3. Draw layered hills at the bottom
                    // Hill 1 (Back)
                    val hillPath1 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - 160.dp.toPx())
                        quadraticTo(size.width * 0.4f, size.height - 210.dp.toPx(), size.width, size.height - 130.dp.toPx())
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = hillPath1, color = hillColor1)

                    // Hill 2 (Mid)
                    val hillPath2 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - 110.dp.toPx())
                        quadraticTo(size.width * 0.6f, size.height - 80.dp.toPx(), size.width, size.height - 150.dp.toPx())
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = hillPath2, color = hillColor2)

                    // Hill 3 (Front)
                    val hillPath3 = Path().apply {
                        moveTo(0f, size.height)
                        lineTo(0f, size.height - 70.dp.toPx())
                        quadraticTo(size.width * 0.3f, size.height - 100.dp.toPx(), size.width, size.height - 80.dp.toPx())
                        lineTo(size.width, size.height)
                        close()
                    }
                    drawPath(path = hillPath3, color = hillColor3)

                    // 4. Draw Tree trunk & branches sweeping up
                    val treePath = Path().apply {
                        val startX = size.width * 0.45f
                        val startY = size.height - 85.dp.toPx()
                        moveTo(startX - 12.dp.toPx(), size.height)
                        lineTo(startX + 12.dp.toPx(), size.height)
                        cubicTo(
                            startX + 10.dp.toPx(), startY + 30.dp.toPx(),
                            startX - 15.dp.toPx(), startY - 50.dp.toPx(),
                            startX - 25.dp.toPx(), startY - 110.dp.toPx()
                        )
                        // Main branch left
                        cubicTo(
                            startX - 60.dp.toPx(), startY - 145.dp.toPx(),
                            startX - 85.dp.toPx(), startY - 195.dp.toPx(),
                            startX - 75.dp.toPx(), startY - 245.dp.toPx()
                        )
                        lineTo(startX - 70.dp.toPx(), startY - 240.dp.toPx())
                        cubicTo(
                            startX - 55.dp.toPx(), startY - 170.dp.toPx(),
                            startX + 15.dp.toPx(), startY - 145.dp.toPx(),
                            startX + 50.dp.toPx(), startY - 195.dp.toPx()
                        )
                        lineTo(startX + 54.dp.toPx(), startY - 190.dp.toPx())
                        cubicTo(
                            startX + 8.dp.toPx(), startY - 120.dp.toPx(),
                            startX - 4.dp.toPx(), startY - 95.dp.toPx(),
                            startX - 8.dp.toPx(), startY - 75.dp.toPx()
                        )
                        close()
                    }
                    drawPath(path = treePath, color = trunkColor)

                    // Side branches
                    val bPathLeft = Path().apply {
                        val bx = size.width * 0.45f - 30.dp.toPx()
                        val by = size.height - 85.dp.toPx() - 135.dp.toPx()
                        moveTo(bx, by)
                        cubicTo(bx - 25.dp.toPx(), by - 15.dp.toPx(), bx - 40.dp.toPx(), by - 5.dp.toPx(), bx - 65.dp.toPx(), by - 30.dp.toPx())
                        lineTo(bx - 63.dp.toPx(), by - 33.dp.toPx())
                        cubicTo(bx - 38.dp.toPx(), by - 12.dp.toPx(), bx - 22.dp.toPx(), by - 18.dp.toPx(), bx + 1.dp.toPx(), by - 2.dp.toPx())
                    }
                    drawPath(path = bPathLeft, color = trunkColor)

                    val bPathRight = Path().apply {
                        val bx = size.width * 0.45f + 8.dp.toPx()
                        val by = size.height - 85.dp.toPx() - 120.dp.toPx()
                        moveTo(bx, by)
                        cubicTo(bx + 25.dp.toPx(), by - 10.dp.toPx(), bx + 50.dp.toPx(), by - 5.dp.toPx(), bx + 75.dp.toPx(), by - 25.dp.toPx())
                        lineTo(bx + 73.dp.toPx(), by - 28.dp.toPx())
                        cubicTo(bx + 48.dp.toPx(), by - 8.dp.toPx(), bx + 22.dp.toPx(), by - 14.dp.toPx(), bx - 1.dp.toPx(), by - 2.dp.toPx())
                    }
                    drawPath(path = bPathRight, color = trunkColor)

                    // 5. Beautiful blossoms
                    val blossoms = listOf(
                        Offset(size.width * 0.45f - 75.dp.toPx(), size.height - 85.dp.toPx() - 250.dp.toPx()) to 14.dp.toPx(),
                        Offset(size.width * 0.45f - 90.dp.toPx(), size.height - 85.dp.toPx() - 230.dp.toPx()) to 10.dp.toPx(),
                        Offset(size.width * 0.45f - 60.dp.toPx(), size.height - 85.dp.toPx() - 260.dp.toPx()) to 12.dp.toPx(),
                        Offset(size.width * 0.45f - 30.dp.toPx(), size.height - 85.dp.toPx() - 200.dp.toPx()) to 16.dp.toPx(),
                        Offset(size.width * 0.45f - 40.dp.toPx(), size.height - 85.dp.toPx() - 180.dp.toPx()) to 12.dp.toPx(),
                        Offset(size.width * 0.45f + 50.dp.toPx(), size.height - 85.dp.toPx() - 195.dp.toPx()) to 15.dp.toPx(),
                        Offset(size.width * 0.45f + 65.dp.toPx(), size.height - 85.dp.toPx() - 175.dp.toPx()) to 11.dp.toPx(),
                        Offset(size.width * 0.45f + 40.dp.toPx(), size.height - 85.dp.toPx() - 210.dp.toPx()) to 10.dp.toPx(),
                        Offset(size.width * 0.45f - 95.dp.toPx(), size.height - 85.dp.toPx() - 165.dp.toPx()) to 11.dp.toPx(),
                        Offset(size.width * 0.45f + 83.dp.toPx(), size.height - 85.dp.toPx() - 145.dp.toPx()) to 12.dp.toPx()
                    )

                    blossoms.forEach { (center, radius) ->
                        // Draw flower shadow
                        drawCircle(color = fDark, radius = radius, center = center)
                        // Draw main body
                        drawCircle(color = fMain, radius = radius * 0.85f, center = center - Offset(1.dp.toPx(), 1.dp.toPx()))
                        // Draw core
                        drawCircle(color = fAccent, radius = radius * 0.4f, center = center - Offset(1.5.dp.toPx(), 1.5.dp.toPx()))
                    }

                    // 6. Flying birds
                    val birdScale = 0.8f
                    val birds = listOf(
                        Offset(80.dp.toPx(), 150.dp.toPx()),
                        Offset(120.dp.toPx(), 125.dp.toPx()),
                        Offset(size.width * 0.5f + 25.dp.toPx(), 95.dp.toPx()),
                        Offset(size.width - 120.dp.toPx(), 260.dp.toPx())
                    )
                    birds.forEach { bCenter ->
                        val bPath = Path().apply {
                            moveTo(bCenter.x - 12.dp.toPx() * birdScale, bCenter.y - 2.dp.toPx() * birdScale)
                            quadraticTo(bCenter.x - 4.dp.toPx() * birdScale, bCenter.y - 10.dp.toPx() * birdScale, bCenter.x, bCenter.y)
                            quadraticTo(bCenter.x + 4.dp.toPx() * birdScale, bCenter.y - 10.dp.toPx() * birdScale, bCenter.x + 12.dp.toPx() * birdScale, bCenter.y - 2.dp.toPx() * birdScale)
                            quadraticTo(bCenter.x + 2.dp.toPx() * birdScale, bCenter.y - 3.dp.toPx() * birdScale, bCenter.x, bCenter.y - 1.dp.toPx() * birdScale)
                            quadraticTo(bCenter.x - 2.dp.toPx() * birdScale, bCenter.y - 3.dp.toPx() * birdScale, bCenter.x - 12.dp.toPx() * birdScale, bCenter.y - 2.dp.toPx() * birdScale)
                            close()
                        }
                        drawPath(path = bPath, color = birdColor)
                    }
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == "me"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) colors.bubbleMe else colors.bubbleOther
                            ),
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .pointerInput(msg) {
                                    detectTapGestures(
                                        onLongPress = {
                                            selectedMessageForAction = msg
                                        }
                                    )
                                }
                                .testTag("message_bubble_${msg.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!isMe && chat.isGroup) {
                                    Text(
                                        text = msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = colors.primary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                // Media Attachment rendering
                                if (msg.mediaType != null) {
                                    when (msg.mediaType) {
                                        "photo", "image" -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.LightGray.copy(alpha = 0.2f))
                                                    .padding(bottom = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                coil.compose.AsyncImage(
                                                    model = msg.mediaUrl,
                                                    contentDescription = "Shared Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color.LightGray.copy(alpha = 0.4f)),
                                                    error = androidx.compose.ui.graphics.painter.ColorPainter(Color.DarkGray.copy(alpha = 0.6f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(8.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (msg.mediaType == "photo") Icons.Default.Camera else Icons.Default.Image,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = msg.mediaType!!.uppercase(),
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        "document" -> {
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isMe) colors.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFE3F2FD)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.InsertDriveFile,
                                                            contentDescription = "Doc",
                                                            tint = Color(0xFF1565C0),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = msg.text.ifBlank { "Attachment.pdf" },
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            color = colors.onBackground
                                                        )
                                                        Text(
                                                            text = "2.4 MB • PDF Document",
                                                            fontSize = 10.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDownward,
                                                        contentDescription = "Download",
                                                        tint = colors.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                        "video" -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.DarkGray)
                                                    .padding(bottom = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                coil.compose.AsyncImage(
                                                    model = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400",
                                                    contentDescription = "Video Thumbnail",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(Color.Black),
                                                    error = androidx.compose.ui.graphics.painter.ColorPainter(Color.Black)
                                                )
                                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.8f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Play Video",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(8.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayCircle,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "VIDEO",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(8.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(text = "0:45", color = Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }
                                        "custom" -> {
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isMe) colors.primary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(colors.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Share,
                                                            contentDescription = "Custom Share",
                                                            tint = colors.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = msg.text.ifBlank { "Custom Shared Item" },
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = colors.onBackground,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "Tap to open customised source",
                                                            fontSize = 9.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowForward,
                                                        contentDescription = "Open",
                                                        tint = colors.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                        "audio", "voice" -> {
                                            VoiceMessagePlayer(
                                                msg = msg,
                                                colors = colors,
                                                isMe = isMe
                                            )
                                        }
                                        "contact" -> {
                                            Card(
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                            ) {
                                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.primaryContainer),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Person, contentDescription = null, tint = colors.primary)
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        val parts = msg.text.split("\n")
                                                        val cName = parts.getOrNull(0)?.removePrefix("Contact: ") ?: "Wings Friend"
                                                        val cNum = parts.getOrNull(1)?.removePrefix("Phone: ") ?: ""
                                                        Text(cName, fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 14.sp)
                                                        if (cNum.isNotBlank()) {
                                                            Text(cNum, color = Color.Gray, fontSize = 12.sp)
                                                        }
                                                    }
                                                    TextButton(onClick = {}) {
                                                        Text("Message", color = colors.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                        "location", "live_location" -> {
                                            Card(
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                            ) {
                                                Column {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(110.dp)
                                                            .background(Brush.linearGradient(listOf(Color(0xFFE0F2F1), Color(0xFFB2DFDB)))),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                                                    }
                                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = if (msg.mediaType == "live_location") Icons.Default.DirectionsRun else Icons.Default.Map,
                                                            contentDescription = null,
                                                            tint = colors.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = if (msg.mediaType == "live_location") "Live Location" else "Pinned Location",
                                                                fontWeight = FontWeight.Bold,
                                                                color = colors.onBackground,
                                                                fontSize = 13.sp
                                                            )
                                                            Text(msg.text, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        "poll" -> {
                                            val lines = msg.text.lines()
                                            val question = lines.getOrNull(0)?.removePrefix("[Poll] ") ?: "Select an option"
                                            val options = lines.drop(1).filter { it.startsWith("-") }.map { it.removePrefix("-").trim() }
                                            
                                            val votesMap by viewModel.pollVotes.collectAsStateWithLifecycle()
                                            val userVotes by viewModel.pollUserVotes.collectAsStateWithLifecycle()
                                            
                                            val votes = votesMap[msg.id] ?: emptyMap()
                                            val totalVotes = votes.values.sum()
                                            val myVote = userVotes[msg.id]

                                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                                                Text(question, fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                options.forEach { option ->
                                                    val optionVotes = votes[option] ?: 0
                                                    val pct = if (totalVotes > 0) (optionVotes.toFloat() / totalVotes) else 0f
                                                    val hasVotedThis = myVote == option

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(colors.surface)
                                                            .border(
                                                                width = if (hasVotedThis) 1.5.dp else 0.5.dp,
                                                                color = if (hasVotedThis) colors.primary else Color.Gray.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                viewModel.voteInPoll(msg.id, option)
                                                            }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .matchParentSize()
                                                                .fillMaxWidth(pct)
                                                                .background(colors.primary.copy(alpha = 0.12f))
                                                        )
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                RadioButton(
                                                                    selected = hasVotedThis,
                                                                    onClick = { viewModel.voteInPoll(msg.id, option) },
                                                                    colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(option, color = colors.onBackground, fontSize = 13.sp)
                                                            }
                                                            Text("$optionVotes votes", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (msg.mediaType == null || msg.mediaType == "photo" || msg.mediaType == "image" || msg.mediaType == "video") {
                                    if (msg.text.isNotBlank()) {
                                        if (msg.text.startsWith("💬 Replying to ")) {
                                            val parts = msg.text.split("\n\n", limit = 2)
                                            val replyHeader = parts.getOrNull(0)?.removePrefix("💬 ") ?: ""
                                            val mainText = parts.getOrNull(1) ?: ""
                                            
                                            Column {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(colors.surface.copy(alpha = 0.45f))
                                                        .padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(3.dp)
                                                            .height(28.dp)
                                                            .clip(RoundedCornerShape(1.5.dp))
                                                            .background(colors.primary)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = replyHeader,
                                                        fontSize = 11.sp,
                                                        color = colors.primary,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = mainText,
                                                    color = colors.onBackground,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = msg.text,
                                                color = colors.onBackground,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val time = remember(msg.timestamp) {
                                        val date = java.util.Date(msg.timestamp)
                                        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                        sdf.format(date)
                                    }
                                    Text(
                                        text = time,
                                        color = Color.LightGray.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                    if (starredMessages.contains(msg.id)) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Starred Message",
                                            tint = Color(0xFFFFD600),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    if (isMe) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = null,
                                            tint = colors.activeGlow,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI typing loading indicator
                if (isTyping) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.bubbleOther),
                                modifier = Modifier.widthIn(max = 120.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "typing",
                                        color = colors.onBackground,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    // Animated pulsing dot
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.2f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Text("...", color = colors.primary.copy(alpha = alpha), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // SMART AI HELP: Assist drafting user's text!
        if (inputText.isNotEmpty() && !isDrafting) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "AI Assist Drafting",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Ask AI to elaborate/refine the input sentence
                TextButton(
                    onClick = {
                        isDrafting = true
                        coroutineScope.launch {
                            val draftInstruction = "You are helping a chat user write a refined reply. Elaborate this short sentence briefly (max 1 sentence) in a casual, pleasant chat style: '$inputText'"
                            val refined = com.example.data.api.GeminiClient.getAiResponse(inputText, draftInstruction)
                            if (refined.isNotBlank() && !refined.contains("Please configure your GEMINI_API_KEY")) {
                                inputText = refined
                            }
                            isDrafting = false
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Refine with AI ✨", color = colors.activeGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isDrafting) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = colors.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Drafting response with Gemini...", color = Color.Gray, fontSize = 11.sp)
            }
        }

        // Floating Attachment Options Row (WhatsApp-style all-in-one share)
        AnimatedVisibility(
            visible = showAttachmentOptions,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Customize & Share",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.primary,
                        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AttachmentItem(
                            label = "Voice Note",
                            icon = Icons.Default.Mic,
                            bgColor = Color(0xFFE0F7FA),
                            iconColor = Color(0xFF006064),
                            onClick = {
                                showVoiceRecorderDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Photo",
                            icon = Icons.Default.Camera,
                            bgColor = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF2E7D32),
                            onClick = {
                                selectedShareType = "photo"
                                showCustomizeShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Image",
                            icon = Icons.Default.Image,
                            bgColor = Color(0xFFF3E5F5),
                            iconColor = Color(0xFF7B1FA2),
                            onClick = {
                                selectedShareType = "image"
                                showCustomizeShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Document",
                            icon = Icons.Default.InsertDriveFile,
                            bgColor = Color(0xFFE3F2FD),
                            iconColor = Color(0xFF1565C0),
                            onClick = {
                                selectedShareType = "document"
                                showCustomizeShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Video",
                            icon = Icons.Default.PlayCircle,
                            bgColor = Color(0xFFFFF3E0),
                            iconColor = Color(0xFFEF6C00),
                            onClick = {
                                selectedShareType = "video"
                                showCustomizeShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "All-in-One",
                            icon = Icons.Default.Share,
                            bgColor = Color(0xFFFCE4EC),
                            iconColor = Color(0xFFC2185B),
                            onClick = {
                                selectedShareType = "custom"
                                showCustomizeShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Create Poll",
                            icon = Icons.Default.Poll,
                            bgColor = Color(0xFFE8F5E9),
                            iconColor = Color(0xFF4CAF50),
                            onClick = {
                                showPollCreatorDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Location",
                            icon = Icons.Default.LocationOn,
                            bgColor = Color(0xFFFFF3E0),
                            iconColor = Color(0xFFFF9800),
                            onClick = {
                                showLocationShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                        AttachmentItem(
                            label = "Contact",
                            icon = Icons.Default.Person,
                            bgColor = Color(0xFFE8EAF6),
                            iconColor = Color(0xFF3F51B5),
                            onClick = {
                                showContactShareDialog = true
                                showAttachmentOptions = false
                            }
                        )
                    }
                }
            }
        }
        
        // Active Reply Bar banner (WhatsApp-style)
        if (activeReplyMessage != null) {
            val replyMsg = activeReplyMessage!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(width = 0.5.dp, color = colors.primary.copy(alpha = 0.15f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Replying to ${if (replyMsg.senderId == "me") "You" else replyMsg.senderName}",
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = replyMsg.text,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = { viewModel.messageRepliedTo.value = null },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Reply",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                showAttachmentOptions = !showAttachmentOptions
            }) {
                Icon(
                    imageVector = Icons.Default.Attachment, 
                    contentDescription = "Share All-in-One Attachment", 
                    tint = if (showAttachmentOptions) colors.primary else Color.Gray
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                placeholder = { Text("Message...", color = Color.Gray, fontSize = 14.sp) },
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = colors.background,
                    unfocusedContainerColor = colors.background,
                    focusedTextColor = colors.onBackground,
                    unfocusedTextColor = colors.onBackground
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    } else {
                        showVoiceRecorderDialog = true
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .testTag("send_message_button")
            ) {
                Icon(
                    imageVector = if (inputText.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                    contentDescription = if (inputText.isNotBlank()) "Send" else "Record Voice Note",
                    tint = colors.background,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (showCustomizeShareDialog) {
            val shareType = selectedShareType ?: "custom"
            CustomizeShareDialog(
                shareType = shareType,
                colors = colors,
                onDismiss = { showCustomizeShareDialog = false },
                onConfirm = { caption, url ->
                    viewModel.sendMessage(text = caption, mediaType = shareType, mediaUrl = url)
                    showCustomizeShareDialog = false
                }
            )
        }

        if (showVoiceRecorderDialog) {
            VoiceRecorderDialog(
                colors = colors,
                onDismiss = { showVoiceRecorderDialog = false },
                onConfirm = { duration, transcript ->
                    viewModel.sendMessage(text = transcript, mediaType = "audio", mediaUrl = duration.toString())
                    showVoiceRecorderDialog = false
                }
            )
        }

        if (showJornCreator) {
            JornCreatorDialog(
                colors = colors,
                creatorType = jornCreatorType,
                onDismiss = { showJornCreator = false },
                viewModel = viewModel
            )
        }

        if (selectedMessageForAction != null) {
            val actionMsg = selectedMessageForAction!!
            val isMsgMe = actionMsg.senderId == "me"
            val isStarred = starredMessages.contains(actionMsg.id)

            AlertDialog(
                onDismissRequest = { selectedMessageForAction = null },
                title = { Text("Message Options", fontWeight = FontWeight.Bold, color = colors.onBackground, fontSize = 16.sp) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = {
                                viewModel.messageRepliedTo.value = actionMsg
                                selectedMessageForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Reply, contentDescription = null, tint = colors.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Reply", color = colors.onBackground, fontSize = 14.sp)
                            }
                        }
                        
                        TextButton(
                            onClick = {
                                viewModel.toggleStarMessage(actionMsg.id)
                                selectedMessageForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Default.StarOutline else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD600)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(if (isStarred) "Unstar" else "Star", color = colors.onBackground, fontSize = 14.sp)
                            }
                        }

                        if (isMsgMe) {
                            TextButton(
                                onClick = {
                                    showEditMessageDialog = actionMsg
                                    selectedMessageForAction = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = colors.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Edit Message", color = colors.onBackground, fontSize = 14.sp)
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                viewModel.deleteMessage(actionMsg.id)
                                selectedMessageForAction = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete Message", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { selectedMessageForAction = null }) {
                        Text("Cancel", color = colors.primary)
                    }
                }
            )
        }

        if (showEditMessageDialog != null) {
            val editMsg = showEditMessageDialog!!
            var editFieldText by remember { mutableStateOf(editMsg.text) }

            AlertDialog(
                onDismissRequest = { showEditMessageDialog = null },
                title = { Text("Edit Message", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                text = {
                    OutlinedTextField(
                        value = editFieldText,
                        onValueChange = { editFieldText = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editFieldText.isNotBlank()) {
                                viewModel.editMessage(editMsg.id, editFieldText)
                            }
                            showEditMessageDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Save", color = colors.background)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditMessageDialog = null }) {
                        Text("Cancel", color = colors.primary)
                    }
                }
            )
        }

        if (showPollCreatorDialog) {
            var pollQuestionField by remember { mutableStateOf("") }
            var pollOpt1 by remember { mutableStateOf("") }
            var pollOpt2 by remember { mutableStateOf("") }
            var pollOpt3 by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showPollCreatorDialog = false },
                title = { Text("Create Interactive Poll", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = pollQuestionField,
                            onValueChange = { pollQuestionField = it },
                            label = { Text("Ask Question...") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = pollOpt1,
                            onValueChange = { pollOpt1 = it },
                            label = { Text("Option 1") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = pollOpt2,
                            onValueChange = { pollOpt2 = it },
                            label = { Text("Option 2") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = pollOpt3,
                            onValueChange = { pollOpt3 = it },
                            label = { Text("Option 3 (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pollQuestionField.isNotBlank() && pollOpt1.isNotBlank() && pollOpt2.isNotBlank()) {
                                var pollText = "[Poll] $pollQuestionField\n- $pollOpt1\n- $pollOpt2"
                                if (pollOpt3.isNotBlank()) {
                                    pollText += "\n- $pollOpt3"
                                }
                                viewModel.sendMessage(text = pollText, mediaType = "poll")
                            }
                            showPollCreatorDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Create", color = colors.background)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPollCreatorDialog = false }) {
                        Text("Cancel", color = colors.primary)
                    }
                }
            )
        }

        if (showLocationShareDialog) {
            var locationNameField by remember { mutableStateOf("Connaught Place, New Delhi") }
            var isLiveLoc by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showLocationShareDialog = false },
                title = { Text("Share Location", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = locationNameField,
                            onValueChange = { locationNameField = it },
                            label = { Text("Address / Location Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isLiveLoc, onCheckedChange = { isLiveLoc = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share as Live Location (Real-time)", fontSize = 13.sp, color = colors.onBackground)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val type = if (isLiveLoc) "live_location" else "location"
                            val prefix = if (isLiveLoc) "Live coordinates: 28.6304° N, 77.2177° E" else locationNameField
                            viewModel.sendMessage(text = prefix, mediaType = type)
                            showLocationShareDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Share", color = colors.background)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationShareDialog = false }) {
                        Text("Cancel", color = colors.primary)
                    }
                }
            )
        }

        if (showContactShareDialog) {
            var contactNameField by remember { mutableStateOf("") }
            var contactPhoneField by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showContactShareDialog = false },
                title = { Text("Share Contact Details", fontWeight = FontWeight.Bold, color = colors.onBackground) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = contactNameField,
                            onValueChange = { contactNameField = it },
                            label = { Text("Contact Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = contactPhoneField,
                            onValueChange = { contactPhoneField = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (contactNameField.isNotBlank() && contactPhoneField.isNotBlank()) {
                                val cText = "Contact: $contactNameField\nPhone: $contactPhoneField"
                                viewModel.sendMessage(text = cText, mediaType = "contact")
                            }
                            showContactShareDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                    ) {
                        Text("Share", color = colors.background)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showContactShareDialog = false }) {
                        Text("Cancel", color = colors.primary)
                    }
                }
            )
        }
    }
}

// ==========================================
// --- INTERACTIVE OVERLAYS ---
// ==========================================

@Composable
fun CallingOverlayScreen(
    call: CallHistory,
    count: Int,
    colors: WingsThemeColors,
    viewModel: WingsViewModel,
    onEndCall: () -> Unit
) {
    val minutes = count / 60
    val seconds = count % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val membersCount by viewModel.callMembersCount.collectAsStateWithLifecycle()
    val isMeMuted by viewModel.callMuted.collectAsStateWithLifecycle()
    val isMeCameraEnabled by viewModel.callCameraEnabled.collectAsStateWithLifecycle()
    val isMeBlurEnabled by viewModel.callBlurBackground.collectAsStateWithLifecycle()
    val isScreenshotPreventionOn by viewModel.screenshotPreventionEnabled.collectAsStateWithLifecycle()

    // Determine active speaker (cycling every 4 seconds)
    val activeSpeakerIdx = if (count == 0) -1 else (count / 4) % membersCount

    // Build the list of participants dynamically
    val participants = remember(membersCount, call, isMeMuted, isMeCameraEnabled, isMeBlurEnabled) {
        val list = mutableListOf<CallParticipant>()
        // Participant 0: Me
        list.add(
            CallParticipant(
                name = "You",
                initials = "ME",
                isMuted = isMeMuted,
                isCameraOn = isMeCameraEnabled,
                isBlurActive = isMeBlurEnabled,
                gradientColors = listOf(Color(0xFF2C3E50), Color(0xFF3498DB))
            )
        )
        // Participant 1: Primary Callee
        list.add(
            CallParticipant(
                name = call.name,
                initials = getInitials(call.name),
                isMuted = false,
                isCameraOn = true,
                isBlurActive = false,
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            )
        )
        // Participant 2: Emma (if limit >= 3)
        if (membersCount >= 3) {
            list.add(
                CallParticipant(
                    name = "Emma (UI Designer)",
                    initials = "EM",
                    isMuted = false,
                    isCameraOn = true,
                    isBlurActive = true, // Emma uses background blur privacy
                    gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                )
            )
        }
        // Participant 3: Liam (if limit >= 4)
        if (membersCount >= 4) {
            list.add(
                CallParticipant(
                    name = "Liam (Lead Developer)",
                    initials = "LI",
                    isMuted = true, // Liam is muted
                    isCameraOn = false, // Liam camera off
                    isBlurActive = false,
                    gradientColors = listOf(Color(0xFFF12711), Color(0xFFF5AF19))
                )
            )
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Sleek dark slate cinema background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Brand Header & Encryption Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure",
                    tint = colors.activeGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "End-to-End Encrypted",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Call Meta Title
            Text(
                text = if (membersCount == 2) "Wings 1-on-1 Privacy Call" else "Wings Multi-Member Group Call (${participants.size} Active)",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            // Duration and status
            Text(
                text = if (count == 0) "Connecting Secure Line..." else "Connected • $formattedTime",
                color = if (count == 0) Color.Gray else colors.activeGlow,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Dynamic Privacy Alerts Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isScreenshotPreventionOn) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE53935).copy(alpha = 0.2f))
                            .border(0.5.dp, Color(0xFFE53935), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Screenshot Privacy Shield Active", color = Color(0xFFEF5350), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (isMeBlurEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.primary.copy(alpha = 0.2f))
                            .border(0.5.dp, colors.primary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BlurOn, contentDescription = null, tint = colors.primary, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Background Blur Shield On", color = colors.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PARTICIPANTS CINEMATIC GRID (Supports 2, 3, or 4 members speaking together simultaneously)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (participants.size) {
                    2 -> {
                        // Stacked layout for dual calling
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            participants.forEachIndexed { idx, part ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = part,
                                        isSpeaking = idx == activeSpeakerIdx,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        // 1 large top tile, 2 side-by-side bottom tiles
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                ParticipantTile(
                                    participant = participants[0],
                                    isSpeaking = activeSpeakerIdx == 0,
                                    colors = colors
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[1],
                                        isSpeaking = activeSpeakerIdx == 1,
                                        colors = colors
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[2],
                                        isSpeaking = activeSpeakerIdx == 2,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // 2x2 classic grid layout
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[0],
                                        isSpeaking = activeSpeakerIdx == 0,
                                        colors = colors
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[1],
                                        isSpeaking = activeSpeakerIdx == 1,
                                        colors = colors
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[2],
                                        isSpeaking = activeSpeakerIdx == 2,
                                        colors = colors
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ParticipantTile(
                                        participant = participants[3],
                                        isSpeaking = activeSpeakerIdx == 3,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CALLING ACTION CONTROL FLOATING BOARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Button
                    IconButton(
                        onClick = { viewModel.callMuted.value = !isMeMuted },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isMeMuted) Color(0xFFEF5350) else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (isMeMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Camera Enable / Disable Button
                    IconButton(
                        onClick = { viewModel.callCameraEnabled.value = !isMeCameraEnabled },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (!isMeCameraEnabled) Color(0xFFEF5350) else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = if (isMeCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Toggle Camera",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Background Blur Privacy Toggle Button
                    IconButton(
                        onClick = { viewModel.callBlurBackground.value = !isMeBlurEnabled },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isMeBlurEnabled) colors.primary else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.BlurOn,
                            contentDescription = "Toggle Background Blur",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Add Participant / Member (Do / Multi Member talking helper)
                    IconButton(
                        onClick = {
                            if (membersCount < 4) {
                                viewModel.callMembersCount.value = membersCount + 1
                            } else {
                                viewModel.callMembersCount.value = 2
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Member to Talk Together",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // RED END CALL BUTTON
                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                            .testTag("end_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

data class CallParticipant(
    val name: String,
    val initials: String,
    val isMuted: Boolean,
    val isCameraOn: Boolean,
    val isBlurActive: Boolean,
    val gradientColors: List<Color>
)

@Composable
fun ParticipantTile(
    participant: CallParticipant,
    isSpeaking: Boolean,
    colors: WingsThemeColors
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxSize()
            .border(
                width = if (isSpeaking) 2.5.dp else 1.dp,
                color = if (isSpeaking) colors.primary else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Camera Feed Simulation
            if (participant.isCameraOn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(participant.gradientColors)
                        )
                ) {
                    // Simulation Background Blur Layer
                    if (participant.isBlurActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)) // Visual shield dimming
                        ) {
                            // Render concentric frosted circles to simulate camera lens focus blur
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(170.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Camera Off",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Foreground initials and graphics
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(2.dp, if (isSpeaking) colors.activeGlow else Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        participant.initials,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                
                if (isSpeaking) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.primary.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = colors.activeGlow,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Speaking...",
                            color = colors.activeGlow,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Participant Name label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = participant.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Participant Status Icons (Muted/Blur indicators)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (participant.isMuted) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MicOff,
                            contentDescription = "Muted",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                if (participant.isBlurActive && participant.isCameraOn) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BlurOn,
                            contentDescription = "Blur Active",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusImmersiveViewer(
    statusList: List<StatusEntity>,
    startIndex: Int,
    colors: WingsThemeColors,
    viewModel: WingsViewModel,
    onFinished: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(startIndex) }
    val currentStatus = statusList[currentIndex]

    // Segment progress tracking
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(currentIndex) {
        progress = 0f
        // Increment progress every 100ms up to 5 seconds
        for (i in 1..50) {
            delay(100)
            progress = i / 50f
        }
        // Advance story
        if (currentIndex < statusList.size - 1) {
            currentIndex++
        } else {
            onFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor(currentStatus.backgroundColorHex))),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Horizontal Segment Indicator Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 12.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                statusList.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i < currentIndex) Color.White
                                else if (i == currentIndex) Color.White.copy(alpha = 0.3f)
                                else Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                        if (i == currentIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }

            // User Info header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getInitials(currentStatus.userName), color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(currentStatus.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Wings Status Story", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }

                IconButton(onClick = onFinished) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Central Caption / Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clickable {
                        // Tap right to advance, left to go back
                        if (currentIndex < statusList.size - 1) {
                            currentIndex++
                        } else {
                            onFinished()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentStatus.caption,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )
            }

            // Bottom quick status reply
            var replyText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Reply to status...", color = Color.LightGray) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    if (replyText.isNotBlank()) {
                        viewModel.selectChat(currentStatus.userName) // quick setup
                        viewModel.sendMessage("Status reply: $replyText")
                        replyText = ""
                        onFinished()
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = Color.White)
                }
            }
        }
    }
}

// ==========================================
// --- DIALOG BUILDERS ---
// ==========================================

@Composable
fun AddContactDialog(
    colors: WingsThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Wings Contact", color = colors.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Custom Status/Bio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, status, phoneNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Fly Together ✈️", color = colors.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.primary)
            }
        },
        containerColor = colors.surface
    )
}

@Composable
fun CreateStatusDialog(
    colors: WingsThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val backgrounds = listOf("#FF0F2027", "#FF1F4037", "#FF2C5364", "#FF8E54E9", "#FF9E2C1A")
    var selectedBg by remember { mutableStateOf(backgrounds.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Wings Status Story", color = colors.onBackground) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("What's on your mind?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Canvas Background Color:", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    backgrounds.forEach { bg ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(bg)))
                                .border(
                                    width = 2.dp,
                                    color = if (selectedBg == bg) colors.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedBg = bg }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text, selectedBg) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Post Status", color = colors.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.primary)
            }
        },
        containerColor = colors.surface
    )
}

// --- Dynamic Box scale helper ---
fun Modifier.scale(scale: Float) = this.graphicsLayer {
    scaleX = scale
    scaleY = scale
}

@Composable
fun AttachmentItem(
    label: String,
    icon: ImageVector,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CustomizeShareDialog(
    shareType: String,
    colors: WingsThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (caption: String, url: String) -> Unit
) {
    var caption by remember { 
        mutableStateOf(
            when (shareType) {
                "photo" -> "Captured a beautiful sunset 📸"
                "image" -> "Check out this stunning high-res wallpaper! 🖼️"
                "document" -> "Project_Proposal_Wings.pdf"
                "video" -> "Scenic Drone Video tour! 🎥"
                else -> "Wings customized all-in-one shared parcel ✈️"
            }
        ) 
    }
    
    var url by remember {
        mutableStateOf(
            when (shareType) {
                "photo" -> "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05"
                "image" -> "https://images.unsplash.com/photo-1472214222555-d404758b1c42"
                "document" -> "https://wings-share.example.com/proposal_v2.pdf"
                "video" -> "https://assets.mixkit.co/videos/preview/mixkit-flying-over-a-snowy-mountain-range-43093-large.mp4"
                else -> "https://wings-share.example.com/custom_bundle.zip"
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (shareType) {
                        "photo" -> Icons.Default.Camera
                        "image" -> Icons.Default.Image
                        "document" -> Icons.Default.InsertDriveFile
                        "video" -> Icons.Default.PlayCircle
                        else -> Icons.Default.Share
                    },
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Share ${shareType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.onBackground
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Customize details below before sharing in your chat with contacts:",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption / File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("File URL / Source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(caption, url)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Share Now ✈️", color = colors.background)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.primary)
            }
        },
        containerColor = colors.surface
    )
}

@Composable
fun VoiceMessagePlayer(
    msg: MessageEntity,
    colors: WingsThemeColors,
    isMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableStateOf(0.0f) }
    
    val durationSeconds = remember(msg.mediaUrl) {
        msg.mediaUrl?.toIntOrNull() ?: 12 // default to 12s
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val stepTime = 100L
            val totalSteps = (durationSeconds * 1000) / stepTime
            for (step in 1..totalSteps) {
                if (!isPlaying) break
                delay(stepTime)
                playbackProgress = step.toFloat() / totalSteps
            }
            isPlaying = false
            playbackProgress = 0.0f
        }
    }

    // A nice horizontal card for the audio note
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause Icon
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isMe) colors.primary else Color.Gray.copy(alpha = 0.2f))
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isMe) colors.background else colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Waveform & Time Column
        Column(modifier = Modifier.weight(1f)) {
            // Simulated Soundwave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Let's draw 24 vertical bars representing a sound wave
                val waveHeights = remember {
                    listOf(
                        10, 15, 6, 22, 14, 8, 18, 25, 12, 7, 20, 16, 
                        10, 24, 15, 8, 12, 18, 22, 7, 14, 19, 10, 5
                    )
                }
                waveHeights.forEachIndexed { index, height ->
                    val progressRatio = index.toFloat() / waveHeights.size
                    val isPlayed = playbackProgress >= progressRatio
                    val barColor = if (isPlayed) {
                        colors.primary
                    } else {
                        colors.onBackground.copy(alpha = 0.25f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(height.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(barColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time/Transcript labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentSeconds = (playbackProgress * durationSeconds).toInt()
                Text(
                    text = String.format("%d:%02d", currentSeconds / 60, currentSeconds % 60),
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Note",
                        tint = colors.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Voice Note (${durationSeconds}s)",
                        fontSize = 9.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (msg.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transcript: \"${msg.text}\"",
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = colors.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun VoiceRecorderDialog(
    colors: WingsThemeColors,
    onDismiss: () -> Unit,
    onConfirm: (duration: Int, transcript: String) -> Unit
) {
    var isRecording by remember { mutableStateOf(true) }
    var duration by remember { mutableStateOf(0) }
    var transcriptionText by remember { mutableStateOf("Hey, let's connect and fly together! ✈️ Checking in on progress.") }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            duration += 1
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recording Voice Message...",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.onBackground
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ticking timer
                Text(
                    text = String.format("%d:%02d", duration / 60, duration % 60),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecording) Color.Red else colors.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Animated sound waves
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val barCount = 15
                    for (i in 0 until barCount) {
                        val randomOffset = remember { (10..40).random() }
                        val animationProgress by animateFloatAsState(
                            targetValue = if (isRecording) (randomOffset + (duration % 3) * 10).toFloat().coerceAtMost(48f) else 10f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 300, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(4.dp)
                                .height(animationProgress.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isRecording) Color.Red.copy(alpha = 0.8f) else Color.Gray)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Voice Transcript editing (Speech to text simulation)
                OutlinedTextField(
                    value = transcriptionText,
                    onValueChange = { transcriptionText = it },
                    label = { Text("Simulated Voice Transcript") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isRecording = false
                    onConfirm(if (duration > 0) duration else 5, transcriptionText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Send Voice Note 🎤", color = colors.background)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    isRecording = false
                    onDismiss()
                }
            ) {
                Text("Cancel", color = Color.Gray)
            }
        },
        containerColor = colors.surface
    )
}

@Composable
fun JornQuickActionsBar(
    colors: WingsThemeColors,
    onActionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Little label
        Text(
            "Jorn 6.0 Studio: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD600),
            modifier = Modifier.padding(end = 4.dp)
        )

        // Actions
        val actions = listOf(
            Triple("Photo", "🎨 Create Photo", Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476)))),
            Triple("Video", "🎬 Create Video", Brush.linearGradient(listOf(Color(0xFF4776E6), Color(0xFF8E54E9)))),
            Triple("Exam", "📝 Exam Notes", Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))),
            Triple("Handnotes", "✍️ Handnotes", Brush.linearGradient(listOf(Color(0xFFF12711), Color(0xFFF5AF19))))
        )

        actions.forEach { (type, label, brush) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush)
                    .clickable { onActionSelected(type) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun JornCreatorDialog(
    colors: WingsThemeColors,
    creatorType: String,
    onDismiss: () -> Unit,
    viewModel: WingsViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember {
        mutableStateOf(
            when (creatorType) {
                "Photo" -> "A futuristic golden android falcon taking off from a cyber city, digital art, 8k"
                "Video" -> "Drone flythrough of a cosmic mountain range under twin suns, neon particles"
                "Exam" -> "Physics Exam paper on Newton's Laws and Gravity with 5 MCQs and 3 short answers"
                else -> "Complete Organic Chemistry revision study notes on Benzene and Alkyl Halides"
            }
        )
    }

    var isAdPlaying by remember { mutableStateOf(false) }
    var adTimer by remember { mutableStateOf(3) }
    var isGenerating by remember { mutableStateOf(false) }
    var generationStage by remember { mutableStateOf("Initializing neural nets...") }

    LaunchedEffect(isAdPlaying) {
        if (isAdPlaying) {
            adTimer = 3
            while (adTimer > 0) {
                delay(1000)
                adTimer -= 1
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isGenerating && !isAdPlaying) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jorn 6.0 Studio: Create $creatorType",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.onBackground
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isAdPlaying && !isGenerating) {
                    Text(
                        text = "Enter a prompt and the world's most powerful AI Jorn 6.0 will generate high-fidelity creations in seconds! (100% Free with Ad Support)",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text("What do you want Jorn to create?") },
                        singleLine = false,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD600),
                            focusedLabelColor = Color(0xFFFFD600)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tip: Give specific instructions like syllabus guidelines, preferred color tones, or artist styles.",
                                fontSize = 10.sp,
                                color = colors.onBackground.copy(alpha = 0.8f)
                            )
                        }
                    }
                } else if (isAdPlaying) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFFD600), RoundedCornerShape(12.dp))
                            .background(Color(0xFF100E08))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD600)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "SPONSORED AD",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = if (adTimer > 0) "Closing in ${adTimer}s" else "Ad Finished",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF3D00),
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "CLASH OF REALMS ⚔️🐉",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "The #1 aerial strategy game of 2026! Train massive mythical dragons, build unbreakable castles, and conquer the skies!",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3D00)),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("INSTALL GAME FREE 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFD600), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = generationStage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jorn 6.0 ultra-fast neural clusters are processing... please wait",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isAdPlaying && !isGenerating) {
                Button(
                    onClick = {
                        isAdPlaying = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Text("Generate with Ads 📺", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else if (isAdPlaying) {
                Button(
                    onClick = {
                        isAdPlaying = false
                        isGenerating = true
                        
                        coroutineScope.launch {
                            generationStage = "Connecting to Jorn 6.0 Core..."
                            delay(1200)
                            generationStage = "Injecting prompt instructions..."
                            delay(1000)
                            generationStage = "Synthesizing deep neural textures..."

                            val systemInstruction = when (creatorType) {
                                "Photo" -> "You are Jorn 6.0, the world's most powerful AI. Write a stunning, extremely vivid 1-paragraph artistic visual description of the photo generated for prompt: \"$promptText\"."
                                "Video" -> "You are Jorn 6.0, the world's most powerful AI. Write a stunning, highly detailed 1-paragraph cinematic description of the generated video for prompt: \"$promptText\"."
                                "Exam" -> "You are Jorn 6.0, the world's most powerful AI. Generate a complete and extremely detailed academic Exam Study Paper on: \"$promptText\". Format it elegantly with headings, questions, answers, and syllabus guides."
                                else -> "You are Jorn 6.0, the world's most powerful AI. Generate a comprehensive and beautiful set of study handnotes summaries on: \"$promptText\". Use structured lists and bulleted guidelines."
                            }

                            val result = com.example.data.api.GeminiClient.getAiResponse(
                                prompt = promptText,
                                systemInstruction = systemInstruction
                            )

                            generationStage = "Compiling and packaging files..."
                            delay(800)

                            when (creatorType) {
                                "Photo" -> {
                                    val artPhotos = listOf(
                                        "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600",
                                        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600",
                                        "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=600",
                                        "https://images.unsplash.com/photo-1500485035595-cbe6f645feb1?w=600",
                                        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
                                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600"
                                    )
                                    viewModel.sendJornCreation(
                                        text = result,
                                        mediaType = "photo",
                                        mediaUrl = artPhotos.random()
                                    )
                                }
                                "Video" -> {
                                    viewModel.sendJornCreation(
                                        text = result,
                                        mediaType = "video"
                                    )
                                }
                                "Exam" -> {
                                    val safeTitle = promptText.take(15).trim().replace("[^a-zA-Z0-9]".toRegex(), "_")
                                    viewModel.sendJornCreation(
                                        text = "Exam_Prep_${safeTitle}.pdf",
                                        mediaType = "document"
                                    )
                                    delay(500)
                                    viewModel.sendJornCreation(
                                        text = "📝 JORN 6.0 EXAM GENERATION RESULT:\n\n$result"
                                    )
                                }
                                "Handnotes" -> {
                                    val safeTitle = promptText.take(15).trim().replace("[^a-zA-Z0-9]".toRegex(), "_")
                                    viewModel.sendJornCreation(
                                        text = "Handnotes_${safeTitle}.pdf",
                                        mediaType = "document"
                                    )
                                    delay(500)
                                    viewModel.sendJornCreation(
                                        text = "✍️ JORN 6.0 HANDNOTES SUMMARY:\n\n$result"
                                    )
                                }
                            }

                            isGenerating = false
                            onDismiss()
                        }
                    },
                    enabled = adTimer == 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), disabledContainerColor = Color.Gray.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (adTimer > 0) {
                            "Play Ad (${adTimer}s)"
                        } else {
                            "Unlock & Generate 🚀"
                        },
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!isAdPlaying && !isGenerating) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        },
        containerColor = colors.surface
    )
}
