package com.example.daprworkflowjavamoneytransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.ApplicationRunner;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import com.example.daprworkflowjavamoneytransfer.workflows.*;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.Approver1Activity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.Approver2Activity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.CreateAccountActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.FraudDetectionActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.NotifyActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.RequestAccountApprovalActivity;
import com.example.daprworkflowjavamoneytransfer.workflows.activities.TransferMoneyActivity;

@SpringBootApplication
public class DaprWorkflowJavaMoneyTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(DaprWorkflowJavaMoneyTransferApplication.class, args);
	}

    @Bean
    ApplicationRunner applicationRunner() {
        return args -> {
            WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder();
            builder.registerWorkflow(MoneyTransferWorkflow.class);
            builder.registerWorkflow(CreateAccountWorkflow.class);

            // Register workflow activities, visible to all workflows
            builder.registerActivity(Approver1Activity.class);
            builder.registerActivity(Approver2Activity.class);
            builder.registerActivity(CreateAccountActivity.class);
            builder.registerActivity(FraudDetectionActivity.class);
            builder.registerActivity(NotifyActivity.class);
            builder.registerActivity(RequestAccountApprovalActivity.class);
            builder.registerActivity(TransferMoneyActivity.class);

            // Build and then start the workflow runtime pulling and executing tasks
            try (WorkflowRuntime runtime = builder.build()) {
                System.out.println("--- Start workflow runtime ---");
                runtime.start();
            }
        };
    }
}
