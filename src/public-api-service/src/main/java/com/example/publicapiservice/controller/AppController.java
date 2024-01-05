package com.example.publicapiservice.controller;

import org.springframework.web.bind.annotation.RestController;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.InvokeMethodRequest;
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

import com.example.publicapiservice.model.*;

import org.springframework.http.MediaType;

@RestController
public class AppController {

    private static final String STATE_STORE = "statestore";

    private static final String PUBSUB_NAME = "money-transfer-pubsub";
    private static final String ACCOUNT_TOPIC = "accounts";
    private static final String TRANSFER_TOPIC = "transfers";

    private static Logger logger = LoggerFactory.getLogger(AppController.class);

    private DaprClient daprClient;

    public AppController() {
        this.daprClient = new DaprClientBuilder().build();
    }

    @PostMapping(path = "/accounts", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        try {
            logger.info("Create Account Request Received");
            // // via pubsub event publish
            // daprClient.publishEvent(PUBSUB_NAME, ACCOUNT_TOPIC, request).block();

            // // via daprClient invokeMethod
            // InvokeMethodRequest invokeMethodRequest = new
            // InvokeMethodRequest("account-service", "accounts");
            // invokeMethodRequest.setBody(request);

            CreateAccountResponse response = daprClient.invokeMethod(
            "account-service",
            "accounts",
            request,
            HttpExtension.POST,
            CreateAccountResponse.class).block();

            System.out.printf("workflow completed with result: %s%n", response);

            return ResponseEntity.ok(response);

            // return ResponseEntity.ok(CreateAccountResponse.builder()
            //         .account(AccountResponse.builder()
            //                 .owner(request.getOwner())
            //                 .amount(request.getAmount()))
            //         .message("Account created successfully")
            //         .build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    @PostMapping(path = "/transfers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> createTransferRequest(@RequestBody TransferRequest request) {

        try {
            logger.info("Transfer Request Received");
            // // via pubsub event publish
            // daprClient.publishEvent(PUBSUB_NAME, ACCOUNT_TOPIC, request).block();

            // via daprClient invokeMethod
            // InvokeMethodRequest invokeMethodRequest = new
            // InvokeMethodRequest("transfer-service", "transfers",
            // request);
            // daprClient.invokeMethod(invokeMethodRequest,
            // CreateAccountResponse.class).block();

            TransferResponse response = daprClient.invokeMethod(
                    "transfer-service",
                    "createTransfer",
                    request,
                    HttpExtension.POST,
                    TransferResponse.class).block();

            System.out.printf("Transfer completed with result: %s%n", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(path = "/transfers/{transferId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getTransfer(@PathVariable String transferId) {

        return Mono.fromSupplier(() -> {
            try {
                logger.info("Get Transfer State Received: transferId: {}", transferId);

                var transferResponse = daprClient.getState(STATE_STORE, transferId, TransferResponse.class).block();
                if (transferResponse.getValue() == null) {
                    logger.error("Transfer Request for id {} does not exist.", transferId);
                    return ResponseEntity.badRequest()
                            .body(String.format("Transfer Request for id %s does not exist.", transferId));
                }

                return ResponseEntity.ok(transferResponse.getValue());

            } catch (Exception e) {

                logger.error("Error while getting account request: " + e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
