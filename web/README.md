# Remix: Wings - Web Interface

A web-based interface for the Remix: Wings AI chat application. This allows you to use the Wings Assistant directly in your browser without needing to set up Android Studio.

## Features

✨ **Interactive Chat Interface**
- Clean, modern WhatsApp-style design
- Real-time message rendering with animations
- Typing indicators for AI responses
- Multiple chat channels

🎨 **Customizable Experience**
- Light/Dark theme toggle
- Responsive design (desktop, tablet, mobile)
- Smooth transitions and animations

🤖 **AI Integration Ready**
- Settings panel to configure Gemini API key
- Local storage for API keys (browser-only)
- Mock AI responses (ready to integrate with real Gemini API)

## Quick Start

### Option 1: Direct Browser Access
1. Clone or download this repository
2. Open `web/index.html` in your Chrome browser
3. Start chatting!

### Option 2: Local Web Server
```bash
# Using Python 3
python -m http.server 8000

# Then open: http://localhost:8000/web/index.html
```

### Option 3: Using Node.js
```bash
# Using http-server
npx http-server web/

# Then open: http://localhost:8080
```

## Configuration

### Adding Your Gemini API Key

1. Click the ⚙️ settings button in the top right
2. Enter your Gemini API key
3. Click "Save Settings"
4. Your key is stored locally in your browser

**Note:** API keys are stored in browser localStorage only. They are never sent to any server.

## File Structure

```
web/
├── index.html    # Main HTML structure
├── styles.css    # Complete styling and theming
├── app.js        # Chat logic and interactions
└── README.md     # This file
```

## Features to Explore

- 💬 **Send Messages**: Type and hit Enter or click Send
- 📎 **File Attachment**: Click the paperclip button (feature coming soon)
- 🔄 **Switch Channels**: Click on different chats in the sidebar
- 🎨 **Change Theme**: Open settings and toggle between Light/Dark
- ⚙️ **Settings**: Configure API key and preferences

## Integration with Real Gemini API

To integrate with the real Gemini API, update the `getAIResponse()` function in `app.js`:

```javascript
// Example integration
async function getAIResponse(userText) {
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
            }]
        })
    });
    
    const data = await response.json();
    return data.candidates[0].content.parts[0].text;
}
```

## Browser Compatibility

✅ Chrome/Edge (v90+)
✅ Firefox (v88+)
✅ Safari (v14+)
✅ Mobile browsers

## Troubleshooting

**Messages not appearing?**
- Check browser console (F12) for errors
- Ensure JavaScript is enabled

**Settings not saving?**
- Check that localStorage is enabled in browser settings
- Clear browser cache and try again

**Styling issues?**
- Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
- Try a different browser

## Next Steps

1. Integrate with real Gemini API
2. Add message persistence (local database or backend)
3. Implement user authentication
4. Add rich media support (images, emojis, etc.)
5. Mobile app wrapper

## License

This web interface is part of the Remix: Wings project.

## Support

For issues or feature requests, visit the main repository.
