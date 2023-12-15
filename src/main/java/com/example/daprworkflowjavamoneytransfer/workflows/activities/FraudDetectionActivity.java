package com.example.daprworkflowjavamoneytransfer.workflows.activities;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.daprworkflowjavamoneytransfer.enums.ApprovalResult;
import com.example.daprworkflowjavamoneytransfer.enums.TransferStatus;
import com.example.daprworkflowjavamoneytransfer.model.CreateAccountRequest;
import com.example.daprworkflowjavamoneytransfer.model.TransferRequest;
import com.example.daprworkflowjavamoneytransfer.model.TransferResponse;

public class FraudDetectionActivity implements WorkflowActivity {

  private static final String STATE_STORE = "statestore";

  private static Logger logger = LoggerFactory.getLogger(FraudDetectionActivity.class);

  private DaprClient daprClient;

  public FraudDetectionActivity() {
    this.daprClient = new DaprClientBuilder().build();
  }

  @Override
  public Object run(WorkflowActivityContext ctx) {
    TransferRequest transferRequest = ctx.getInput(TransferRequest.class);

    logger.info("FraudDetection - Validating Transfer: {}", transferRequest);

    String outputMessage;
    // accounts without a 1st deposit do not get approved
    if (transferRequest.getAmount() > 1000) {
      outputMessage = "Fraud detected, amount has to be less than 1000.";
      logger.error(outputMessage);
      logger.info("Rejecting Transfer for: {}", transferRequest);

      transferRequest.setStatus(TransferStatus.REJECTED);
    } else {
      // hard code to Validate any amount
      outputMessage = String.format("Validated, amount is less than 1000 for: %s", transferRequest.toString());
      logger.info(outputMessage);

      transferRequest.setStatus(TransferStatus.VALIDATED);
    }

    // save transfer Status in StateStore
    daprClient.saveState(STATE_STORE, transferRequest.getTransferId(), transferRequest.getStatus()).block();

    return (TransferResponse.builder()
        .message(outputMessage)
        .status(transferRequest.getStatus().toString())
        .transferId(transferRequest.getTransferId())
        .build());
  }

}
