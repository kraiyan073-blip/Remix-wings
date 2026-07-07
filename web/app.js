// Chat App State
const state = {
    currentChannel: 'wings',
    messages: {
        wings: [
            { id: 1, sender: 'ai', text: 'Hello! 👋 I\'m Wings, your AI assistant. How can I help you today?', timestamp: new Date(Date.now() - 5 * 60000) }
        ],
        support: [],
        updates: []
    },
    apiKey: localStorage.getItem('gemini_api_key') || '',
    theme: localStorage.getItem('theme') || 'dark'
};

// DOM Elements
const messageInput = document.getElementById('messageInput');
const sendBtn = document.getElementById('sendBtn');
const messagesContainer = document.getElementById('messagesContainer');
const channelsList = document.getElementById('channelsList');
const settingsModal = document.getElementById('settingsModal');
const modalOverlay = document.getElementById('modalOverlay');
const settingsBtn = document.getElementById('settingsBtn');
const apiKeyInput = document.getElementById('apiKeyInput');
const themeSelect = document.getElementById('themeSelect');
const saveSettingsBtn = document.getElementById('saveSettingsBtn');
const closeBtn = document.querySelector('.close-btn');
const attachBtn = document.getElementById('attachBtn');

// Initialize
function init() {
    // Set theme
    document.body.className = state.theme === 'dark' ? '' : 'light-theme';
    themeSelect.value = state.theme;
    apiKeyInput.value = state.apiKey;

    // Event listeners
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Channel switching
    channelsList.addEventListener('click', (e) => {
        const channel = e.target.closest('.channel');
        if (channel) {
            switchChannel(channel.dataset.channel);
        }
    });

    // Settings
    settingsBtn.addEventListener('click', openSettings);
    closeBtn.addEventListener('click', closeSettings);
    modalOverlay.addEventListener('click', closeSettings);
    saveSettingsBtn.addEventListener('click', saveSettings);
    themeSelect.addEventListener('change', changeTheme);

    // Attach button
    attachBtn.addEventListener('click', () => {
        alert('File attachment feature coming soon! 📎');
    });

    // Render initial messages
    renderMessages();
}

// Send Message
function sendMessage() {
    const text = messageInput.value.trim();
    if (!text) return;

    // Add user message
    const userMessage = {
        id: Date.now(),
        sender: 'user',
        text: text,
        timestamp: new Date()
    };
    state.messages[state.currentChannel].push(userMessage);
    messageInput.value = '';
    renderMessages();
    scrollToBottom();

    // Show typing indicator
    const typingIndicator = document.createElement('div');
    typingIndicator.className = 'message-group ai';
    typingIndicator.innerHTML = '<div class="typing-indicator"><div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div></div>';
    messagesContainer.appendChild(typingIndicator);
    scrollToBottom();

    // Simulate AI response
    setTimeout(() => {
        typingIndicator.remove();
        const aiMessage = {
            id: Date.now() + 1,
            sender: 'ai',
            text: getAIResponse(text),
            timestamp: new Date()
        };
        state.messages[state.currentChannel].push(aiMessage);
        renderMessages();
        scrollToBottom();
    }, 1000 + Math.random() * 2000);
}

// Get AI Response (Mock)
function getAIResponse(userText) {
    const responses = [
        'That\'s a great question! 🤔',
        'I think you\'re absolutely right! ✨',
        'Let me help you with that! 💡',
        'Interesting! Tell me more about that. 👂',
        'I\'ve got some ideas for you! 🚀',
        'That\'s a clever observation! 🎯',
        'I understand what you mean. Let me think... 🧠',
        'Sounds good to me! 👍'
    ];

    // Simple keyword-based responses
    const lowerText = userText.toLowerCase();
    
    if (lowerText.includes('hello') || lowerText.includes('hi')) {
        return 'Hello! 👋 Nice to chat with you. What can I assist with today?';
    }
    if (lowerText.includes('how are you')) {
        return 'I\'m doing great, thanks for asking! 😊 Ready to help you with anything.';
    }
    if (lowerText.includes('help')) {
        return 'Of course! I\'m here to help. What do you need assistance with? 🆘';
    }
    if (lowerText.includes('thanks') || lowerText.includes('thank you')) {
        return 'You\'re welcome! Happy to help! 😄';
    }
    if (lowerText.includes('bye') || lowerText.includes('goodbye')) {
        return 'Bye! It was great chatting with you. See you next time! 👋';
    }

    return responses[Math.floor(Math.random() * responses.length)];
}

// Switch Channel
function switchChannel(channelName) {
    state.currentChannel = channelName;

    // Update active channel UI
    document.querySelectorAll('.channel').forEach(ch => {
        ch.classList.toggle('active', ch.dataset.channel === channelName);
    });

    renderMessages();
    scrollToBottom();
}

// Render Messages
function renderMessages() {
    messagesContainer.innerHTML = '';
    const messages = state.messages[state.currentChannel];

    if (messages.length === 0) {
        messagesContainer.innerHTML = `
            <div class="message-group system">
                <div class="system-message">
                    <p>Welcome to Remix: Wings! 🪽</p>
                    <p>Start chatting with the Wings Assistant powered by Gemini AI</p>
                </div>
            </div>
        `;
        return;
    }

    messages.forEach((msg, index) => {
        const isConsecutive = index > 0 && messages[index - 1].sender === msg.sender;
        const messageGroup = document.createElement('div');
        messageGroup.className = `message-group ${msg.sender}`;

        const timeStr = msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        messageGroup.innerHTML = `
            <div class="message ${msg.sender}">
                ${msg.text}
                <div class="message-time">${timeStr}</div>
            </div>
        `;
        messagesContainer.appendChild(messageGroup);
    });
}

// Scroll to bottom
function scrollToBottom() {
    setTimeout(() => {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }, 0);
}

// Settings Modal
function openSettings() {
    settingsModal.classList.add('active');
    modalOverlay.classList.add('active');
}

function closeSettings() {
    settingsModal.classList.remove('active');
    modalOverlay.classList.remove('active');
}

function saveSettings() {
    state.apiKey = apiKeyInput.value;
    localStorage.setItem('gemini_api_key', state.apiKey);
    closeSettings();
    alert('Settings saved! ✅');
}

function changeTheme() {
    const theme = themeSelect.value;
    state.theme = theme;
    localStorage.setItem('theme', theme);
    document.body.className = theme === 'dark' ? '' : 'light-theme';
}

// Initialize app
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
