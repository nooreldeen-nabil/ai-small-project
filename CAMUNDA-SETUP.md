# 🚀 Camunda Platform 8 Setup Guide

**Created:** 2026-01-15
**For:** Phase 6 - Agentic AI with Camunda Workflows
**Version:** Camunda 8.7.x (Zeebe-based)

---

## 📋 Overview

This docker-compose setup provides a complete Camunda Platform 8 environment including:

- **Zeebe** - Workflow engine (BPMN execution)
- **Operate** - Monitoring and operations UI
- **Tasklist** - Human task management UI
- **Optimize** - Analytics and reporting
- **Identity** - User and access management
- **Keycloak** - Authentication provider
- **Elasticsearch** - Data storage
- **Web Modeler** - Visual BPMN designer
- **Connectors** - Integration framework
- **Mailpit** - Email testing (for notifications)

---

## 🔧 What Was Changed

This setup is based on your original Camunda docker-compose with the following modifications:

### ✅ Removed
- **Custom Oracle Database Exporter** configuration
- All `ZEEBE_BROKER_EXPORTERS_DUMYEXPORTER_*` environment variables
- Volume mount for `/usr/local/zeebe/exporters`

### ✅ Kept
- All other services (Zeebe, Operate, Tasklist, Optimize, Identity, Keycloak, etc.)
- Elasticsearch exporter (default)
- All UI components
- Web Modeler
- Connectors framework
- Authentication configuration

### ✅ Added
- Separate `.env.camunda` file for Camunda-specific configuration
- `.web-modeler/` directory for Web Modeler config
- `.optimize/` directory for Optimize config
- `connector-secrets.txt` for connector credentials
- This setup guide (CAMUNDA-SETUP.md)

---

## 📁 File Structure

```
ai-small-project/
├── docker-compose-camunda.yml         ⭐ NEW - Camunda services
├── .env.camunda                       ⭐ NEW - Camunda environment variables
├── .web-modeler/                      ⭐ NEW - Web Modeler configuration
│   └── cluster-config-authentication-mode-none.env
├── .optimize/                         ⭐ NEW - Optimize configuration
│   └── environment-config.yaml
├── connector-secrets.txt              ⭐ NEW - Connector secrets (optional)
├── CAMUNDA-SETUP.md                   ⭐ NEW - This file
│
├── docker-compose.yml                 (Existing - Oracle 23c for AI app)
└── .env                               (Existing - AI app environment vars)
```

**Note:** You now have **two separate docker-compose files**:
1. `docker-compose.yml` - Your AI application with Oracle 23c
2. `docker-compose-camunda.yml` - Camunda Platform 8 services

---

## 🚀 Quick Start

### 1. Start Camunda Platform

```bash
# Start all Camunda services
docker-compose -f docker-compose-camunda.yml up -d

# Check service status
docker-compose -f docker-compose-camunda.yml ps

# View logs (all services)
docker-compose -f docker-compose-camunda.yml logs -f

# View logs (specific service)
docker-compose -f docker-compose-camunda.yml logs -f zeebe
```

### 2. Wait for Services to be Healthy

Camunda has many services that need to start in order. Wait 2-3 minutes for all services to be healthy.

```bash
# Check health status
docker-compose -f docker-compose-camunda.yml ps

# All services should show "Up" and "healthy" status
```

### 3. Access Camunda UIs

Once all services are healthy, access the UIs:

| Service | URL | Credentials |
|---------|-----|-------------|
| **Web Modeler** | http://localhost:8070 | demo / demo |
| **Operate** (Monitoring) | http://localhost:8081 | demo / demo |
| **Tasklist** (Human Tasks) | http://localhost:8082 | demo / demo |
| **Optimize** (Analytics) | http://localhost:8083 | demo / demo |
| **Identity** (User Mgmt) | http://localhost:8084 | demo / demo |
| **Keycloak** (Auth) | http://localhost:18080/auth | admin / admin |
| **Mailpit** (Email) | http://localhost:8075 | - |
| **Elasticsearch** | http://localhost:9200 | - |

**Zeebe Endpoints:**
- **gRPC API:** localhost:26500
- **REST API:** http://localhost:8088
- **Metrics:** http://localhost:9600/actuator/metrics

### 4. Verify Installation

```bash
# Check Zeebe status
curl http://localhost:9600/actuator/health

# Check Zeebe topology
curl http://localhost:8088/actuator/cluster

# Check Operate health
curl http://localhost:8081/actuator/health

# Check Elasticsearch
curl http://localhost:9200/_cluster/health
```

---

## 🎯 What Each Service Does

### Core Services

**Zeebe** (Port 26500, 8088, 9600)
- The workflow engine that executes BPMN processes
- Handles process instances, jobs, and state
- Your AI application will connect to Zeebe via gRPC or REST

**Operate** (Port 8081)
- Visual monitoring of running workflows
- Inspect process instances, variables, incidents
- Debug workflows in real-time

**Tasklist** (Port 8082)
- Manage human tasks assigned in workflows
- Complete forms, approve/reject requests
- User inbox for workflow tasks

