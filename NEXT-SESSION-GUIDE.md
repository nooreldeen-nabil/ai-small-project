# 🚀 Next Session Guide - Phase 6: Camunda Workflows

**Date Created:** 2026-01-15
**Updated:** 2026-01-15 (Revised for Camunda 8.x)
**For Session:** Session 9 (Phase 6 Implementation)
**Current Status:** Phase 5 Complete ✅ (All 9/9 Tests Passing)
**Next Phase:** Phase 6 - Agentic AI with Camunda Workflows (BPMN)

---

## 📋 Quick Session Startup Checklist

When starting your next session, do these in order:

1. **Read this guide completely** ⬅️ You are here
2. **Read CLAUDE.md** - Session 8 notes and project status
3. **Read CAMUNDA-SETUP.md** - Camunda 8 setup guide
4. **Check git status** - Verify you're on the correct branch
5. **Review Phase 6 requirements** - See "Phase 6 Overview" below
6. **Start implementation** - Follow "Implementation Plan" section

---

## ✅ Phase 5 Status Summary

### What Was Completed

**Phase 5: Agentic AI - Tool Use (Function Calling)**
- ✅ 4 fully functional tools (search, date, calculate, database)
- ✅ Agent service with autonomous decision-making
- ✅ Multi-step reasoning (agent can chain tools)
- ✅ Complete execution logging
- ✅ Gemini function calling integration
- ✅ All 9/9 test cases passing
- ✅ Bug fixes completed (Java 21 compatibility)
- ✅ Documentation complete (PHASE-5-GUIDE.md)

### Branch Status

**Current Branch:** `claude/add-camunda-docker-compose-0FXHW`
- ✅ Camunda 8 docker-compose setup complete
- ✅ All configuration files added
- ✅ CAMUNDA-SETUP.md guide created
- ✅ Ready for Phase 6 implementation

---

## 🎯 Phase 6 Overview

### What is Phase 6?

**Phase 6: Agentic AI - Camunda Workflows (BPMN)**

While Phase 5 gives us tool use in a **single request**, Phase 6 adds:

1. **Long-running processes**
   - Tasks that span days/weeks/months
   - Persistent state across sessions
   - Process resumption after interruptions

2. **BPMN Workflows**
   - Visual process design (flowcharts)
   - Conditional branching (if/else)
   - Parallel execution (do multiple things at once)
   - Human-in-the-loop approvals

3. **Camunda Platform 8 Integration**
   - Zeebe workflow engine (orchestrates processes)
   - Operate UI (monitor running workflows)
   - Tasklist UI (manage human tasks)
   - Job workers (execute automated tasks)

### Example Workflow

**Document Processing Pipeline:**
```
Start
  ↓
Upload Document
  ↓
AI Classification → [Decision: Confident?]
  ├─ Yes → AI Extraction → Save → Notify → End
  │
  └─ No → Human Review (Wait for human)
            ↓
          [Human Decision: Approve/Reject]
            ↓
          Extract Data (AI)
            ↓
          Save to Database
            ↓
          Notify User
            ↓
          End
```

### Why Camunda 8?

- **Industry Standard** - Used by enterprises worldwide
- **BPMN 2.0** - Standard notation for business processes
- **Cloud-Native** - Microservices architecture, highly scalable
- **gRPC/REST API** - Control workflows programmatically
- **Operate & Tasklist UI** - Visual monitoring and task management
- **Zeebe** - High-performance workflow engine

---

## 📚 What You Need to Learn

### Core Concepts

1. **BPMN (Business Process Model and Notation)**
   - Standard for drawing business processes
   - Tasks, Gateways, Events, Sequence Flows
   - Industry-standard flowchart language

2. **Zeebe (Camunda 8 Workflow Engine)**
   - Cloud-native workflow engine
   - Executes BPMN processes
   - gRPC API for communication
   - Horizontal scalability

3. **Job Workers Pattern**
   - Zeebe delegates work to job workers
   - Workers poll for jobs, execute, and complete
   - Your AI agent becomes a job worker
   - Decoupled from workflow engine

4. **Process Variables**
   - Data that flows through the process
   - Example: documentId, confidence, classification
   - Shared across process steps
   - Serialized as JSON

5. **Service Tasks**
   - Automated tasks (no human needed)
   - Always asynchronous in Zeebe
   - Handled by job workers

