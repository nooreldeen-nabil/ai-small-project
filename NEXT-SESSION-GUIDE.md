# 🚀 Next Session Guide - Phase 6: Camunda Workflows

**Date Created:** 2026-01-15
**For Session:** Session 9 (Phase 6 Implementation)
**Current Status:** Phase 5 Complete ✅ (All 9/9 Tests Passing)
**Next Phase:** Phase 6 - Agentic AI with Camunda Workflows (BPMN)

---

## 📋 Quick Session Startup Checklist

When starting your next session, do these in order:

1. **Read this guide completely** ⬅️ You are here
2. **Read CLAUDE.md** - Session 8 notes and project status
3. **Check git status** - Verify you're on the correct branch
4. **Review Phase 6 requirements** - See "Phase 6 Overview" below
5. **Start implementation** - Follow "Implementation Plan" section

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

**Current Branch:** `claude/phase-5-tool-use-unNpt`
- ✅ All code committed (4 commits)
- ✅ All tests validated
- ✅ Documentation updated
- ✅ Ready for merge to `claude/develop-stable-nSxCU`

**Action Required:** Merge Phase 5 to stable branch before starting Phase 6

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

3. **Camunda Integration**
   - Workflow engine (orchestrates processes)
   - Task management (assigns work to humans/bots)
   - Process monitoring (track progress)
   - Historical data (audit trail)

### Example Workflow

**Document Processing Pipeline:**
```
Start
  ↓
Upload Document
  ↓
AI Classification → [Decision: Confident?]
  ├─ Yes → Extract Data (AI)
  │         ↓
  │       Save to Database
  │         ↓
  │       Notify User
  │         ↓
  │       End
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

### Why Camunda?

- **Industry Standard** - Used by enterprises worldwide
- **BPMN 2.0** - Standard notation for business processes
- **Java Integration** - Native Spring Boot support
- **REST API** - Control workflows via HTTP
- **Cockpit UI** - Visual monitoring dashboard
- **Open Source** - Free Community Edition

---

## 📚 What You Need to Learn

### Core Concepts

1. **BPMN (Business Process Model and Notation)**
   - Standard for drawing business processes
   - Tasks, Gateways, Events, Sequence Flows
   - Industry-standard flowchart language

2. **Camunda Platform**
   - Workflow engine (executes BPMN processes)
   - Tasklist (human task management)
   - Cockpit (process monitoring)
   - REST API (programmatic control)

3. **External Tasks Pattern**
   - Camunda can delegate work to external workers
   - Your AI agent becomes an external worker
   - Fetch tasks, execute, complete

4. **Process Variables**
   - Data that flows through the process
   - Example: documentId, confidence, classification
   - Shared across process steps

5. **Service Tasks**
   - Automated tasks (no human needed)
   - Can be synchronous or asynchronous
   - Executes Java code or external service

### Technologies to Add

- **Camunda Platform 7.x** (Community Edition)
- **BPMN Modeler** (to design workflows)
- **Camunda External Task Client** (Java library)
- **Process Engine API** (Spring Boot integration)

---

## 🏗️ Implementation Plan

### Step 1: Setup Camunda (30-45 min)

**Goal:** Get Camunda running with Spring Boot

**Tasks:**
1. Add Camunda dependencies to `pom.xml`
2. Configure Camunda properties in `application.yml`
3. Create Camunda database schema
4. Start application and verify Camunda UI

**Expected Result:**
- Camunda running at http://localhost:8080/camunda
- Can access Cockpit, Tasklist, Admin apps
- Database has Camunda tables

**Resources:**
- [Camunda Spring Boot Starter](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/)
- [Get Started Guide](https://docs.camunda.org/get-started/spring-boot/)

### Step 2: Create Your First BPMN Process (20-30 min)

**Goal:** Design and deploy a simple workflow

**Tasks:**
1. Install Camunda Modeler (desktop app) OR use online modeler
2. Design a simple process:
   ```
   Start → Service Task (Call AI) → End
   ```
3. Save BPMN file to `src/main/resources/processes/`
4. Deploy to Camunda engine
5. Start a process instance via API

**Expected Result:**
- BPMN file in resources folder
- Process deployed and visible in Cockpit
- Can start process instance via REST API

**Resources:**
- [Camunda Modeler Download](https://camunda.com/download/modeler/)
- [BPMN Tutorial](https://camunda.com/bpmn/)

### Step 3: Integrate AI Agent with Camunda (45-60 min)

**Goal:** Make AI agent execute Camunda tasks

**Tasks:**
1. Create External Task Worker service
2. Configure worker to subscribe to AI tasks
3. Implement task handlers that call AgentService
4. Test: Workflow triggers AI agent

**Expected Result:**
- Camunda workflow starts
- External task worker picks up task
- Calls your AgentService from Phase 5
- Completes task in Camunda
- Process continues to next step

**Key Code Pattern:**
```java
@Component
public class AiAgentWorker {

