// Q&A Component (Phase 4 - RAG)

function initializeQA() {
    const submitBtn = document.getElementById('qa-submit-btn');
    if (submitBtn) {
        submitBtn.addEventListener('click', handleQuestionSubmit);
    }

    const questionInput = document.getElementById('qa-question');
    if (questionInput) {
        questionInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleQuestionSubmit();
            }
        });
    }
}

async function handleQuestionSubmit() {
    const questionInput = document.getElementById('qa-question');
    const question = questionInput.value.trim();

    if (!question) {
        alert('Please enter a question');
        return;
    }

    showLoading('Searching documents and generating answer...');

    try {
        const response = await askQuestion(question, 5, 0.5);

        displayQAResult(response);

    } catch (error) {
        showError(`Failed to get answer: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function displayQAResult(response) {
    const resultBox = document.getElementById('qa-result');
    const answerDiv = document.getElementById('qa-answer');
    const confidenceSpan = document.getElementById('qa-confidence');
    const providerSpan = document.getElementById('qa-provider');
    const tokensSpan = document.getElementById('qa-tokens');
    const citationsDiv = document.getElementById('qa-citations');

    if (!resultBox || !answerDiv) return;

    // Show result box
    resultBox.classList.remove('hidden');

    // Display answer
    answerDiv.textContent = response.answer || 'No answer generated';

    // Display confidence
    if (confidenceSpan) {
        confidenceSpan.textContent = response.confidence || 'N/A';
        confidenceSpan.className = `confidence-badge ${response.confidence || ''}`;
    }

    // Display provider
    if (providerSpan) {
        providerSpan.textContent = response.provider || 'N/A';
    }

    // Display tokens
    if (tokensSpan) {
        tokensSpan.textContent = response.tokensUsed || 'N/A';
    }

    // Display citations
    if (citationsDiv) {
        citationsDiv.innerHTML = '';

        if (response.citations && response.citations.length > 0) {
            response.citations.forEach((citation, index) => {
                const citationDiv = document.createElement('div');
                citationDiv.className = 'citation-item';

                const header = document.createElement('h4');
                header.innerHTML = `
                    Source ${index + 1}: ${citation.documentTitle || 'Unknown'}
                    <span class="similarity">(${formatSimilarity(citation.similarityScore)})</span>
                `;

                const excerpt = document.createElement('p');
                excerpt.textContent = truncateText(citation.excerpt, 200);

                citationDiv.appendChild(header);
                citationDiv.appendChild(excerpt);

                citationsDiv.appendChild(citationDiv);
            });
        } else {
            citationsDiv.innerHTML = '<p style="color: #6b7280;">No citations available</p>';
        }
    }

    // Scroll to result
    resultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}
