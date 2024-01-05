package com.example.transferservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import com.example.transferservice.workflows.*;
import com.example.transferservice.workflows.activities.Approver1Activity;
import com.example.transferservice.workflows.activities.Approver2Activity;
import com.example.transferservice.workflows.activities.FraudDetectionActivity;
import com.example.transferservice.workflows.activities.NotifyActivity;
import com.example.transferservice.workflows.activities.TransferMoneyActivity;

@SpringBootApplication
public class TransferServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransferServiceApplication.class, args);

		startWorkflowWorker();
	}

	public static void startWorkflowWorker() {
		// Register Workflows
		WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder();
		builder.registerWorkflow(MoneyTransferWorkflow.class);

		// Register workflow activities
		builder.registerActivity(Approver1Activity.class);
		builder.registerActivity(Approver2Activity.class);
		builder.registerActivity(FraudDetectionActivity.class);
		builder.registerActivity(NotifyActivity.class);
		builder.registerActivity(TransferMoneyActivity.class);

		// Build and then start the workflow runtime pulling and executing tasks
		try (WorkflowRuntime runtime = builder.build()) {
			System.out.println("--- Start workflow runtime ---");
			runtime.start();
		}
	}
}