    @Autowired
    private AgentService agentService;

    @ExternalTaskHandler(topic = "ai-task")
    public void handleAiTask(ExternalTask task, ExternalTaskService service) {
        // Get process variables
        String userQuestion = task.getVariable("question");

        // Call AI agent (from Phase 5)
        AgentTaskRequest request = new AgentTaskRequest();
        request.setTask(userQuestion);
        AgentTaskResponse response = agentService.executeTask(request);

        // Complete task with result
        Map<String, Object> variables = new HashMap<>();
        variables.put("answer", response.getAnswer());
        service.complete(task, variables);
    }
}
```

### Step 4: Build Document Processing Workflow (60-90 min)

**Goal:** Complete end-to-end workflow

**Tasks:**
1. Design BPMN process:
   ```
   Start → Upload Document →
   AI Classification →
   [Decision: Confident?] →
     Yes → AI Extraction → Save → Notify → End
     No → Human Review → AI Extraction → Save → Notify → End
   ```
2. Implement service tasks
3. Add user tasks for human review
4. Test complete workflow

**Expected Result:**
- Document uploaded triggers workflow
- AI classifies document
- If low confidence, waits for human
- Extracts data with AI
- Saves to database
- Sends notification
- Process completes

### Step 5: Add Monitoring & Testing (30-45 min)

**Goal:** Validate and monitor workflows

**Tasks:**
1. Create REST endpoints to:
   - Start workflows
   - Query process status
   - Get task lists
   - Complete user tasks
2. Test error scenarios
3. View execution in Cockpit

**Expected Result:**
- Can start workflows via API
- Can track process progress
- Cockpit shows running instances
- Can see historical data

### Step 6: Documentation (30-45 min)

**Goal:** Document Phase 6

**Tasks:**
1. Create PHASE-6-GUIDE.md
2. Include:
   - BPMN diagrams (screenshots)
   - API examples
   - Testing guide
   - Troubleshooting
3. Update CLAUDE.md with Session 9 notes

**Expected Result:**
- Complete Phase 6 documentation
- Ready for testing and handoff

---

## 📁 File Structure for Phase 6

```
ai-small-project/
├── src/main/java/com/ai/mvp/
│   ├── workflow/                          ⭐ NEW
│   │   ├── worker/                        ⭐ NEW
│   │   │   ├── AiAgentWorker.java        ⭐ NEW (External task handler)
│   │   │   ├── DocumentClassificationWorker.java  ⭐ NEW
│   │   │   └── DataExtractionWorker.java ⭐ NEW
│   │   ├── delegate/                      ⭐ NEW
│   │   │   └── NotificationDelegate.java ⭐ NEW (Service task)
│   │   └── listener/                      ⭐ NEW
│   │       └── ProcessEventListener.java ⭐ NEW
│   ├── controller/
│   │   └── WorkflowController.java        ⭐ NEW (Start/monitor workflows)
│   ├── service/
│   │   ├── AgentService.java              (Already exists from Phase 5)
│   │   └── WorkflowService.java           ⭐ NEW (Camunda integration)
│   └── dto/
│       └── workflow/                       ⭐ NEW
│           ├── StartProcessRequest.java   ⭐ NEW
│           └── ProcessStatusResponse.java ⭐ NEW
│
├── src/main/resources/
│   ├── processes/                          ⭐ NEW
│   │   ├── document-processing.bpmn       ⭐ NEW
│   │   └── simple-ai-task.bpmn           ⭐ NEW
│   └── application.yml                     (Update with Camunda config)
│
├── pom.xml                                 (Add Camunda dependencies)
└── PHASE-6-GUIDE.md                        ⭐ NEW
```

---

## 🔑 Key Dependencies to Add

Add to `pom.xml`:

```xml
<!-- Camunda Spring Boot Starter -->
<dependency>
    <groupId>org.camunda.bpm.springboot</groupId>
    <artifactId>camunda-bpm-spring-boot-starter-webapp</artifactId>
    <version>7.20.0</version>
