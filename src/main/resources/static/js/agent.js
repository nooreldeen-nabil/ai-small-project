// Agent Component (Phase 5)

function initializeAgent() {
    const submitBtn = document.getElementById('agent-submit-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', handleAgentTaskSubmit);
    }

    // Load available tools
    loadAgentTools();
}

async function loadAgentTools() {
    try {
        const tools = await getAgentTools();

        displayAgentTools(tools);

    } catch (error) {
        console.error('Failed to load agent tools:', error);
    }
}

function displayAgentTools(tools) {
    const toolsList = document.getElementById('agent-tools-list');
    if (!toolsList) return;

    toolsList.innerHTML = '';

    if (!tools || tools.length === 0) {
        toolsList.innerHTML = '<p style="color: #6b7280;">No tools available</p>';
        return;
    }

    tools.forEach(tool => {
        const toolDiv = document.createElement('div');
        toolDiv.className = 'tool-item';

        toolDiv.innerHTML = `
            <strong>${tool.name || 'Unknown'}</strong><br>
            <span style="font-size: 12px; color: #6b7280;">${truncateText(tool.description || '', 60)}</span>
        `;

        toolsList.appendChild(toolDiv);
    });
}

async function handleAgentTaskSubmit() {
    const taskInput = document.getElementById('agent-task');
    const task = taskInput.value.trim();

    if (!task) {
        alert('Please enter a task for the agent');
        return;
    }

    showLoading('Agent is executing task...');

    try {
        const response = await executeAgentTask(task, 10, 0.7);

        displayAgentResult(response);

    } catch (error) {
        showError(`Agent task failed: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function displayAgentResult(response) {
    const resultBox = document.getElementById('agent-result');
    const logDiv = document.getElementById('agent-log');
    const answerDiv = document.getElementById('agent-answer');
    const llmCallsSpan = document.getElementById('agent-llm-calls');
    const toolCallsSpan = document.getElementById('agent-tool-calls');
    const tokensSpan = document.getElementById('agent-tokens');

    if (!resultBox || !logDiv || !answerDiv) return;

    // Show result box
    resultBox.classList.remove('hidden');

    // Display execution log
    logDiv.innerHTML = '';

    if (response.executionLog && response.executionLog.length > 0) {
        response.executionLog.forEach(step => {
            const stepDiv = document.createElement('div');
            stepDiv.className = `log-step ${step.type}`;

            let content = '';

            if (step.type === 'THINKING') {
                content = `💭 THINKING: ${step.thinking || step.content || ''}`;
            } else if (step.type === 'TOOL_CALL') {
                content = `🔧 TOOL CALL: ${step.toolName || ''}\nArgs: ${JSON.stringify(step.toolArgs || {}, null, 2)}`;
            } else if (step.type === 'TOOL_RESULT') {
                const result = typeof step.toolResult === 'object' ? JSON.stringify(step.toolResult, null, 2) : step.toolResult;
                content = `✅ RESULT: ${step.toolName || ''}\n${truncateText(result, 200)}`;
            } else if (step.type === 'FINAL_ANSWER') {
                content = `🎯 FINAL: ${step.thinking || step.content || ''}`;
            }

            stepDiv.textContent = content;
            logDiv.appendChild(stepDiv);
        });
    } else {
        logDiv.innerHTML = '<p>No execution log available</p>';
    }

    // Display final answer
    answerDiv.textContent = response.answer || 'No answer available';

    // Display metadata
    if (llmCallsSpan) {
        llmCallsSpan.textContent = response.llmCallCount || 0;
    }

    if (toolCallsSpan) {
        toolCallsSpan.textContent = response.toolCallCount || 0;
    }

    if (tokensSpan) {
        tokensSpan.textContent = response.totalTokens || 'N/A';
    }

    // Scroll to result
    resultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}
