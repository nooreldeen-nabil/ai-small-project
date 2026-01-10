-- ============================================================================
-- Oracle 23c AI Vector Search - Schema Creation Script
-- ============================================================================
-- This script creates tables with AI Vector Search capabilities
-- Demonstrates Oracle 23c's built-in vector data type
-- ============================================================================

-- Connect as ai_user
CONNECT ai_user/ai_password_2024@//localhost:1521/FREEPDB1;

-- Enable DBMS_OUTPUT for logging
SET SERVEROUTPUT ON;

BEGIN
  DBMS_OUTPUT.PUT_LINE('===========================================');
  DBMS_OUTPUT.PUT_LINE('Starting AI Vector Schema Creation');
  DBMS_OUTPUT.PUT_LINE('===========================================');
END;
/

-- ============================================================================
-- 1. DOCUMENTS Table - Stores original documents
-- ============================================================================
CREATE TABLE documents (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    title VARCHAR2(500) NOT NULL,
    content CLOB NOT NULL,
    document_type VARCHAR2(50),
    source_url VARCHAR2(2000),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR2(100) DEFAULT USER,
    CONSTRAINT chk_document_type CHECK (document_type IN ('PDF', 'TEXT', 'HTML', 'MARKDOWN', 'WORD', 'OTHER'))
);

-- Index for faster document retrieval
CREATE INDEX idx_documents_title ON documents(title);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_created ON documents(created_at);

COMMENT ON TABLE documents IS 'Stores original documents for RAG system';
COMMENT ON COLUMN documents.id IS 'Unique document identifier (UUID)';
COMMENT ON COLUMN documents.metadata IS 'JSON metadata about the document';

