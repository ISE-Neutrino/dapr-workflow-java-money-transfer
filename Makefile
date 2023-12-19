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
	@mvn install -Dmaven.test.skip=true

clean: ## 🧹 Clean compilation files
	@echo -e "\e[34m$@\e[0m" || true
	@mvn clean

start-client:  ## 🚀 Start client
	@echo -e "\e[34m$@\e[0m" || true
	@dapr run --app-id demoworkflowclient --resources-path ./src/components --dapr-grpc-port 50001 -- java -jar target/dapr-workflow-java-money-transfer-0.0.1-SNAPSHOT.jar com.example.daprworkflowjavamoneytransfer.DaprWorkflowJavaMoneyTransferApplication

run: clean build start-client ## 💿 Run app locally
	
dapr-dashboard: ## 🔬 Open the Dapr Dashboard
	@echo -e "\e[34m$@\e[0m" || true
	@dapr dashboard -p 9000

init-dapr: ## 🧹 Initialize Dapr
	@echo -e "\e[34m$@\e[0m" || true
	@dapr init

stop-dapr: ## 🧹 Uninstall Dapr
	@echo -e "\e[34m$@\e[0m" || true
	@dapr uninstall