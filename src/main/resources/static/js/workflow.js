// Workflow Component (Phase 6)

function initializeWorkflow() {
    const startBtn = document.getElementById('workflow-start-btn');
    if (startBtn) {
        startBtn.addEventListener('click', handleWorkflowStart);
    }
}

async function handleWorkflowStart() {
    const titleInput = document.getElementById('workflow-doc-title');
    const contentInput = document.getElementById('workflow-doc-content');

    const title = titleInput.value.trim();
    const content = contentInput.value.trim();

    if (!title || !content) {
        alert('Please fill in both title and content');
        return;
    }

    showLoading('Starting workflow...');

    try {
        const response = await startDocumentWorkflow(title, content);

        displayWorkflowStatus(response);

        // Clear form
        titleInput.value = '';
        contentInput.value = '';

    } catch (error) {
        showError(`Failed to start workflow: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function displayWorkflowStatus(response) {
    const statusSection = document.getElementById('workflow-status');
    const processIdSpan = document.getElementById('workflow-process-id');
    const documentIdSpan = document.getElementById('workflow-document-id');
    const statusTextSpan = document.getElementById('workflow-status-text');

    if (!statusSection) return;

    // Show status section
    statusSection.classList.remove('hidden');

    // Display workflow information
    if (processIdSpan) {
        processIdSpan.textContent = response.processInstanceKey || response.workflowId || 'N/A';
    }

    if (documentIdSpan) {
        documentIdSpan.textContent = response.documentId || 'N/A';
    }

    if (statusTextSpan) {
        statusTextSpan.innerHTML = '<span style="color: #10b981; font-weight: bold;">✓ Started Successfully</span>';
    }

    // Scroll to status
    statusSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

    // Show success message
    alert(`Workflow started successfully!\n\nProcess ID: ${response.processInstanceKey || response.workflowId}\nDocument ID: ${response.documentId}\n\nView the workflow execution in Camunda Operate (http://localhost:8081)`);
}
