package com.example.accountservice.workflows.activities;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.accountservice.enums.ApprovalResult;
import com.example.accountservice.model.CreateAccountRequest;

public class CreateAccountActivity implements WorkflowActivity {

  private static final String STATE_STORE = "statestore";

  private static Logger logger = LoggerFactory.getLogger(CreateAccountActivity.class);

  private DaprClient daprClient;

  public CreateAccountActivity() {
    this.daprClient = new DaprClientBuilder().build();
  }

  @Override
  public Object run(WorkflowActivityContext ctx) {

    CreateAccountRequest request = ctx.getInput(CreateAccountRequest.class);
    logger.info(String.format("Saving to State: Owner: %s, Amount: %f", request.getOwner(), request.getAmount()));
    daprClient.saveState(STATE_STORE, request.getOwner(), request.getAmount()).block();

    var message = "Account created for: " + request;

    return message;
  }

}
