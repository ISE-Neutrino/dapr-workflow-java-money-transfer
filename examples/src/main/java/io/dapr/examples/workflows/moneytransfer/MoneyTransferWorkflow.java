package io.dapr.examples.workflows.moneytransfer;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import io.dapr.examples.workflows.moneytransfer.activities.NotifyActivity;

public class MoneyTransferWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("Starting Workflow: " + ctx.getName());

      String result = "";
      result += ctx.callActivity(NotifyActivity.class.getName(), "\n === \n\t Testing workflow julio \n===\n", String.class).await();
      
      ctx.getLogger().info("Workflow finished with result: " + result);
      ctx.complete(result);
    };
  }
}

