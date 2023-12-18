package com.example.daprworkflowjavamoneytransfer.workflows.activities;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.workflows.runtime.WorkflowActivity;
import io.dapr.workflows.runtime.WorkflowActivityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.daprworkflowjavamoneytransfer.enums.TransferStatus;
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
    // transfers with high ammounts do not get approved
    if (!validAmount(transferRequest.getAmount()) ||
        !validAccount(transferRequest.getSender()) ||
        !validAccount(transferRequest.getReceiver())) {
      outputMessage = String.format("Rejected Transfer: %s", transferRequest);
      transferRequest.setStatus(TransferStatus.REJECTED);
    } else {
      outputMessage = String.format("Validated Transfer: %s", transferRequest);
      transferRequest.setStatus(TransferStatus.VALIDATED);
    }

    logger.info(outputMessage);

    // save transfer Status in StateStore
    daprClient.saveState(STATE_STORE, transferRequest.getTransferId(), transferRequest.getStatus()).block();

    return (TransferResponse.builder()
        .message(outputMessage)
        .status(transferRequest.getStatus().toString())
        .transferId(transferRequest.getTransferId())
        .build());
  }

  public boolean validAmount(double amount) {

    if (amount > 1000) {
      logger.error("Fraud detected, amount has to be less than 1000.");
      return false;
    }

    // hard code to Validate any smaller amount
    logger.info("Validated, amount is less than 1000.");
    return true;
  }

  public boolean validAccount(String accountOwner) {

    var accountBalance = daprClient.getState(STATE_STORE, accountOwner, Double.class).block();
    if (accountBalance.getValue() == null) {
      logger.error("Fraud detected, Account {} does not exist.", accountOwner);
      return false;
    }
    return true;
  }

}
