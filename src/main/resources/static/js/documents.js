// Document Management Component (Phase 3)

function initializeDocuments() {
    // Upload button
    const uploadBtn = document.getElementById('doc-upload-btn');
    if (uploadBtn) {
        uploadBtn.addEventListener('click', handleDocumentUpload);
    }

    // Search button
    const searchBtn = document.getElementById('search-btn');
    if (searchBtn) {
        searchBtn.addEventListener('click', handleSearch);
    }

    // Search on Enter key
    const searchInput = document.getElementById('search-query');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }
}

async function handleDocumentUpload() {
    const titleInput = document.getElementById('doc-title');
    const contentInput = document.getElementById('doc-content');
    const categorySelect = document.getElementById('doc-category');

    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    const category = categorySelect.value;

    if (!title || !content) {
        alert('Please fill in both title and content');
        return;
    }

    showLoading('Uploading and processing document...');

    try {
        const response = await uploadDocument(title, content, category);

        alert(`Document uploaded successfully!\nDocument ID: ${response.id}\nChunks created: ${response.chunkCount || 'N/A'}`);

        // Clear form
        titleInput.value = '';
        contentInput.value = '';

        // Reload documents list
        loadAllDocuments();

    } catch (error) {
        showError(`Failed to upload document: ${error.message}`);
    } finally {
        hideLoading();
    }
}

async function handleSearch() {
    const searchInput = document.getElementById('search-query');
    const query = searchInput.value.trim();

    if (!query) {
        alert('Please enter a search query');
        return;
    }

    showLoading('Searching documents...');

    try {
        const results = await searchDocuments(query, 5, 0.5, null);

        displaySearchResults(results);

    } catch (error) {
        showError(`Search failed: ${error.message}`);
    } finally {
        hideLoading();
    }
}

function displaySearchResults(results) {
    const resultsContainer = document.getElementById('search-results');
    if (!resultsContainer) return;

    resultsContainer.innerHTML = '';

    if (!results.results || results.results.length === 0) {
        resultsContainer.innerHTML = '<p style="color: #6b7280;">No results found. Try a different query or lower similarity threshold.</p>';
        return;
    }

    results.results.forEach(result => {
        const resultDiv = document.createElement('div');
        resultDiv.className = 'search-result-item';

        const title = document.createElement('h4');
        title.textContent = result.documentTitle || 'Untitled';

        const score = document.createElement('span');
        score.className = 'similarity-score';
        score.textContent = formatSimilarity(result.similarityScore);

        const category = document.createElement('span');
        category.style.marginLeft = '10px';
        category.style.color = '#6b7280';
        category.textContent = `[${result.category || 'N/A'}]`;

        const content = document.createElement('p');
        content.textContent = truncateText(result.content, 200);

        title.appendChild(score);
        title.appendChild(category);
        resultDiv.appendChild(title);
        resultDiv.appendChild(content);

        resultsContainer.appendChild(resultDiv);
    });
}

async function loadAllDocuments() {
    try {
        const documents = await getAllDocuments();

        displayDocumentsList(documents);

    } catch (error) {
        console.error('Failed to load documents:', error);
    }
}

function displayDocumentsList(documents) {
    const listContainer = document.getElementById('documents-list');
    if (!listContainer) return;

    listContainer.innerHTML = '';

    if (!documents || documents.length === 0) {
        listContainer.innerHTML = '<p style="color: #6b7280;">No documents uploaded yet. Upload your first document above!</p>';
        return;
    }

    documents.forEach(doc => {
        const docDiv = document.createElement('div');
        docDiv.className = 'document-item';

        const title = document.createElement('h4');
        title.textContent = doc.title || 'Untitled';

        const meta = document.createElement('div');
        meta.className = 'doc-meta';
        meta.innerHTML = `
            <span>Category: ${doc.category || 'N/A'}</span>
            <span>ID: ${doc.id}</span>
            <span>Created: ${doc.createdAt ? formatDate(doc.createdAt) : 'N/A'}</span>
        `;

        docDiv.appendChild(title);
        docDiv.appendChild(meta);

        listContainer.appendChild(docDiv);
    });
}
