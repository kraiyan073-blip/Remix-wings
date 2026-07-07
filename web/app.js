// Chat App State
const state = {
    currentChannel: 'wings',
    messages: {
        wings: [
            { id: 1, sender: 'ai', text: 'Hello! 👋 I\'m Wings, your AI assistant powered by Gemini. How can I help you today?', timestamp: new Date(Date.now() - 5 * 60000) }
        ],
        support: [],
        updates: []
    },
    apiKey: localStorage.getItem('gemini_api_key') || '',
    theme: localStorage.getItem('theme') || 'dark',
    isLoading: false
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
    if (!text || state.isLoading) return;

    // Check if API key is set
    if (!state.apiKey && state.currentChannel === 'wings') {
        alert('Please set your Gemini API key in settings first! ⚙️');
        openSettings();
        return;
    }

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

    state.isLoading = true;
    sendBtn.disabled = true;

    // Get AI response
    getAIResponseFromGemini(text)
        .then((aiResponse) => {
            typingIndicator.remove();
            const aiMessage = {
                id: Date.now() + 1,
                sender: 'ai',
                text: aiResponse,
                timestamp: new Date()
            };
            state.messages[state.currentChannel].push(aiMessage);
            renderMessages();
            scrollToBottom();
        })
        .catch((error) => {
            typingIndicator.remove();
            const errorMessage = {
                id: Date.now() + 1,
                sender: 'ai',
                text: `Sorry, I encountered an error: ${error.message}. Please check your API key and try again.`,
                timestamp: new Date()
            };
            state.messages[state.currentChannel].push(errorMessage);
            renderMessages();
            scrollToBottom();
        })
        .finally(() => {
            state.isLoading = false;
            sendBtn.disabled = false;
        });
}

// Get AI Response from Gemini API
async function getAIResponseFromGemini(userText) {
    // Validate API key
    if (!state.apiKey) {
        throw new Error('API key not configured');
    }

    try {
        // Call Gemini API v1beta
        const response = await fetch('https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'x-goog-api-key': state.apiKey
            },
            body: JSON.stringify({
                contents: [{
                    parts: [{
                        text: userText
                    }]
                }],
                generationConfig: {
                    temperature: 0.7,
                    topK: 40,
                    topP: 0.95,
                    maxOutputTokens: 1024,
                }
            })
        });

        if (!response.ok) {
            const errorData = await response.json();
            
            if (response.status === 401) {
                throw new Error('Invalid API key. Please check your key in settings.');
            }
            if (response.status === 429) {
                throw new Error('Rate limit exceeded. Please wait a moment and try again.');
            }
            if (response.status === 400) {
                throw new Error('Bad request: ' + (errorData.error?.message || 'Invalid input'));
            }
            
            throw new Error(errorData.error?.message || `API Error: ${response.statusText}`);
        }

        const data = await response.json();

        // Extract text from response
        if (data.candidates && data.candidates.length > 0) {
            const candidate = data.candidates[0];
            if (candidate.content && candidate.content.parts && candidate.content.parts.length > 0) {
                return candidate.content.parts[0].text;
            }
        }

        throw new Error('No content in response from Gemini API');
    } catch (error) {
        if (error instanceof TypeError) {
            throw new Error('Network error: Unable to reach Gemini API. Check your internet connection.');
        }
        throw error;
    }
}

// Get AI Response (Fallback for other channels)
function getAIResponseFallback(userText) {
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
                ${escapeHtml(msg.text)}
                <div class="message-time">${timeStr}</div>
            </div>
        `;
        messagesContainer.appendChild(messageGroup);
    });
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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
    state.apiKey = apiKeyInput.value.trim();
    localStorage.setItem('gemini_api_key', state.apiKey);
    closeSettings();
    alert('Settings saved! ✅ You can now chat with Wings Assistant.');
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
