// Prompt Engineering Component (Phase 2)

let currentTechnique = 'zero-shot';

function initializePrompts() {
    // Set up technique tabs
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const technique = this.getAttribute('data-technique');
            switchTechnique(technique);
        });
    });

    // Set up submit button
    const submitBtn = document.getElementById('prompt-submit-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', handlePromptSubmit);
    }
}

function switchTechnique(technique) {
    currentTechnique = technique;

    // Update active tab
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    const activeTab = document.querySelector(`[data-technique="${technique}"]`);
    if (activeTab) {
        activeTab.classList.add('active');
    }

    // Update placeholder text
    const inputField = document.getElementById('prompt-input');
    const placeholders = {
        'zero-shot': 'e.g., What is machine learning?',
        'few-shot': 'e.g., Translate "Hello" to Spanish',
        'chain-of-thought': 'e.g., If I have 5 apples and buy 3 more, how many do I have?',
        'structured-output': 'e.g., Describe a car in JSON format'
    };

    if (inputField) {
        inputField.placeholder = placeholders[technique];
    }
}

async function handlePromptSubmit() {
    const inputField = document.getElementById('prompt-input');
    const input = inputField.value.trim();

    if (!input) {
        alert('Please enter a question');
        return;
    }

    showLoading(`Processing with ${currentTechnique} technique...`);

    try {
        const response = await sendPrompt(currentTechnique, input);

        // Display result
        displayPromptResult(response);

    } catch (error) {
        showError(`Failed to get response: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function displayPromptResult(response) {
    const resultBox = document.getElementById('prompt-result');
    const responseDiv = document.getElementById('prompt-response');
    const techniqueSpan = document.getElementById('prompt-technique-used');
    const tokensSpan = document.getElementById('prompt-tokens');

    if (!resultBox || !responseDiv) return;

    // Show result box
    resultBox.classList.remove('hidden');

    // Display response
    responseDiv.textContent = response.response || response.content || 'No response';

    // Display metadata
    if (techniqueSpan) {
        techniqueSpan.textContent = currentTechnique;
    }

    if (tokensSpan && response.tokensUsed) {
        tokensSpan.textContent = response.tokensUsed;
    } else if (tokensSpan) {
        tokensSpan.textContent = 'N/A';
    }

    // Scroll to result
    resultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}