**Optimize** (Port 8083)
- Analytics and reporting on workflow performance
- Process duration, bottleneck analysis
- Business metrics and KPIs

### Supporting Services

**Identity** (Port 8084)
- User and permission management
- Integrates with Keycloak for auth

**Keycloak** (Port 18080)
- OAuth2/OIDC authentication provider
- Manages users, roles, clients

**Elasticsearch** (Port 9200)
- Primary data store for Operate, Tasklist, Optimize
- Stores workflow history and state

**Web Modeler** (Port 8070)
- Visual BPMN designer (browser-based)
- Create, edit, and deploy workflows
- Alternative to Camunda Modeler desktop app

**Connectors** (Port 8085)
- Pre-built integrations (REST, Kafka, AWS, etc.)
- Extend workflows with external services

**Mailpit** (Port 8075)
- Email testing tool
- Catch outgoing emails from workflows
- No emails actually sent (for dev/testing)

---

## 🔐 Authentication Configuration

### Current Setup: `ZEEBE_AUTHENTICATION_MODE=none`

This means:
- ✅ **No authentication required** to connect to Zeebe
- ✅ **Easier for development** - No need for tokens
- ❌ **Not production-ready** - Anyone can access Zeebe API

### For Production: `ZEEBE_AUTHENTICATION_MODE=identity`

To enable authentication:

1. Edit `.env.camunda`:
   ```env
   ZEEBE_AUTHENTICATION_MODE=identity
   ```

2. Create `.web-modeler/cluster-config-authentication-mode-identity.env`:
   ```env
   CAMUNDA_MODELER_CLUSTERS_0_URL_AUTH_OAUTH_CLIENT_ID=${ZEEBE_CLIENT_ID}
   CAMUNDA_MODELER_CLUSTERS_0_URL_AUTH_OAUTH_CLIENT_SECRET=${ZEEBE_CLIENT_SECRET}
   CAMUNDA_MODELER_CLUSTERS_0_URL_AUTH_OAUTH_TOKEN_URL=http://keycloak:18080/auth/realms/camunda-platform/protocol/openid-connect/token
   ```

3. Your Java app will need to authenticate:
   ```java
   ZeebeClient client = ZeebeClient.newClientBuilder()
       .gatewayAddress("localhost:26500")
       .credentialsProvider(
           new OAuthCredentialsProviderBuilder()
               .clientId("zeebe")
               .clientSecret("zecret")
               .audience("zeebe-api")
               .authorizationServerUrl("http://localhost:18080/auth/realms/camunda-platform/protocol/openid-connect/token")
               .build()
       )
       .build();
   ```

---

## 🛠️ Common Operations

### Start/Stop Services

```bash
# Start all services
docker-compose -f docker-compose-camunda.yml up -d

# Stop all services
docker-compose -f docker-compose-camunda.yml down

# Stop and remove volumes (CAUTION: Deletes all data!)
docker-compose -f docker-compose-camunda.yml down -v

# Restart a specific service
docker-compose -f docker-compose-camunda.yml restart zeebe
```

### View Logs

```bash
# All services
docker-compose -f docker-compose-camunda.yml logs -f

# Specific service
docker-compose -f docker-compose-camunda.yml logs -f zeebe
docker-compose -f docker-compose-camunda.yml logs -f operate

# Last 100 lines
docker-compose -f docker-compose-camunda.yml logs --tail=100 zeebe
```

### Check Resource Usage

```bash
# Show container stats
docker stats

# Show disk usage
docker system df

# Show specific container stats
docker stats zeebe operate tasklist
```

### Cleanup

```bash
# Remove stopped containers
docker-compose -f docker-compose-camunda.yml rm

# Remove unused volumes
docker volume prune

# Remove unused images
docker image prune
```

---

## 🐛 Troubleshooting

### Services Won't Start

**Issue:** Services stuck in "starting" state

**Solutions:**
```bash
# 1. Check logs for errors
docker-compose -f docker-compose-camunda.yml logs zeebe

# 2. Check disk space (Camunda needs ~5GB)
df -h

# 3. Increase Docker memory (needs 8GB+ RAM)
# Docker Desktop → Settings → Resources → Memory → 8GB

# 4. Start services one by one
docker-compose -f docker-compose-camunda.yml up -d postgres elasticsearch
# Wait 30s
docker-compose -f docker-compose-camunda.yml up -d keycloak identity
# Wait 30s
docker-compose -f docker-compose-camunda.yml up -d zeebe
```

### Elasticsearch Yellow/Red Health

**Issue:** Elasticsearch shows yellow or red health

**Solutions:**
```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health

# For development, yellow is OK (single node)
# Red means data loss - check logs
docker-compose -f docker-compose-camunda.yml logs elasticsearch

# Increase memory if needed
# Edit docker-compose-camunda.yml:
# ES_JAVA_OPTS: "-Xms1g -Xmx1g"
```

### Zeebe Not Accepting Connections

**Issue:** `io.grpc.StatusRuntimeException: UNAVAILABLE`

