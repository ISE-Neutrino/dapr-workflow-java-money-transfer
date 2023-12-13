package com.example.daprworkflowjavamoneytransfer.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;

public class MoneyTransferWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("---\n\tStarting Workflow: " + ctx.getName() + "\n---\n");

      String result = "";
      result += ctx.callActivity(NotifyActivity.class.getName(), "\n === \n\t Testing workflow julio \n===\n", String.class).await();
      
      ctx.getLogger().info("---\n\tWorkflow finished with result: " + result + "\n---");
      ctx.complete(result);
    };
  }
}

