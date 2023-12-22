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

    TransferResponse transferResponse = TransferResponse.builder()
        .transferId(transferRequest.getTransferId())
        .status(TransferStatus.APPROVED.toString())
        .message("Transfer Approved. Updating Balances...")
        .build();
    saveTransferState(transferResponse);

    // check amount in sender balance
    var senderBalance = daprClient.getState(STATE_STORE, transferRequest.getSender(), Double.class).block();
    if (senderBalance.getValue() - transferRequest.getAmount() < 0) {
      transferResponse.setMessage("Insufficient Funds.");
      transferResponse.setStatus(TransferStatus.REJECTED.toString());
      saveTransferState(transferResponse);

      return transferResponse;
    }

    updateBalances(transferRequest);

    transferResponse.setStatus(TransferStatus.COMPLETED.toString());
    transferResponse.setMessage("Transfer Completed.");
    saveTransferState(transferResponse);

    return transferResponse;
  }

  private void updateBalances(TransferRequest transferRequest) {
    // update sender and receiver balances
    var senderBalance = daprClient.getState(STATE_STORE, transferRequest.getSender(), Double.class).block();
    var receiverBalance = daprClient.getState(STATE_STORE, transferRequest.getReceiver(), Double.class).block();
    
    var newSenderBalance = senderBalance.getValue() - transferRequest.getAmount();
    var newReceiverBalance = receiverBalance.getValue() + transferRequest.getAmount();

    // Save states
    daprClient.saveState(STATE_STORE, transferRequest.getSender(), newSenderBalance).block();
    daprClient.saveState(STATE_STORE, transferRequest.getReceiver(), newReceiverBalance).block();
  }

  private void saveTransferState(TransferResponse transferResponse) {
    logger.info(transferResponse.getMessage());
    daprClient.saveState(STATE_STORE, transferResponse.getTransferId(), transferResponse).block();
  }

}
