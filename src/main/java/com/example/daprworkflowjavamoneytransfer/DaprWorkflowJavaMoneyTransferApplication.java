package com.example.daprworkflowjavamoneytransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import com.example.daprworkflowjavamoneytransfer.workflows.*;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;

@SpringBootApplication
public class DaprWorkflowJavaMoneyTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaprWorkflowJavaMoneyTransferApplication.class, args);

		startWorkflowWorkers();
	}

	public static void startWorkflowWorkers() {
		// Register Workflows
		WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder();
		builder.registerWorkflow(MoneyTransferWorkflow.class);
		builder.registerWorkflow(CreateAccountWorkflow.class);

		// Register workflow activities, visible to all workflows
		builder.registerActivity(NotifyActivity.class);

		// Build and then start the workflow runtime pulling and executing tasks
		try (WorkflowRuntime runtime = builder.build()) {
			System.out.println("---\n\tStart workflow runtime\n---");
			runtime.start();
		}
	}
}
