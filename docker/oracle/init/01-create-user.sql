-- ============================================================================
-- Oracle 23c AI Vector Search - User Creation Script
-- ============================================================================
-- This script creates the application user and grants necessary privileges
-- Runs automatically on first container startup
-- ============================================================================

-- Connect to pluggable database
ALTER SESSION SET CONTAINER = FREEPDB1;

-- Create application user
CREATE USER ai_user IDENTIFIED BY ai_password_2024
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant basic privileges
GRANT CONNECT, RESOURCE TO ai_user;
GRANT CREATE SESSION TO ai_user;
GRANT CREATE TABLE TO ai_user;
GRANT CREATE VIEW TO ai_user;
GRANT CREATE SEQUENCE TO ai_user;
GRANT CREATE PROCEDURE TO ai_user;

-- Grant privileges for AI Vector Search
GRANT EXECUTE ON DBMS_VECTOR TO ai_user;
GRANT EXECUTE ON DBMS_VECTOR_CHAIN TO ai_user;

-- Grant advanced privileges for development
GRANT CREATE ANY DIRECTORY TO ai_user;
GRANT SELECT_CATALOG_ROLE TO ai_user;

-- Verify user creation
SELECT username, account_status, default_tablespace, temporary_tablespace
FROM dba_users
WHERE username = 'AI_USER';

-- Show granted roles
SELECT grantee, granted_role
FROM dba_role_privs
WHERE grantee = 'AI_USER';

-- Show system privileges
SELECT grantee, privilege
FROM dba_sys_privs
WHERE grantee = 'AI_USER'
ORDER BY privilege;

COMMIT;

-- Log completion
BEGIN
  DBMS_OUTPUT.PUT_LINE('===========================================');
  DBMS_OUTPUT.PUT_LINE('User AI_USER created successfully');
  DBMS_OUTPUT.PUT_LINE('Password: ai_password_2024');
  DBMS_OUTPUT.PUT_LINE('===========================================');
END;
/

EXIT;