</dependency>

<!-- Camunda External Task Client -->
<dependency>
    <groupId>org.camunda.bpm</groupId>
    <artifactId>camunda-external-task-client</artifactId>
    <version>7.20.0</version>
</dependency>

<!-- H2 for Camunda (or use existing Oracle) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## ⚙️ Configuration Example

Add to `application.yml`:

```yaml
camunda:
  bpm:
    admin-user:
      id: demo
      password: demo
      firstName: Demo
    filter:
      create: All tasks
    webapp:
      index-redirect-enabled: true
    database:
      schema-update: true
    generic-properties:
      properties:
        history: full
```

---

## 🧪 Testing Strategy

### Test 1: Simple Process
- Start process with one AI task
- Verify AI agent executes
- Check result in process variables
- Confirm process completes

### Test 2: Decision Gateway
- Process with conditional branch
- High confidence → Direct to extraction
- Low confidence → Human review

### Test 3: Human Task
- Create user task
- Query task list
- Complete task via API
- Verify process continues

### Test 4: Error Handling
- Simulate AI agent failure
- Verify error boundary works
- Check retry logic
- Confirm process rollback

### Test 5: Long-running Process
- Start process
- Stop application
- Restart application
- Verify process resumes

### Test 6: Parallel Execution
- Multiple AI tasks in parallel
- Verify all complete before proceeding
- Check execution time improvement

---

## 📖 Learning Resources