6. **User Tasks**
   - Human tasks (require human interaction)
   - Appear in Tasklist UI
   - Can be completed via API or UI

### Technologies to Add

- **Camunda Platform 8.7.x** (Zeebe, Operate, Tasklist)
- **Zeebe Java Client** (Java library to connect to Zeebe)
- **Web Modeler** (browser-based BPMN designer)
- **Docker Compose** (run Camunda services)

---

## 🏗️ Implementation Plan (Camunda 8)

### Step 1: Start Camunda Platform (15-20 min)

**Goal:** Get Camunda 8 running with Docker

**Tasks:**
1. Review docker-compose-camunda.yml configuration
2. Start all Camunda services
3. Wait for services to be healthy (2-3 min)
4. Verify all UIs are accessible

**Commands:**
```bash
# Start Camunda Platform 8
docker-compose -f docker-compose-camunda.yml up -d

# Check status
docker-compose -f docker-compose-camunda.yml ps

# View logs
docker-compose -f docker-compose-camunda.yml logs -f zeebe
```

**Expected Result:**
- Zeebe running: http://localhost:8088 (REST), localhost:26500 (gRPC)
- Operate running: http://localhost:8081 (demo/demo)
- Tasklist running: http://localhost:8082 (demo/demo)
- Web Modeler running: http://localhost:8070 (demo/demo)

