package com.example.daprworkflowjavamoneytransfer.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import com.example.daprworkflowjavamoneytransfer.enums.ApprovalResult;
import com.example.daprworkflowjavamoneytransfer.enums.TransferStatus;
import com.example.daprworkflowjavamoneytransfer.model.AccountResponse;
import com.example.daprworkflowjavamoneytransfer.model.CreateAccountResponse;
import com.example.daprworkflowjavamoneytransfer.model.Notification;
import com.example.daprworkflowjavamoneytransfer.model.TransferRequest;
import com.example.daprworkflowjavamoneytransfer.model.TransferResponse;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.CreateAccountActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.FraudDetectionActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.RequestAccountApprovalActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.TransferMoneyActivity;

public class MoneyTransferWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("--- Starting Workflow: " + ctx.getName() + " ---");

      TransferRequest transferRequest = ctx.getInput(TransferRequest.class);

      // Notify a transfer request
      Notification notification = Notification.builder()
          .message("Money Transfer Request Received: " + transferRequest.toString()).build();
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      // Validate Request with Fraud Detection
      TransferResponse validationResponse = ctx
          .callActivity(FraudDetectionActivity.class.getName(), transferRequest, TransferResponse.class)
          .await();
      if (validationResponse.getStatus().equals(TransferStatus.REJECTED.toString())) {

        notification.setMessage(validationResponse.getMessage());
        ctx.callActivity(NotifyActivity.class.getName(), notification).await();

        ctx.complete(validationResponse);
        return;
      }

      // TODO:: Approval from both actors

      // Perform the actual transfer
      var result = ctx.callActivity(TransferMoneyActivity.class.getName(), transferRequest, TransferResponse.class).await();

      notification.setMessage(result.getMessage());
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      ctx.complete(result);

      ctx.getLogger().info("--- Workflow finished with result: " + notification.toString() + " ---");
    };
  }
}
