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

    TransferResponse transferResponse = TransferResponse.builder()
        .transferId(transferRequest.getTransferId())
        .status(TransferStatus.ACCEPTED.toString())
        .message(String.format("Transfer %s Accepted. Validating...", transferRequest.getTransferId()))
        .build();
    saveTransferState(transferResponse);

    // transfers with high ammounts do not get approved
    if (!validAmount(transferRequest.getAmount())) {
      transferResponse.setStatus(TransferStatus.REJECTED.toString());
      transferResponse.setMessage(String.format("Rejected Transfer - Invalid Amount: %s", transferRequest.getAmount()));

    } else if (!validAccount(transferRequest.getSender())) {
      transferResponse.setStatus(TransferStatus.REJECTED.toString());
      transferResponse.setMessage(String.format("Rejected Transfer - Invalid Sender: %s", transferRequest.getSender()));

    } else if (!validAccount(transferRequest.getReceiver())) {
      transferResponse.setStatus(TransferStatus.REJECTED.toString());
      transferResponse.setMessage(String.format("Rejected Transfer - Invalid Receiver: %s", transferRequest.getReceiver()));

    } else {
      transferResponse.setStatus(TransferStatus.VALIDATED.toString());
      transferResponse.setMessage(String.format("Validated Transfer: %s", transferRequest.getTransferId()));
    }

    saveTransferState(transferResponse);
    return transferResponse;
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

  private void saveTransferState(TransferResponse transferResponse) {
    logger.info(transferResponse.getMessage());
    daprClient.saveState(STATE_STORE, transferResponse.getTransferId(), transferResponse).block();
  }

}