### Official Documentation
- [Camunda Docs](https://docs.camunda.org/)
- [BPMN Tutorial](https://camunda.com/bpmn/)
- [Spring Boot Integration](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/)
- [External Task Pattern](https://docs.camunda.org/manual/latest/user-guide/process-engine/external-tasks/)

### Video Tutorials
- [Camunda YouTube Channel](https://www.youtube.com/c/Camunda)
- "Getting Started with Camunda Platform" series

### BPMN Learning
- [BPMN Specification](https://www.omg.org/spec/BPMN/2.0/)
- [BPMN Quick Reference](https://www.bpmn.org/)

### Example Projects
- [Camunda Platform Examples](https://github.com/camunda/camunda-bpm-examples)
- [Camunda Spring Boot Examples](https://github.com/camunda/camunda-bpm-platform/tree/master/spring-boot-starter)

---

## ⚠️ Potential Challenges

### Challenge 1: Database Schema
**Issue:** Camunda creates ~80 tables in database
**Solution:** Use separate database OR prefix tables OR use H2 for Camunda

### Challenge 2: Process Versioning
**Issue:** Redeploying BPMN creates new version
**Solution:** Understand version migration, use version tags

### Challenge 3: Debugging Workflows
**Issue:** Hard to see what's happening inside process
**Solution:** Use Cockpit extensively, add logging in delegates

### Challenge 4: Transaction Management
**Issue:** Database transactions across process steps
**Solution:** Understand Camunda transaction boundaries, use async continuations

### Challenge 5: Long Variables
**Issue:** Large JSON in process variables
**Solution:** Store in separate table, only store ID in process

---

## 🎯 Success Criteria for Phase 6

- [ ] Camunda engine running and accessible
- [ ] At least 1 BPMN process designed and deployed
- [ ] AI agent integrated as external task worker
- [ ] Document processing workflow complete
- [ ] Human tasks working (create, query, complete)
- [ ] Error handling implemented
- [ ] Monitoring via Cockpit working
- [ ] REST API for workflow operations
- [ ] All tests passing
- [ ] PHASE-6-GUIDE.md complete

---

## 🚀 Getting Started Commands

When you start your next session:

```bash
# 1. Check git status
git status
git log --oneline -5

# 2. Read documentation
cat CLAUDE.md | head -50
cat NEXT-SESSION-GUIDE.md

# 3. Pull latest changes (if needed)
git pull origin claude/phase-5-tool-use-unNpt

# 4. (Optional) Merge Phase 5 to stable
git checkout claude/develop-stable-nSxCU
git merge claude/phase-5-tool-use-unNpt
git push -u origin claude/develop-stable-nSxCU

# 5. Create new Phase 6 branch
git checkout -b claude/phase-6-camunda-workflows-<sessionId>

# 6. Add Camunda dependencies
# Edit pom.xml to add dependencies

# 7. Start implementation!
```

---

## 💡 Tips for Success

1. **Start Simple**
   - Don't build the complex workflow first
   - Start with "Start → AI Task → End"
   - Add complexity incrementally

2. **Use Camunda Modeler**
   - Visual design is easier than XML
   - Validates BPMN syntax
   - Can deploy directly to engine

3. **Leverage Cockpit**
   - See what's happening in real-time
   - Inspect process variables
   - Manually complete stuck tasks

4. **Test Early, Test Often**
   - Deploy process, test, iterate
   - Don't write entire workflow before testing
   - Use Cockpit to debug

5. **Read Error Messages**
   - Camunda error messages are detailed
   - Check stack traces carefully
   - Look for "BPMN error" vs "Java error"

6. **Keep BPMN Simple**
   - Avoid deeply nested processes
   - Use subprocesses for reusability
   - One process per file

---

## 📞 Quick Reference

**Camunda Endpoints:**
- Cockpit: http://localhost:8080/camunda/app/cockpit
- Tasklist: http://localhost:8080/camunda/app/tasklist
- Admin: http://localhost:8080/camunda/app/admin
- REST API: http://localhost:8080/engine-rest

**Default Credentials:**
- Username: demo
- Password: demo

**Important Camunda Tables:**
- `ACT_RE_PROCDEF` - Process definitions
- `ACT_RU_EXECUTION` - Running process instances
- `ACT_RU_TASK` - Active tasks
- `ACT_HI_PROCINST` - Historical process instances

**Common BPMN Elements:**
- **Start Event** - Circle (where process begins)
- **End Event** - Circle with thick border (where process ends)
- **Service Task** - Rounded rectangle with gear (automated task)
- **User Task** - Rounded rectangle with person (human task)
- **Gateway** - Diamond (decision point, parallel split/join)
- **Sequence Flow** - Arrow (connects elements)

---

## ✅ Pre-Session Checklist

Before starting your next session, ensure:

- [ ] Phase 5 is fully complete ✅
- [ ] All Phase 5 tests passing ✅
- [ ] Documentation updated ✅
- [ ] This guide read completely
- [ ] Camunda documentation bookmarked
- [ ] Ready to create new branch for Phase 6
- [ ] Understand BPMN basics

---

## 🎓 Expected Learning Outcomes

After Phase 6, you will know:

1. **How to design business processes** using BPMN
2. **How to integrate Camunda** with Spring Boot
3. **How to orchestrate AI workflows** across multiple steps
4. **How to handle human tasks** in automated processes
5. **How to monitor and debug** running processes
6. **How to build production-ready** workflow systems

---

## 📊 Estimated Time

**Total Phase 6 Time:** 4-6 hours

- Setup Camunda: 30-45 min
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

**Remaining:**
- ⏳ Phase 6: Camunda Workflows (Next!)
- ⏳ Phase 7: Integration & Polish

You're building a production-grade AI system. Phase 6 adds enterprise workflow capabilities that make this a real-world solution.

**Good luck with Phase 6!** 🚀

---

**END OF NEXT-SESSION-GUIDE.MD**

*Created: 2026-01-15*
*For: Session 9 (Phase 6 Implementation)*
*Status: Ready to use*
