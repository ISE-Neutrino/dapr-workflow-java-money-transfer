SHELL := /bin/bash

VERSION := 0.0.1
BUILD_INFO := Manual build

ENV_FILE := .env
ifeq ($(filter $(MAKECMDGOALS),config clean),)
	ifneq ($(strip $(wildcard $(ENV_FILE))),)
		ifneq ($(MAKECMDGOALS),config)
			include $(ENV_FILE)
			export
		endif
	endif
endif

.PHONY: help lint image push build run
.DEFAULT_GOAL := help

help: ## 💬 This help message :)
	@grep -E '[a-zA-Z_-]+:.*?## .*$$' $(firstword $(MAKEFILE_LIST)) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## 🧹 Build application
	@echo -e "\e[34m$@\e[0m" || true
	@mvn install

build-examples: ## 🧹 Build examples folder
	@echo -e "\e[34m$@\e[0m" || true
	@cd examples && mvn clean install -Dcheckstyle.skip

clean: ## 🧹 Clean compilation files
	@echo -e "\e[34m$@\e[0m" || true
	@mvn clean

start-chain-worker: ## Start chain workflow worker
	@echo -e "\e[34m$@\e[0m" || true
	@cd examples && dapr run --app-id demoworkflowworker --resources-path ./components/workflows --dapr-grpc-port 50001 -- java -jar target/dapr-java-sdk-examples-exec.jar io.dapr.examples.workflows.chain.DemoChainWorker

start-chain-client: ## Start chain workflow client
	@echo -e "\e[34m$@\e[0m" || true
	@cd examples && java -jar target/dapr-java-sdk-examples-exec.jar io.dapr.examples.workflows.chain.DemoChainClient


start-money-worker: ## Start money transfer workflow worker
	@echo -e "\e[34m$@\e[0m" || true
	@cd examples && dapr run --app-id demoworkflowworker --resources-path ./components/workflows --dapr-grpc-port 50001 -- java -jar target/dapr-java-sdk-examples-exec.jar io.dapr.examples.workflows.moneytransfer.MoneyTransferWorker

start-money-client: ## Start money transfer workflow client
	@echo -e "\e[34m$@\e[0m" || true
	@cd examples && java -jar target/dapr-java-sdk-examples-exec.jar io.dapr.examples.workflows.moneytransfer.MoneyTransferClient


start-client: 
	@echo -e "\e[34m$@\e[0m" || true
	@dapr run --app-id demoworkflowclient --resources-path ./src/components --dapr-grpc-port 50001 -- java -jar target/dapr-workflow-java-money-transfer-0.0.1-SNAPSHOT.jar com.example.daprworkflowjavamoneytransfer.DaprWorkflowJavaMoneyTransferApplication


start-worker: 
	@echo -e "\e[34m$@\e[0m" || true
	@dapr run --app-id demoworkflowworker --resources-path ./src/components --dapr-grpc-port 50001 -- java -jar target/dapr-workflow-java-money-transfer-0.0.1-SNAPSHOT.jar com.example.daprworkflowjavamoneytransfer.workflows.MoneyTransferWorker