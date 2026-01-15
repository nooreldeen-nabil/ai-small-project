// Dashboard Component

function initializeDashboard() {
    // Load dashboard data on initialization
    loadDashboardData();

    // Refresh dashboard every 10 seconds
    setInterval(loadDashboardData, 10000);
}

async function loadDashboardData() {
    try {
        // Load system health
        const health = await checkSystemHealth();
        updateHealthDisplay(health);

        // Load document count
        const documents = await getAllDocuments();
        updateDocumentCount(documents.length || 0);

        // Workflow count (placeholder - would need actual API)
        updateWorkflowCount('N/A');
    } catch (error) {
        console.error('Failed to load dashboard data:', error);
    }
}

function updateHealthDisplay(health) {
    const healthStatus = document.getElementById('health-status');
    if (!healthStatus) return;

    if (health.status === 'UP') {
        healthStatus.innerHTML = '<span style="color: #10b981; font-size: 36px;">✓ Healthy</span>';
    } else {
        healthStatus.innerHTML = '<span style="color: #ef4444; font-size: 36px;">✗ Down</span>';
    }
}

function updateDocumentCount(count) {
    const docCount = document.getElementById('document-count');
    if (docCount) {
        docCount.textContent = count;
    }
}

function updateWorkflowCount(count) {
    const workflowCount = document.getElementById('workflow-count');
    if (workflowCount) {
        workflowCount.textContent = count;
    }
}
