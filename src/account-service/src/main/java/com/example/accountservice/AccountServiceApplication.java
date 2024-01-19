package com.example.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import com.example.accountservice.workflows.*;
import com.example.accountservice.workflows.activities.CreateAccountActivity;
import com.example.accountservice.workflows.activities.NotifyActivity;
import com.example.accountservice.workflows.activities.RequestAccountApprovalActivity;

@SpringBootApplication
public class AccountServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountServiceApplication.class, args);

		startWorkflowWorkers();
	}

	public static void startWorkflowWorkers() {
		// Register Workflows
		WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder();
		builder.registerWorkflow(CreateAccountWorkflow.class);

		// Register workflow activities
		builder.registerActivity(CreateAccountActivity.class);
		builder.registerActivity(NotifyActivity.class);
		builder.registerActivity(RequestAccountApprovalActivity.class);

		// Build and then start the workflow runtime pulling and executing tasks
		try (WorkflowRuntime runtime = builder.build()) {
			System.out.println("--- Start workflow runtime ---");
			runtime.start();
		}
	}
}
