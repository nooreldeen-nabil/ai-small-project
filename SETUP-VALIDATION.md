# Environment Validation Checklist

## Prerequisites Validation

Please run these commands in your **Windows PowerShell** or **Command Prompt** and share the results:

### 1. Java Development Kit (JDK 17+)
```bash
java -version
javac -version
```
**Expected:** Java 17 or higher (Java 21 recommended)

---

### 2. Maven or Gradle
```bash
mvn -version
```
OR
```bash
gradle -version
```
**Expected:** Maven 3.8+ or Gradle 8+

---

### 3. Docker Desktop
```bash
docker --version
docker compose version
```
**Expected:** Docker 20+ and Docker Compose V2

---

### 4. Docker Containers Status
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```
**Expected:** Should show Camunda 8.7 containers running (web-modeler, tasklist, operate, connectors, optimize)

---

### 5. Camunda Access Validation
Open in browser:
- **Operate:** http://localhost:8081
- **Tasklist:** http://localhost:8082
- **Optimize:** http://localhost:8083
- **Web Modeler:** http://localhost:8070
- **Zeebe Gateway:** localhost:26500 (gRPC)

**Expected:** All URLs should be accessible

---

### 6. Oracle Database 23c Setup

#### Option A: Using Docker (Recommended for MVP)
Check if Oracle 23c is running:
```bash
docker ps | findstr oracle
```

If NOT running, we'll create a docker-compose for Oracle 23c AI.

#### Option B: Using Existing Oracle 19c
If you want to use existing Oracle 19c (won't have native vector support):
- We'll implement custom vector similarity functions
- Less optimal but still demonstrates concepts

**Which option do you prefer?**

---

### 7. LLM API Keys

#### Primary (FREE): Google Gemini API Key
**Recommended for learning and MVP development**
- Go to: https://aistudio.google.com/app/apikey
- Click "Create API Key"
- Select "Create API key in new project"
- Keep it ready (we'll use it in `.env` file)

**Format:** `AIzaSy...`
**FREE Tier:** 60 requests/min, 1500 requests/day

#### Optional (PAID): Anthropic Claude API Key
Only needed if you want to use Claude instead of Gemini
- Go to: https://console.anthropic.com/settings/keys
- Create a new API key if needed
- Keep it ready (optional)

**Format:** `sk-ant-api03-...`

---

### 8. IntelliJ IDEA Setup
- **Version:** IntelliJ IDEA 2023+ (Community or Ultimate)
- **Plugins needed:**
  - Lombok Plugin
  - Spring Boot Plugin (usually pre-installed)
  - Docker Plugin (optional, for convenience)

---

### 9. Network Ports Availability
Check these ports are free (or used by expected services):
```bash
netstat -an | findstr "8080 8081 8082 8083 8070 26500 1521"
```

**Expected ports:**
- 8080: Spring Boot (our app)
- 8081: Camunda Operate
- 8082: Camunda Tasklist
- 8083: Camunda Optimize
- 8070: Camunda Web Modeler
- 26500: Zeebe Gateway
- 1521: Oracle Database (if running)

---

### 10. Git Configuration
```bash
git --version
git config --get user.name
git config --get user.email
```

---

## Validation Results

Please copy the output of all commands above and share with me so I can:
1. Identify any missing components
2. Provide specific setup instructions if needed
3. Proceed with the project plan

---

## What to Do Next?

1. ✅ Run all validation commands above
2. ✅ Share the results with me
3. ✅ I'll review and help fix any issues
4. ✅ Then we'll proceed with the phased project plan
