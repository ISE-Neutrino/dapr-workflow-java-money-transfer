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
	@dapr run --app-id money-transfer-service --resources-path ./src/components -- java -jar target/money-transfer-service.jar

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

test: ## 🧪 Run tests
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/test.sh

###

start-local: ## 🧹 Setup local Kind Cluster
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/start-local-env.sh

deploy-local: ## 🚀 Deploy application resources locally
	@echo -e "\e[34m$@\e[0m" || true
	@./scripts/deploy-services-local.sh
	@echo -e "\e[34mYOU WILL NEED TO START A NEW TERMINAL AND RUN  make test\e[0m" || true

kind-clean: ## 🧹 Clean up local files
	@echo -e "\e[34m$@\e[0m" || true
	@kind delete cluster --name azd-aks
	@docker rm kind-registry -f

port-forward-local: ## ⏩ Forward the local port
	@echo -e "\e[34m$@\e[0m" || true
	@kubectl port-forward service/money-transfer-service 80:80 --pod-running-timeout=3m0s