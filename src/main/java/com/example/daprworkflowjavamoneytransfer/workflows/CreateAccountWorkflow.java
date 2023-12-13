package com.example.daprworkflowjavamoneytransfer.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;

public class CreateAccountWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("\n---\n\tStarting Workflow: " + ctx.getName() + "\n---\n");

      String result = "";
      result += ctx.callActivity(NotifyActivity.class.getName(), "\n === \n\t Testing workflow - Creating Account \n===\n", String.class).await();
      
      ctx.getLogger().info("\n---\n\tWorkflow finished with result: " + result + "\n---");
      ctx.complete(result);
    };
  }
}

