// Chat Component (Phase 1)

let chatMessages = [];

function initializeChat() {
    const sendBtn = document.getElementById('chat-send-btn');
    const inputField = document.getElementById('chat-input');

    if (sendBtn) {
        sendBtn.addEventListener('click', handleChatSend);
    }

    if (inputField) {
        inputField.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleChatSend();
            }
        });
    }
}

async function handleChatSend() {
    const inputField = document.getElementById('chat-input');
    const providerSelect = document.getElementById('chat-provider');
    const message = inputField.value.trim();

    if (!message) {
        return;
    }

    const provider = providerSelect.value;

    // Add user message to chat
    addMessageToChat('user', message);
    inputField.value = '';

    // Show loading
    showLoading('Sending message to LLM...');

    try {
        const response = await sendChatMessage(message, provider);

        // Add assistant response to chat
        addMessageToChat('assistant', response.response || response.content || 'No response');

    } catch (error) {
        addMessageToChat('assistant', `Error: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function addMessageToChat(role, content) {
    const messagesContainer = document.getElementById('chat-messages');
    if (!messagesContainer) return;

    const messageDiv = document.createElement('div');
    messageDiv.className = `chat-message ${role}`;
    messageDiv.textContent = content;

    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    chatMessages.push({ role, content });
}
