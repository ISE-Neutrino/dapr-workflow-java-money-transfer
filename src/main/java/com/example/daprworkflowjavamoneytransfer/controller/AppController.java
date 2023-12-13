package com.example.daprworkflowjavamoneytransfer.controller;

import org.springframework.web.bind.annotation.RestController;

import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;

import java.util.concurrent.TimeoutException;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.daprworkflowjavamoneytransfer.workflows.*;

@RestController
public class AppController {

    @GetMapping("/transfer")
    public String getCreateWorkflow() {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            String instanceId = client.scheduleNewWorkflow(MoneyTransferWorkflow.class);
            System.out.printf("Started a new money transfer workflow with instance ID: %s%n", instanceId);

            WorkflowInstanceStatus workflowInstanceStatus = client.waitForInstanceCompletion(instanceId, null, true);

            String result = workflowInstanceStatus.readOutputAs(String.class);
            System.out.printf("workflow instance with ID: %s completed with result: %s%n", instanceId, result);

            return result;

        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/create")
    public String getTransferWorkflow() {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            String instanceId = client.scheduleNewWorkflow(CreateAccountWorkflow.class);
            System.out.printf("Started a new CreateAccount workflow with instance ID: %s%n", instanceId);

            WorkflowInstanceStatus workflowInstanceStatus = client.waitForInstanceCompletion(instanceId, null, true);

            String result = workflowInstanceStatus.readOutputAs(String.class);
            System.out.printf("workflow instance with ID: %s completed with result: %s%n", instanceId, result);

            return result;

        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/test")
    public String getTestingEndpoint() {
        return new String("Testing endpoint");
    }

}
