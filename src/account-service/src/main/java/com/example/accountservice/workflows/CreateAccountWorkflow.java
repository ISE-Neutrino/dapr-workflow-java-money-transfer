package com.example.accountservice.workflows;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

import com.example.accountservice.enums.ApprovalResult;
import com.example.accountservice.model.AccountResponse;
import com.example.accountservice.model.CreateAccountRequest;
import com.example.accountservice.model.CreateAccountResponse;
import com.example.accountservice.model.Notification;
import com.example.accountservice.workflows.activities.CreateAccountActivity;
import com.example.accountservice.workflows.activities.NotifyActivity;
import com.example.accountservice.workflows.activities.RequestAccountApprovalActivity;

public class CreateAccountWorkflow extends Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("--- Starting Workflow: " + ctx.getName() + " ---");

      CreateAccountRequest newAccountRequest = ctx.getInput(CreateAccountRequest.class);

      // ---------------------------
      // Notify new account request
      // ---------------------------
      Notification notification = Notification.builder()
          .message("Create Account Request Received: " + newAccountRequest.toString()).build();
      ctx.callActivity(NotifyActivity.class.getName(), notification).await();

      // ---------------------------
      // Request Approval
      // ---------------------------
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

      // ---------------------------
      // Create Account
      // ---------------------------
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
