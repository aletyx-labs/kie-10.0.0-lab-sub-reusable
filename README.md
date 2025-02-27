# Lab: Reusable Subprocess!

A reusable subprocess is a BPMN construct that allows process modularization by defining a standalone subprocess that can be invoked from multiple parent processes. Unlike embedded subprocesses, reusable subprocesses are independent and can be executed in different workflows, promoting reusability and consistency across process definitions.

Reusable subprocesses are particularly useful when the same set of activities needs to be performed in multiple processes, such as order validation, loan risk assessment, or customer onboarding.


## Lab Overview

In this exercise, you will create two BPMN processes:

- A reusable subprocess designed for user task-based document validation. This subprocess will serve as a generic validation mechanism that can be reused across different workflows.


- A main process for content approval, which will invoke the reusable subprocess. Based on the validation outcome, the main process will print different messages to the system output (sysout).


### Supporting Files

You have the scafolded project for this lab available in https://github.com/aletyx-labs/kie-10.0.0-lab-sub-reusable. The following POJO is privided for convenience

### Document.java
This class represents a Document with id, name and content as field. 

### Instructions

#### Step 1: Clone the Repository

Clone the lab project from this repository, which contains all the necessary files and infrastructure for the lab.

#### Step 2: Import the Project

Import the project into VS Code and explore the provided Java classes.

#### Step 3: Create the reusable subprocess file

Create a new file name docReview.bpmn under src/main/resources and open it using the Apache KIE BPMN Editor.

Once file is created, create the process variables that we'll need.

| Name      | Data Type                                    |
|-----------|----------------------------------------------|
| document  | ai.aletyx.kie.workshop.sub.reusable.Document |
| review    | String                                       |

#### Step 4: Create the reusable subprocess diagram

This process should have a simple user task and a script task that will print the result in the console.

In general this is the nodes you'll have to create:

1. Start Event
2. Document Review User Task

     Task Name: DocumentReview

     Groups: managers

     Variables - Input Mapping:

     | Name   | Data Type | Source     |
     |--------|----------|------------|
     | document | ai.aletyx.kie.workshop.sub.reusable.Document   | document   |
     | review | String   | review   |

     Variables - Output Mapping:

     | Name   | Data Type | Target |
     |--------|----------|--------|
     | review | String   | review   |

3. Log Info Script Task

     Script:
        ```java
            System.out.println("Finished Review: " + review);
        ```

5. End


#### Step 5: Create the main process file

Create a new file name contentApproval.bpmn under src/main/resources and open it using the Apache KIE BPMN Editor.

Once file is created, create the process variables that we'll need.

| Name      | Data Type                                    |
|-----------|----------------------------------------------|
| document  | ai.aletyx.kie.workshop.sub.reusable.Document |
| review    | String                                       |

#### Step 6: Create the main process diagram

This process will start inviking the reusable process, and depending on the result using a XOR gateway, a different script task will be executed.

In general this is the nodes you'll have to create:

1. Start Event
2. Review Reusable Subprocess

     Called Element: docReview

     Abort Parent: checked

     Wait for Completion: checked

     Variables - Input Mapping:

     | Name   | Data Type | Source     |
     |--------|----------|------------|
     | document | ai.aletyx.kie.workshop.sub.reusable.Document   | document   |

     Variables - Output Mapping:

     | Name   | Data Type | Target |
     |--------|----------|--------|
     | review | String   | review   |

3. Exclusive Gateway: We'll check if the review got an "yes" or not.
    - On Fail 

        ```java
        return !"yes".equals(review);
        ```
        3.2. Create a Didn't work Script Task

            Script:
                ```java
                    System.out.println("NOT DONE: " + review);
                ```

       3.3. Create an End Event

    - On Success:

       ```java
       return "yes".equals(review);
       ```

       3.4. Create a Done Script Task

            Script:
                ```java
                    System.out.println("DONE: " + review);
                ```
       3.5. Create an End Event


#### Step 7: Test the Process

- Run the process in different scenarios.

#### Step 8: Add a Process Listener

Now we'll create our own process listener, we want to check the current riskLevel of the current process instance.

For that we'll first add to the main process `contentApproval.bpmn` a metadata under Advanced group of the process properties panel:

| Name      | Value  |
|-----------|--------|
| riskLevel |  low   |

Now we'll create a Java class called MyListener, and we'll list the ProcessStartedEvent! Here's an implementation

**Important note:** The listener needs to be a valid bean for dependency injection.

```java
import java.util.Map;

import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MyListener extends DefaultKogitoProcessEventListener {
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        Map<String, Object> metadata = event.getProcessInstance().getProcess().getMetaData();
        String riskLevel = (String) metadata.get("riskLevel");
        if (riskLevel != null && riskLevel == "low") {
            System.out.println("This is low risk process");
        }
    }
}
```

If you create a new instance, you should see the message in your console log.

#### Step 9: Crete your own custom endpoint

Kogito automatically creates domain based endpoint, using your process model as reference. However you may not want that, you may want to start your process programatically or expose your own custom endpoint.

In this section we'll disable the rest endpoint generation in the `application.properties` bvy adding the following line:

```properties
kogito.generate.rest=false
```

And we'll create a new class named `MyService` - which for the convenience of testing, we'll create a REST endpoint. Here's the code:

```java
import java.util.HashMap;
import java.util.Map;

import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
@Path("/custom")
public class MyService {

   @Inject
   ProcessService processService;

   @Inject
   @Named("contentApproval")
   Process<? extends Model> process;

   @GET
   public void startMyProcessCustomWay() {
        for (int i = 0; i < 2; i++) {
            Model m = process.createModel();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("document", new Document("id" + i, "name" + i, "content"+i));
            m.fromMap(parameters);

            ProcessInstance<?> processInstance = processService.createProcessInstance((Process)process, null, m, null, null);
            System.out.println("Status:" + processInstance.checkError().variables());
        }
    }
}
```

**Note:** in this case we'll create 3 process instances.

#### Step 10: Exploring GraphQL

In this last step, we'll play with GraphQL, and execute a few queries.

First let's list all UserTask for our process:

```graphql
{
  UserTaskInstances(where: {processId: {equal: "contentApproval"}}) {
    id
    name
    actualOwner
    state
  }
}
```

Then we'll list all the Process Instances:

```graphql
{
  ProcessInstances {
    id
    processId
    state
    parentProcessInstanceId
    rootProcessInstanceId
    variables
  }
}
```
