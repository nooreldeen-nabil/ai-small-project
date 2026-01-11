# Setup Instructions - LLM Agentic AI MVP

## 🎯 Quick Start Guide

Follow these steps on your **Windows machine** to get the environment ready.

---

## Step 1: Fix Git Email Configuration

Open PowerShell or Command Prompt and run:

```bash
git config --global user.email "nooreldeen.nabil@gmail.com"
```

Verify the change:
```bash
git config --get user.email
```

Expected output: `nooreldeen.nabil@gmail.com`

---

## Step 2: Clone the Project

```bash
# Navigate to your workspace directory
cd C:\Users\nooreldeen.nabil\workspace  # or your preferred location

# Clone the repository
git clone <your-repo-url> ai-small-project
cd ai-small-project

# Checkout the feature branch
git checkout claude/llm-agentic-ai-mvp-bRhG6

# Verify you're on the correct branch
git branch
```

---

## Step 3: Start Oracle 23c AI Database

**IMPORTANT:** Oracle 23c Free is ~4GB. First startup will take 2-5 minutes to download and initialize.

```bash
# In the project directory
docker-compose up -d oracle-23c

# Watch the logs (wait for "DATABASE IS READY TO USE")
docker-compose logs -f oracle-23c
```

**What to look for in logs:**
- `DATABASE IS READY TO USE!` - Oracle is ready
- `DONE: Executing user defined scripts` - Initialization complete
- Container status changes to `healthy`

**Verify Oracle is running:**
```bash
docker-compose ps
```

Expected output:
```
NAME            STATUS                    PORTS
oracle-23c-ai   Up X minutes (healthy)    0.0.0.0:1521->1521/tcp, 0.0.0.0:5500->5500/tcp
```

---

## Step 4: Verify Database Connection

### Option A: Using SQL*Plus (if installed)
```bash
sqlplus ai_user/ai_password_2024@//localhost:1521/FREEPDB1

-- Run a test query
SELECT table_name FROM user_tables;

-- Exit
EXIT;
```

### Option B: Using IntelliJ Database Tool

1. Open IntelliJ IDEA
2. View → Tool Windows → Database
3. Click `+` → Data Source → Oracle
4. Configure:
   - **Host:** localhost
   - **Port:** 1521
   - **Service:** FREEPDB1
   - **User:** ai_user
   - **Password:** ai_password_2024
5. Click "Test Connection"
6. Click "OK"

You should see these tables:
- `DOCUMENTS`
- `DOCUMENT_CHUNKS`
- `CONVERSATIONS`
- `MESSAGES`
- `AGENT_EXECUTIONS`
- `TOOL_CALLS`
- `CAMUNDA_TASKS`

---

## Step 5: Verify Camunda 8.7 is Running

Check all Camunda containers:
```bash
docker ps --format "table {{.Names}}\t{{.Status}}" | findstr camunda
```

Test Camunda UIs in browser:
- **Operate:** http://localhost:8081
- **Tasklist:** http://localhost:8082
- **Optimize:** http://localhost:8083
- **Web Modeler:** http://localhost:8070
- **Identity:** http://localhost:8084

**Default credentials:**
- Username: `demo`
- Password: `demo`

---

## Step 6: Open Project in IntelliJ IDEA

1. Open IntelliJ IDEA
2. File → Open
3. Navigate to `C:\Users\nooreldeen.nabil\workspace\ai-small-project`
4. Click "OK"
5. IntelliJ will detect it as a Maven project
6. Wait for Maven to download dependencies (~2-3 minutes)

---

## Step 7: Verify Environment Variables

Check that `.env` file exists in project root:
```bash
# In project directory
dir .env
```

The file should contain your Gemini API key (primary FREE provider) and all configurations.

---

## Step 8: Ready for Phase 1!

Your environment is now ready! You should have:

✅ Git configured with correct email
✅ Project cloned and on correct branch
✅ Oracle 23c AI running with vector tables
✅ Camunda 8.7 running (all services)
✅ Port 8080 free for Spring Boot
✅ IntelliJ opened with project loaded
✅ Environment variables configured

---

## Troubleshooting

### Oracle Container Won't Start

**Issue:** Port 1521 already in use

**Solution:** Your organization's Oracle connections are using port 1521. We need to use a different port.

Edit `docker-compose.yml` and change:
```yaml
ports:
  - "1522:1521"  # Use 1522 instead
```

Then update `.env`:
```
ORACLE_PORT=1522
```

Restart:
```bash
docker-compose down
docker-compose up -d
```

### Oracle Initialization Failed

**Check logs:**
```bash
docker-compose logs oracle-23c | findstr ERROR
```

**Reset and try again:**
```bash
docker-compose down -v  # CAUTION: Deletes all data
docker-compose up -d
```

### Camunda Not Accessible

**Restart Camunda:**
```bash
# In your Camunda directory (not this project)
docker-compose restart
```

### IntelliJ Not Recognizing Maven Project

1. Right-click on `pom.xml`
2. Maven → Reload Project
3. File → Invalidate Caches → Invalidate and Restart

---

## Port Reference

| Service | Port | URL |
|---------|------|-----|
| Spring Boot (Our App) | 8080 | http://localhost:8080 |
| Oracle Database | 1521 | jdbc:oracle:thin:@localhost:1521/FREEPDB1 |
| Oracle EM Express | 5500 | https://localhost:5500/em |
| Camunda Operate | 8081 | http://localhost:8081 |
| Camunda Tasklist | 8082 | http://localhost:8082 |
| Camunda Optimize | 8083 | http://localhost:8083 |
| Camunda Web Modeler | 8070 | http://localhost:8070 |
| Camunda Identity | 8084 | http://localhost:8084 |
| Zeebe Gateway (gRPC) | 26500 | localhost:26500 |

---

## Next Steps

Once all verification steps pass, notify me and we'll start **Phase 1: Foundation Setup + LLM Integration**!

---

## Useful Commands

```bash
# View all running containers
docker ps

# View Oracle logs
docker-compose logs -f oracle-23c

# Stop Oracle (keeps data)
docker-compose stop oracle-23c

# Start Oracle
docker-compose start oracle-23c

# Restart Oracle
docker-compose restart oracle-23c

# Stop Oracle and remove data (CAUTION)
docker-compose down -v

# Check Oracle health
docker inspect oracle-23c-ai | findstr Health

# Connect to Oracle container shell
docker exec -it oracle-23c-ai bash

# Inside container, connect to SQL*Plus
sqlplus ai_user/ai_password_2024@FREEPDB1
```

---

## Questions?

If you encounter any issues during setup:
1. Check the troubleshooting section above
2. Share the error message with me
3. I'll help you resolve it quickly!

Let's get started! 🚀
