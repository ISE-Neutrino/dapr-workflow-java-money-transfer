package com.example.publicapiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;

@SpringBootApplication
public class PublicApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicApiServiceApplication.class, args);

	}
}
