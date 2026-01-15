// Main Application Logic

// Global state
let currentPage = 'dashboard';

// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('LLM Agentic AI MVP - Frontend Loaded');

    // Set up navigation
    setupNavigation();

    // Initialize all modules
    initializeDashboard();
    initializeChat();
    initializePrompts();
    initializeDocuments();
    initializeQA();
    initializeAgent();
    initializeWorkflow();

    // Check system health on startup
    updateSystemStatus();

    // Refresh system status every 30 seconds
    setInterval(updateSystemStatus, 30000);
});

// Navigation Setup
function setupNavigation() {
    const navButtons = document.querySelectorAll('.nav-btn');

    navButtons.forEach(button => {
        button.addEventListener('click', function() {
            const pageName = this.getAttribute('data-page');
            navigateTo(pageName);
        });
    });
}

// Navigate to a specific page
function navigateTo(pageName) {
    // Hide all pages
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });

    // Show selected page
    const targetPage = document.getElementById(`${pageName}-page`);
    if (targetPage) {
        targetPage.classList.add('active');
    }

    // Update navigation buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    const activeBtn = document.querySelector(`[data-page="${pageName}"]`);
    if (activeBtn) {
        activeBtn.classList.add('active');
    }

    currentPage = pageName;

    // Trigger page-specific initialization if needed
    if (pageName === 'documents') {
        loadAllDocuments();
    } else if (pageName === 'agent') {
        loadAgentTools();
    }
}

// Update system status in header
async function updateSystemStatus() {
    try {
        const health = await checkSystemHealth();
        const statusDot = document.getElementById('system-status');
        const statusText = document.getElementById('system-status-text');

        if (health.status === 'UP') {
            statusDot.className = 'status-dot healthy';
            statusText.textContent = 'System Healthy';
        } else {
            statusDot.className = 'status-dot unhealthy';
            statusText.textContent = 'System Down';
        }
    } catch (error) {
        const statusDot = document.getElementById('system-status');
        const statusText = document.getElementById('system-status-text');
        statusDot.className = 'status-dot unhealthy';
        statusText.textContent = 'Connection Error';
    }
}

// Utility: Format date
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString();
}

// Utility: Truncate text
function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// Utility: Format similarity score
function formatSimilarity(score) {
    return `${(score * 100).toFixed(1)}%`;
}

// Utility: Create element with classes and text
function createElement(tag, classes, text) {
    const element = document.createElement(tag);
    if (classes) element.className = classes;
    if (text) element.textContent = text;
    return element;
}

// Export for debugging
window.app = {
    navigateTo,
    updateSystemStatus,
    formatDate,
    truncateText,
    formatSimilarity
};
