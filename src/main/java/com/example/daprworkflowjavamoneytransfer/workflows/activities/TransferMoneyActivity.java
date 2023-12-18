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

public class TransferMoneyActivity implements WorkflowActivity {

  private static final String STATE_STORE = "statestore";

  private static Logger logger = LoggerFactory.getLogger(TransferMoneyActivity.class);

  private DaprClient daprClient;

  public TransferMoneyActivity() {
    this.daprClient = new DaprClientBuilder().build();
  }

  @Override
  public Object run(WorkflowActivityContext ctx) {

    TransferRequest transferRequest = ctx.getInput(TransferRequest.class);
    transferRequest.setStatus(TransferStatus.APPROVED);

    String outputMessage;

    var senderBalance = daprClient.getState(STATE_STORE, transferRequest.getSender(), Double.class).block();
    var receiverBalance = daprClient.getState(STATE_STORE, transferRequest.getReceiver(), Double.class).block();

    // check amount in sender balance
    if (senderBalance.getValue() - transferRequest.getAmount() < 0) {
      outputMessage = String.format("Insufficient funds.");
      logger.info(outputMessage);
      daprClient.saveState(STATE_STORE, transferRequest.getTransferId(), TransferStatus.REJECTED).block();

      return TransferResponse.builder()
          .message(outputMessage)
          .status(TransferStatus.REJECTED.toString())
          .transferId(transferRequest.getTransferId())
          .build();
    }

    // update sender and receiver balances
    var newSenderBalance = senderBalance.getValue() - transferRequest.getAmount();
    var newReceiverBalance = receiverBalance.getValue() + transferRequest.getAmount();
    transferRequest.setStatus(TransferStatus.APPROVED);

    // Save states
    daprClient.saveState(STATE_STORE, transferRequest.getSender(), newSenderBalance).block();
    daprClient.saveState(STATE_STORE, transferRequest.getReceiver(), newReceiverBalance).block();
    daprClient.saveState(STATE_STORE, transferRequest.getTransferId(), transferRequest).block();

    outputMessage = String.format("Transfer completed: transferId: %s, Transfer: %s", transferRequest.getTransferId(),
        transferRequest.toString());
    logger.info(outputMessage);

    return TransferResponse.builder()
        .message(outputMessage)
        .status(TransferStatus.APPROVED.toString())
        .transferId(transferRequest.getTransferId())
        .build();
  }

}
