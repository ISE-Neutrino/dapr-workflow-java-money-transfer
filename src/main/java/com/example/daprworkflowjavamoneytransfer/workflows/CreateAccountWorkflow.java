package com.example.daprworkflowjavamoneytransfer.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import com.example.daprworkflowjavamoneytransfer.enums.ApprovalResult;
import com.example.daprworkflowjavamoneytransfer.model.AccountResponse;
import com.example.daprworkflowjavamoneytransfer.model.CreateAccountRequest;
import com.example.daprworkflowjavamoneytransfer.model.CreateAccountResponse;
import com.example.daprworkflowjavamoneytransfer.model.Notification;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.CreateAccountActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.RequestAccountApprovalActivity;

public class CreateAccountWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("--- Starting Workflow: " + ctx.getName() + " ---");

      CreateAccountRequest newAccountRequest = ctx.getInput(CreateAccountRequest.class);

      // Notify new account request
      Notification notification = Notification.builder()
          .message("Create Account Request Received: " + newAccountRequest.toString()).build();
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      // Request Approval
      ApprovalResult approvalResult = ctx
          .callActivity(RequestAccountApprovalActivity.class.getName(), newAccountRequest, ApprovalResult.class)
          .await();
      if (approvalResult.equals(ApprovalResult.REJECTED)) {

        notification.setMessage("Insufficient funds for opening account");
        ctx.callActivity(NotifyActivity.class.getName(), notification).await();

        ctx.complete(CreateAccountResponse.builder()
            .account(null)
            .message(notification.getMessage())
            .build());
        return;
      }

      var result = ctx.callActivity(CreateAccountActivity.class.getName(), newAccountRequest, String.class).await();

      notification.setMessage(result);
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      ctx.complete(CreateAccountResponse.builder()
          .account(AccountResponse.builder()
              .owner(newAccountRequest.getOwner())
              .amount(newAccountRequest.getAmount()).build())
          .message(result)
          .build());

      ctx.getLogger().info("--- Workflow finished with result: " + notification.toString() + " ---");
    };
  }
}
