package io.dapr.examples.workflows.moneytransfer;


import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class MoneyTransferClient {
  /**
   * The main method to start the client.
   *
   * @param args Input arguments (unused).
   * @throws InterruptedException If program has been interrupted.
   */
    public static void main(String[] args) {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            String instanceId = client.scheduleNewWorkflow(MoneyTransferWorkflow.class);
            System.out.printf("Started a new money transfer workflow with instance ID: %s%n", instanceId);
            
            WorkflowInstanceStatus workflowInstanceStatus =
                client.waitForInstanceCompletion(instanceId, null, true);

            String result = workflowInstanceStatus.readOutputAs(String.class);
            System.out.printf("workflow instance with ID: %s completed with result: %s%n", instanceId, result);


        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
