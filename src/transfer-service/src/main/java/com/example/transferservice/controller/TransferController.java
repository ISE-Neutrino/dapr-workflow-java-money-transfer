package com.example.transferservice.controller;

import org.springframework.web.bind.annotation.RestController;

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

import com.example.transferservice.model.TransferRequest;
import com.example.transferservice.model.TransferResponse;
import com.example.transferservice.workflows.*;
import org.springframework.http.MediaType;

@RestController
public class TransferController {

    private static final String STATE_STORE = "statestore";

    private static Logger logger = LoggerFactory.getLogger(TransferController.class);

    private DaprClient daprClient;

    public TransferController() {
        this.daprClient = new DaprClientBuilder().build();
    }

    @PostMapping(path = "/transfers", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferResponse> createTransferRequest(@RequestBody TransferRequest request) {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {

            request.setTransferId(TransferRequest.generateId());

            String instanceId = client.scheduleNewWorkflow(MoneyTransferWorkflow.class, request);
            System.out.printf("Started a new Money Transfer workflow with instance ID: %s%n", instanceId);

            WorkflowInstanceStatus workflowInstanceStatus = client.waitForInstanceCompletion(instanceId, null, true);

            var result = workflowInstanceStatus.readOutputAs(TransferResponse.class);
            System.out.printf("workflow instance with ID: %s completed with result: %s%n", instanceId, result);

            return ResponseEntity.ok(result);

        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: REMOVE getTransfer endpoint
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