-- ============================================================================
-- 2. DOCUMENT_CHUNKS Table - Stores document chunks with vector embeddings
-- ============================================================================
-- This is the core table for AI Vector Search
-- Uses Oracle 23c's native VECTOR data type
-- ============================================================================
CREATE TABLE document_chunks (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    document_id RAW(16) NOT NULL,
    chunk_index NUMBER(10) NOT NULL,
    chunk_text CLOB NOT NULL,
    chunk_size NUMBER(10),
    -- Vector embedding (1536 dimensions for OpenAI/Anthropic embeddings)
    embedding VECTOR(1536, FLOAT32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chunk_document FOREIGN KEY (document_id)
        REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT uq_document_chunk UNIQUE (document_id, chunk_index)
);

-- Create vector index for similarity search (HNSW algorithm)
-- This enables fast approximate nearest neighbor search
CREATE VECTOR INDEX idx_chunks_vector ON document_chunks(embedding)
    ORGANIZATION NEIGHBOR PARTITIONS
    WITH DISTANCE COSINE
    WITH TARGET ACCURACY 95;

-- Regular indexes for faster joins and queries
CREATE INDEX idx_chunks_document ON document_chunks(document_id);
CREATE INDEX idx_chunks_created ON document_chunks(created_at);

COMMENT ON TABLE document_chunks IS 'Document chunks with vector embeddings for semantic search';
COMMENT ON COLUMN document_chunks.embedding IS 'Vector embedding (1536 dimensions) for similarity search';
COMMENT ON COLUMN document_chunks.chunk_index IS 'Sequential index of chunk within document';

-- ============================================================================
-- 3. CONVERSATIONS Table - Stores chat conversations
-- ============================================================================
CREATE TABLE conversations (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    user_id VARCHAR2(100),
    title VARCHAR2(500),
    context JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_user ON conversations(user_id);
CREATE INDEX idx_conversations_created ON conversations(created_at);

COMMENT ON TABLE conversations IS 'Stores conversation history for chat interface';

-- ============================================================================
-- 4. MESSAGES Table - Stores individual messages in conversations
-- ============================================================================
CREATE TABLE messages (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    conversation_id RAW(16) NOT NULL,
    role VARCHAR2(20) NOT NULL,
    content CLOB NOT NULL,
    tokens_used NUMBER(10),
    model VARCHAR2(100),
    prompt_type VARCHAR2(50),
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT chk_message_role CHECK (role IN ('user', 'assistant', 'system')),
    CONSTRAINT chk_prompt_type CHECK (prompt_type IN ('ZERO_SHOT', 'FEW_SHOT', 'CHAIN_OF_THOUGHT', 'STRUCTURED'))
);

CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_messages_created ON messages(created_at);

COMMENT ON TABLE messages IS 'Individual messages in conversations';
COMMENT ON COLUMN messages.prompt_type IS 'Type of prompting technique used';

-- ============================================================================
-- 5. AGENT_EXECUTIONS Table - Tracks agent execution history
-- ============================================================================
CREATE TABLE agent_executions (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    task_description VARCHAR2(2000) NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING',
    result CLOB,
    tools_used JSON,
    execution_steps JSON,
    error_message VARCHAR2(4000),
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms NUMBER(10),
    CONSTRAINT chk_agent_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_agent_status ON agent_executions(status);
CREATE INDEX idx_agent_started ON agent_executions(started_at);

COMMENT ON TABLE agent_executions IS 'Tracks agentic AI execution history and results';

-- ============================================================================
-- 6. TOOL_CALLS Table - Logs individual tool calls by agents
-- ============================================================================
CREATE TABLE tool_calls (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    execution_id RAW(16) NOT NULL,
    tool_name VARCHAR2(100) NOT NULL,
    parameters JSON,
    result CLOB,
    success NUMBER(1) DEFAULT 1,
    error_message VARCHAR2(4000),
    called_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms NUMBER(10),
    CONSTRAINT fk_tool_execution FOREIGN KEY (execution_id)
        REFERENCES agent_executions(id) ON DELETE CASCADE,
    CONSTRAINT chk_tool_success CHECK (success IN (0, 1))
);

CREATE INDEX idx_tool_execution ON tool_calls(execution_id);
CREATE INDEX idx_tool_name ON tool_calls(tool_name);
CREATE INDEX idx_tool_called ON tool_calls(called_at);

COMMENT ON TABLE tool_calls IS 'Logs individual tool/function calls made by agents';

-- ============================================================================
-- 7. CAMUNDA_TASKS Table - Integration with Camunda workflows
-- ============================================================================
CREATE TABLE camunda_tasks (
    id RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    process_instance_key VARCHAR2(100) NOT NULL,
    task_type VARCHAR2(100) NOT NULL,
    task_status VARCHAR2(20) DEFAULT 'PENDING',
    input_variables JSON,
    output_variables JSON,
    ai_processing_result CLOB,
    error_message VARCHAR2(4000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT chk_camunda_status CHECK (task_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_camunda_process ON camunda_tasks(process_instance_key);
CREATE INDEX idx_camunda_status ON camunda_tasks(task_status);
CREATE INDEX idx_camunda_type ON camunda_tasks(task_type);

COMMENT ON TABLE camunda_tasks IS 'Tracks Camunda workflow tasks processed by AI';

-- ============================================================================
-- VIEWS FOR ANALYTICS
-- ============================================================================

-- View: Document statistics
CREATE OR REPLACE VIEW v_document_stats AS
SELECT
    d.id,
    d.title,
    d.document_type,
    d.created_at,
    COUNT(dc.id) AS chunk_count,
    SUM(dc.chunk_size) AS total_size
FROM documents d
LEFT JOIN document_chunks dc ON d.id = dc.document_id
GROUP BY d.id, d.title, d.document_type, d.created_at;

COMMENT ON VIEW v_document_stats IS 'Document statistics including chunk counts';

-- View: Agent performance metrics
CREATE OR REPLACE VIEW v_agent_performance AS
SELECT
    DATE_TRUNC('day', started_at) AS execution_date,
    status,
    COUNT(*) AS execution_count,
    AVG(duration_ms) AS avg_duration_ms,
    MAX(duration_ms) AS max_duration_ms,
    MIN(duration_ms) AS min_duration_ms
FROM agent_executions
WHERE started_at >= SYSDATE - 30
GROUP BY DATE_TRUNC('day', started_at), status
ORDER BY execution_date DESC, status;

COMMENT ON VIEW v_agent_performance IS 'Agent execution performance metrics';

-- ============================================================================
-- SAMPLE DATA (for demonstration)
-- ============================================================================

-- Insert sample document
INSERT INTO documents (title, content, document_type, metadata)
VALUES (
    'Getting Started with AI Vector Search',
    'Oracle 23c introduces native AI Vector Search capabilities. This feature allows you to store and search high-dimensional vectors efficiently. Vector embeddings are numerical representations of data that capture semantic meaning. Common use cases include semantic search, recommendation systems, and retrieval augmented generation (RAG).',
    'TEXT',
    JSON_OBJECT(
        'author' VALUE 'Oracle Corporation',
        'category' VALUE 'Documentation',
        'tags' VALUE JSON_ARRAY('AI', 'Vectors', 'Search')
    )
);

-- ============================================================================
-- HELPER PROCEDURES
-- ============================================================================

-- Procedure to clean up old data
CREATE OR REPLACE PROCEDURE cleanup_old_data(days_to_keep NUMBER DEFAULT 90)
IS
    v_deleted_count NUMBER;
BEGIN
    -- Delete old conversations
    DELETE FROM conversations WHERE created_at < SYSDATE - days_to_keep;
    v_deleted_count := SQL%ROWCOUNT;
    DBMS_OUTPUT.PUT_LINE('Deleted ' || v_deleted_count || ' old conversations');

    -- Delete old agent executions
    DELETE FROM agent_executions WHERE started_at < SYSDATE - days_to_keep;
    v_deleted_count := SQL%ROWCOUNT;
    DBMS_OUTPUT.PUT_LINE('Deleted ' || v_deleted_count || ' old agent executions');

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Cleanup completed successfully');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error during cleanup: ' || SQLERRM);
        RAISE;
END;
/

-- ============================================================================
-- COMPLETION MESSAGE
-- ============================================================================

BEGIN
  DBMS_OUTPUT.PUT_LINE('===========================================');
  DBMS_OUTPUT.PUT_LINE('AI Vector Schema Created Successfully');
  DBMS_OUTPUT.PUT_LINE('===========================================');
  DBMS_OUTPUT.PUT_LINE('Tables created:');
  DBMS_OUTPUT.PUT_LINE('  1. documents (original documents)');
  DBMS_OUTPUT.PUT_LINE('  2. document_chunks (with vector embeddings)');
  DBMS_OUTPUT.PUT_LINE('  3. conversations (chat history)');
  DBMS_OUTPUT.PUT_LINE('  4. messages (individual messages)');
  DBMS_OUTPUT.PUT_LINE('  5. agent_executions (agent tracking)');
  DBMS_OUTPUT.PUT_LINE('  6. tool_calls (tool usage logs)');
  DBMS_OUTPUT.PUT_LINE('  7. camunda_tasks (workflow integration)');
  DBMS_OUTPUT.PUT_LINE('');
  DBMS_OUTPUT.PUT_LINE('Vector Index: idx_chunks_vector (HNSW, COSINE)');
  DBMS_OUTPUT.PUT_LINE('Sample data inserted');
  DBMS_OUTPUT.PUT_LINE('===========================================');
END;
/

EXIT;
