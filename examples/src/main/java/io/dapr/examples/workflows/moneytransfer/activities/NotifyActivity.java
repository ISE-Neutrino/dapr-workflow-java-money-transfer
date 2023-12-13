package io.dapr.examples.workflows.moneytransfer.activities;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotifyActivity implements WorkflowActivity {
  private static Logger logger = LoggerFactory.getLogger(NotifyActivity.class);

  @Override
  public Object run(WorkflowActivityContext ctx) {
    logger.info("Starting Activity: " + ctx.getName());
    
    var message = ctx.getInput(String.class);
    logger.info("Message Received from input: " + message);

    return "activity returned!";
  }

}
