package com.example.transferservice.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import java.util.ArrayList;
import java.util.List;

import com.example.transferservice.enums.ApprovalResult;
import com.example.transferservice.enums.TransferStatus;
import com.example.transferservice.model.Notification;
import com.example.transferservice.model.TransferRequest;
import com.example.transferservice.model.TransferResponse;
import com.example.transferservice.workflows.activities.Approver1Activity;
import com.example.transferservice.workflows.activities.Approver2Activity;
import com.example.transferservice.workflows.activities.FraudDetectionActivity;
import com.example.transferservice.workflows.activities.NotifyActivity;
import com.example.transferservice.workflows.activities.TransferMoneyActivity;
import com.microsoft.durabletask.Task;

public class MoneyTransferWorkflow extends Workflow {

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


      // ----------------------------------
      // Ask Approval from multiple actors
      // ----------------------------------
      List<Task<ApprovalResult>> approversList = new ArrayList<Task<ApprovalResult>>() {
        {
          add(ctx.callActivity(Approver1Activity.class.getName(), transferRequest, ApprovalResult.class));
          add(ctx.callActivity(Approver2Activity.class.getName(), transferRequest, ApprovalResult.class));
        }
      };
      List<ApprovalResult> approversResult = ctx.allOf(approversList).await();
      if (approversResult.stream().anyMatch(t -> t.equals(ApprovalResult.REJECTED))) {
        notification.setMessage("Transfer was not approved from all actors");
        ctx.callActivity(NotifyActivity.class.getName(), notification).await();

        ctx.complete(TransferResponse.builder()
            .message("Transfer was not approved from all approvers")
            .status(TransferStatus.REJECTED.toString())
            .transferId(transferRequest.getTransferId())
            .build());
        return;
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
}
