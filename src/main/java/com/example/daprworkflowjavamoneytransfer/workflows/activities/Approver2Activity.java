package com.example.daprworkflowjavamoneytransfer.workflows.activities;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.daprworkflowjavamoneytransfer.enums.ApprovalResult;
import com.example.daprworkflowjavamoneytransfer.model.TransferRequest;

public class Approver2Activity implements WorkflowActivity {
  private static Logger logger = LoggerFactory.getLogger(Approver2Activity.class);

  @Override
  public Object run(WorkflowActivityContext ctx) {
    TransferRequest transferRequest = ctx.getInput(TransferRequest.class);

    // Simulate slow processing
    try {
      Thread.sleep(2 * 1000);
    } catch (InterruptedException e) {
    }

    // hard code to Approved any request
    logger.info("Approver2: Approving transfer for: {}", transferRequest);
    return ApprovalResult.APPROVED;
  }

}