**Resources:**
- See CAMUNDA-SETUP.md for detailed instructions
- [Camunda 8 Docs](https://docs.camunda.io/)
- [Zeebe Documentation](https://docs.camunda.io/docs/components/zeebe/zeebe-overview/)

---

### Step 2: Add Zeebe Client to Your AI App (15-20 min)

**Goal:** Integrate Zeebe Java Client into Spring Boot application

**Tasks:**
1. Add Zeebe Spring Boot Starter dependency to `pom.xml`
2. Configure Zeebe connection in `application.yml`
3. Create ZeebeClientConfiguration class
4. Test connection to Zeebe

**Add to `pom.xml`:**
```xml
<!-- Zeebe Spring Boot Starter -->
<dependency>
    <groupId>io.camunda</groupId>
    <artifactId>spring-boot-starter-camunda</artifactId>
    <version>8.7.0</version>
</dependency>

<!-- Zeebe Client -->
<dependency>
    <groupId>io.camunda</groupId>
    <artifactId>zeebe-client-java</artifactId>
    <version>8.7.21</version>
</dependency>
```

**Add to `application.yml`:**
```yaml
zeebe:
  client:
    broker:
      gateway-address: localhost:26500
    security:
      plaintext: true
    cloud:
      region: ""
      cluster-id: ""
```

**Create Configuration:**
```java
// src/main/java/com/ai/mvp/config/ZeebeClientConfiguration.java
@Configuration
public class ZeebeClientConfiguration {

    @Bean
    public ZeebeClient zeebeClient() {
        return ZeebeClient.newClientBuilder()
            .gatewayAddress("localhost:26500")
            .usePlaintext()
            .build();
    }
}
```

**Expected Result:**
- ZeebeClient bean available in Spring context
- Application connects to Zeebe successfully
- No connection errors in logs

**Resources:**
- [Zeebe Spring Boot Starter](https://docs.camunda.io/docs/apis-tools/spring-zeebe-sdk/getting-started/)

---

### Step 3: Create Your First BPMN Process (20-30 min)

**Goal:** Design and deploy a simple workflow

**Tasks:**
1. Open Web Modeler (http://localhost:8070)
2. Login with demo/demo
3. Create new BPMN diagram
4. Design a simple process:
   ```
   Start Event → Service Task (ai-task) → End Event
   ```
5. Configure service task:
   - Name: "Call AI Agent"
   - Task type: "ai-task"
6. Save and deploy to Zeebe

**BPMN Configuration:**
```xml
<!-- Service Task Configuration in Web Modeler -->
Task Type: ai-task
Task Headers:
  - Name: taskType
    Value: ai-question
```

**Alternative: Use Desktop Modeler**
```bash
# Download Camunda Modeler
# https://camunda.com/download/modeler/

# Create process, save as: src/main/resources/processes/simple-ai-task.bpmn
```

**Deploy via API:**
```java
// DeploymentService.java
@Service
public class DeploymentService {

    @Autowired
    private ZeebeClient zeebeClient;

    public void deployProcess() {
        zeebeClient.newDeployResourceCommand()
            .addResourceFromClasspath("processes/simple-ai-task.bpmn")
            .send()
            .join();
    }
}
```

**Expected Result:**
- BPMN process created in Web Modeler
- Process deployed to Zeebe
- Visible in Operate UI (http://localhost:8081)

**Resources:**
- [BPMN Tutorial](https://camunda.com/bpmn/)
- [Web Modeler Guide](https://docs.camunda.io/docs/components/modeler/web-modeler/launch-web-modeler/)

---

### Step 4: Integrate AI Agent with Zeebe (45-60 min)

**Goal:** Create job worker that executes AI tasks

**Tasks:**
1. Create JobWorker service
2. Implement handler for "ai-task" jobs
3. Call AgentService (from Phase 5)
4. Complete job with result
5. Test workflow execution

**Create Job Worker:**
```java
// src/main/java/com/ai/mvp/workflow/worker/AiAgentWorker.java
package com.ai.mvp.workflow.worker;

import com.ai.mvp.service.AgentService;
import com.ai.mvp.dto.agent.AgentTaskRequest;
import com.ai.mvp.dto.agent.AgentTaskResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiAgentWorker {

    @Autowired
    private AgentService agentService;

    @JobWorker(type = "ai-task")
    public Map<String, Object> handleAiTask(
        final ActivatedJob job,
        @Variable(name = "question") String question
    ) {
        // Log job details
        System.out.println("Processing AI task job: " + job.getKey());
        System.out.println("Question: " + question);

        // Call AI agent (from Phase 5)
        AgentTaskRequest request = new AgentTaskRequest();
        request.setTask(question);
        request.setMaxToolCalls(10);
        request.setTemperature(0.7);

        AgentTaskResponse response = agentService.executeTask(request);

        // Return result as process variables
        return Map.of(
            "answer", response.getAnswer(),
            "tokensUsed", response.getTotalTokens(),
            "toolCallCount", response.getToolCallCount(),
            "success", response.isSuccess()
        );
    }
}
```

**Create WorkflowService (to start processes):**
```java
// src/main/java/com/ai/mvp/service/WorkflowService.java
package com.ai.mvp.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WorkflowService {

    @Autowired
    private ZeebeClient zeebeClient;

    public long startAiTaskProcess(String question) {
        ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
            .bpmnProcessId("simple-ai-task")  // Process ID from BPMN
            .latestVersion()
            .variables(Map.of("question", question))
            .send()
            .join();

        return event.getProcessInstanceKey();
    }

    public Map<String, Object> getProcessStatus(long processInstanceKey) {
        // Query Operate API or use Zeebe client
        // For now, return basic info
        return Map.of(
            "processInstanceKey", processInstanceKey,
            "status", "running"
        );
    }
}
```

**Create WorkflowController:**
```java
// src/main/java/com/ai/mvp/controller/WorkflowController.java
package com.ai.mvp.controller;

import com.ai.mvp.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/start")
    public Map<String, Object> startWorkflow(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        long processInstanceKey = workflowService.startAiTaskProcess(question);

        return Map.of(
            "processInstanceKey", processInstanceKey,
            "message", "Workflow started successfully"
        );
    }

    @GetMapping("/status/{processInstanceKey}")
    public Map<String, Object> getStatus(@PathVariable long processInstanceKey) {
        return workflowService.getProcessStatus(processInstanceKey);
    }
}
```

**Expected Result:**
- Job worker running and polling Zeebe
- POST /api/workflow/start starts process instance
- Worker picks up job, calls AI agent
- Job completes with AI response
- Process finishes successfully
- Can see execution in Operate UI

**Test:**
```bash
# Start a workflow
curl -X POST http://localhost:8080/api/workflow/start \
  -H "Content-Type: application/json" \
  -d '{"question": "What is the capital of France?"}'

# Response:
# {
#   "processInstanceKey": 2251799813685249,
#   "message": "Workflow started successfully"
# }

# Check Operate UI: http://localhost:8081
# Should see process instance running/completed
```

**Resources:**
- [Job Workers](https://docs.camunda.io/docs/components/concepts/job-workers/)
- [Zeebe Spring Client](https://docs.camunda.io/docs/apis-tools/spring-zeebe-sdk/getting-started/)

---

### Step 5: Build Document Processing Workflow (60-90 min)

**Goal:** Complete end-to-end AI workflow with human tasks

**Tasks:**
1. Design BPMN process in Web Modeler:
   ```
   Start → Upload Doc → AI Classification →
   [Gateway: confidence > 0.8?] →
     Yes → AI Extraction → Save → Notify → End
     No → Human Review (User Task) → AI Extraction → Save → Notify → End
   ```
2. Create job workers for each task
3. Implement user task handling
4. Test complete workflow

**BPMN Design:**

Create in Web Modeler:

1. **Start Event** - "Document Uploaded"
2. **Service Task** - "AI Classification" (type: classify-document)
3. **Exclusive Gateway** - "High Confidence?"
   - Condition: `=confidence > 0.8`
4. **Path 1 (Yes):**
   - Service Task: "AI Extract Data" (type: extract-data)
5. **Path 2 (No):**
   - User Task: "Human Review" (assignee: demo)
   - Service Task: "AI Extract Data" (type: extract-data)
6. **Paths Converge**
7. **Service Task** - "Save to Database" (type: save-document)
8. **Service Task** - "Notify User" (type: send-notification)
9. **End Event** - "Process Complete"

**Create Workers:**

```java
// ClassificationWorker.java
@Component
public class ClassificationWorker {

    @Autowired
    private AgentService agentService;

    @JobWorker(type = "classify-document")
    public Map<String, Object> classifyDocument(
        @Variable(name = "documentContent") String content
    ) {
        // Use AI agent to classify
        AgentTaskRequest request = new AgentTaskRequest();
        request.setTask("Classify this document: " + content);

        AgentTaskResponse response = agentService.executeTask(request);

        // Extract confidence from response
        double confidence = 0.85; // Simplified for example

        return Map.of(
            "classification", response.getAnswer(),
            "confidence", confidence
        );
    }
}

// DataExtractionWorker.java
@Component
public class DataExtractionWorker {

    @Autowired
    private AgentService agentService;

    @JobWorker(type = "extract-data")
    public Map<String, Object> extractData(
        @Variable(name = "documentContent") String content
    ) {
        AgentTaskRequest request = new AgentTaskRequest();
        request.setTask("Extract key data from: " + content);

        AgentTaskResponse response = agentService.executeTask(request);

        return Map.of(
            "extractedData", response.getAnswer()
        );
    }
}

// SaveDocumentWorker.java
@Component
public class SaveDocumentWorker {

    @Autowired
    private DocumentService documentService;

    @JobWorker(type = "save-document")
    public Map<String, Object> saveDocument(
        @Variable(name = "extractedData") String data,
        @Variable(name = "classification") String classification
    ) {
        // Save to database (using Phase 3 DocumentService)
        // Simplified example
        System.out.println("Saving document: " + classification);

        return Map.of(
            "documentId", 12345L,
            "saved", true
        );
    }
}

// NotificationWorker.java
@Component
public class NotificationWorker {

    @JobWorker(type = "send-notification")
    public Map<String, Object> sendNotification(
        @Variable(name = "documentId") Long documentId
    ) {
        System.out.println("Notification sent for document: " + documentId);

        return Map.of(
            "notificationSent", true
        );
    }
}
```

**Handle User Tasks:**

```java
// UserTaskService.java
@Service
public class UserTaskService {

    @Autowired
    private ZeebeClient zeebeClient;

    public void completeUserTask(String jobKey, Map<String, Object> variables) {
        // Note: In Camunda 8, user tasks are completed via Tasklist API or Zeebe client
        // This is a simplified example

        zeebeClient.newCompleteCommand(Long.parseLong(jobKey))
            .variables(variables)
            .send()
            .join();
    }
}
```

**Start Document Workflow:**

```java
public long startDocumentProcessing(String documentContent, String title) {
    ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand()
        .bpmnProcessId("document-processing")
        .latestVersion()
        .variables(Map.of(
            "documentContent", documentContent,
            "title", title
        ))
        .send()
        .join();

    return event.getProcessInstanceKey();
}
```

**Expected Result:**
- Complete BPMN workflow deployed
- High confidence documents processed automatically
- Low confidence documents wait for human review
- Human can review in Tasklist UI (http://localhost:8082)
- After approval, AI extracts data
- Document saved to database
- Notification sent
- Process completes

**Test:**
```bash
# Test high confidence path
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Invoice",
    "content": "Invoice #12345 for $500"
  }'

# Test low confidence path (requires human review)
curl -X POST http://localhost:8080/api/workflow/start-document \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Unknown",
    "content": "Unclear document text..."
  }'

# Check Tasklist UI for user task: http://localhost:8082
# Complete task via UI or API
```

---

### Step 6: Add Monitoring & Testing (30-45 min)

**Goal:** Validate and monitor workflows

**Tasks:**
1. Enhance REST API for workflow operations
2. Add error handling in workers
3. Test error scenarios
4. Monitor in Operate

**Enhanced API:**

```java
@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startWorkflow(
        @RequestBody Map<String, String> request
    ) {
        try {
            String question = request.get("question");
            long processInstanceKey = workflowService.startAiTaskProcess(question);

            return ResponseEntity.ok(Map.of(
                "processInstanceKey", processInstanceKey,
                "message", "Workflow started successfully",
                "operateUrl", "http://localhost:8081/processes/" + processInstanceKey
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/processes")
    public ResponseEntity<Map<String, Object>> listProcesses() {
        // This would query Operate API
        // For now, return placeholder
        return ResponseEntity.ok(Map.of(
            "message", "Use Operate UI: http://localhost:8081"
        ));
    }

    @PostMapping("/cancel/{processInstanceKey}")
    public ResponseEntity<Map<String, Object>> cancelProcess(
        @PathVariable long processInstanceKey
    ) {
        workflowService.cancelProcess(processInstanceKey);
        return ResponseEntity.ok(Map.of(
            "message", "Process cancelled"
        ));
    }
}
```

**Error Handling in Workers:**

```java
@JobWorker(type = "ai-task")
public Map<String, Object> handleAiTask(
    final ActivatedJob job,
    @Variable(name = "question") String question
) {
    try {
        AgentTaskRequest request = new AgentTaskRequest();
        request.setTask(question);

        AgentTaskResponse response = agentService.executeTask(request);

        if (!response.isSuccess()) {
            throw new RuntimeException("AI agent failed: " + response.getAnswer());
        }

        return Map.of(
            "answer", response.getAnswer(),
            "success", true
        );
    } catch (Exception e) {
        // Log error
        System.err.println("Error in AI task: " + e.getMessage());

        // Throw exception to trigger Zeebe incident
        throw new RuntimeException("AI task failed", e);
    }
}
```

**Test Error Scenarios:**

1. **Network Error** - Stop AI service, start workflow
2. **Invalid Input** - Send malformed data
3. **Timeout** - Simulate long-running task
4. **Job Failure** - Throw exception in worker

**Expected Result:**
- Errors visible in Operate as "Incidents"
- Can retry failed jobs in Operate UI
- Error messages logged
- Process state preserved

---

### Step 7: Documentation (30-45 min)

**Goal:** Document Phase 6

**Tasks:**
1. Create PHASE-6-GUIDE.md
2. Include:
   - Architecture overview (Zeebe + Spring Boot)
   - BPMN diagrams (screenshots from Web Modeler)
   - API examples (curl commands)
   - Worker implementation guide
   - Testing guide (9 test cases)
   - Troubleshooting section
3. Update CLAUDE.md with Session 9 notes

**PHASE-6-GUIDE.md Structure:**
```markdown
# Phase 6: Camunda Workflows Integration

## Architecture
- Diagram showing Zeebe + AI App + Operate/Tasklist

## Setup
- Docker compose commands
- Zeebe client configuration

## BPMN Processes
- Simple AI task process
- Document processing workflow
- Screenshots from Web Modeler

## Job Workers
- AiAgentWorker implementation
- ClassificationWorker implementation
- Pattern for creating new workers

## API Reference
- POST /api/workflow/start
- GET /api/workflow/status/{key}
- POST /api/workflow/cancel/{key}

## Testing
- Test 1: Simple AI task
- Test 2: Document classification (high confidence)
- Test 3: Document classification (low confidence, human review)
- Test 4: Error handling (job failure)
- Test 5: Process cancellation
- Test 6: Parallel execution
- Test 7: Long-running process
- Test 8: Monitor in Operate
- Test 9: Complete user task in Tasklist

## Troubleshooting
- Worker not picking up jobs
- Process stuck
- Incidents in Operate
```

**Expected Result:**
- Complete Phase 6 documentation
- Ready for testing and handoff

---

## 📁 File Structure for Phase 6

```
ai-small-project/
├── src/main/java/com/ai/mvp/
│   ├── workflow/                          ⭐ NEW
│   │   └── worker/                        ⭐ NEW
│   │       ├── AiAgentWorker.java        ⭐ NEW
│   │       ├── ClassificationWorker.java ⭐ NEW
│   │       ├── DataExtractionWorker.java ⭐ NEW
│   │       ├── SaveDocumentWorker.java   ⭐ NEW
│   │       └── NotificationWorker.java   ⭐ NEW
│   ├── controller/
│   │   └── WorkflowController.java        ⭐ NEW
│   ├── service/
│   │   ├── AgentService.java              (Exists from Phase 5)
│   │   ├── WorkflowService.java           ⭐ NEW
│   │   └── UserTaskService.java           ⭐ NEW
│   ├── config/
│   │   └── ZeebeClientConfiguration.java  ⭐ NEW
│   └── dto/
│       └── workflow/                       ⭐ NEW (Optional)
│           ├── StartProcessRequest.java
│           └── ProcessStatusResponse.java
│
├── src/main/resources/
│   ├── processes/                          ⭐ NEW (Optional if using Web Modeler)
│   │   ├── simple-ai-task.bpmn           ⭐ NEW
│   │   └── document-processing.bpmn       ⭐ NEW
│   └── application.yml                     (Update with Zeebe config)
│
├── docker-compose-camunda.yml              (Existing)
├── .env                                    (Existing)
├── pom.xml                                 (Update with Zeebe dependencies)
├── CAMUNDA-SETUP.md                        (Existing)
├── PHASE-6-GUIDE.md                        ⭐ NEW
└── CLAUDE.md                               (Update with Session 9 notes)
```

---

## 🔑 Key Dependencies to Add

Add to `pom.xml`:

```xml
<!-- Zeebe Spring Boot Starter -->
<dependency>
    <groupId>io.camunda</groupId>
    <artifactId>spring-boot-starter-camunda</artifactId>
    <version>8.7.0</version>
</dependency>

<!-- Zeebe Client -->
<dependency>
    <groupId>io.camunda</groupId>
    <artifactId>zeebe-client-java</artifactId>
    <version>8.7.21</version>
</dependency>
```

---

## ⚙️ Configuration Example

Add to `application.yml`:

```yaml
# Zeebe Configuration
zeebe:
  client:
    broker:
      gateway-address: localhost:26500
    security:
      plaintext: true
    worker:
      max-jobs-active: 32
      threads: 2
```

---

## 🧪 Testing Strategy

### Test 1: Simple AI Task Workflow
- Start process with question
- Worker picks up job
- AI agent responds
- Process completes
- Verify in Operate

### Test 2: Document Classification (High Confidence)
- Upload document with clear classification
- AI classifies with >0.8 confidence
- Automatically extracts data
- Saves to database
- Sends notification
- Process completes without human intervention

### Test 3: Document Classification (Low Confidence)
- Upload ambiguous document
- AI classifies with <0.8 confidence
- Process waits at user task
- Human reviews in Tasklist (http://localhost:8082)
- Human approves
- AI extracts data
- Process continues

### Test 4: Job Failure & Retry
- Simulate worker failure (throw exception)
- Incident created in Zeebe
- View incident in Operate
- Retry incident
- Job completes successfully

### Test 5: Process Cancellation
- Start long-running process
- Cancel via API
- Verify cancellation in Operate
- Process terminates

### Test 6: Parallel Execution
- Start multiple process instances
- Workers handle jobs concurrently
- All processes complete
- No conflicts

### Test 7: Process Variables Flow
- Start process with variables
- Verify variables passed to workers
- Workers modify variables
- Variables flow through process
- End event has all variables

### Test 8: Monitor in Operate
- Start process
- Open Operate UI (http://localhost:8081)
- View running instance
- Inspect variables
- See execution flow
- Check audit log

### Test 9: User Task Completion
- Start process that creates user task
- Task appears in Tasklist (http://localhost:8082)
- Login as demo/demo
- Complete task with data
- Process continues
- Verify variables passed to next step

---

## 📖 Learning Resources

### Official Documentation
- [Camunda 8 Docs](https://docs.camunda.io/)
- [Zeebe Documentation](https://docs.camunda.io/docs/components/zeebe/zeebe-overview/)
- [Job Workers](https://docs.camunda.io/docs/components/concepts/job-workers/)
- [BPMN Coverage in Zeebe](https://docs.camunda.io/docs/components/modeler/bpmn/bpmn-coverage/)
- [Zeebe Spring SDK](https://docs.camunda.io/docs/apis-tools/spring-zeebe-sdk/getting-started/)

### Video Tutorials
- [Camunda 8 YouTube Channel](https://www.youtube.com/c/Camunda)
- "Getting Started with Camunda 8" series

### BPMN Learning
- [BPMN Tutorial](https://camunda.com/bpmn/)
- [BPMN Quick Reference](https://www.bpmn.org/)

### Example Projects
- [Camunda 8 Examples](https://github.com/camunda-community-hub/camunda-8-examples)
- [Zeebe Get Started Guide](https://docs.camunda.io/docs/guides/)

---

## ⚠️ Potential Challenges

### Challenge 1: Zeebe Connection Issues
**Issue:** Workers can't connect to Zeebe
**Solution:**
- Ensure Zeebe is running: `docker-compose -f docker-compose-camunda.yml ps zeebe`
- Check gateway address: `localhost:26500`
- Verify plaintext mode enabled
- Check firewall/network settings

### Challenge 2: Jobs Not Picked Up
**Issue:** Worker running but jobs not being processed
**Solution:**
- Verify job type matches BPMN task type exactly (case-sensitive)
- Check worker is annotated with `@JobWorker`
- Ensure Spring component scanning includes worker package
- Check Zeebe logs for errors

### Challenge 3: Process Variables Not Passing
**Issue:** Variables not available in workers
**Solution:**
- Use `@Variable` annotation correctly
- Verify variable names match BPMN process
- Check variable serialization (must be JSON-serializable)
- Use Map<String, Object> for dynamic variables

### Challenge 4: Incidents in Operate
**Issue:** Processes stuck with incidents
**Solution:**
- Check worker logs for exceptions
- View incident details in Operate
- Fix root cause (e.g., null pointer, API error)
- Retry incident in Operate
- Consider adding error handling in workers

### Challenge 5: Performance Issues
**Issue:** Slow process execution
**Solution:**
- Increase worker threads in application.yml
- Scale Zeebe horizontally (add more brokers)
- Optimize AI agent response time
- Use async patterns for long-running tasks

---

## 🎯 Success Criteria for Phase 6

- [ ] Camunda 8 running via docker-compose ✅ (Already done)
- [ ] Zeebe client integrated in Spring Boot
- [ ] At least 2 BPMN processes designed (simple + document)
- [ ] AI agent integrated as job worker
- [ ] Document processing workflow complete
- [ ] User tasks working (Tasklist integration)
- [ ] Error handling implemented (incidents)
- [ ] Monitoring via Operate working
- [ ] REST API for workflow operations
- [ ] All 9 tests passing
- [ ] PHASE-6-GUIDE.md complete
- [ ] CLAUDE.md updated

---

## 🚀 Getting Started Commands

When you start your next session:

```bash
# 1. Check git status
git status
git log --oneline -5

# 2. Read documentation
cat CAMUNDA-SETUP.md
cat NEXT-SESSION-GUIDE.md

# 3. Start Camunda Platform 8
docker-compose -f docker-compose-camunda.yml up -d

# 4. Check services are healthy (wait 2-3 min)
docker-compose -f docker-compose-camunda.yml ps

# 5. Access UIs
# Web Modeler: http://localhost:8070 (demo/demo)
# Operate: http://localhost:8081 (demo/demo)
# Tasklist: http://localhost:8082 (demo/demo)

# 6. Add Zeebe dependencies to pom.xml

# 7. Create ZeebeClientConfiguration

# 8. Create first job worker

# 9. Design BPMN in Web Modeler

# 10. Test workflow execution!
```

---

## 💡 Tips for Success

1. **Start with Docker**
   - Get Camunda running first
   - Verify all UIs accessible
   - Don't code until infrastructure works

2. **Use Web Modeler**
   - Easier than desktop modeler for beginners
   - Deploy directly from browser
   - Visual feedback immediate

3. **Leverage Operate**
   - Monitor every process execution
   - Inspect variables at each step
   - Debug with incident details
   - Don't guess - look at Operate!

4. **Test Incrementally**
   - Start with simplest workflow (Start → Task → End)
   - Add complexity one step at a time
   - Don't build full workflow before testing

5. **Read Error Messages**
   - Zeebe errors are descriptive
   - Check worker logs for exceptions
   - Look at incident details in Operate
   - Stack traces tell you exactly what failed

6. **Use Job Types Carefully**
   - Job type must match exactly (case-sensitive)
   - `ai-task` ≠ `AI-task` ≠ `ai_task`
   - Use consistent naming convention

7. **Handle Errors Gracefully**
   - Don't let workers crash silently
   - Log errors clearly
   - Throw exceptions to create incidents
   - Incidents can be retried in Operate

---

## 📞 Quick Reference

**Camunda 8 Services:**
- **Zeebe gRPC:** localhost:26500
- **Zeebe REST:** http://localhost:8088
- **Zeebe Metrics:** http://localhost:9600
- **Operate:** http://localhost:8081 (demo/demo)
- **Tasklist:** http://localhost:8082 (demo/demo)
- **Web Modeler:** http://localhost:8070 (demo/demo)
- **Optimize:** http://localhost:8083 (demo/demo)

**Key Commands:**
```bash
# Start Camunda
docker-compose -f docker-compose-camunda.yml up -d

# Stop Camunda
docker-compose -f docker-compose-camunda.yml down

# View Zeebe logs
docker-compose -f docker-compose-camunda.yml logs -f zeebe

# Restart Zeebe
docker-compose -f docker-compose-camunda.yml restart zeebe
```

**Key Annotations:**
```java
@JobWorker(type = "ai-task")  // Register job worker
@Variable(name = "question")  // Inject process variable
@Component                     // Make worker a Spring bean
```

---

## ✅ Pre-Session Checklist

Before starting Phase 6 implementation:

- [ ] Phase 5 complete ✅
- [ ] All Phase 5 tests passing ✅
- [ ] Camunda docker-compose created ✅
- [ ] CAMUNDA-SETUP.md read ✅
- [ ] This guide read completely ✅
- [ ] Docker has 8GB+ RAM allocated
- [ ] Ports 8070-8088, 9200, 9600, 18080, 26500 available
- [ ] Ready to add Zeebe dependencies
- [ ] Understand job worker pattern
- [ ] Know how to use Web Modeler

---

## 🎓 Expected Learning Outcomes

After Phase 6, you will know:

1. **How to design BPMN workflows** visually in Web Modeler
2. **How to integrate Zeebe** with Spring Boot applications
3. **How to create job workers** that execute workflow tasks
4. **How to orchestrate AI agents** across multi-step processes
5. **How to handle human tasks** with Tasklist UI
6. **How to monitor and debug** processes in Operate
7. **How to build production-ready** workflow systems with Camunda 8

---

## 📊 Estimated Time

**Total Phase 6 Time:** 4-5 hours

- Start Camunda: 15-20 min ✅ (Already done)
- Add Zeebe client: 15-20 min
- First BPMN process: 20-30 min
- AI agent integration: 45-60 min
- Document workflow: 60-90 min
- Monitoring & testing: 30-45 min
- Documentation: 30-45 min
- Debugging & iteration: 30-60 min

**Can be split across multiple sessions if needed**

---

## 🎉 Final Notes

**You've completed 5 out of 7 phases!** 🎊

**Completed:**
- ✅ Phase 1: LLM Integration
- ✅ Phase 2: Prompt Engineering
- ✅ Phase 3: Vector Database
- ✅ Phase 4: RAG
- ✅ Phase 5: Tool Use

**Current:**
- 🚧 Phase 6: Camunda Workflows (In Progress)

**Remaining:**
- ⏳ Phase 7: Integration & Polish

**Key Differences from Original Guide:**
- ❌ Camunda 7.x (embedded engine) → ✅ Camunda 8.x (Zeebe microservices)
- ❌ Spring Boot Starter → ✅ Zeebe Java Client
- ❌ Embedded webapps → ✅ Separate Operate/Tasklist
- ❌ External Task Client → ✅ Job Workers
- ❌ Embedded deployment → ✅ Docker Compose

You're building a **modern, cloud-native** AI system with Camunda 8. This is cutting-edge enterprise architecture!

**Good luck with Phase 6!** 🚀

---

**END OF NEXT-SESSION-GUIDE.MD**

*Created: 2026-01-15*
*Updated: 2026-01-15 (Revised for Camunda 8.x)*
*For: Session 9 (Phase 6 Implementation)*
*Status: Ready to use*
