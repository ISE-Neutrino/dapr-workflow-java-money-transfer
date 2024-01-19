package com.example.transferservice.workflows.activities;

import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.transferservice.enums.ApprovalResult;
import com.example.transferservice.model.TransferRequest;

public class Approver1Activity implements WorkflowActivity {
  private static Logger logger = LoggerFactory.getLogger(Approver1Activity.class);

  @Override
  public Object run(WorkflowActivityContext ctx) {
    TransferRequest transferRequest = ctx.getInput(TransferRequest.class);

    // hard code to Approved any request
    logger.info("Approver1: Approving transfer for: {}", transferRequest);
    return ApprovalResult.APPROVED;
  }

}
