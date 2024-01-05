package com.example.accountservice.controller;

import org.springframework.web.bind.annotation.RestController;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.accountservice.model.AccountResponse;
import com.example.accountservice.model.CreateAccountRequest;
import com.example.accountservice.model.CreateAccountResponse;
import com.example.accountservice.workflows.*;
import org.springframework.http.MediaType;

@RestController
public class AccountController {

    private static final String STATE_STORE = "statestore";
    // private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String ACCOUNT_TOPIC = "accounts";

    private static Logger logger = LoggerFactory.getLogger(AccountController.class);

    private DaprClient daprClient;

    public AccountController() {
        this.daprClient = new DaprClientBuilder().build();
    }

    // @Topic(name = ACCOUNT_TOPIC, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            String instanceId = client.scheduleNewWorkflow(CreateAccountWorkflow.class, request);
            System.out.printf("Started a new CreateAccount workflow with instance ID: %s%n", instanceId);

            WorkflowInstanceStatus workflowInstanceStatus = client.waitForInstanceCompletion(instanceId, null, true);

            var result = workflowInstanceStatus.readOutputAs(CreateAccountResponse.class);
            System.out.printf("workflow instance with ID: %s completed with result: %s%n", instanceId, result);

            return ResponseEntity.ok(result);

        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: REMOVE getAccount endpoint
    @GetMapping(path = "/accounts/{owner}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getAccount(@PathVariable String owner) {

        return Mono.fromSupplier(() -> {
            try {
                logger.info("Get Account Request Received");

                var accountAmount = daprClient.getState(STATE_STORE, owner, Double.class).block();
                if (accountAmount.getValue() == null) {
                    logger.error("Account {} does not exist.", owner);
                    return ResponseEntity.badRequest().body(String.format("Account %s does not exist.", owner));
                }

                return ResponseEntity.ok(AccountResponse.builder()
                        .owner(owner)
                        .amount(accountAmount.getValue())
                        .build());

            } catch (Exception e) {

                logger.error("Error while getting account request: " + e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
