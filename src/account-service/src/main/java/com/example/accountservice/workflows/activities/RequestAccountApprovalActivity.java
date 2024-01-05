package com.example.accountservice.workflows.activities;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.accountservice.enums.ApprovalResult;
import com.example.accountservice.model.CreateAccountRequest;

public class RequestAccountApprovalActivity implements WorkflowActivity {
  private static Logger logger = LoggerFactory.getLogger(RequestAccountApprovalActivity.class);

  @Override
  public Object run(WorkflowActivityContext ctx) {
    CreateAccountRequest newAccountRequest = ctx.getInput(CreateAccountRequest.class);

    logger.info("Requesting approval for account: {}", newAccountRequest);

    // accounts without a 1st deposit do not get approved
    if (newAccountRequest.getAmount() == 0) {
      logger.info("Rejecting account creation for: {}", newAccountRequest);

      return ApprovalResult.REJECTED;
    }

    // hard code to Approve any amount
    logger.info("Approving account creation for: {}", newAccountRequest);
    return ApprovalResult.APPROVED;
  }

}
