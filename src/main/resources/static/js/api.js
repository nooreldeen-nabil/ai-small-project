// API Client for Backend Communication

const API_BASE_URL = 'http://localhost:8080';

// Utility function to show/hide loading overlay
function showLoading(message = 'Processing...') {
    const overlay = document.getElementById('loading-overlay');
    const text = document.getElementById('loading-text');
    if (overlay && text) {
        text.textContent = message;
        overlay.classList.remove('hidden');
    }
}

function hideLoading() {
    const overlay = document.getElementById('loading-overlay');
    if (overlay) {
        overlay.classList.add('hidden');
    }
}

// Utility function to show error messages
function showError(message) {
    console.error('Error:', message);
    alert(`Error: ${message}`);
    hideLoading();
}

// Generic API call function
async function apiCall(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`HTTP ${response.status}: ${errorText || response.statusText}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

// ===== System APIs =====

async function checkSystemHealth() {
    try {
        return await apiCall('/actuator/health');
    } catch (error) {
        return { status: 'DOWN', error: error.message };
    }
}

// ===== Chat APIs (Phase 1) =====

async function sendChatMessage(message, provider = 'GEMINI') {
    return await apiCall('/api/chat', {
        method: 'POST',
        body: JSON.stringify({
            message: message,
            provider: provider
        })
    });
}

// ===== Prompt APIs (Phase 2) =====

async function sendPrompt(technique, input) {
    const endpoints = {
        'zero-shot': '/api/prompt/zero-shot',
        'few-shot': '/api/prompt/few-shot',
        'chain-of-thought': '/api/prompt/chain-of-thought',
        'structured-output': '/api/prompt/structured-output'
    };

    return await apiCall(endpoints[technique], {
        method: 'POST',
        body: JSON.stringify({ input: input })
    });
}

// ===== Document APIs (Phase 3) =====

async function uploadDocument(title, content, category) {
    return await apiCall('/api/documents/upload', {
        method: 'POST',
        body: JSON.stringify({
            title: title,
            content: content,
            category: category
        })
    });
}

async function searchDocuments(query, topK = 5, similarityThreshold = 0.5, category = null) {
    return await apiCall('/api/documents/search/semantic', {
        method: 'POST',
        body: JSON.stringify({
            query: query,
            topK: topK,
            similarityThreshold: similarityThreshold,
            category: category
        })
    });
}

async function getAllDocuments() {
    return await apiCall('/api/documents');
}

async function getDocumentById(id) {
    return await apiCall(`/api/documents/${id}`);
}

async function deleteDocument(id) {
    return await apiCall(`/api/documents/${id}`, {
        method: 'DELETE'
    });
}

// ===== Q&A APIs (Phase 4 - RAG) =====

async function askQuestion(question, topK = 5, similarityThreshold = 0.5) {
    return await apiCall('/api/qa/document', {
        method: 'POST',
        body: JSON.stringify({
            question: question,
            topK: topK,
            similarityThreshold: similarityThreshold
        })
    });
}

// ===== Agent APIs (Phase 5) =====

async function executeAgentTask(task, maxToolCalls = 10, temperature = 0.7) {
    return await apiCall('/api/agent/task', {
        method: 'POST',
        body: JSON.stringify({
            task: task,
            maxToolCalls: maxToolCalls,
            temperature: temperature
        })
    });
}

async function getAgentTools() {
    return await apiCall('/api/agent/tools');
}

async function checkAgentHealth() {
    return await apiCall('/api/agent/health');
}

// ===== Workflow APIs (Phase 6) =====

async function startDocumentWorkflow(title, content) {
    return await apiCall('/api/workflow/start-document', {
        method: 'POST',
        body: JSON.stringify({
            documentTitle: title,
            documentContent: content
        })
    });
}

async function getWorkflowStatus(processInstanceKey) {
    return await apiCall(`/api/workflow/status/${processInstanceKey}`);
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        showLoading,
        hideLoading,
        showError,
        checkSystemHealth,
        sendChatMessage,
        sendPrompt,
        uploadDocument,
        searchDocuments,
        getAllDocuments,
        getDocumentById,
        deleteDocument,
        askQuestion,
        executeAgentTask,
        getAgentTools,
        checkAgentHealth,
        startDocumentWorkflow,
        getWorkflowStatus
    };
}
