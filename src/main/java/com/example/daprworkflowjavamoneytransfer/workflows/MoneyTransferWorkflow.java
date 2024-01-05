package com.example.daprworkflowjavamoneytransfer.workflows;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.daprworkflowjavamoneytransfer.enums.TransferStatus;
import com.example.daprworkflowjavamoneytransfer.model.Notification;
import com.example.daprworkflowjavamoneytransfer.model.TransferRequest;
import com.example.daprworkflowjavamoneytransfer.model.TransferResponse;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.FraudDetectionActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.TransferMoneyActivity;
import com.microsoft.durabletask.TaskCanceledException;

public class MoneyTransferWorkflow extends Workflow {

  private static final String STATE_STORE = "statestore";
  private static Logger logger = LoggerFactory.getLogger(MoneyTransferWorkflow.class);
  private DaprClient daprClient;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("--- Starting Workflow: " + ctx.getName() + " ---");

      TransferRequest transferRequest = ctx.getInput(TransferRequest.class);

      // ------------------------
      // Notify transfer request
      // ------------------------
      Notification notification = Notification.builder()
          .message("Money Transfer Request Received: " + transferRequest.toString()).build();
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      // --------------------------------------
      // Validate Request with Fraud Detection
      // --------------------------------------
      TransferResponse transferResponse = ctx
          .callActivity(FraudDetectionActivity.class.getName(), transferRequest, TransferResponse.class)
          .await();
      if (transferResponse.getStatus().equals(TransferStatus.REJECTED.toString())) {
        notification.setMessage(transferResponse.getMessage());
        ctx.callActivity(NotifyActivity.class.getName(), notification).await();
        ctx.complete(transferResponse);
        return;
      }

      if (transferRequest.getAmount() > 100) {
        ctx.getLogger().info("Waiting for approval...");
        try {
          Boolean approved = ctx.waitForExternalEvent("Approval", 
                            Duration.ofMinutes(10), boolean.class).await();
                            
          if (!approved) {
            ctx.getLogger().info("approval denied - send a notification");
            notification.setMessage("Transfer was not approved from all actors.");
            ctx.callActivity(NotifyActivity.class.getName(), notification).await();

            transferResponse = TransferResponse.builder()
                .message("Transfer was not approved from all actors.")
                .status(TransferStatus.REJECTED.toString())
                .transferId(transferRequest.getTransferId())
                .build();

            saveTransferState(transferResponse);
            ctx.complete(transferResponse);
            return;
          }
        } catch (TaskCanceledException ex) {
          transferResponse = TransferResponse.builder()
              .message("Approval timed-out.")
              .status(TransferStatus.REJECTED.toString())
              .transferId(transferRequest.getTransferId())
              .build();

          saveTransferState(transferResponse);
          ctx.complete(transferResponse);

          return;
        }
      }

      // ----------------------------
      // Perform the actual transfer
      // ----------------------------
      transferResponse = ctx
          .callActivity(TransferMoneyActivity.class.getName(), transferRequest, TransferResponse.class)
          .await();

      notification.setMessage(transferResponse.getMessage());
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      ctx.complete(transferResponse);
      ctx.getLogger().info("--- Workflow finished with result: " + notification.toString() + " ---");
    };
  }

  private void saveTransferState(TransferResponse transferResponse) {
    logger.info(transferResponse.getMessage());
    daprClient.saveState(STATE_STORE, transferResponse.getTransferId(), transferResponse).block();
  }
}