**Solutions:**
```bash
# 1. Check Zeebe is running
docker-compose -f docker-compose-camunda.yml ps zeebe

# 2. Check Zeebe logs
docker-compose -f docker-compose-camunda.yml logs zeebe

# 3. Check Zeebe health
curl http://localhost:9600/actuator/health

# 4. Check Zeebe is ready
curl http://localhost:8088/ready

# 5. Wait longer (first startup takes 60-90s)
```

### Cannot Access Web UIs

**Issue:** http://localhost:8081 not loading

**Solutions:**
```bash
# 1. Check service is running
docker-compose -f docker-compose-camunda.yml ps operate

# 2. Check service health
docker-compose -f docker-compose-camunda.yml ps | grep healthy

# 3. Check port is not in use
netstat -an | grep 8081

# 4. Try http://127.0.0.1:8081 instead

# 5. Check firewall settings
```

### Out of Memory Errors

**Issue:** `java.lang.OutOfMemoryError`

**Solutions:**
```bash
# 1. Increase Docker memory to 8GB+
# Docker Desktop → Settings → Resources

# 2. Reduce service memory limits in docker-compose-camunda.yml:
# JAVA_TOOL_OPTIONS: "-Xms256m -Xmx256m"

# 3. Run fewer services (remove Optimize, Web Modeler)
docker-compose -f docker-compose-camunda.yml up -d zeebe operate tasklist elasticsearch keycloak postgres identity
```

---

## 📊 Monitoring

### Health Endpoints

```bash
# Zeebe
curl http://localhost:9600/actuator/health
curl http://localhost:9600/actuator/metrics

# Operate
curl http://localhost:8081/actuator/health

# Tasklist
curl http://localhost:8082/actuator/health

# Optimize
curl http://localhost:8083/api/readyz

# Elasticsearch
curl http://localhost:9200/_cluster/health
```

### Metrics

```bash
# Zeebe Prometheus metrics
curl http://localhost:9600/actuator/prometheus

# Zeebe execution metrics
curl http://localhost:9600/actuator/metrics/zeebe.process.instances.running

# Elasticsearch stats
curl http://localhost:9200/_stats
```

---

## 🔗 Integration with Your AI Application

Your AI application (running on `docker-compose.yml`) will connect to Camunda:

```
┌─────────────────────────────────────┐
│  AI Application (docker-compose.yml)│
│  - Spring Boot App (port 8080)     │
│  - Oracle 23c (port 1521)          │
│  - LLM Integration (Gemini)        │
│  - Phase 5 Agent Service           │
└──────────────┬──────────────────────┘
               │
               │ Zeebe Java Client
               │ (gRPC: localhost:26500)
               ↓
┌─────────────────────────────────────┐
│  Camunda (docker-compose-camunda.yml)│
│  - Zeebe (port 26500)              │
│  - Operate (port 8081)             │
│  - Tasklist (port 8082)            │
│  - Elasticsearch (port 9200)       │
└─────────────────────────────────────┘
```

### Connection Pattern

1. **Your Spring Boot app** runs outside Docker (or in docker-compose.yml)
2. **Zeebe Client** connects to `localhost:26500`
3. **External Task Workers** poll Zeebe for jobs
4. **Workers call AgentService** (from Phase 5)
5. **Workers complete jobs** in Zeebe
6. **Monitor in Operate** (http://localhost:8081)

---

## 📚 Next Steps

Now that Camunda is running:

1. **Test the UIs** - Login to Web Modeler, Operate, Tasklist
2. **Add Zeebe Client dependency** to your `pom.xml`
3. **Create first BPMN process** in Web Modeler
4. **Deploy process** to Zeebe
5. **Create External Task Worker** in your AI application
6. **Start process instance** via API
7. **Monitor execution** in Operate

See the main Phase 6 guide for detailed implementation steps.

---

## 📞 Quick Reference

**Stop all Camunda services:**
```bash
docker-compose -f docker-compose-camunda.yml down
```

**Start all Camunda services:**
```bash
docker-compose -f docker-compose-camunda.yml up -d
```

**View all logs:**
```bash
docker-compose -f docker-compose-camunda.yml logs -f
```

**Reset everything (DELETE ALL DATA!):**
```bash
docker-compose -f docker-compose-camunda.yml down -v
docker-compose -f docker-compose-camunda.yml up -d
```

**Default credentials:**
- Username: `demo`
- Password: `demo`

**Zeebe connection:**
- gRPC: `localhost:26500`
- REST: `http://localhost:8088`

---

## ⚠️ Important Notes

1. **Disk Space:** Camunda needs ~5GB disk space
2. **Memory:** Needs 8GB+ RAM (set in Docker Desktop)
3. **Startup Time:** First start takes 2-3 minutes
4. **Port Conflicts:** Ensure ports 8070-8088, 9200, 9600, 18080, 26500 are free
5. **Data Persistence:** Volumes persist data between restarts
6. **Network:** All services on `camunda-platform` network
7. **Development Only:** This setup is NOT production-ready

---

**END OF CAMUNDA-SETUP.MD**

*Created: 2026-01-15*
*For: Phase 6 - Camunda Workflows Integration*
